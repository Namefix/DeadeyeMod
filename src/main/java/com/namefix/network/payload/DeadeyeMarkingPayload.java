package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeMarkingPayload(boolean status) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeMarkingPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, DeadeyeMarkingPayload::status, DeadeyeMarkingPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_MARKING;
    }
}
