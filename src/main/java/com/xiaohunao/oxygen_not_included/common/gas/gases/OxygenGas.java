package com.xiaohunao.oxygen_not_included.common.gas.gases;

import com.xiaohunao.oxygen_not_included.common.block.GasBlock;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class OxygenGas extends Gas {
    public OxygenGas() {
        super(GasProperties.builder()
                .color(0x80C8E8FF)
                .density(16.0f)
                .heatCapacity(1.005)
                .build()
        );
    }

    @Override
    public GasBlock createBlock() {
        return ONIBlocks.OXYGEN.get();
    }
}
