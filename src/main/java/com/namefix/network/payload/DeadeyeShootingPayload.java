package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeShootingPayload(boolean status) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeShootingPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, DeadeyeShootingPayload::status, DeadeyeShootingPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_SHOOTING;
    }
}
