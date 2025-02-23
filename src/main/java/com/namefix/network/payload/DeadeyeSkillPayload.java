package com.namefix.network.payload;

import com.namefix.network.DeadeyeNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeadeyeSkillPayload(int skill) implements CustomPayload {
	public static final PacketCodec<RegistryByteBuf, DeadeyeSkillPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, DeadeyeSkillPayload::skill, DeadeyeSkillPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return DeadeyeNetworking.DEADEYE_SKILL;
	}
}
