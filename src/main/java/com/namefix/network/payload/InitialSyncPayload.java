package com.namefix.network.payload;


import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record InitialSyncPayload(float deadeyeMeter) implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, InitialSyncPayload> CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, InitialSyncPayload::deadeyeMeter, InitialSyncPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.INITIAL_SYNC;
    }
}