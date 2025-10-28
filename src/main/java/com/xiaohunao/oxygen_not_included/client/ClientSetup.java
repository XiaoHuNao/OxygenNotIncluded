package com.xiaohunao.oxygen_not_included.client;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.client.render.GasRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = OxygenNotIncluded.MODID, value = Dist.CLIENT)
public class ClientSetup {

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		GasRenderer.handle(event);
	}
}


