package com.xiaohunao.oxygen_not_included.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.client.ShaderManager;
import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class GasBlockEntityRenderer implements BlockEntityRenderer<GasBlockEntity> {
    private static int vaoId;
    private static int vboId;
    private static final int VERTEX_COUNT = 36;


    @Override
    public void render(@NotNull GasBlockEntity gasBlockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        if (ShaderManager.shaderProgramId == 0) {
            return;
        }

        Matrix4f projectionMatrix = poseStack.last().pose();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
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
        GL20.glUniform1f(ShaderManager.u_Density_loc, 0.3f);
        GL20.glUniform4f(ShaderManager.u_GasColor_loc, 0.8f, 0.8f, 0.8f, 1.0f);

        GL30.glBindVertexArray(vaoId);

//        for (AABB volume : GasVolumeManager.getVolumes()) {
//            Matrix4f modelMatrix = new Matrix4f().identity()
//                    .translate((float)volume.minX, (float)volume.minY, (float)volume.minZ)
//                    .scale(
//                            (float)(volume.maxX - volume.minX),
//                            (float)(volume.maxY - volume.minY),
//                            (float)(volume.maxZ - volume.minZ)
//                    );
//
//            Matrix4f modelViewMatrix = new Matrix4f(viewMatrix).mul(modelMatrix);
//
//            GL20.glUniformMatrix4fv(ShaderManager.u_ModelViewMatrix_loc, false, modelViewMatrix.get(new float[16]));
//            GL20.glUniform3f(ShaderManager.u_VolumeMin_loc, (float)volume.minX, (float)volume.minY, (float)volume.minZ);
//            GL20.glUniform3f(ShaderManager.u_VolumeMax_loc, (float)volume.maxX, (float)volume.maxY, (float)volume.maxZ);
//
//            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, VERTEX_COUNT);
//        }
        AABB volume = new AABB(gasBlockEntity.getBlockPos()).inflate(5.0f);
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

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, VERTEX_COUNT);





        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }


    public static void init() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(GasBlockEntityRenderer::init);
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
}
