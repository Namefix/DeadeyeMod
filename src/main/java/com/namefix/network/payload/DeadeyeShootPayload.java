package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.joml.Vector3f;

public record DeadeyeShootPayload(Vector3f shootTarget, boolean isLast) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, com.namefix.network.payload.DeadeyeShootPayload> CODEC = PacketCodec.tuple(PacketCodecs.VECTOR3F, DeadeyeShootPayload::shootTarget, PacketCodecs.BOOL, DeadeyeShootPayload::isLast, DeadeyeShootPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.DEADEYE_SHOOT;
    }
}
