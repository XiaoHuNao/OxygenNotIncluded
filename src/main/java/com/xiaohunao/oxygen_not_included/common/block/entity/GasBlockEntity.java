package com.xiaohunao.oxygen_not_included.common.block.entity;

import com.xiaohunao.oxygen_not_included.common.init.ONIBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GasBlockEntity extends BlockEntity {

    public GasBlockEntity(BlockPos pos, BlockState blockState) {
        super(ONIBlockEntityTypes.GAS.get(),pos,blockState);
    }


    public static void serverTick(Level level, BlockPos pos, BlockState blockState, GasBlockEntity blockEntity) {
        System.out.println(level + "" + pos);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState blockState, GasBlockEntity blockEntity) {
        System.out.println(level + "" + pos);
    }
}
