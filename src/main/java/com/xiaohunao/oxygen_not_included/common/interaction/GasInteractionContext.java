package com.xiaohunao.oxygen_not_included.common.interaction;

import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.gas.GasStack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * 表示一次气体交互的上下文信息。
 * 下文会针对不同交互对象给出具体的子类。
 */
public record GasInteractionContext(
		Level level,
		GasBlockEntity source,
		Gas gas,
		long amount,
		double kelvin,
		BlockPos targetPos,
		BlockState targetState,
		Direction face,
		Optional<List<Entity>> entities) {


	public Optional<FluidState> fluidState() {
		return Optional.of(level.getFluidState(targetPos));
	}

	public static class Builder {
		private final Level level;
		private final GasBlockEntity source;
		private final Gas gas;
		private final long amount;
		private final double kelvin;
		private final BlockPos targetPos;
		private final BlockState targetState;
		private final Direction face;
		private List<Entity> entities;

		public Builder(Level level, GasBlockEntity source,BlockPos targetPos,Direction face) {
			this.level = level;
			this.source = source;
			this.gas = source.gas();
			this.amount = source.getAmount();
			this.kelvin = source.getKelvin();
			this.targetPos = targetPos;
			this.targetState = level.getBlockState(targetPos);
			this.face = face;
		}

		public Builder entities(List<Entity> entities) {
			this.entities = entities;
			return this;
		}

		public GasInteractionContext build() {
			return new GasInteractionContext(level, source, gas, amount, kelvin, targetPos, targetState, face, Optional.ofNullable(entities));
		}
	}
}

