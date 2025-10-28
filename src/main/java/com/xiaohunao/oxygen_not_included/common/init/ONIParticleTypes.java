package com.xiaohunao.oxygen_not_included.common.init;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ONIParticleTypes {
    public static final FlexibleRegister<ParticleType<?>> PARTICLE_TYPE = FlexibleRegister.create(BuiltInRegistries.PARTICLE_TYPE, OxygenNotIncluded.MODID);

    public static final FlexibleHolder<ParticleType<?>, SimpleParticleType> GAS = PARTICLE_TYPE.registerStatic(
            "gas",
            () -> new SimpleParticleType(true)
    );
}


