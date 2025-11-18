package com.xiaohunao.oxygen_not_included.common.interaction;

import com.xiaohunao.oxygen_not_included.common.block.GasBlock;
import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;
import com.xiaohunao.xhn_lib.api.data.loader.BaseDynamicLoader;
import com.xiaohunao.xhn_lib.common.serialization.IDynamicSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class GasInteractionManager extends BaseDynamicLoader<GasInteraction> {

	public GasInteractionManager(Registry<GasInteraction> registry, IDynamicSerializer<GasInteraction> serializer) {
		super(registry, IDynamicSerializer.of(GasInteraction.CODEC));
	}

	public static void handleBlockInteractions(GasBlockEntity source) {
		Level level = source.getLevel();
		if (level == null || level.isClientSide) {
			return;
		}

		BlockPos sourcePos = source.getBlockPos();

		handleSelfInteractions(level, source, sourcePos);

		for (Direction direction : Direction.values()) {
			BlockPos targetPos = sourcePos.relative(direction);
			BlockState targetState = level.getBlockState(targetPos);
			Block block = targetState.getBlock();

			GasInteractionContext.Builder context = new GasInteractionContext.Builder(level, source, targetPos, direction);

			if (block instanceof GasBlock) {
				BlockEntity entity = level.getBlockEntity(targetPos);
				if (entity instanceof GasBlockEntity) {
					dispatch(GasInteractionCategory.GAS, context.build());
				}
				continue;
			}

			FluidState fluidState = targetState.getFluidState();
			if (!fluidState.isEmpty()) {
				dispatch(GasInteractionCategory.FLUID, context.build());
			}

			if (block instanceof AirBlock) {
				AABB cellBounds = new AABB(targetPos);
				List<Entity> entities = level.getEntitiesOfClass(Entity.class, cellBounds);
                entities.removeIf(entity -> entity == null || entity.isRemoved());
				dispatch(GasInteractionCategory.ENTITY, context.entities(entities).build());
				continue;
			}

			dispatch(GasInteractionCategory.BLOCK, context.build());
		}
	}

	private static void handleSelfInteractions(Level level, GasBlockEntity source, BlockPos sourcePos) {
		AABB selfBounds = new AABB(sourcePos);
		List<Entity> entities = level.getEntitiesOfClass(Entity.class, selfBounds);
		entities.removeIf(entity -> entity == null || entity.isRemoved());
		if (entities.isEmpty()) {
			return;
		}

		GasInteractionContext.Builder context = new GasInteractionContext.Builder(level, source, sourcePos, Direction.UP);
		dispatch(GasInteractionCategory.ENTITY, context.entities(entities).build());
	}

	private static void dispatch(GasInteractionCategory category, GasInteractionContext context) {
		Registry<GasInteraction> registry = ONIRegistries.GAS_INTERACTION;

        for (GasInteraction interaction : registry) {
			if (interaction == null) continue;
			if (!interaction.category().contains(category)) {
				continue;
			}
            if (!interaction.matches(context)) {
				continue;
			}
			interaction.apply(context);
		}
	}
}

