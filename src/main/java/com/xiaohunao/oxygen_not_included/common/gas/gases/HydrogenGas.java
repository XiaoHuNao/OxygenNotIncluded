package com.xiaohunao.oxygen_not_included.common.gas.gases;

import com.xiaohunao.oxygen_not_included.common.block.GasBlock;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlocks;

public class HydrogenGas extends Gas {
    public HydrogenGas() {
        super(GasProperties.builder()
                .color(0x80E0E0FF)
                .density(1.01f)
                .heatCapacity(2.4)
                .build()
        );
    }

    @Override
    public GasBlock createBlock() {
        return ONIBlocks.HYDROGEN.get();
    }
}

