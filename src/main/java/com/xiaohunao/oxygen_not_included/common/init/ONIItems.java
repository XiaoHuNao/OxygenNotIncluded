package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.component.GasGogglesRenderModeData;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesItem;
import com.xiaohunao.oxygen_not_included.common.item.GasTankItem;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

public class ONIItems {
    public static final FlexibleRegister<Item> ITEM = FlexibleRegister.create(Registries.ITEM, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<Item, GasTankItem> GAS_TANK = ITEM.registerStatic(
        "gas_tank",
        () -> new GasTankItem(new Item.Properties(), 1000)
    );

    public static final FlexibleHolder<Item, GasGogglesItem> GAS_GOGGLES = ITEM.registerStatic(
        "gas_goggles",
        () -> new GasGogglesItem(
                net.minecraft.core.registries.BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(ArmorMaterials.LEATHER.value()),
                new Item.Properties().component(ONIComponents.GAS_GOGGLES_MODE.get(), GasGogglesRenderModeData.DEFAULT)
        )
    );
}


