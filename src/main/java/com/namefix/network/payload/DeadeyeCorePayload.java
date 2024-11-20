package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeCorePayload(float amount) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeCorePayload> CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, DeadeyeCorePayload::amount, DeadeyeCorePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_CORE;
    }
}
