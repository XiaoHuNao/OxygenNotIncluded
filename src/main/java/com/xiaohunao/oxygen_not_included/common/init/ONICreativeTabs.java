package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.capability.impl.ItemStackGasStorage;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.item.GasTankItem;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ONICreativeTabs {
    public static final FlexibleRegister<CreativeModeTab> CREATIVE_TAB = FlexibleRegister.create(Registries.CREATIVE_MODE_TAB, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TAB.registerStatic("main", () -> CreativeModeTab.builder()
            .title(Component.translatable(OxygenNotIncluded.asDescriptionId("creative_tab.main")))
            .icon(() -> new ItemStack(ONIItems.GAS_TANK.get()))
            .displayItems((params, output) -> {
                // 空罐
                output.accept(new ItemStack(ONIItems.GAS_TANK.get()));

                // 每种气体各一个满罐
                for (Gas gas : ONIRegistries.GAS) {
                    ItemStack stack = new ItemStack(ONIItems.GAS_TANK.get());
                    ItemStackGasStorage storage = new ItemStackGasStorage(stack, GasTankItem.DEFAULT_CAPACITY);
                    storage.insert(gas, GasTankItem.DEFAULT_CAPACITY, false);
                    output.accept(stack);
                }
            })
            .build());
}


