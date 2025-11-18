package com.xiaohunao.oxygen_not_included.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.init.ONIItems;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesItem;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesRenderMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.OptionalInt;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    private static final double MAX_RENDER_DISTANCE_SQR = 4096.0D;
    private static final float LABEL_Y_OFFSET = 0.35F;

    protected ItemEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    private void oxygen_not_included$renderSmeltCountdown(ItemEntity itemEntity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (!shouldRenderCountdown()) {
            return;
        }

        OptionalInt smeltTime = findSmeltTime(itemEntity);
        if (smeltTime.isEmpty()) {
            return;
        }

        renderCountdownLabel(itemEntity, partialTick, poseStack, bufferSource, packedLight, smeltTime.getAsInt());
    }

    private boolean shouldRenderCountdown() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return false;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !helmet.is(ONIItems.GAS_GOGGLES.get())) {
            return false;
        }

        return GasGogglesItem.getRenderMode(helmet) == GasGogglesRenderMode.TEMPERATURE;
    }

    private OptionalInt findSmeltTime(ItemEntity itemEntity) {
        CompoundTag data = itemEntity.getPersistentData();

        if (data.contains("ONIData", Tag.TAG_COMPOUND)) {
            CompoundTag oniData = data.getCompound("ONIData");
            if (oniData.contains("SmeltTime", Tag.TAG_INT)) {
                return OptionalInt.of(oniData.getInt("SmeltTime"));
            }
        }

        return OptionalInt.empty();
    }

    private void renderCountdownLabel(ItemEntity itemEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int smeltTimeTicks) {
        if (this.entityRenderDispatcher.distanceToSqr(itemEntity) > MAX_RENDER_DISTANCE_SQR) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Quaternionf cameraRotation = camera.rotation();

        Vec3 entityPos = itemEntity.getPosition(partialTick);
        double centerX = entityPos.x;
        double centerY = entityPos.y + itemEntity.getBbHeight() + 1.0F;
        double centerZ = entityPos.z;

        poseStack.pushPose();
        poseStack.translate(centerX - cameraPos.x, centerY - cameraPos.y, centerZ - cameraPos.z);
        poseStack.mulPose(new Quaternionf(cameraRotation));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));


        poseStack.scale(-OxygenNotIncluded.scale, -OxygenNotIncluded.scale, OxygenNotIncluded.scale);

        Component label = Component.literal(formatSmeltTime(smeltTimeTicks));
        Font textRenderer = mc.font;
        float textWidth = textRenderer.width(label);
        Matrix4f matrix4f = poseStack.last().pose();
        textRenderer.drawInBatch(
                label,
                -textWidth / 2.0F,
                0.0F,
                0xFFFFFF,
                false,
                matrix4f,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                0xF000F0
        );
        poseStack.popPose();
    }

    private String formatSmeltTime(int smeltTimeTicks) {
        float seconds = Math.max(0, smeltTimeTicks) / 20.0F;
        if (seconds >= 10.0F) {
            return String.format(Locale.ROOT, "%.0fs", seconds);
        }
        return String.format(Locale.ROOT, "%.1fs", seconds);
    }
}

