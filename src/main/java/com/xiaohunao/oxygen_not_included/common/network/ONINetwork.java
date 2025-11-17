package com.xiaohunao.oxygen_not_included.common.network;

import com.xiaohunao.oxygen_not_included.OxygenNotIncluded;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ONINetwork {
    private ONINetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(OxygenNotIncluded.MODID);
        registrar.playToServer(GasGogglesModePayload.TYPE, GasGogglesModePayload.STREAM_CODEC, GasGogglesModePayload::handle);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
