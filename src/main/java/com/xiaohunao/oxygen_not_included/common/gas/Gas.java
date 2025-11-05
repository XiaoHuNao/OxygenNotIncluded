package com.xiaohunao.oxygen_not_included.common.gas;

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


    public abstract BlockState createBlock();
}
