package com.xiaohunao.oxygen_not_included.common.interaction.interaction;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.oxygen_not_included.common.heat_source.HeatSourceManager;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionCategory;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.List;

public class HeatSourceInteraction implements GasInteraction {
    public static final MapCodec<HeatSourceInteraction> CODEC = MapCodec.unit(new HeatSourceInteraction());

    @Override
    public List<GasInteractionCategory> category() {
        return List.of(GasInteractionCategory.BLOCK, GasInteractionCategory.FLUID, GasInteractionCategory.ENTITY);
    }

    @Override
    public boolean matches(GasInteractionContext context) {
        return HeatSourceManager.getInstance().matches(context);
    }

    @Override
    public void apply(GasInteractionContext context) {
        HeatSourceManager.getInstance().apply(context);
    }

    @Override
    public MapCodec<? extends GasInteraction> codec() {
        return CODEC;
    }
}
