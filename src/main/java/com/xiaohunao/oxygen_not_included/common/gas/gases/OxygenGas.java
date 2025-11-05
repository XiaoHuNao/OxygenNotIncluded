package com.xiaohunao.oxygen_not_included.common.gas.gases;

import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class OxygenGas extends Gas {
    public OxygenGas() {
        super(GasProperties.builder(0xFF404040));
    }

    @Override
    public BlockState createBlock() {
        return ONIBlocks.OXYGEN.get().defaultBlockState();
    }
}
