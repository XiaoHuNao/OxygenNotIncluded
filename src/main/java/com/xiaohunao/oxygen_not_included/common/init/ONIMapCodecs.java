package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.interaction.HeatSourceInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.interaction.OverpressureBlockInteraction;
import com.xiaohunao.xhn_lib.api.register.register.MapCodecFlexibleRegister;

public class ONIMapCodecs {
    public static final MapCodecFlexibleRegister<GasInteraction> GAS_INTERACTION_CODEC = MapCodecFlexibleRegister.createMapCodec(ONIRegistries.Keys.GAS_INTERACTION_CODEC, OxygenNotIncluded.MODID)
            .addMapCodec("heat_source", HeatSourceInteraction.CODEC)
            .addMapCodec("overpressure_block", OverpressureBlockInteraction.CODEC);
}
