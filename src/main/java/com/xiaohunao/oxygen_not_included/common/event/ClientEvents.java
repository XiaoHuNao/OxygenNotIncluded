package com.xiaohunao.oxygen_not_included.common.event;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.client.renderer.GasGogglesOverlayRenderer;
import com.xiaohunao.oxygen_not_included.client.renderer.ShaderManager;
import com.xiaohunao.oxygen_not_included.client.renderer.GasRenderer;
import com.xiaohunao.oxygen_not_included.client.GasGogglesKeyHandler;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;


@EventBusSubscriber(modid = OxygenNotIncluded.MODID, value = Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		GasRenderer.render(event);
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ShaderManager.init();
			ShaderManager.initNoiseTexture();
			GasRenderer.init();
		});
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		GasGogglesKeyHandler.register(event);
	}

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		if (GasGogglesOverlayRenderer.handleScroll(event.getScrollDeltaY())) {
			event.setCanceled(true);
		}
	}
}
