package com.xiaohunao.oxygen_not_included.common.interaction.interaction;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.gas.GasStack;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionCategory;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionContext;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

/**
 * 当气体产生的压强超过邻近方块的承受能力时，尝试破坏该方块。
 */
public class OverpressureBlockInteraction implements GasInteraction {
	public static final MapCodec<OverpressureBlockInteraction> CODEC = MapCodec.unit(OverpressureBlockInteraction::new);


	@Override
	public List<GasInteractionCategory> category() {
		return Collections.singletonList(GasInteractionCategory.BLOCK);
	}

	@Override
	public boolean matches(GasInteractionContext context) {
		return !context.targetState().isAir() && !context.targetState().canBeReplaced();
	}

	@Override
	public void apply(GasInteractionContext context) {
		long amount = context.amount();
		if (amount <= GasStack.STANDARD_PRESSURE) {
			return;
		}

		BlockState barrier = context.targetState();
		float destroySpeed = barrier.getDestroySpeed(context.level(), context.targetPos());
		if (destroySpeed < 0.0F) {
			return;
		}

		GasProperties properties = context.gas().getProperties();
		double pressureFactor = (double) amount / GasStack.STANDARD_PRESSURE;

		double baseTolerance = 1.0D + Math.max(0.0D, destroySpeed);
		double thermalBonus = Math.max(0.5D, properties.heatCapacity);
		double viscosityFactor = Math.max(0.5D, properties.viscosity);

		double tolerance = baseTolerance * thermalBonus * viscosityFactor;
		double overload = pressureFactor - tolerance;
		if (overload <= 0.0D) {
			return;
		}

		double breakChance = Math.min(1.0D, overload * 0.25D);
		if (context.level().random.nextDouble() < breakChance) {
			context.level().destroyBlock(context.targetPos(), true);
		}
	}

	@Override
	public MapCodec<? extends GasInteraction> codec() {
		return CODEC;
	}
}

