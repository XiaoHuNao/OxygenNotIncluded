package com.xiaohunao.oxygen_not_included.common.heat_source;

import com.xiaohunao.oxygen_not_included.common.init.ONIRegistries;
import com.xiaohunao.oxygen_not_included.common.interaction.GasInteractionContext;
import com.xiaohunao.xhn_lib.api.data.loader.BaseDynamicLoader;
import com.xiaohunao.xhn_lib.common.serialization.IDynamicSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HeatSourceManager extends BaseDynamicLoader<HeatSource> {
    private static final HeatSourceManager INSTANCE = new HeatSourceManager();

    private final Map<Block, HeatSource> blockHeatSources = new HashMap<>();
    private final Map<Fluid, HeatSource> fluidHeatSources = new HashMap<>();
    private final Map<EntityType<?>, HeatSource> entityHeatSources = new HashMap<>();
    private final Map<ResourceLocation, HeatSource> heatSources = new HashMap<>();

    public HeatSourceManager() {
        super(ONIRegistries.HEAT_SOURCE, IDynamicSerializer.of(HeatSource.CODEC));
    }

    public static HeatSourceManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void onAfterRegister(ResourceLocation location, HeatSource value) {
        if (value instanceof HeatSource.BlockHeatSource blockHeatSource) {
            blockHeatSources.put(blockHeatSource.getBlock(), blockHeatSource);
        } else if (value instanceof HeatSource.FluidHeatSource fluidHeatSource) {
            fluidHeatSources.put(fluidHeatSource.getFluid(), fluidHeatSource);
        } else if (value instanceof HeatSource.EntityHeatSource entityHeatSource) {
            entityHeatSources.put(entityHeatSource.getEntityType(), entityHeatSource);
        }
        heatSources.put(location, value);
    }

    @Override
    protected void onValueRemoved(ResourceLocation location) {
        HeatSource heatSource = heatSources.get(location);
        if (heatSource instanceof HeatSource.BlockHeatSource blockHeatSource) {
            blockHeatSources.remove(blockHeatSource.getBlock());
        } else if (heatSource instanceof HeatSource.FluidHeatSource fluidHeatSource) {
            fluidHeatSources.remove(fluidHeatSource.getFluid());
        } else if (heatSource instanceof HeatSource.EntityHeatSource entityHeatSource) {
            entityHeatSources.remove(entityHeatSource.getEntityType());
        }
        heatSources.remove(location);
    }

    public boolean matches(GasInteractionContext context){
        BlockState blockState = context.targetState();
        FluidState fluidState = context.level().getFluidState(context.targetPos());
        List<Entity> entities = context.entities().orElse(List.of());

        return blockHeatSources.containsKey(blockState.getBlock()) ||
                fluidHeatSources.containsKey(fluidState.getType()) ||
                entities.stream().anyMatch(entity -> entityHeatSources.containsKey(entity.getType()));
    }

    /**
     * 应用热源效果，向周围气体传递热量
     * 热源会不断尝试将周围气体加热到目标温度
     */
    public void apply(GasInteractionContext context) {
        if (context.source() == null || context.source().getAmount() <= 0L) {
            return;
        }

        Level level = context.level();
        BlockPos targetPos = context.targetPos();
        BlockState targetState = context.targetState();
        FluidState fluidState = level.getFluidState(targetPos);
        List<Entity> entities = context.entities().orElse(List.of());

        HeatSource heatSource = null;

        if (!targetState.isAir()) {
            heatSource = blockHeatSources.get(targetState.getBlock());
        }
        // 检查流体热源
        if (heatSource == null && !fluidState.isEmpty()) {
            heatSource = fluidHeatSources.get(fluidState.getType());
        }
        // 检查实体热源
        if (heatSource == null && !entities.isEmpty()) {
            for (Entity entity : entities) {
                heatSource = entityHeatSources.get(entity.getType());
                if (heatSource != null) {
                    break;
                }
            }
        }

        // 如果找到热源，向气体传递热量
        if (heatSource != null) {
            HeatTransferManager.getInstance().transferHeatFromSource(
                    context.source(),
                    heatSource,
                    level,
                    context.source().getBlockPos()
            );
        }
    }
}
