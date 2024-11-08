package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.joml.Vector3f;

public record DeadeyeMarkPayload(Vector3f pos, int entityId) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, DeadeyeMarkPayload> CODEC = PacketCodec.tuple(PacketCodecs.VECTOR3F, DeadeyeMarkPayload::pos, PacketCodecs.INTEGER, DeadeyeMarkPayload::entityId, DeadeyeMarkPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_MARK;
    }
}
