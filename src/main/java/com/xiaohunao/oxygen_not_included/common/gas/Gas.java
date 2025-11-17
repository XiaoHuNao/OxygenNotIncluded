package com.xiaohunao.oxygen_not_included.common.gas;

import com.xiaohunao.oxygen_not_included.common.block.GasBlock;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public abstract class Gas {
    private final GasProperties properties;

    public Gas(GasProperties properties) {
        this.properties = properties;
    }

    public GasProperties getProperties() {
        return properties;
    }


    public abstract GasBlock createBlock();

    public ResourceLocation getRegistryName() {
        return ONIRegistries.GAS.getKey(this);
    }

}
