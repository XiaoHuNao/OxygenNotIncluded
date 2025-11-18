package com.xiaohunao.oxygen_not_included.common.network;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ItemEntityONIDataPayload(int entityId, CompoundTag oniData) implements CustomPacketPayload {
    public static final Type<ItemEntityONIDataPayload> TYPE = new Type<>(OxygenNotIncluded.asResource("item_entity_oni_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEntityONIDataPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.entityId());
                buf.writeNbt(payload.oniData());
            },
            buf -> new ItemEntityONIDataPayload(buf.readVarInt(), buf.readNbt())
    );

    @Override
    public Type<ItemEntityONIDataPayload> type() {
        return TYPE;
    }

    public static void handle(ItemEntityONIDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            if (level == null) {
                return;
            }

            Entity entity = level.getEntity(payload.entityId());
            if (!(entity instanceof ItemEntity itemEntity)) {
                return;
            }

            CompoundTag oniData = payload.oniData();
            if (oniData == null) {
                itemEntity.getPersistentData().remove("ONIData");
                return;
            }

            itemEntity.getPersistentData().put("ONIData", oniData.copy());
        });
    }
}


