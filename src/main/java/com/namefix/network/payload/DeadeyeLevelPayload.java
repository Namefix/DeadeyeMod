package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeLevelPayload(int level) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeLevelPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, DeadeyeLevelPayload::level, DeadeyeLevelPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_LEVEL;
    }
}
