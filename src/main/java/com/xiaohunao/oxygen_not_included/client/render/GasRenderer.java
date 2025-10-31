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
import net.minecraft.world.phys.Vec3;
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
		var cam = event.getCamera();
		var leftV = cam.getLeftVector(); // Vector3f
		Vec3 camRight = new Vec3(-leftV.x(), -leftV.y(), -leftV.z()); // 屏幕右
		var upV = cam.getUpVector(); // Vector3f
		Vec3 camUp = new Vec3(upV.x(), upV.y(), upV.z()); // 屏幕上
		int cx = Mth.floor(camPos.x);
		int cy = Mth.floor(camPos.y);
		int cz = Mth.floor(camPos.z);
        int radius = 8; // 稍微扩大一点范围，但依旧可控
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
                    // 颜色与透明度混合（包含时序噪声）
                    int argb = mixColorWithTurbulence(air.getGasMap(), total, level.getGameTime(), x, y, z);
					float a = ((argb >>> 24) & 0xFF) / 255.0f;
					if (a < 0.02f) continue;
					float r = ((argb >>> 16) & 0xFF) / 255.0f;
					float g = ((argb >>> 8) & 0xFF) / 255.0f;
					float b = (argb & 0xFF) / 255.0f;
                    // 绘制略微内缩的半透明立方体以避免Z冲突

                    // 使用面向摄像机的云雾广告牌，避免方块感
					renderGasBillboards(poseStack, vc, x + 0.5f, y + 0.5f, z + 0.5f,
							r, g, b, a, camRight, camUp, level.getGameTime());

					// 三向切片叠加，提升体积密度与非方向性
					renderTriPlanarSlices(poseStack, vc, x + 0.5f, y + 0.5f, z + 0.5f,
							r, g, b, a, level.getGameTime());

                    // 轻量粒子：少量向上飘散，颜色取气体混合色
                    spawnGasParticles(level, x + 0.5, y + 0.9, z + 0.5, r, g, b, a, level.getGameTime(), camPos);
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
        float turbulence = (float) Mth.clamp(0.55 + 0.6 * n, 0.0, 1.15);
        // 垂直渐变更像“轻气体”往上淡化
        float verticalFalloff = 0.9f + 0.1f * (float)Mth.clamp((y % 16) / 16.0, 0.0, 1.0);
        float alpha = baseVisibility * 1.6f * turbulence * verticalFalloff;
        alpha = Math.max(alpha, 0.16f);
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

    


	private static void vertexUV(VertexConsumer vc, Pose pose,
			float x, float y, float z, float u, float v, float r, float g, float b, float a) {
		vc.addVertex(pose, x, y, z)
				.setColor(r, g, b, a)
				.setUv(u, v)
				.setUv2(0, 240);
	}

    private static void renderGasBillboards(PoseStack poseStack, VertexConsumer vc,
                                            float cx, float cy, float cz,
                                            float r, float g, float b, float a,
                                            Vec3 camRight, Vec3 camUp, long gameTime) {
        // 基于透明度的小扰动，生成 3~4 张云雾卡片（更致密）
        int count = 5 + (a > 0.55f ? 1 : 0);
        float baseSize = 0.55f * (0.45f + a * 0.7f);
        double seed = (long)Mth.floor(cx * 7347 + cy * 9151 + cz * 1217);
        Pose p = poseStack.last();
        for (int i = 0; i < count; i++) {
            // 抖动与旋转（绕相机法线），避免四边形方向性过强
            double phase = tri(seed + i * 13.37 + gameTime * 0.035);
            float jitterX = (float) (phase * 0.10);
            float jitterY = (float) (tri(seed * 0.5 + i * 7.23) * 0.08);
            float sizeJitter = 0.85f + (float)Math.abs(phase) * 0.25f;
            float size = baseSize * sizeJitter;
            float alpha = a * (0.95f - 0.10f * i);

            // 随机旋转基向量
            float angle = (float)(tri(seed * 2.17 + i * 5.31) * Mth.PI);
            float cos = Mth.cos(angle), sin = Mth.sin(angle);
            // rotate (camRight, camUp)
            float rx = (float)(camRight.x * cos + camUp.x * sin);
            float ry = (float)(camRight.y * cos + camUp.y * sin);
            float rz = (float)(camRight.z * cos + camUp.z * sin);
            float ux = (float)(-camRight.x * sin + camUp.x * cos);
            float uy = (float)(-camRight.y * sin + camUp.y * cos);
            float uz = (float)(-camRight.z * sin + camUp.z * cos);

            float hx = rx * size;
            float hy = ry * size;
            float hz = rz * size;
            ux *= size * 0.75f;
            uy *= size * 0.75f;
            uz *= size * 0.75f;

            float px = cx + jitterX;
            float py = cy + jitterY;
            float pz = cz;

            // 四角点（逆时针）
            float x0 = px - hx - ux, y0 = py - hy - uy, z0 = pz - hz - uz;
            float x1 = px - hx + ux, y1 = py - hy + uy, z1 = pz - hz + uz;
            float x2 = px + hx + ux, y2 = py + hy + uy, z2 = pz + hz + uz;
            float x3 = px + hx - ux, y3 = py + hy - uy, z3 = pz + hz - uz;

            // 采用软边四边形（角落透明更低，中心整体用层叠实现更浓）
            float cornerA = alpha * 0.65f; // 角落更淡
            float midA = alpha * 0.95f;   // 四边形整体适中

            // 两个三角形组成的四边形，传 UV 为 -1..1 的圆盘空间
            vertexUV(vc, p, x0, y0, z0, -1f, -1f, r, g, b, cornerA);
            vertexUV(vc, p, x1, y1, z1, -1f,  1f, r, g, b, cornerA);
            vertexUV(vc, p, x2, y2, z2,  1f,  1f, r, g, b, midA);

            vertexUV(vc, p, x2, y2, z2,  1f,  1f, r, g, b, midA);
            vertexUV(vc, p, x3, y3, z3,  1f, -1f, r, g, b, cornerA);
            vertexUV(vc, p, x0, y0, z0, -1f, -1f, r, g, b, cornerA);
        }
    }

    

    private static void spawnGasParticles(Level level, double x, double y, double z,
                                          float r, float g, float b, float a,
                                          long gameTime, Vec3 camPos) {
        if (level == null || level.isClientSide == false) return;
        // 与距离相关的速率限制，避免远处开销
        double distSq = camPos.distanceToSqr(x, y, z);
        if (distSq > 64.0) return; // 距离>8格不生成
        // 时序与随机控制的稀疏生成
        if ((gameTime % 2) != 0) return; // 降低频率
        if (level.random.nextFloat() > Math.min(0.15f, a)) return;

        // 使用 DustColorTransition（可着色、柔和）
        // 颜色与体素一致，向上轻微漂移
        float scale = 0.6f + level.random.nextFloat() * 0.4f;
        var options = new net.minecraft.core.particles.DustColorTransitionOptions(
                new net.minecraft.world.phys.Vec3(r, g, b).toVector3f(),
                new net.minecraft.world.phys.Vec3(r, g, b).toVector3f(),
                scale * 0.6f);
        double vx = (level.random.nextDouble() - 0.5) * 0.01;
        double vy = 0.02 + level.random.nextDouble() * 0.02;
        double vz = (level.random.nextDouble() - 0.5) * 0.01;
        ((net.minecraft.client.multiplayer.ClientLevel)level).addParticle(options,
                x + (level.random.nextDouble() - 0.5) * 0.6,
                y + (level.random.nextDouble()) * 0.3,
                z + (level.random.nextDouble() - 0.5) * 0.6,
                vx, vy, vz);
    }

    private static void renderTriPlanarSlices(PoseStack poseStack, VertexConsumer vc,
                                              float cx, float cy, float cz,
                                              float r, float g, float b, float a,
                                              long gameTime) {
        if (a <= 0.03f) return;
        Pose p = poseStack.last();
        // 每个轴向 1~2 层薄片
        int layers = 2 + (a > 0.65f ? 1 : 0);
        float base = 0.42f * (0.7f + a * 0.6f);
        double seed = (long)Mth.floor(cx * 15731 + cy * 789221 + cz * 1379);
        float t = gameTime * 0.03f;

        // XY 平面（法线 Z）
        renderAxisSlices(p, vc, cx, cy, cz, r, g, b, a,
                base, layers, seed, 0,
                1, 0, 0,   // right = X
                0, 1, 0,   // up = Y
                0, 0, 1,   // normal = Z
                t);

        // XZ 平面（法线 Y）
        renderAxisSlices(p, vc, cx, cy, cz, r, g, b, a,
                base, layers, seed, 1,
                1, 0, 0,   // right = X
                0, 0, 1,   // up = Z
                0, 1, 0,   // normal = Y
                t);

        // YZ 平面（法线 X）
        renderAxisSlices(p, vc, cx, cy, cz, r, g, b, a,
                base, layers, seed, 2,
                0, 1, 0,   // right = Y
                0, 0, 1,   // up = Z
                1, 0, 0,   // normal = X
                t);
    }

    private static void renderAxisSlices(Pose pose, VertexConsumer vc,
                                         float cx, float cy, float cz,
                                         float r, float g, float b, float a,
                                         float baseSize, int layers, double seed, int axisIndex,
                                         float rx, float ry, float rz, // right
                                         float ux, float uy, float uz, // up
                                         float nx, float ny, float nz, // normal
                                         float t) { // time
        for (int i = 0; i < layers; i++) {
            double phase = tri(seed * (axisIndex + 1) + i * 11.17 + t);
            float jitterR = (float)(phase * 0.08);
            float jitterU = (float)(tri(seed + i * 7.71 + t * 0.7f) * 0.08);
            float size = baseSize * (0.85f + 0.18f * (float)Math.abs(phase));
            float alpha = a * (0.55f - 0.10f * i);
            if (alpha <= 0.01f) continue;

            // 在该平面内随机旋转四边形，减少规则方形边界感
            float angle = (float)(tri(seed * 3.17 + i * 9.91 + t * 0.5f) * Mth.PI);
            float cos = Mth.cos(angle), sin = Mth.sin(angle);
            float rrx = rx * cos + ux * sin;
            float rry = ry * cos + uy * sin;
            float rrz = rz * cos + uz * sin;
            float uux = -rx * sin + ux * cos;
            float uuy = -ry * sin + uy * cos;
            float uuz = -rz * sin + uz * cos;

            // 椭圆缩放：一个轴略短，近似圆盘边缘
            float sizeR = size;
            float sizeU = size * 0.70f;
            float hx = rrx * sizeR, hy = rry * sizeR, hz = rrz * sizeR;
            float uxv = uux * sizeU, uyv = uuy * sizeU, uzv = uuz * sizeU;

            float px = cx + rx * jitterR + ux * jitterU + nx * (0.02f * i);
            float py = cy + ry * jitterR + uy * jitterU + ny * (0.02f * i);
            float pz = cz + rz * jitterR + uz * jitterU + nz * (0.02f * i);

            float x0 = px - hx - uxv, y0 = py - hy - uyv, z0 = pz - hz - uzv;
            float x1 = px - hx + uxv, y1 = py - hy + uyv, z1 = pz - hz + uzv;
            float x2 = px + hx + uxv, y2 = py + hy + uyv, z2 = pz + hz + uzv;
            float x3 = px + hx - uxv, y3 = py + hy - uyv, z3 = pz + hz - uzv;

            float cornerA = alpha * 0.55f;
            float midA = alpha * 0.90f;

            vertexUV(vc, pose, x0, y0, z0, -1f, -1f, r, g, b, cornerA);
            vertexUV(vc, pose, x1, y1, z1, -1f,  1f, r, g, b, cornerA);
            vertexUV(vc, pose, x2, y2, z2,  1f,  1f, r, g, b, midA);

            vertexUV(vc, pose, x2, y2, z2,  1f,  1f, r, g, b, midA);
            vertexUV(vc, pose, x3, y3, z3,  1f, -1f, r, g, b, cornerA);
            vertexUV(vc, pose, x0, y0, z0, -1f, -1f, r, g, b, cornerA);
        }
    }
}


