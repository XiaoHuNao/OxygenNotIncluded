package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.heat_source.HeatSource;
import com.xiaohunao.oxygen_not_included.common.heat_source.HeatSourceManager;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class ONIHeatSources {
    public static final FlexibleRegister<HeatSource> HEAT_SOURCE = FlexibleRegister.create(ONIRegistries.HEAT_SOURCE, OxygenNotIncluded.MODID, HeatSourceManager.getInstance());

    //火
    public static final FlexibleHolder<HeatSource, HeatSource> FIRE = HEAT_SOURCE.registerStatic("fire", () -> new HeatSource.BlockHeatSource(Blocks.FIRE,800, 0.8));
    //岩浆块
    public static final FlexibleHolder<HeatSource, HeatSource> MAGMA_BLOCK = HEAT_SOURCE.registerStatic("magma_block", () -> new HeatSource.BlockHeatSource(Blocks.MAGMA_BLOCK,1300, 0.5));

    //岩浆
    public static final FlexibleHolder<HeatSource, HeatSource> LAVA = HEAT_SOURCE.registerStatic("lava", () -> new HeatSource.FluidHeatSource(Fluids.LAVA,1300, 1.5));

    //岩浆怪
    public static final FlexibleHolder<HeatSource, HeatSource> MAGMA_CUBE = HEAT_SOURCE.registerStatic("magma_cube", () -> new HeatSource.EntityHeatSource(EntityType.MAGMA_CUBE,1000, 1.0));
    //烈焰人
    public static final FlexibleHolder<HeatSource, HeatSource> BLAZE = HEAT_SOURCE.registerStatic("blaze", () -> new HeatSource.EntityHeatSource(EntityType.BLAZE,1000, 1.0));
}
