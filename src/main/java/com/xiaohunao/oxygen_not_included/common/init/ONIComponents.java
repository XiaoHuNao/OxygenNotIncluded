package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.component.GasGogglesRenderModeData;
import com.xiaohunao.oxygen_not_included.common.component.GasTankData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ONIComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OxygenNotIncluded.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GasTankData>> GAS_TANK = DATA_COMPONENT_TYPES.register(
            "gas_tank",
            () -> DataComponentType.<GasTankData>builder()
                    .persistent(GasTankData.CODEC)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GasGogglesRenderModeData>> GAS_GOGGLES_MODE = DATA_COMPONENT_TYPES.register(
            "gas_goggles_mode",
            () -> DataComponentType.<GasGogglesRenderModeData>builder()
                    .persistent(GasGogglesRenderModeData.CODEC)
                    .build()
    );
}


