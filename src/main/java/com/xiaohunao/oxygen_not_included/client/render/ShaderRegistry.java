package com.xiaohunao.oxygen_not_included.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@EventBusSubscriber(modid = OxygenNotIncluded.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ShaderRegistry {
	private ShaderRegistry() {}

	public static ShaderInstance GAS_SHADER;

	@SubscribeEvent
	public static void onRegisterShaders(RegisterShadersEvent event) {
		try {
			ShaderInstance shader = new ShaderInstance(
				event.getResourceProvider(),
				ResourceLocation.fromNamespaceAndPath(OxygenNotIncluded.MODID, "oni_gas"),
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
			event.registerShader(shader, s -> GAS_SHADER = s);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load oni_gas shader", e);
		}
	}
}


