package com.xiaohunao.oxygen_not_included.common.util;

import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlocks;
import com.xiaohunao.oxygen_not_included.common.init.ONIGases;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 世界相关的工具方法
 */
public final class WorldUtils {
    private WorldUtils() {}

    public static boolean tryPlaceContainedGas(Level level, BlockPos pos, Gas gas, Player actor) {
        if (level == null || pos == null || gas == null) return false;
        BlockState state = level.getBlockState(pos);
        if (!state.canBeReplaced()) return false;
        return level.setBlock(pos, gas.createBlock(), Block.UPDATE_ALL_IMMEDIATE);
    }

}


