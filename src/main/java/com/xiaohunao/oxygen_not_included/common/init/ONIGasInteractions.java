package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.interaction.HeatSourceInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.interaction.HotGasSmeltingInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.interaction.OverpressureBlockInteraction;

import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;


public final class ONIGasInteractions {
	public static final FlexibleRegister<GasInteraction> GAS_INTERACTIONS = FlexibleRegister.create(ONIRegistries.Keys.GAS_INTERACTION, OxygenNotIncluded.MODID);

	public static final FlexibleHolder<GasInteraction, OverpressureBlockInteraction> OVERPRESSURE = GAS_INTERACTIONS.registerStatic("overpressure_block_interaction", OverpressureBlockInteraction::new);
	public static final FlexibleHolder<GasInteraction, HeatSourceInteraction> HEAT_SOURCE = GAS_INTERACTIONS.registerStatic("heat_source_interaction", HeatSourceInteraction::new);
	public static final FlexibleHolder<GasInteraction, HotGasSmeltingInteraction> HOT_GAS_SMELTING = GAS_INTERACTIONS.registerStatic("hot_gas_smelting_interaction", HotGasSmeltingInteraction::new);

	public static void register(IEventBus bus) {
		GAS_INTERACTIONS.register(bus);
	}
}

