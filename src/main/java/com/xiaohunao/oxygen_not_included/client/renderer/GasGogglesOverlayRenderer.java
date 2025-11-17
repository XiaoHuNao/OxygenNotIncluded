package com.xiaohunao.oxygen_not_included.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaohunao.oxygen_not_included.client.GasGogglesKeyHandler;
import com.xiaohunao.oxygen_not_included.common.block.entity.GasBlockEntity;
import com.xiaohunao.oxygen_not_included.common.gas.Gas;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesItem;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesRenderMode;
import com.xiaohunao.oxygen_not_included.common.init.ONIItems;
import com.xiaohunao.oxygen_not_included.common.network.GasGogglesModePayload;
import com.xiaohunao.oxygen_not_included.common.network.ONINetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class GasGogglesOverlayRenderer {
    private static final double MAX_DISTANCE = 32.0D;
    private static final double MIN_SEPARATION = 0.8D;

    private GasGogglesOverlayRenderer() {
    }

    public static void render(RenderLevelStageEvent event, Vec3 cameraPos, Quaternionf cameraRotation) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !helmet.is(ONIItems.GAS_GOGGLES.get())) {
            return;
        }

        GasGogglesRenderMode mode = GasGogglesItem.getRenderMode(helmet);
        switch (mode) {
            case NAME -> renderGasNames(event, cameraPos, cameraRotation);
            case AMOUNT -> renderGasAmounts(event, cameraPos, cameraRotation);
            case TEMPERATURE -> renderGasTemperatures(event, cameraPos, cameraRotation);
        }
    }

    private static void renderGasNames(RenderLevelStageEvent event, Vec3 cameraPos, Quaternionf cameraRotation) {
        renderOverlay(event, cameraPos, cameraRotation, blockEntity -> {
            Gas gas = blockEntity.gas();
            ResourceLocation key = gas.getRegistryName();
            if (key == null) {
                return Component.empty();
            }
            return Component.translatable("gas." + key.getNamespace() + "." + key.getPath());
        });
    }

    private static void renderGasAmounts(RenderLevelStageEvent event, Vec3 cameraPos, Quaternionf cameraRotation) {
        renderOverlay(event, cameraPos, cameraRotation, blockEntity -> Component.literal(Long.toString(blockEntity.getAmount())));
    }

    private static void renderGasTemperatures(RenderLevelStageEvent event, Vec3 cameraPos, Quaternionf cameraRotation) {
        renderOverlay(event, cameraPos, cameraRotation, blockEntity -> {
            double kelvin = blockEntity.getKelvin();
            if (kelvin <= 0) {
                return Component.empty();
            }
            return Component.literal(String.format(Locale.ROOT, "%.2fK", kelvin));
        });
    }

    private static void renderOverlay(RenderLevelStageEvent event, Vec3 cameraPos, Quaternionf cameraRotation, Function<GasBlockEntity, Component> labelFactory) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        PoseStack poseStack = event.getPoseStack();
        var bufferSource = mc.renderBuffers().bufferSource();

        List<GasBlockEntity> entitiesToRender = collectRenderableGasBlocks(cameraPos);
        if (entitiesToRender.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        Set<BlockPos> renderedPositions = new HashSet<>();

        for (GasBlockEntity blockEntity : entitiesToRender) {
            BlockPos blockPos = blockEntity.getBlockPos();

            if (isTooClose(renderedPositions, blockPos)) {
                continue;
            }

            Component text = labelFactory.apply(blockEntity);
            if (text == null || StringUtil.isNullOrEmpty(text.getString())) {
                continue;
            }

            renderedPositions.add(blockPos);

            double centerX = blockPos.getX() + 0.5D;
            double centerY = blockPos.getY() + 0.5D;
            double centerZ = blockPos.getZ() + 0.5D;

            poseStack.pushPose();
            poseStack.translate(centerX - cameraPos.x, centerY - cameraPos.y, centerZ - cameraPos.z);

            poseStack.mulPose(new Quaternionf(cameraRotation));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            float scale = 0.02F;
            poseStack.scale(-scale, -scale, scale);

            float textWidth = font.width(text);
            font.drawInBatch(
                    text,
                    -textWidth / 2.0F,
                    0,
                    0xFFFFFF,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    0xF000F0
            );
            poseStack.popPose();
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    private static boolean isTooClose(Set<BlockPos> renderedPositions, BlockPos current) {
        for (BlockPos other : renderedPositions) {
            if (other == current) {
                continue;
            }
            double dx = current.getX() - other.getX();
            double dy = current.getY() - other.getY();
            double dz = current.getZ() - other.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < MIN_SEPARATION) {
                return true;
            }
        }
        return false;
    }

    private static List<GasBlockEntity> collectRenderableGasBlocks(Vec3 cameraPos) {
        Map<BlockPos, GasBlockEntity> posToEntity = new HashMap<>();
        for (GasBlockEntity blockEntity : GasBlockEntity.getLoaded()) {
            long amount = blockEntity.getAmount();
            if (amount <= 0) {
                continue;
            }

            BlockPos blockPos = blockEntity.getBlockPos();
            double centerX = blockPos.getX() + 0.5D;
            double centerY = blockPos.getY() + 0.5D;
            double centerZ = blockPos.getZ() + 0.5D;
            double distance = cameraPos.distanceToSqr(centerX, centerY, centerZ);
            if (distance > MAX_DISTANCE * MAX_DISTANCE) {
                continue;
            }

            GasBlockEntity existing = posToEntity.get(blockPos);
            if (existing == null || blockEntity.getAmount() > existing.getAmount()) {
                posToEntity.put(blockPos, blockEntity);
            }
        }

        List<GasBlockEntity> entities = new ArrayList<>(posToEntity.values());
        entities.sort((a, b) -> {
            BlockPos posA = a.getBlockPos();
            BlockPos posB = b.getBlockPos();
            double distA = cameraPos.distanceToSqr(posA.getX() + 0.5D, posA.getY() + 0.5D, posA.getZ() + 0.5D);
            double distB = cameraPos.distanceToSqr(posB.getX() + 0.5D, posB.getY() + 0.5D, posB.getZ() + 0.5D);
            return Double.compare(distA, distB);
        });
        return entities;
    }

    public static boolean handleScroll(double delta) {
        if (delta == 0) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return false;
        }

        if (!GasGogglesKeyHandler.RENDER_MODE_KEY.isDown()) {
            return false;
        }

        Player player = mc.player;
        if (player == null) {
            return false;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !helmet.is(ONIItems.GAS_GOGGLES.get())) {
            return false;
        }

        int direction = delta > 0 ? 1 : -1;
        GasGogglesRenderMode current = GasGogglesItem.getRenderMode(helmet);
        GasGogglesRenderMode next = current.cycle(direction);
        if (next == current) {
            return false;
        }

        GasGogglesItem.setRenderMode(helmet, next);
        ONINetwork.sendToServer(new GasGogglesModePayload(next));
        player.displayClientMessage(next.getDisplayName(), true);
        return true;
    }
}
