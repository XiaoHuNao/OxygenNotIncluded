package com.xiaohunao.oxygen_not_included.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlockEntityTypes;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GasBlock extends BaseEntityBlock {
    public static final MapCodec<GasBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ONIRegistries.GAS.byNameCodec().fieldOf("gas").forGetter(GasBlock::getGas)
    ).apply(instance,GasBlock::new));

    private final Gas gas;

    public GasBlock(Gas gas) {
        super(BlockBehaviour.Properties.of().replaceable().noCollission().noLootTable().air());
        this.gas = gas;
    }

    public Gas getGas() {
        return gas;
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    public @NotNull MapCodec<GasBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GasBlockEntity(blockPos,blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ?
                BaseEntityBlock.createTickerHelper(blockEntityType, ONIBlockEntityTypes.GAS.get(), GasBlockEntity::clientTick) :
                BaseEntityBlock.createTickerHelper(blockEntityType, ONIBlockEntityTypes.GAS.get(), GasBlockEntity::serverTick);
    }
}
