package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.item.GasTankItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 自定义创造模式物品栏标签。
 */
public class ONICreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OxygenNotIncluded.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GAS_TANKS = CREATIVE_MODE_TABS.register(
            "gas_tanks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable(OxygenNotIncluded.asDescriptionId("creative_tab.gas_tanks")))
                    .icon(() -> new ItemStack(ONIItems.GAS_TANK.get()))
                    .displayItems((params, output) -> {
                        // 遍历所有已注册的气体，为每种气体添加一个满的储气罐
                        HolderLookup.Provider provider = params.holders();
                        provider.lookup(ONIRegistries.Keys.GAS).ifPresent(lookup -> {
                            for (Holder.Reference<Gas> ref : lookup.listElements().toList()) {
                                Gas gas = ref.value();
                                ItemStack stack = new ItemStack(ONIItems.GAS_TANK.get());
                                // 填充满容量
                                GasTankItem.setStored(stack, gas, ONIItems.GAS_TANK.get().getCapacity());
                                output.accept(stack);
                            }
                        });
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable(OxygenNotIncluded.asDescriptionId("creative_tab.main")))
                    .icon(() -> new ItemStack(ONIItems.GAS_GOGGLES.get()))
                    .displayItems((params, output) -> {
                        output.accept(ONIItems.GAS_GOGGLES.get());
                    })
                    .build()
    );
}



















