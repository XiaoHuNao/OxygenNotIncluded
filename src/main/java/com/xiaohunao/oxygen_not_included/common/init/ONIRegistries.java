package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ONIRegistries {
    public static final Registry<Gas> GAS = new RegistryBuilder<>(Keys.GAS).create();

    public static final class Keys {
        public static final ResourceKey<Registry<Gas>> GAS = OxygenNotIncluded.asResourceKey("gas");
    }

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(GAS);
    }

}
