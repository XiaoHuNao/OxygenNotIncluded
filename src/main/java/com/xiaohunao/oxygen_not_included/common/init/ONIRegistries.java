package com.xiaohunao.oxygen_not_included.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.heat_source.HeatSource;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteraction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ONIRegistries {
    public static final Registry<Gas> GAS = new RegistryBuilder<>(Keys.GAS).create();
    public static final Registry<GasInteraction> GAS_INTERACTION = new RegistryBuilder<>(Keys.GAS_INTERACTION).create();

    public static final Registry<HeatSource> HEAT_SOURCE = new RegistryBuilder<>(Keys.HEAT_SOURCE).create();

    public static final Registry<MapCodec<? extends GasInteraction>> GAS_INTERACTION_CODEC = new RegistryBuilder<>(Keys.GAS_INTERACTION_CODEC).create();
    public static final Registry<MapCodec<? extends HeatSource>> HEAT_SOURCE_CODEC = new RegistryBuilder<>(Keys.HEAT_SOURCE_CODEC).create();




    public static final class Keys {
        public static final ResourceKey<Registry<Gas>> GAS = OxygenNotIncluded.asResourceKey("gas");
        public static final ResourceKey<Registry<GasInteraction>> GAS_INTERACTION = OxygenNotIncluded.asResourceKey("gas_interaction");
        public static final ResourceKey<Registry<HeatSource>> HEAT_SOURCE = OxygenNotIncluded.asResourceKey("heat_source");

        public static final ResourceKey<Registry<MapCodec<? extends GasInteraction>>> GAS_INTERACTION_CODEC = OxygenNotIncluded.asResourceKey("gas_interaction_codec");
        public static final ResourceKey<Registry<MapCodec<? extends HeatSource>>> HEAT_SOURCE_CODEC = OxygenNotIncluded.asResourceKey("heat_source_codec");
    }

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(GAS);
        event.register(GAS_INTERACTION);
        event.register(HEAT_SOURCE);

        event.register(GAS_INTERACTION_CODEC);
        event.register(HEAT_SOURCE_CODEC);
    }
}
