package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.joml.Vector3f;

public record DeadeyeShotRequestPayload(int interactionType, Vector3f shootPos) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeShotRequestPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, DeadeyeShotRequestPayload::interactionType, PacketCodecs.VECTOR3F, DeadeyeShotRequestPayload::shootPos, DeadeyeShotRequestPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_SHOT_REQUEST;
    }
}
