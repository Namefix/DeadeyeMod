package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ReloadConfigPayload() implements CustomPayload {
    public static final PacketCodec<RegistryByteBuf, ReloadConfigPayload> CODEC = PacketCodec.unit(new ReloadConfigPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return DeadeyeNetworking.RELOAD_CONFIG;
    }
}
