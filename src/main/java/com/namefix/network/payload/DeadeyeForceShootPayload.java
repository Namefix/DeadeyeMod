package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeForceShootPayload() implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeForceShootPayload> CODEC = PacketCodec.unit(new DeadeyeForceShootPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_FORCE_SHOOT;
    }
}
