package com.xiaohunao.oxygen_not_included.common.heat_source;

import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.util.TemperatureType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 热传递管理器
 * 负责处理气体之间以及热源与气体之间的热传递
 */
public class HeatTransferManager {
    private static final HeatTransferManager INSTANCE = new HeatTransferManager();
    /** 单位时间步（以 tick 计，1.0 == 每 tick 计算一次） */
    private static final double TIME_STEP = 1.0D;
    /** 全局传热调节系数（越小整体传热越慢） */
    private static final double GAS_EXCHANGE_COEFFICIENT = 0.02D;
    /** 热源向气体传热的调节系数 */
    private static final double SOURCE_EXCHANGE_COEFFICIENT = 0.03D;
    /** 当热源与气体温差小于该阈值时直接视为达到目标温度 */
    private static final double SOURCE_TARGET_SNAP_THRESHOLD = 0.25D;
    /** 热源传热时的最小相对推进比例，防止能量增量被浮点误差抹掉 */
    private static final double MIN_SOURCE_PROGRESS_RATIO = 1.0E-3D;

    public static HeatTransferManager getInstance() {
        return INSTANCE;
    }

    private HeatTransferManager() {
    }

    /**
     * 根据群系基础温度初始化气体温度
     * 如果气体温度未初始化（小于-273.0K），则根据所在群系的基础温度进行初始化
     *
     * @param gasEntity 气体方块实体
     * @param level 世界
     * @param pos 位置
     */
    public static void ensureTemperatureInitialized(GasBlockEntity gasEntity, Level level, BlockPos pos) {
        if (gasEntity == null || level == null || pos == null) {
            return;
        }
        if (gasEntity.getKelvin() < -273.0D) {
            float baseTemperature = level.getBiome(pos).value().getBaseTemperature();
            gasEntity.setKelvin(TemperatureType.convert(baseTemperature, TemperatureType.Mc, TemperatureType.Kelvin));
        }
    }

    /**
     * 气体之间的热传递
     * 根据双方的导热率、比热容和温度差进行热交换
     *
     * @param self 自身气体方块实体
     * @param neighbor 相邻气体方块实体
     * @param level 世界
     * @param selfPos 自身位置
     * @param neighborPos 相邻位置
     */
    public void transferHeatBetweenGases(GasBlockEntity self, GasBlockEntity neighbor, Level level, BlockPos selfPos, BlockPos neighborPos) {
        if (self == null || neighbor == null || self.getAmount() <= 0L || neighbor.getAmount() <= 0L) {
            return;
        }

        // 确保温度已初始化（根据群系基础温度）
        ensureTemperatureInitialized(self, level, selfPos);
        ensureTemperatureInitialized(neighbor, level, neighborPos);

        double selfTemp = self.getKelvin();
        double neighborTemp = neighbor.getKelvin();

        // 如果温度差很小，跳过计算
        if (Math.abs(selfTemp - neighborTemp) < 0.01D) {
            return;
        }

        // 读取气体属性（导热率、比热容等），用于后续计算
        GasProperties selfProps = self.getGasProperties();
        GasProperties neighborProps = neighbor.getGasProperties();

        double selfConductivity = Math.max(0.01D, selfProps.thermalConductivity);
        double neighborConductivity = Math.max(0.01D, neighborProps.thermalConductivity);
        double selfCapacityTotal = computeTotalHeatCapacity(self.getAmount(), selfProps.heatCapacity);
        double neighborCapacityTotal = computeTotalHeatCapacity(neighbor.getAmount(), neighborProps.heatCapacity);

        if (selfCapacityTotal <= 0.0D || neighborCapacityTotal <= 0.0D) {
            return;
        }

        // 通过平均导热率和温度梯度来估算传热能力
        double effectiveConductivity = (selfConductivity + neighborConductivity) * 0.5D;
        double tempGradient = selfTemp - neighborTemp;
        double targetTemp = (selfTemp * selfCapacityTotal + neighborTemp * neighborCapacityTotal) / (selfCapacityTotal + neighborCapacityTotal);

        // Q = k * ΔT * Δt，之后除以热容得到温度变化
        double energyTransfer = tempGradient * effectiveConductivity * GAS_EXCHANGE_COEFFICIENT * TIME_STEP;

        if (tempGradient > 0.0D) {
            double maxEnergy = (selfTemp - targetTemp) * selfCapacityTotal;
            energyTransfer = Math.min(Math.max(energyTransfer, 0.0D), maxEnergy);
        } else {
            double maxEnergy = (neighborTemp - targetTemp) * neighborCapacityTotal;
            energyTransfer = -Math.min(Math.max(-energyTransfer, 0.0D), maxEnergy);
        }

        // 将传递的能量均摊到双方热容上得到新的温度
        double newSelf = selfTemp - energyTransfer / selfCapacityTotal;
        double newNeighbor = neighborTemp + energyTransfer / neighborCapacityTotal;

        self.setKelvin(Math.max(1.0D, newSelf));
        neighbor.setKelvin(Math.max(1.0D, newNeighbor));
    }

