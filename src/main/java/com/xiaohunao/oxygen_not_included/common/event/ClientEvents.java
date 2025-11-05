package com.xiaohunao.oxygen_not_included.common.event;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.client.ShaderManager;
import com.xiaohunao.oxygen_not_included.client.renderer.GasBlockEntityRenderer;
import com.xiaohunao.oxygen_not_included.common.init.ONIBlockEntityTypes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;


@EventBusSubscriber(modid = OxygenNotIncluded.MODID, value = Dist.CLIENT)
public class ClientEvents {
//    @SubscribeEvent
//    public static void onRenderLevelStage(RenderLevelStageEvent event) {
//        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
//            GasRenderer.render(event);
//        }
//    }


    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ShaderManager.init();
            ShaderManager.initNoiseTexture();
            GasBlockEntityRenderer.init();
        });
    }

	@SubscribeEvent
	public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ONIBlockEntityTypes.GAS.get(), context -> new GasBlockEntityRenderer());
	}
}
