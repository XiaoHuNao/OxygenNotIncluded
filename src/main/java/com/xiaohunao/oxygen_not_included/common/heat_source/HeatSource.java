package com.xiaohunao.oxygen_not_included.common.heat_source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteraction;
import com.xiaohunao.xhn_lib.common.codec.ICodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Function;

public abstract class HeatSource implements ICodec<HeatSource> {
    public static final Codec<HeatSource> CODEC = Codec.lazyInitialized(ONIRegistries.HEAT_SOURCE_CODEC::byNameCodec).dispatch(HeatSource::codec, Function.identity());

    private final double targetTemperature;
    private final double thermalConductivity;

    public HeatSource(double targetTemperature, double thermalConductivity) {
        this.targetTemperature = targetTemperature;
        this.thermalConductivity = thermalConductivity;
    }

    public double getThermalConductivity() {
        return thermalConductivity;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public static class BlockHeatSource extends HeatSource {
        public static final MapCodec<BlockHeatSource> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Block.CODEC.fieldOf("block").forGetter(BlockHeatSource::getBlock),
                Codec.DOUBLE.fieldOf("target_temperature").forGetter(BlockHeatSource::getTargetTemperature),
                Codec.DOUBLE.fieldOf("thermal_conductivity").forGetter(BlockHeatSource::getThermalConductivity)
        ).apply(builder, BlockHeatSource::new));

        private final Block block;

        public BlockHeatSource(Block block, double targetTemperature, double thermalConductivity) {
            super(targetTemperature, thermalConductivity);
            this.block = block;
        }

        public Block getBlock() {
            return block;
        }

        @Override
        public MapCodec<? extends HeatSource> codec() {
            return CODEC;
        }
    }

    public static class FluidHeatSource extends HeatSource {
        public static final MapCodec<FluidHeatSource> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidHeatSource::getFluid),
                Codec.DOUBLE.fieldOf("target_temperature").forGetter(FluidHeatSource::getTargetTemperature),
                Codec.DOUBLE.fieldOf("thermal_conductivity").forGetter(FluidHeatSource::getThermalConductivity)
        ).apply(builder, FluidHeatSource::new));

        private final Fluid fluid;

        public FluidHeatSource(Fluid fluid, double targetTemperature, double thermalConductivity) {
            super(targetTemperature, thermalConductivity);
            this.fluid = fluid;
        }

        public Fluid getFluid() {
            return fluid;
        }

        @Override
        public MapCodec<? extends HeatSource> codec() {
            return CODEC;
        }
    }

    public static class EntityHeatSource extends HeatSource {
        public static final MapCodec<EntityHeatSource> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(EntityHeatSource::getEntityType),
                Codec.DOUBLE.fieldOf("target_temperature").forGetter(EntityHeatSource::getTargetTemperature),
                Codec.DOUBLE.fieldOf("thermal_conductivity").forGetter(EntityHeatSource::getThermalConductivity)
        ).apply(builder, EntityHeatSource::new));

        private final EntityType<?> entityType;

        public EntityHeatSource(EntityType<?> entityType, double targetTemperature, double thermalConductivity) {
            super(targetTemperature, thermalConductivity);
            this.entityType = entityType;
        }

        public EntityType<?> getEntityType() {
            return entityType;
        }

        @Override
        public MapCodec<? extends HeatSource> codec() {
            return CODEC;
        }
    }
}
