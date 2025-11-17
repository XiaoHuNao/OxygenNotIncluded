package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.block.GasBlock;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class ONIBlocks {
    public static final FlexibleRegister<Block> BLOK = FlexibleRegister.create(Registries.BLOCK, OxygenNotIncluded.MODID);


    public static final FlexibleHolder<Block, GasBlock> OXYGEN = BLOK.registerStatic("oxygen",
        () -> new GasBlock(ONIGases.OXYGEN));

    public static final FlexibleHolder<Block, GasBlock> CARBON_DIOXIDE = BLOK.registerStatic("carbon_dioxide",
        () -> new GasBlock(ONIGases.CARBON_DIOXIDE));

    public static final FlexibleHolder<Block, GasBlock> HYDROGEN = BLOK.registerStatic("hydrogen",
        () -> new GasBlock(ONIGases.HYDROGEN));

    public static final FlexibleHolder<Block, GasBlock> CHLORINE = BLOK.registerStatic("chlorine",
        () -> new GasBlock(ONIGases.CHLORINE));
}
