package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeForcePayload(boolean status) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeForcePayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, DeadeyeForcePayload::status, DeadeyeForcePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_FORCE;
    }
}