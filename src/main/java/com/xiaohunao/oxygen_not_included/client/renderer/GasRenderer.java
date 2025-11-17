package com.xiaohunao.oxygen_not_included.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import com.xiaohunao.oxygen_not_included.common.gas.GasProperties;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class GasRenderer {

	private static int vaoId;
	private static int vboId;
	private static final int VERTEX_COUNT = 36;

	public static void init() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(GasRenderer::init);
			return;
		}

		float[] vertices = {
				0, 0, 0,  1, 0, 0,  1, 1, 0,  1, 1, 0,  0, 1, 0,  0, 0, 0,
				0, 0, 1,  0, 1, 1,  1, 1, 1,  1, 1, 1,  1, 0, 1,  0, 0, 1,
				0, 1, 1,  0, 1, 0,  0, 0, 0,  0, 0, 0,  0, 0, 1,  0, 1, 1,
				1, 1, 1,  1, 0, 1,  1, 0, 0,  1, 0, 0,  1, 1, 0,  1, 1, 1,
				0, 0, 0,  0, 0, 1,  1, 0, 1,  1, 0, 1,  1, 0, 0,  0, 0, 0,
				0, 1, 0,  1, 1, 0,  1, 1, 1,  1, 1, 1,  0, 1, 1,  0, 1, 0
		};

		FloatBuffer buffer = MemoryUtil.memAllocFloat(vertices.length);
		buffer.put(vertices).flip();

		vaoId = GL30.glGenVertexArrays();
		vboId = GL15.glGenBuffers();

		GL30.glBindVertexArray(vaoId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(0);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		MemoryUtil.memFree(buffer);

		OxygenNotIncluded.LOGGER.info("GasRenderer initialized. VAO ID: " + vaoId);
	}


	public static void render(RenderLevelStageEvent event) {
		if (ShaderManager.shaderProgramId == 0 || GasBlockEntity.getLoaded().isEmpty()) {
			return;
		}

		// 确保 VAO 已创建且有效，避免 GL_INVALID_OPERATION（Array object is not active）
		if (vaoId == 0 || !GL30.glIsVertexArray(vaoId)) {
			init();
			if (vaoId == 0 || !GL30.glIsVertexArray(vaoId)) {
				return;
			}
		}

		Minecraft mc = Minecraft.getInstance();

		Matrix4f projectionMatrix = event.getProjectionMatrix();

		Camera camera = mc.gameRenderer.getMainCamera();
		Vec3 cameraPos = camera.getPosition();
		Quaternionf cameraRotation = camera.rotation();

		Matrix4f viewMatrix = new Matrix4f().identity();
		viewMatrix.rotate(cameraRotation.conjugate(new Quaternionf()));
		viewMatrix.translate(-(float)cameraPos.x, -(float)cameraPos.y, -(float)cameraPos.z);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);

		GL20.glUseProgram(ShaderManager.shaderProgramId);

		GL20.glUniformMatrix4fv(ShaderManager.u_ProjectionMatrix_loc, false, projectionMatrix.get(new float[16]));
		GL20.glUniform3f(ShaderManager.u_CameraPos_loc, (float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);


		RenderSystem.activeTexture(GL13.GL_TEXTURE0);
		RenderSystem.bindTexture(ShaderManager.noiseTextureId);
		GL20.glUniform1i(ShaderManager.u_NoiseTex_loc, 0);

		float time = (System.currentTimeMillis() % 80000L) / 1000.0f;
		GL20.glUniform1f(ShaderManager.u_Time_loc, time);
		// 下面两项（Density/Color）改为在每个方块实体内按属性单独设置

		GL30.glBindVertexArray(vaoId);

		for (GasBlockEntity blockEntity : GasBlockEntity.getLoaded()) {
			// 跳过含量为0或极小的方块，避免闪动
			long amount = blockEntity.getAmount();
			if (amount <= 0L) {
				continue;
			}
			
			GasProperties gasProps = blockEntity.getGasProperties();
			float concentration = blockEntity.getConcentration();
			// 将实体含量与气体物性密度组合为渲染用密度（可按需调整）
			float renderDensity = Math.max(0.0f, Math.min(1.0f, gasProps.density * concentration));

			int color = gasProps.color; // ARGB
			float a = ((color >> 24) & 0xFF) / 255.0f;
			float r = ((color >> 16) & 0xFF) / 255.0f;
			float g = ((color >> 8) & 0xFF) / 255.0f;
			float b = (color & 0xFF) / 255.0f;
			BlockPos blockPos = blockEntity.getBlockPos();
			AABB volume = new AABB(blockPos);
			Matrix4f modelMatrix = new Matrix4f().identity()
					.translate((float)volume.minX, (float)volume.minY, (float)volume.minZ)
					.scale(
							(float)(volume.maxX - volume.minX),
							(float)(volume.maxY - volume.minY),
							(float)(volume.maxZ - volume.minZ)
					);

			Matrix4f modelViewMatrix = new Matrix4f(viewMatrix).mul(modelMatrix);

			GL20.glUniformMatrix4fv(ShaderManager.u_ModelViewMatrix_loc, false, modelViewMatrix.get(new float[16]));
			GL20.glUniform3f(ShaderManager.u_VolumeMin_loc, (float)volume.minX, (float)volume.minY, (float)volume.minZ);
			GL20.glUniform3f(ShaderManager.u_VolumeMax_loc, (float)volume.maxX, (float)volume.maxY, (float)volume.maxZ);
			GL20.glUniform1f(ShaderManager.u_Density_loc, renderDensity);
			GL20.glUniform4f(ShaderManager.u_GasColor_loc, r, g, b, a);

			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, VERTEX_COUNT);
		}

		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();

		GasGogglesOverlayRenderer.render(event, cameraPos, new Quaternionf(cameraRotation));
	}
}
