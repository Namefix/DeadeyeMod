package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeMeterPayload(float amount) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeMeterPayload> CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, DeadeyeMeterPayload::amount, DeadeyeMeterPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_METER;
    }
}
