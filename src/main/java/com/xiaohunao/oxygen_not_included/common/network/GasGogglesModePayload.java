package com.xiaohunao.oxygen_not_included.common.network;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesItem;
import com.xiaohunao.oxygen_not_included.common.item.GasGogglesRenderMode;
import com.xiaohunao.oxygen_not_included.common.init.ONIItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GasGogglesModePayload(GasGogglesRenderMode mode) implements CustomPacketPayload {
    public GasGogglesModePayload {
        if (mode == null) {
            throw new IllegalArgumentException("mode");
        }
    }

    public static final Type<GasGogglesModePayload> TYPE = new Type<>(OxygenNotIncluded.asResource("gas_goggles_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GasGogglesModePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeEnum(payload.mode()),
            buf -> new GasGogglesModePayload(buf.readEnum(GasGogglesRenderMode.class))
    );

    @Override
    public Type<GasGogglesModePayload> type() {
        return TYPE;
    }

    public static void handle(GasGogglesModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            ItemStack helmet = serverPlayer.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.isEmpty() || !helmet.is(ONIItems.GAS_GOGGLES.get())) {
                return;
            }

            GasGogglesItem.setRenderMode(helmet, payload.mode());
        });
    }
}
