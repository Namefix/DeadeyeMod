package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeTogglePayload(boolean status) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeTogglePayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, DeadeyeTogglePayload::status, DeadeyeTogglePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_TOGGLE;
    }
}
