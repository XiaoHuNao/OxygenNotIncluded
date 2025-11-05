package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.item.GasTankItem;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ONIItems {
    public static final FlexibleRegister<Item> ITEM = FlexibleRegister.create(Registries.ITEM, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<Item, GasTankItem> GAS_TANK = ITEM.registerStatic(
        "gas_tank",
        () -> new GasTankItem(new Item.Properties(), 4 * 1000) // 4 标准单位容量
    );
}


