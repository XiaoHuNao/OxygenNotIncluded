package com.xiaohunao.oxygen_not_included.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesItem;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesRenderMode;
import com.xiaohunao.oxygen_not_included.common.init.ONIItems;
import com.xiaohunao.oxygen_not_included.common.network.GasGogglesModePayload;
import com.xiaohunao.oxygen_not_included.common.network.ONINetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public final class GasGogglesKeyHandler {
    public static final KeyMapping RENDER_MODE_KEY = new KeyMapping(
            "key.oxygen_not_included.gas_goggles_mode",
            KeyConflictContext.IN_GAME,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "key.categories.oxygen_not_included"
    );

    private GasGogglesKeyHandler() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(RENDER_MODE_KEY);
    }
}
