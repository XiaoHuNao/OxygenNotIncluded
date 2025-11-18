package com.xiaohunao.oxygen_not_included.common.network;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ONINetwork {
    private ONINetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(OxygenNotIncluded.MODID);
        registrar.playToServer(GasGogglesModePayload.TYPE, GasGogglesModePayload.STREAM_CODEC, GasGogglesModePayload::handle);
        registrar.playToClient(ItemEntityONIDataPayload.TYPE, ItemEntityONIDataPayload.STREAM_CODEC, ItemEntityONIDataPayload::handle);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void sendToTracking(Entity entity, CustomPacketPayload payload) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ChunkPos chunkPos = entity.chunkPosition();
        for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}
