package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.network.payload.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class DeadeyeNetworking {
    public static final CustomPayload.Id<DeadeyeTogglePayload> DEADEYE_TOGGLE = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_toggle"));
    public static final CustomPayload.Id<DeadeyeShootPayload> DEADEYE_SHOOT = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_shoot"));
    public static final CustomPayload.Id<ReloadConfigPayload> RELOAD_CONFIG = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "reload_config"));
    public static final CustomPayload.Id<DeadeyeMeterPayload> DEADEYE_METER = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_meter"));
    public static final CustomPayload.Id<InitialSyncPayload> INITIAL_SYNC = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "initial_sync"));
    public static final CustomPayload.Id<DeadeyeForcePayload> DEADEYE_FORCE = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_force"));
    public static final CustomPayload.Id<DeadeyeMarkingPayload> DEADEYE_MARKING = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_shooting_status"));
    public static final CustomPayload.Id<DeadeyeForceShootPayload> DEADEYE_FORCE_SHOOT = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_force_shoot"));

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(RELOAD_CONFIG, ReloadConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_METER, DeadeyeMeterPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(INITIAL_SYNC, InitialSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_FORCE, DeadeyeForcePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_FORCE_SHOOT, DeadeyeForceShootPayload.CODEC);
    }

    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(DEADEYE_TOGGLE, DeadeyeTogglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DEADEYE_SHOOT, DeadeyeShootPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DEADEYE_MARKING, DeadeyeMarkingPayload.CODEC);
    }
}
