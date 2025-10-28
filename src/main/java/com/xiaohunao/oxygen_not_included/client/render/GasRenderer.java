package com.xiaohunao.oxygen_not_included.client.render;

import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xiaohunao.oxygen_not_included.common.block.entity.AirBlockEntity;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class GasRenderer {

	public static void handle(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		if (level == null) return;
		var camPos = event.getCamera().getPosition();
		int cx = Mth.floor(camPos.x);
		int cy = Mth.floor(camPos.y);
		int cz = Mth.floor(camPos.z);
		int radius = 6; // 进一步降低可视半径以改善性能
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		RenderType rt = ONIRenderTypes.gasTranslucent();
		VertexConsumer vc = buffers.getBuffer(rt);
		poseStack.pushPose();
		// 将坐标系平移到以摄像机为原点，便于用世界坐标直接绘制
		poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
		// 遍历近景范围内的空气方块实体
		for (int x = cx - radius; x <= cx + radius; x++) {
			for (int y = cy - radius; y <= cy + radius; y++) {
				for (int z = cz - radius; z <= cz + radius; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					BlockEntity be = level.getBlockEntity(pos);
					if (!(be instanceof AirBlockEntity air)) continue;
					int total = air.getTotalAmount();
					if (total <= 0) continue;
					// 颜色与透明度混合
					int argb = mixColorWithTurbulence(air.getGasMap(), total, level.getGameTime(), x, y, z);
					float a = ((argb >>> 24) & 0xFF) / 255.0f;
					if (a < 0.02f) continue;
					float r = ((argb >>> 16) & 0xFF) / 255.0f;
					float g = ((argb >>> 8) & 0xFF) / 255.0f;
					float b = (argb & 0xFF) / 255.0f;
					// 绘制略微内缩的半透明立方体以避免Z冲突
					float inset = 0.02f;
					float x0 = x + inset;
					float y0 = y + inset;
					float z0 = z + inset;
					float x1 = x0 + 1.0f - 2 * inset;
					float y1 = y0 + 1.0f - 2 * inset;
					float z1 = z0 + 1.0f - 2 * inset;
					cube(poseStack, vc, x0, y0, z0, x1, y1, z1, r, g, b, a);
				}
			}
		}
		poseStack.popPose();
		buffers.endBatch(rt);
	}

	private static int mixColorWithTurbulence(Map<Gas, Integer> map, int total, long gameTime, int x, int y, int z) {
		if (total <= 0) return 0;
		float r = 0, g = 0, b = 0, baseVisibility = 0;
		for (Map.Entry<Gas, Integer> e : map.entrySet()) {
			Gas gas = e.getKey();
			int amount = Math.max(0, e.getValue());
			float w = amount / (float) total;
			int c = gas.properties.getArgbColor();
			r += ((c >>> 16) & 0xFF) * w;
			g += ((c >>> 8) & 0xFF) * w;
			b += (c & 0xFF) * w;
			baseVisibility += gas.properties.getVisibility() * w;
		}
		// 简易扰流：使用世界时间驱动的分形噪声（与摄像机无关）
		double t = gameTime;
		double n = fbmNoise((x + 0.5) * 0.12, (y + 0.5) * 0.12, (z + 0.5) * 0.12, t * 0.02);
		float turbulence = (float) Mth.clamp(0.5 + 0.5 * n, 0.0, 1.0);
		float alpha = baseVisibility * 1.2f * turbulence;
		alpha = Math.max(alpha, 0.08f);
		int ia = Mth.clamp((int)(alpha * 255.0f), 0, 255);
		int ir = Mth.clamp((int)r, 0, 255);
		int ig = Mth.clamp((int)g, 0, 255);
		int ib = Mth.clamp((int)b, 0, 255);
		return (ia << 24) | (ir << 16) | (ig << 8) | ib;
	}

	private static double fbmNoise(double x, double y, double z, double t) {
		// 组合几层三角波噪声营造涡流感（无需额外依赖）
		double v = 0.0;
		double amp = 1.0;
		double freq = 1.0;
		for (int i = 0; i < 4; i++) {
			v += amp * triNoise(x * freq + t, y * freq, z * freq);
			amp *= 0.5;
			freq *= 2.0;
		}
		return Mth.clamp(v, -1.0, 1.0);
	}

	private static double triNoise(double x, double y, double z) {
		return tri(x + tri(y + tri(z)));
	}

	private static double tri(double v) {
		double f = v - Math.floor(v);
		return Math.abs(f - 0.5) * 2.0 - 1.0;
	}

	private static void cube(PoseStack poseStack, VertexConsumer vc,
			float x0, float y0, float z0, float x1, float y1, float z1,
			float r, float g, float b, float a) {
		Pose p = poseStack.last();
		// 六个面（每面两个三角形）
		// -X
		vertex(vc, p, x0, y0, z0, r, g, b, a);
		vertex(vc, p, x0, y1, z0, r, g, b, a);
		vertex(vc, p, x0, y1, z1, r, g, b, a);
		vertex(vc, p, x0, y0, z1, r, g, b, a);
		// +X
		vertex(vc, p, x1, y0, z1, r, g, b, a);
		vertex(vc, p, x1, y1, z1, r, g, b, a);
		vertex(vc, p, x1, y1, z0, r, g, b, a);
		vertex(vc, p, x1, y0, z0, r, g, b, a);
		// -Y
		vertex(vc, p, x0, y0, z1, r, g, b, a);
		vertex(vc, p, x0, y0, z0, r, g, b, a);
		vertex(vc, p, x1, y0, z0, r, g, b, a);
		vertex(vc, p, x1, y0, z1, r, g, b, a);
		// +Y
		vertex(vc, p, x1, y1, z1, r, g, b, a);
		vertex(vc, p, x1, y1, z0, r, g, b, a);
		vertex(vc, p, x0, y1, z0, r, g, b, a);
		vertex(vc, p, x0, y1, z1, r, g, b, a);
		// -Z
		vertex(vc, p, x1, y0, z0, r, g, b, a);
		vertex(vc, p, x1, y1, z0, r, g, b, a);
		vertex(vc, p, x0, y1, z0, r, g, b, a);
		vertex(vc, p, x0, y0, z0, r, g, b, a);
		// +Z
		vertex(vc, p, x0, y0, z1, r, g, b, a);
		vertex(vc, p, x0, y1, z1, r, g, b, a);
		vertex(vc, p, x1, y1, z1, r, g, b, a);
		vertex(vc, p, x1, y0, z1, r, g, b, a);
	}


	private static void vertex(VertexConsumer vc, Pose pose,
			float x, float y, float z, float r, float g, float b, float a) {
		vc.addVertex(pose, x, y, z)
				.setColor(r, g, b, a);
	}
}


