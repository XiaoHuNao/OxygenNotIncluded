package com.xiaohunao.oxygen_not_included.common.gas.gases;

import com.xiaohunao.oxygen_not_included.common.block.GasBlock;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlocks;

public class ChlorineGas extends Gas {
    public ChlorineGas() {
        super(GasProperties.builder()
                .color(0x80FFE0E0)
                .density(3.21f)
                .heatCapacity(0.48)
                .build()
        );
    }

    @Override
    public GasBlock createBlock() {
        return ONIBlocks.CHLORINE.get();
    }
}

