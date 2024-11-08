package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyePhasePayload(int phase) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyePhasePayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, DeadeyePhasePayload::phase, DeadeyePhasePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_PHASE;
    }
}
