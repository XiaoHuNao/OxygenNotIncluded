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

        GasProperties selfProps = self.getGasProperties();
        GasProperties neighborProps = neighbor.getGasProperties();

        // 获取双方的导热率和比热容
        double selfConductivity = Math.max(0.01D, selfProps.thermalConductivity);
        double neighborConductivity = Math.max(0.01D, neighborProps.thermalConductivity);
        double selfCapacity = Math.max(0.1D, selfProps.heatCapacity);
        double neighborCapacity = Math.max(0.1D, neighborProps.heatCapacity);

        // 计算有效导热率（取两者平均值，表示接触面的导热能力）
        double effectiveConductivity = (selfConductivity + neighborConductivity) * 0.5D;

        // 计算热容权重（质量 × 比热容）
        double selfWeight = Math.max(1L, self.getAmount()) * selfCapacity;
        double neighborWeight = Math.max(1L, neighbor.getAmount()) * neighborCapacity;
        double totalWeight = selfWeight + neighborWeight;

        if (totalWeight <= 0.0D) {
            return;
        }

        // 计算目标平衡温度（质量加权平均）
        double targetTemp = (selfTemp * selfWeight + neighborTemp * neighborWeight) / totalWeight;

        // 根据有效导热率计算热交换速率
        // 导热率越高，交换越快；温度差越大，交换越快
        double tempDelta = Math.abs(selfTemp - neighborTemp);
        double exchangeRate = clamp(effectiveConductivity * 0.1D * (1.0D + tempDelta / 100.0D), 0.01D, 0.5D);

        // 计算新温度
        double newSelf = selfTemp + (targetTemp - selfTemp) * exchangeRate;
        double newNeighbor = neighborTemp + (targetTemp - neighborTemp) * exchangeRate;

        // 确保温度不会低于绝对零度
        newSelf = Math.max(1.0D, newSelf);
        newNeighbor = Math.max(1.0D, newNeighbor);

        self.setKelvin(newSelf);
        neighbor.setKelvin(newNeighbor);
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

        GasProperties gasProps = gasEntity.getGasProperties();

        double sourceConductivity = Math.max(0.01D, heatSource.getThermalConductivity());
        double gasConductivity = Math.max(0.01D, gasProps.thermalConductivity);
        double gasCapacity = Math.max(0.1D, gasProps.heatCapacity);

        // 计算有效导热率（热源和气体的平均）
        double effectiveConductivity = (sourceConductivity + gasConductivity) * 0.5D;

        // 计算温度差
        double tempDelta = targetTemp - gasTemp;

        // 根据有效导热率和比热容计算加热速率
        // 导热率越高，加热越快；比热容越高，加热越慢（需要更多能量）
        double heatingRate = clamp(effectiveConductivity / (gasCapacity + 0.1D) * 0.15D, 0.01D, 0.4D);

        // 计算新温度（向目标温度靠近）
        double newTemp = gasTemp + tempDelta * heatingRate;

        // 确保不超过目标温度
        newTemp = Math.min(newTemp, targetTemp);
        newTemp = Math.max(1.0D, newTemp);

        gasEntity.setKelvin(newTemp);
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

    /**
     * 限制值在指定范围内
     */
    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}

