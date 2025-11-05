package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import com.xiaohunao.oxygen_not_included.common.gas.gases.CarbonDioxideGas;
import com.xiaohunao.oxygen_not_included.common.gas.gases.OxygenGas;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;

public class ONIGases {
    public static final FlexibleRegister<Gas> GAS = FlexibleRegister.create(ONIRegistries.GAS, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<Gas, Gas> OXYGEN = GAS.registerStatic("oxygen", OxygenGas::new);  // 氧气
    public static final FlexibleHolder<Gas, Gas> CARBON_DIOXIDE = GAS.registerStatic("carbon_dioxide", CarbonDioxideGas::new);  // 二氧化碳
}
