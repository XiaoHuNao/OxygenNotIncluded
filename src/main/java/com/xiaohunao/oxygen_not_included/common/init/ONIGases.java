package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;

public class ONIGases {
    public static final FlexibleRegister<Gas> GAS = FlexibleRegister.create(ONIRegistries.Keys.GAS, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<Gas, Gas> OXYGEN = GAS.registerStatic("oxygen", () -> new Gas(GasProperties.oxygen()));
    public static final FlexibleHolder<Gas, Gas> CARBON_DIOXIDE = GAS.registerStatic("carbon_dioxide", () -> new Gas(GasProperties.carbonDioxide()));
    public static final FlexibleHolder<Gas, Gas> HYDROGEN = GAS.registerStatic("hydrogen", () -> new Gas(GasProperties.hydrogen()));
}
