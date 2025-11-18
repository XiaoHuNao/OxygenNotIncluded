package com.xiaohunao.oxygen_not_included.common.heat_source;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteraction;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionContext;
import com.xiaohunao.xhn_lib.common.codec.ICodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public abstract class HeatSource implements ICodec<HeatSource> {
    public static final Codec<HeatSource> CODEC = Codec.lazyInitialized(ONIRegistries.HEAT_SOURCE_CODEC::byNameCodec).dispatch(HeatSource::codec, Function.identity());
    private static final BiConsumer<GasInteraction, GasInteractionContext> NO_OP_CALLBACK = (interaction, context) -> {};

    private final double targetTemperature;
    private final double thermalConductivity;
    private final long maxReleaseHeat; // -1 表示无限
    private final BiConsumer<GasInteraction, GasInteractionContext> onMaxReleaseHeat;
    private final BiConsumer<GasInteraction,GasInteractionContext> onReleaseHeat;

    public HeatSource(double targetTemperature, double thermalConductivity) {
        this(targetTemperature, thermalConductivity, -1L, null, null);
    }

    public HeatSource(double targetTemperature,
                      double thermalConductivity,
                      long maxReleaseHeat,
                      BiConsumer<GasInteraction, GasInteractionContext> onMaxReleaseHeat,
                      BiConsumer<GasInteraction, GasInteractionContext> onReleaseHeat) {
        this.targetTemperature = targetTemperature;
        this.thermalConductivity = thermalConductivity;
        this.maxReleaseHeat = maxReleaseHeat;
        this.onMaxReleaseHeat = onMaxReleaseHeat == null ? NO_OP_CALLBACK : onMaxReleaseHeat;
        this.onReleaseHeat = onReleaseHeat == null ? NO_OP_CALLBACK : onReleaseHeat;
    }

    public double getThermalConductivity() {
        return thermalConductivity;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public long getMaxReleaseHeat() {
        return maxReleaseHeat;
    }

    //当达到最大释放量时
    public void onMaxReleaseHeat(GasInteraction interaction, GasInteractionContext context) {
        this.onMaxReleaseHeat.accept(interaction, context);
    }
    
    //释放热量时
    public void onReleaseHeat(GasInteraction interaction, GasInteractionContext context) {
        this.onReleaseHeat.accept(interaction, context);
    }



    public static class BlockHeatSource extends HeatSource {
        public static final MapCodec<BlockHeatSource> BLOCK_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Block.CODEC.fieldOf("block").forGetter(BlockHeatSource::getBlock),
                Codec.DOUBLE.fieldOf("target_temperature").forGetter(BlockHeatSource::getTargetTemperature),
                Codec.DOUBLE.fieldOf("thermal_conductivity").forGetter(BlockHeatSource::getThermalConductivity)
        ).apply(builder, BlockHeatSource::new));

        private final Block block;

        public BlockHeatSource(Block block, double targetTemperature, double thermalConductivity) {
            super(targetTemperature, thermalConductivity);
            this.block = block;
        }

        public BlockHeatSource(Block block,
                               double targetTemperature,
                               double thermalConductivity,
                               long maxReleaseHeat,
                               BiConsumer<GasInteraction, GasInteractionContext> onMaxReleaseHeat,
                               BiConsumer<GasInteraction, GasInteractionContext> onReleaseHeat) {
            super(targetTemperature, thermalConductivity, maxReleaseHeat, onMaxReleaseHeat, onReleaseHeat);
            this.block = block;
        }

        public Block getBlock() {
            return block;
        }

        @Override
        public MapCodec<? extends HeatSource> codec() {
            return BLOCK_CODEC;
        }
    }

    public static class FluidHeatSource extends HeatSource {
        public static final MapCodec<FluidHeatSource> FLUID_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidHeatSource::getFluid),
                Codec.DOUBLE.fieldOf("target_temperature").forGetter(FluidHeatSource::getTargetTemperature),
                Codec.DOUBLE.fieldOf("thermal_conductivity").forGetter(FluidHeatSource::getThermalConductivity)
        ).apply(builder, FluidHeatSource::new));

        private final Fluid fluid;

        public FluidHeatSource(Fluid fluid, double targetTemperature, double thermalConductivity) {
            super(targetTemperature, thermalConductivity);
            this.fluid = fluid;
        }

        public FluidHeatSource(Fluid fluid,
                               double targetTemperature,
                               double thermalConductivity,
                               long maxReleaseHeat,
                               BiConsumer<GasInteraction, GasInteractionContext> onMaxReleaseHeat,
                               BiConsumer<GasInteraction, GasInteractionContext> onReleaseHeat) {
            super(targetTemperature, thermalConductivity, maxReleaseHeat, onMaxReleaseHeat, onReleaseHeat);
            this.fluid = fluid;
        }

        public Fluid getFluid() {
            return fluid;
        }

        @Override
        public MapCodec<? extends HeatSource> codec() {
            return FLUID_CODEC;
        }
    }

    public static class EntityHeatSource extends HeatSource {
        public static final MapCodec<EntityHeatSource> ENTITY_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(EntityHeatSource::getEntityType),
                Codec.DOUBLE.fieldOf("target_temperature").forGetter(EntityHeatSource::getTargetTemperature),
                Codec.DOUBLE.fieldOf("thermal_conductivity").forGetter(EntityHeatSource::getThermalConductivity)
        ).apply(builder, EntityHeatSource::new));

        private final EntityType<?> entityType;

        public EntityHeatSource(EntityType<?> entityType, double targetTemperature, double thermalConductivity) {
            super(targetTemperature, thermalConductivity);
            this.entityType = entityType;
        }

        public EntityHeatSource(EntityType<?> entityType,
                                double targetTemperature,
                                double thermalConductivity,
                                long maxReleaseHeat,
                                BiConsumer<GasInteraction, GasInteractionContext> onMaxReleaseHeat,
                                BiConsumer<GasInteraction, GasInteractionContext> onReleaseHeat) {
            super(targetTemperature, thermalConductivity, maxReleaseHeat, onMaxReleaseHeat, onReleaseHeat);
            this.entityType = entityType;
        }

        public EntityType<?> getEntityType() {
            return entityType;
        }

        @Override
        public MapCodec<? extends HeatSource> codec() {
            return ENTITY_CODEC;
        }
    }

    public static Builder builder(double targetTemperature, double thermalConductivity) {
        return new Builder(targetTemperature, thermalConductivity);
    }

    public static class Builder {
        private final Double targetTemperature;
        private final Double thermalConductivity;
        private long maxReleaseHeat = -1L;
        private BiConsumer<GasInteraction, GasInteractionContext> onMaxReleaseHeat = NO_OP_CALLBACK;
        private BiConsumer<GasInteraction, GasInteractionContext> onReleaseHeat = NO_OP_CALLBACK;

        private Builder(double targetTemperature, double thermalConductivity) {
            this.targetTemperature = targetTemperature;
            this.thermalConductivity = thermalConductivity;
        }


        public Builder maxReleaseHeat(long maxReleaseHeat) {
            this.maxReleaseHeat = maxReleaseHeat;
            return this;
        }

        public Builder onMaxReleaseHeat(BiConsumer<GasInteraction, GasInteractionContext> callback) {
            this.onMaxReleaseHeat = callback == null ? NO_OP_CALLBACK : callback;
            return this;
        }

        public Builder onReleaseHeat(BiConsumer<GasInteraction, GasInteractionContext> callback) {
            this.onReleaseHeat = callback == null ? NO_OP_CALLBACK : callback;
            return this;
        }

        public BlockHeatSource buildBlock(Block block) {
            if (block == null) {
                throw new IllegalArgumentException("block cannot be null");
            }
            return new BlockHeatSource(block, targetTemperature, thermalConductivity, maxReleaseHeat, onMaxReleaseHeat, onReleaseHeat);
        }

        public FluidHeatSource buildFluid(Fluid fluid) {
            if (fluid == null) {
                throw new IllegalArgumentException("fluid cannot be null");
            }
            return new FluidHeatSource(fluid, targetTemperature, thermalConductivity, maxReleaseHeat, onMaxReleaseHeat, onReleaseHeat);
        }

        public EntityHeatSource buildEntity(EntityType<?> entityType) {
            if (entityType == null) {
                throw new IllegalArgumentException("entityType cannot be null");
            }
            return new EntityHeatSource(entityType, targetTemperature, thermalConductivity, maxReleaseHeat, onMaxReleaseHeat, onReleaseHeat);
        }

    }
}
