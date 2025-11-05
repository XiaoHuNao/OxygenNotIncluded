package com.xiaohunao.oxygen_not_included.mixin;

import com.xiaohunao.oxygen_not_included.common.block.GasBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {
    @Shadow @Final private Set<Block> validBlocks;

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    public void isValid(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof GasBlock){
            for (Block validBlock : validBlocks) {
                if (validBlock instanceof GasBlock) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}
