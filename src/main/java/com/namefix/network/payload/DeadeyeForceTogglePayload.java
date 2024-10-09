package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeForceTogglePayload(boolean status, float meter) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeForceTogglePayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, DeadeyeForceTogglePayload::status, PacketCodecs.FLOAT, DeadeyeForceTogglePayload::meter, DeadeyeForceTogglePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_FORCE_TOGGLE;
    }
}