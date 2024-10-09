package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.deadeye.Deadeye;
import com.namefix.deadeye.DeadeyeServer;
import com.namefix.handlers.ConfigHandler;
import com.namefix.network.payload.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class DeadeyeNetworking {
    public static final CustomPayload.Id<DeadeyeTogglePayload> DEADEYE_TOGGLE = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_toggle"));
    public static final CustomPayload.Id<DeadeyeShootPayload> DEADEYE_SHOOT = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_shoot"));
    public static final CustomPayload.Id<ReloadConfigPayload> RELOAD_CONFIG = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "reload_config"));
    public static final CustomPayload.Id<DeadeyeMeterPayload> DEADEYE_METER = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_meter"));
    public static final CustomPayload.Id<InitialSyncPayload> INITIAL_SYNC = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "initial_sync"));
    public static final CustomPayload.Id<DeadeyeForceTogglePayload> DEADEYE_FORCE_TOGGLE = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_force"));
    public static final CustomPayload.Id<DeadeyeMarkingPayload> DEADEYE_MARKING = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_shooting_status"));
    public static final CustomPayload.Id<DeadeyeForceShootPayload> DEADEYE_FORCE_SHOOT = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_force_shoot"));

    public static void initializeServerPayloads() {
        PayloadTypeRegistry.playS2C().register(RELOAD_CONFIG, ReloadConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_METER, DeadeyeMeterPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(INITIAL_SYNC, InitialSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_FORCE_TOGGLE, DeadeyeForceTogglePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_FORCE_SHOOT, DeadeyeForceShootPayload.CODEC);
    }

    public static void initializeClientPayloads() {
        PayloadTypeRegistry.playC2S().register(DEADEYE_TOGGLE, DeadeyeTogglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DEADEYE_SHOOT, DeadeyeShootPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DEADEYE_MARKING, DeadeyeMarkingPayload.CODEC);
    }

    public static void initializeServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_TOGGLE, DeadeyeServer::deadeyeToggle);
        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_SHOOT, DeadeyeServer::spawnDeadeyeProjectile);
        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_MARKING, DeadeyeServer::updateMarkingStatus);
    }

    public static void initializeClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.RELOAD_CONFIG, (ReloadConfigPayload payload, ClientPlayNetworking.Context context) -> {ConfigHandler.ReloadConfigClient();});
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_METER, Deadeye::deadeyeMeterUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.INITIAL_SYNC, Deadeye::receiveInitialSync);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_FORCE_TOGGLE, Deadeye::deadeyeForceUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_FORCE_SHOOT, Deadeye::deadeyeForceShoot);
    }
}
