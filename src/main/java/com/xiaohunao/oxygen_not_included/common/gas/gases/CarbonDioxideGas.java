package com.xiaohunao.oxygen_not_included.common.gas.gases;

import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarbonDioxideGas extends Gas {
    public CarbonDioxideGas() {
        super(GasProperties.builder()
                .color(0xFF404040)
                .density(1.53f)
                .suffocation(true)
                .build());
    }

    @Override
    public BlockState createBlock() {
        return ONIBlocks.CARBON_DIOXIDE.get().defaultBlockState();
    }
}
