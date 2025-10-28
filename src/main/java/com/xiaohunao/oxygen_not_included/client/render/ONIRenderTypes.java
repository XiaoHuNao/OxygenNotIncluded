package com.xiaohunao.oxygen_not_included.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class ONIRenderTypes {
	private ONIRenderTypes() {}

	private static RenderType GAS_TRANSLUCENT;

	public static RenderType gasTranslucent() {
		if (GAS_TRANSLUCENT == null) {
			RenderType.CompositeState state = RenderType.CompositeState.builder()
				.setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
				.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
				.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
				.setCullState(RenderType.NO_CULL)
				.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
				.setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
				.createCompositeState(true);
			GAS_TRANSLUCENT = RenderType.create("oni_gas_translucent",
				DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
				true, true, state);
		}
		return GAS_TRANSLUCENT;
	}
}


