package com.xiaohunao.oxygen_not_included.common.init;

import com.mojang.datafixers.DSL;
import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ONIBlockEntityTypes {
    public static final FlexibleRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = FlexibleRegister.create(Registries.BLOCK_ENTITY_TYPE, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<BlockEntityType<?>, BlockEntityType<GasBlockEntity>> GAS = BLOCK_ENTITY_TYPE.registerStatic("gas", () ->
            BlockEntityType.Builder.of(GasBlockEntity::new,ONIBlocks.OXYGEN.get()).build(DSL.remainderType()));

}
