package com.xiaohunao.oxygen_not_included.common.init;

import com.mojang.datafixers.DSL;
import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.block.entity.AirBlockEntity;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ONIBlockEntityTypes {
    public static final FlexibleRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = FlexibleRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<BlockEntityType<?>, BlockEntityType<AirBlockEntity>> AIR_BLOCK_ENTITY = BLOCK_ENTITY_TYPE.registerStatic(
            "air_block_entity",
            () -> BlockEntityType.Builder.of(AirBlockEntity::new, Blocks.AIR,Blocks.CAVE_AIR).build(DSL.remainderType())
    );
}
