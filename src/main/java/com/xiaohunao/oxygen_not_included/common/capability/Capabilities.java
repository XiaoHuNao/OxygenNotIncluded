package com.xiaohunao.oxygen_not_included.common.capability;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

/**
 * 气体能力定义类
 * 类似于 NeoForge 的 Capabilities 类，提供气体相关的 Capability 定义
 */
public final class Capabilities {
    /**
     * 气体处理器能力
     */
    public static final class GasHandler {
        /**
         * 方块的气体处理器能力（支持方向）
         * 用于方块和方块实体的自动化访问
         */
        public static final BlockCapability<IGasHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(OxygenNotIncluded.asResource("gas_handler"), IGasHandler.class);

        /**
         * 实体的气体处理器能力
         * 用于实体的气体库存
         */
        public static final EntityCapability<IGasHandler, Void> ENTITY = EntityCapability.createVoid(OxygenNotIncluded.asResource("gas_handler_entity"), IGasHandler.class);

        /**
         * 物品的气体处理器能力
         * 用于物品堆栈的气体库存（如气体罐）
         * 注意：物品的气体处理器应该是 IGasHandlerItem 类型
         */
        public static final ItemCapability<IGasHandlerItem, Void> ITEM = ItemCapability.createVoid(OxygenNotIncluded.asResource("gas_handler_item"), IGasHandlerItem.class);
    }

    private Capabilities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

