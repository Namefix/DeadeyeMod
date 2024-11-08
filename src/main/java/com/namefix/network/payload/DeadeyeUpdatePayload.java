package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeUpdatePayload(int status) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeUpdatePayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, DeadeyeUpdatePayload::status, DeadeyeUpdatePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_UPDATE;
    }
}