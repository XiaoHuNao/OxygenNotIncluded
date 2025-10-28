package com.xiaohunao.oxygen_not_included.common.mixin;

import com.xiaohunao.oxygen_not_included.common.block.entity.AirBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AirBlock.class)
public class AirBlockMixin implements EntityBlock {

    @Override
    @Nullable
    @Unique
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new AirBlockEntity(blockPos, blockState);
    }
}
