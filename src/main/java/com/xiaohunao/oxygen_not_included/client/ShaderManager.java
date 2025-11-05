package com.xiaohunao.oxygen_not_included.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ShaderManager {

    public static int shaderProgramId = 0;
    public static int u_ModelViewMatrix_loc;
    public static int u_ProjectionMatrix_loc;
    public static int u_CameraPos_loc;
    public static int u_VolumeMin_loc;
    public static int u_VolumeMax_loc;
    public static int u_Time_loc;
    public static int u_GasColor_loc;
    public static int u_Density_loc;
    public static int u_NoiseTex_loc;
    public static int noiseTextureId = 0;
    public static void init() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(ShaderManager::init);
            return;
        }
        ResourceLocation vertexShaderLoc = ResourceLocation.fromNamespaceAndPath(OxygenNotIncluded.MODID, "shaders/gas.vert");
        ResourceLocation fragmentShaderLoc = ResourceLocation.fromNamespaceAndPath(OxygenNotIncluded.MODID, "shaders/gas.frag");
        String vertexShaderSource = loadShaderSource(vertexShaderLoc);
        String fragmentShaderSource = loadShaderSource(fragmentShaderLoc);

        if (vertexShaderSource == null || fragmentShaderSource == null) {
            OxygenNotIncluded.LOGGER.error("Failed to load shader sources. Aborting shader initialization.");
            return;
        }

        int vertexShaderId = compileShader(vertexShaderSource, GL20.GL_VERTEX_SHADER);
        int fragmentShaderId = compileShader(fragmentShaderSource, GL20.GL_FRAGMENT_SHADER);

        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            OxygenNotIncluded.LOGGER.error("Shader compilation failed. Aborting linking.");
            return;
        }

        shaderProgramId = linkProgram(vertexShaderId, fragmentShaderId);

        if (shaderProgramId == 0) {
            OxygenNotIncluded.LOGGER.error("Shader program linking failed.");
            return;
        }

        getUniformLocations();

        OxygenNotIncluded.LOGGER.info("Shader program initialized successfully. Program ID: " + shaderProgramId);
    }

    private static String loadShaderSource(ResourceLocation location) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        try (InputStream inputStream = resourceManager.getResourceOrThrow(location).open();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.joining("\n"));

        } catch (IOException e) {
            OxygenNotIncluded.LOGGER.error("Failed to read shader file: " + location, e);
            return null;
        }
    }

    private static int compileShader(String source, int type) {
        int shaderId = GL20.glCreateShader(type);
        if (shaderId == 0) {
            OxygenNotIncluded.LOGGER.error("Could not create shader of type " + type);
            return 0;
        }
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shaderId, 1024);
            OxygenNotIncluded.LOGGER.error("Shader compilation failed for type " + type + ":\n" + log);
            GL20.glDeleteShader(shaderId);
            return 0;
        }

        return shaderId;
    }

    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        int programId = GL20.glCreateProgram();
        if (programId == 0) {
            OxygenNotIncluded.LOGGER.error("Could not create shader program.");
            return 0;
        }
        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);
        GL20.glLinkProgram(programId);

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(programId, 1024);
            OxygenNotIncluded.LOGGER.error("Shader program linking failed:\n" + log);
            GL20.glDeleteProgram(programId);
            return 0;
        }
        GL20.glDeleteShader(vertexShaderId);
        GL20.glDeleteShader(fragmentShaderId);

        return programId;
    }

    public static void initNoiseTexture() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(ShaderManager::initNoiseTexture);
            return;
        }

        OxygenNotIncluded.LOGGER.info("Loading 3D noise texture from resources...");

        final int TEX_SIZE = 64;
        final int byteSize = TEX_SIZE * TEX_SIZE * TEX_SIZE;

        ResourceLocation noiseFileLoc = ResourceLocation.fromNamespaceAndPath(OxygenNotIncluded.MODID, "textures/noise.raw");
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        ByteBuffer buffer = null;

        try (InputStream inputStream = resourceManager.getResourceOrThrow(noiseFileLoc).open()) {

            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length != byteSize) {
                OxygenNotIncluded.LOGGER.error("Noise texture file is wrong size! Expected " + byteSize + ", got " + bytes.length);
                return;
            }

            buffer = BufferUtils.createByteBuffer(byteSize);
            buffer.put(bytes);
            buffer.flip();

        } catch (IOException e) {
            OxygenNotIncluded.LOGGER.error("Failed to read 3D noise texture: " + noiseFileLoc, e);
            return;
        }

        if (buffer == null) {
            return;
        }


        noiseTextureId = GL11.glGenTextures();
        RenderSystem.bindTexture(noiseTextureId);

        GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL30.GL_R8,
                TEX_SIZE, TEX_SIZE, TEX_SIZE,
                0, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, buffer);


        RenderSystem.bindTexture(0);
        OxygenNotIncluded.LOGGER.info("3D noise texture loaded successfully. ID: " + noiseTextureId);
    }

    private static void getUniformLocations() {
        u_ModelViewMatrix_loc = GL20.glGetUniformLocation(shaderProgramId, "u_ModelViewMatrix");
        u_ProjectionMatrix_loc = GL20.glGetUniformLocation(shaderProgramId, "u_ProjectionMatrix");
        u_CameraPos_loc = GL20.glGetUniformLocation(shaderProgramId, "u_CameraPos");
        u_VolumeMin_loc = GL20.glGetUniformLocation(shaderProgramId, "u_VolumeMin");
        u_VolumeMax_loc = GL20.glGetUniformLocation(shaderProgramId, "u_VolumeMax");
        u_Time_loc = GL20.glGetUniformLocation(shaderProgramId, "u_Time");
        u_GasColor_loc = GL20.glGetUniformLocation(shaderProgramId, "u_GasColor");
        u_Density_loc = GL20.glGetUniformLocation(shaderProgramId, "u_Density");
        u_NoiseTex_loc = GL20.glGetUniformLocation(shaderProgramId, "u_NoiseTex");
    }
}
