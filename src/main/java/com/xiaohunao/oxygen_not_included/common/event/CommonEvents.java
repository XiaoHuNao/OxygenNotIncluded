package com.xiaohunao.oxygen_not_included.common.event;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.capability.Capabilities;
import com.xiaohunao.oxygen_not_included.common.init.ONIItems;
import com.xiaohunao.oxygen_not_included.common.item.GasTankItem;

import com.xiaohunao.oxygen_not_included.common.util.TemperatureType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 公共事件处理器
 * 用于注册气体能力等通用功能
 */
@EventBusSubscriber(modid = OxygenNotIncluded.MODID)
public class CommonEvents {
    /**
     * 注册气体能力
     * 在这里可以注册方块、实体和物品的气体处理器
     */
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 示例：为方块实体注册气体处理器
        // event.registerBlockEntity(
        //     Capabilities.GasHandler.BLOCK,
        //     MY_BLOCK_ENTITY_TYPE,
        //     (blockEntity, side) -> blockEntity.getGasHandler()
        // );

        // 示例：为实体注册气体处理器
        // event.registerEntity(
        //     Capabilities.GasHandler.ENTITY,
        //     MY_ENTITY_TYPE,
        //     (entity, context) -> entity.getGasHandler()
        // );

        // 示例：为物品注册气体处理器
        // event.registerItem(
        //     Capabilities.GasHandler.ITEM,
        //     (stack, context) -> new MyGasHandlerItem(stack),
        //     MY_GAS_TANK_ITEM
        // );

        // 物品能力：为气体罐提供 IGasHandlerItem 能力
        event.registerItem(
            Capabilities.GasHandler.ITEM,
            (stack, ctx) -> new GasTankItem.GasHandler(stack),
            ONIItems.GAS_TANK.get()
        );

        OxygenNotIncluded.LOGGER.info("Gas capabilities registered");
    }

    @SubscribeEvent
    public static void onPlayerInteractRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        InteractionHand hand = event.getHand();
        Player player = event.getEntity();
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return;
        }
    }
}

