package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeRequestPayload(boolean status) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeRequestPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, DeadeyeRequestPayload::status, DeadeyeRequestPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_REQUEST;
    }
}