    /**
     * 热源向气体的热传递
     * 热源会不断尝试将周围气体加热到目标温度
     *
     * @param gasEntity 气体方块实体
     * @param heatSource 热源
     * @param level 世界
     * @param gasPos 气体位置
     */
    public void transferHeatFromSource(GasBlockEntity gasEntity, HeatSource heatSource, Level level, BlockPos gasPos) {
        if (gasEntity == null || heatSource == null || gasEntity.getAmount() <= 0L) {
            return;
        }
        
        // 确保温度已初始化（根据群系基础温度）
        ensureTemperatureInitialized(gasEntity, level, gasPos);

        double gasTemp = gasEntity.getKelvin();
        double targetTemp = heatSource.getTargetTemperature();

        if (gasTemp >= targetTemp) {
            return;
        }

        // 获取气体自身的导热率与比热容
        GasProperties gasProps = gasEntity.getGasProperties();

        double sourceConductivity = Math.max(0.01D, heatSource.getThermalConductivity());
        double gasConductivity = Math.max(0.01D, gasProps.thermalConductivity);
        double gasCapacityTotal = computeTotalHeatCapacity(gasEntity.getAmount(), gasProps.heatCapacity);

        if (gasCapacityTotal <= 0.0D) {
            return;
        }

        double effectiveConductivity = (sourceConductivity + gasConductivity) * 0.5D;
        double tempDelta = targetTemp - gasTemp;

//        if (tempDelta <= SOURCE_TARGET_SNAP_THRESHOLD) {
//            gasEntity.setKelvin(targetTemp);
//            return;
//        }

        double energyTransfer = tempDelta * effectiveConductivity * SOURCE_EXCHANGE_COEFFICIENT * TIME_STEP;
        double maxEnergy = tempDelta * gasCapacityTotal; // 能把气体拉到目标温度所需能量
        if (tempDelta > 0.0D) {
            energyTransfer = Math.min(Math.max(energyTransfer, 0.0D), maxEnergy);
        } else {
            energyTransfer = Math.max(Math.min(energyTransfer, 0.0D), maxEnergy);
        }

        // 将能量换算成温度增量，并限制最小增量防止停滞
        double temperatureGain = energyTransfer / gasCapacityTotal;
        double minRelativeGain = computeMinimumSourceGain(tempDelta, effectiveConductivity, gasCapacityTotal);
        if (temperatureGain < minRelativeGain) {
            temperatureGain = minRelativeGain;
        }

        double newTemp = gasTemp + temperatureGain;
        if (targetTemp - newTemp <= SOURCE_TARGET_SNAP_THRESHOLD) {
            newTemp = targetTemp;
        }

        gasEntity.setKelvin(Math.max(1.0D, Math.min(newTemp, targetTemp)));
    }

    /**
     * 在气体扩散时混合温度
     * 当气体从一个位置转移到另一个位置时，根据传输量和比热容混合温度
     *
     * @param fromK 源温度
     * @param toK 目标温度
     * @param transferAmount 传输量
     * @param toAmountBefore 目标位置原有量
     * @param props 气体属性
     * @return 混合后的温度
     */
    public double mixTemperatureOnTransfer(double fromK, double toK, long transferAmount, long toAmountBefore, GasProperties props) {
        if (transferAmount <= 0L) {
            return toK;
        }

        // 使用比热容作为权重因子
        double heatFactor = clamp(props.heatCapacity, 0.1D, 10.0D);
        double wFrom = transferAmount * heatFactor;
        double wTo = Math.max(1L, toAmountBefore) * heatFactor;

        return (fromK * wFrom + toK * wTo) / (wFrom + wTo);
    }

    private static double computeTotalHeatCapacity(long amount, double specificHeat) {
        double mass = Math.max(1L, amount);
        double c = Math.max(0.1D, specificHeat);
        return mass * c;
    }

    /**
     * 限制值在指定范围内
     */
    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    /**
     * 当气体含量极低或接近目标温度时，energyTransfer/gasCapacity 可能因为浮点误差或分辨率不足而趋近 0。
     * 为了避免“卡死”在目标温度附近，允许以温差的一小部分作为兜底推进量。
     */
    private static double computeMinimumSourceGain(double tempDelta, double effectiveConductivity, double gasCapacityTotal) {
        if (tempDelta <= 0.0D) {
            return 0.0D;
        }
        double capacityScale = Math.max(1.0D, gasCapacityTotal / 64.0D);
        double relativeRatio = clamp(
                (effectiveConductivity * SOURCE_EXCHANGE_COEFFICIENT) / capacityScale,
                MIN_SOURCE_PROGRESS_RATIO,
                0.25D
        );
        return tempDelta * relativeRatio;
    }
}

