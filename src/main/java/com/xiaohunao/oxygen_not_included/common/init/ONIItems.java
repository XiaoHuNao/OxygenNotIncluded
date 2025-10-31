package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.item.GasTankItem;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;

import net.minecraft.world.item.Item;

public class ONIItems {
    public static final FlexibleRegister<Item> ITEMS = FlexibleRegister.create(net.minecraft.core.registries.Registries.ITEM, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<Item, Item> GAS_TANK = ITEMS.registerStatic("gas_tank", () -> new GasTankItem(new Item.Properties().stacksTo(1)));
}


