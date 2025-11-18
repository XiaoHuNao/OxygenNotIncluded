package com.xiaohunao.oxygen_not_included.common.block.entity;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.xiaohunao.oxygen_not_included.common.block.GasBlock;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.gas.GasStack;
import com.xiaohunao.oxygen_not_included.common.heat_source.HeatTransferManager;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlockEntityTypes;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class GasBlockEntity extends BlockEntity {
	private static final Set<GasBlockEntity> LOADED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private Gas gas;
	private long amount = 0L;
	private double kelvin = -274.0D;

	public GasBlockEntity(BlockPos pos, BlockState blockState) {
		super(ONIBlockEntityTypes.GAS.get(), pos, blockState);
		this.gas = ((GasBlock) blockState.getBlock()).getGas().value();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putString("gas", gas.getRegistryName().toString());
		tag.putLong("amount", amount);
		if (kelvin > -273.0D) {
			tag.putDouble("kelvin", kelvin);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.gas = ONIRegistries.GAS.get(ResourceLocation.parse(tag.getString("gas")));
		this.amount = tag.getLong("amount");
		if (tag.contains("kelvin")) {
			this.kelvin = tag.getDouble("kelvin");
		}
	}
	@Override
	public void onLoad() {
		super.onLoad();
		LOADED.add(this);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		LOADED.remove(this);
	}

	public static Set<GasBlockEntity> getLoaded() {
		return LOADED;
	}


	public static void serverTick(Level level, BlockPos pos, BlockState blockState, GasBlockEntity blockEntity) {
		if (level.isClientSide) return;

		if (blockEntity.amount <= 0L) {
			level.removeBlock(pos, false);
			return;
		}

		// 初始化温度（根据群系基础温度）
		blockEntity.ensureTemperatureInitialized();

		// 扩散：与六邻居进行基于温度与扩散系数的随机扩散
		blockEntity.performDiffusionStep(level, pos);

		// 按密度进行垂直分层：轻气体上浮，重气体下沉
		blockEntity.applyVerticalStratification(level, pos);

		// 温度平衡：无视气体类型，与相邻气体方块进行热交换
		blockEntity.equalizeTemperatureWithNeighbors(level, pos);

		// 交互：根据周围环境触发气体交互（例如超压破坏墙体等）
		GasInteractionManager.handleBlockInteractions(blockEntity);
	}


	public static void clientTick(Level world, BlockPos pos, BlockState blockState, GasBlockEntity blockEntity) {
		// client-side logic placeholder
	}

	/**
	 * 单步扩散：基于元胞自动机，按随机率（温度、扩散速度）与粘度、压差进行传输。
	 * 同时把热量按传输体积分配。
	 */
	private void performDiffusionStep(Level level, BlockPos selfPos) {
		if (this.amount <= 0L) return;
		final GasProperties props = getGasProperties();
		final float diffusionSpeed = Math.max(0.0f, props.diffusion);
		final float viscosity = Math.max(0.001f, props.viscosity);
		final double temperatureK = Math.max(1.0D, getKelvin());

		// 基于温度与扩散速度的随机率，温度越高扩散越强；粘度越大扩散越慢
		final double diffusionChance = computeDiffusionChance(diffusionSpeed, viscosity, temperatureK);
		if (level.random.nextDouble() > diffusionChance) {
			return;
		}

		// 六个方向
		BlockPos[] neighbors = new BlockPos[] {
			selfPos.north(), selfPos.south(), selfPos.east(), selfPos.west(), selfPos.above(), selfPos.below()
		};

		// 为避免两侧双重结算，随机打乱邻居顺序
		shuffleArray(level, neighbors);

		for (BlockPos neighborPos : neighbors) {
			if (this.amount <= 0L) break;
			// 如果不是气体方块，尝试放置同种气体方块以承接扩散
			GasBlockEntity neighbor = getGasEntity(level, neighborPos);
			if (neighbor == null) {
				BlockState neighborState = level.getBlockState(neighborPos);
				// 仅在可被替换且非液体/流体方块时放置气体，避免把液体与流体替换掉
				if (!neighborState.canBeReplaced() || !neighborState.getFluidState().isEmpty()) {
					continue;
				}
				boolean placed = level.setBlock(neighborPos, this.gas.createBlock().defaultBlockState(), 3);
				if (!placed) continue;
				BlockEntity be = level.getBlockEntity(neighborPos);
				if (!(be instanceof GasBlockEntity)) continue;
				neighbor = (GasBlockEntity) be;
				neighbor.gas = this.gas; // 保底同步
				neighbor.amount = 0L;
				neighbor.kelvin = this.kelvin;
			}

			// 仅在同种气体之间进行体积分配；不同气体先按分层规则处理（本步只扩散同种）
			if (neighbor.gas != this.gas) {
				continue;
			}

			// 基于压差与每tick最大传输比例计算传输量
			long transfer = computeTransferAmountPerTick(this.amount, neighbor.amount, props);
			if (transfer <= 0L) continue;

			if (this.amount - transfer <= 0L){
				level.removeBlock(neighborPos, false);
			}

			if (transfer > this.amount) transfer = this.amount;

			// 进行体积分配
			long newSelf = this.amount - transfer;
			long newNeighbor = neighbor.amount + transfer;

			// 热量传递：混合传输份额的热量，采用简单的质量加权平均
			double mixKelvin = HeatTransferManager.getInstance().mixTemperatureOnTransfer(
					this.kelvin, neighbor.kelvin, transfer, neighbor.amount, props);

			this.setAmount(newSelf);
			neighbor.setAmount(newNeighbor);
			neighbor.setKelvin(mixKelvin);
			// 自身温度也进行微调（向邻居温度靠拢一小步，表示能量扩散）
			double selfAdjust = 0.1D;
			this.setKelvin(this.kelvin * (1.0D - selfAdjust) + neighbor.kelvin * selfAdjust);
		}
	}

	private double computeDiffusionChance(float diffusionSpeed, float viscosity, double kelvin) {
		// 归一化温度影响：相对室温的倍率，最低0.5，最高2.0
		double tempFactor = clamp((kelvin / 293.15D), 0.5D, 2.0D);
		// 扩散速度直接成正比，粘度反比
		double base = diffusionSpeed / viscosity;
		// 映射到 0~1 的概率，并限制范围
		return clamp(base * 0.2D * tempFactor, 0.05D, 0.95D);
	}

	/**
	 * 计算每tick传输量：
	 * - 倾向于把双方向 STANDARD_PRESSURE 靠拢
	 * - 也基于双方差值进行均衡
	 * - 上限由扩散速度限制
	 */
	private long computeTransferAmountPerTick(long selfAmount, long neighborAmount, GasProperties props) {
		int target = GasStack.STANDARD_PRESSURE;
		long pressureDelta = (selfAmount - neighborAmount);
		if (pressureDelta <= 0L) return 0L;

		// 基于“超压”的推动：超出1000的部分更倾向向外扩散
		long selfOver = Math.max(0L, selfAmount - target);

		// 基于扩散速度的每tick最大比例
		double maxFractionPerTick = clamp(props.diffusion / Math.max(0.001f, props.viscosity) * 0.25D, 0.02D, 0.6D);

		// 倾向于把差值的一部分传过去，同时强烈考虑超压部分
		long byDelta = (long) Math.floor(Math.abs(pressureDelta) * 0.5D * maxFractionPerTick);
		long byOver = (long) Math.floor(selfOver * maxFractionPerTick);

		long transfer = Math.max(byDelta, byOver);
		if (transfer <= 0L) transfer = 1L;
		return transfer;
	}

	private void equalizeTemperatureWithNeighbors(Level level, BlockPos selfPos) {
		if (this.amount <= 0L) return;
		BlockPos[] neighbors = new BlockPos[] {
			selfPos.north(), selfPos.south(), selfPos.east(), selfPos.west(), selfPos.above(), selfPos.below()
		};

		for (BlockPos neighborPos : neighbors) {
			if (!isPrimaryPair(selfPos, neighborPos)) {
				continue;
			}
			GasBlockEntity neighbor = getGasEntity(level, neighborPos);
			if (neighbor == null || neighbor.amount <= 0L) {
				continue;
			}

			// 使用HeatTransferManager进行热传递
			HeatTransferManager.getInstance().transferHeatBetweenGases(
					this, neighbor, level, selfPos, neighborPos);
		}
	}

	private void applyVerticalStratification(Level level, BlockPos selfPos) {
		// 检查上下两个方块，如果有相反密度顺序则尝试交换位置
		BlockPos above = selfPos.above();
		BlockPos below = selfPos.below();

		GasBlockEntity aboveBE = getGasEntity(level, above);
		GasBlockEntity belowBE = getGasEntity(level, below);

		// 轻气体应该在上方：如果上方更重而下方更轻（或上方为空）则尝试上浮
		if (aboveBE != null) {
			if (shouldRiseAbove(this, aboveBE)) {
				// 仅在同tick概率门限下进行交换，避免抖动
				if (level.random.nextFloat() < 0.5f) {
					swapGasBlocks(level, selfPos, above, this, aboveBE);
					return;
				}
			}
		}
		// 重气体应该在下方：如果下方更轻而自己更重则尝试下沉
		if (belowBE != null) {
			if (shouldSinkBelow(this, belowBE)) {
				if (level.random.nextFloat() < 0.5f) {
					swapGasBlocks(level, selfPos, below, this, belowBE);
				}
			}
		}
	}

	private boolean shouldRiseAbove(GasBlockEntity self, GasBlockEntity above) {
		// 两者不同气体或不同密度时，密度低的在上方
		float selfD = self.getGasProperties().density;
		float aboveD = above.getGasProperties().density;
		// 如果上方密度更大，则应该上浮
		return selfD < aboveD;
	}

	private boolean shouldSinkBelow(GasBlockEntity self, GasBlockEntity below) {
		float selfD = self.getGasProperties().density;
		float belowD = below.getGasProperties().density;
		// 如果下方密度更小，则应该下沉
		return selfD > belowD;
	}

	private static void shuffleArray(Level level, BlockPos[] arr) {
		for (int i = arr.length - 1; i > 0; i--) {
			int j = level.random.nextInt(i + 1);
			BlockPos tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
	}

	private void ensureTemperatureInitialized() {
		Level level = getLevel();
		BlockPos pos = getBlockPos();
		if (level != null) {
			HeatTransferManager.ensureTemperatureInitialized(this, level, pos);
		}
	}

	private static boolean isPrimaryPair(BlockPos selfPos, BlockPos otherPos) {
		if (otherPos == null) return false;
		if (selfPos.getX() != otherPos.getX()) {
			return selfPos.getX() < otherPos.getX();
		}
		if (selfPos.getY() != otherPos.getY()) {
			return selfPos.getY() < otherPos.getY();
		}
		return selfPos.getZ() < otherPos.getZ();
	}

	private static double clamp(double v, double min, double max) {
		if (v < min) return min;
		if (v > max) return max;
		return v;
	}


	public GasProperties getGasProperties() {
		return gas.getProperties();
	}

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
		this.amount = Math.max(0L, amount);
		setChanged();
		sync();
		if (this.amount <= 0L) {
			Level world = getLevel();
			if (world != null && !world.isClientSide) {
				world.removeBlock(getBlockPos(), false);
			}
		}
    }

	public float getConcentration() {
		double c = (double) amount / (double) GasStack.STANDARD_PRESSURE;
		if (c < 0.0) return 0.0f;
		if (c > 1.0) return 1.0f;
		return (float) c;
	}


    public Gas gas() {
        return gas;
    }

	public double getKelvin() {
		return kelvin;
	}

	public void setKelvin(double kelvin) {
		if (Double.isNaN(kelvin)) {
			return;
		}
		double current = getKelvin();
		if (Math.abs(current - kelvin) < 1.0E-6D) {
			return;
		}
		this.kelvin = kelvin;
		setChanged();
		sync();
	}

	public Gas getGas() {
		return gas;
	}

	private void swapGasBlocks(Level level, BlockPos aPos, BlockPos bPos, GasBlockEntity aBlockEntity, GasBlockEntity bBlockEntity) {
		long aAmount = aBlockEntity.amount;
		long bAmount = bBlockEntity.amount;
		Gas aGas = aBlockEntity.gas;
		Gas bGas = bBlockEntity.gas;

		level.setBlock(aPos, bGas.createBlock().defaultBlockState(), 3);
		level.setBlock(bPos, aGas.createBlock().defaultBlockState(), 3);

		BlockEntity beA = level.getBlockEntity(aPos);
		BlockEntity beB = level.getBlockEntity(bPos);
		if (beA instanceof GasBlockEntity newA) {
			newA.setAmount(aAmount);
		}
		if (beB instanceof GasBlockEntity newB) {
			newB.setAmount(bAmount);
		}
	}


	private GasBlockEntity getGasEntity(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof GasBlockEntity gbe) return gbe;
		return null;
	}

	private void sync() {
		Level world = getLevel();
		if (world != null && !world.isClientSide) {
			BlockState state = getBlockState();
			world.sendBlockUpdated(getBlockPos(), state, state, 3);
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		saveAdditional(tag, registries);
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		loadAdditional(tag, registries);
	}

	@Override
	public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		CompoundTag tag = pkt.getTag();
		loadAdditional(tag, registries);
	}
}
