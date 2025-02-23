package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import com.namefix.handlers.ConfigHandler;
import com.namefix.network.payload.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class DeadeyeNetworking {
    public static final CustomPayload.Id<ReloadConfigPayload> RELOAD_CONFIG = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "reload_config"));
    public static final CustomPayload.Id<DeadeyeMeterPayload> DEADEYE_METER = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_meter"));
    public static final CustomPayload.Id<DeadeyeCorePayload> DEADEYE_CORE = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_core"));
    public static final CustomPayload.Id<DeadeyeLevelPayload> DEADEYE_LEVEL = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_level"));
    public static final CustomPayload.Id<DeadeyeSkillPayload> DEADEYE_SKILL = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_skill"));
    public static final CustomPayload.Id<InitialSyncPayload> INITIAL_SYNC = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "initial_sync"));

    public static final CustomPayload.Id<DeadeyeRequestPayload> DEADEYE_REQUEST = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_request"));
    public static final CustomPayload.Id<DeadeyeUpdatePayload> DEADEYE_UPDATE = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_update"));
    public static final CustomPayload.Id<DeadeyeMarkPayload> DEADEYE_MARK = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_mark"));
    public static final CustomPayload.Id<DeadeyePhasePayload> DEADEYE_PHASE = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_phase"));
    public static final CustomPayload.Id<DeadeyeShotRequestPayload> DEADEYE_SHOT_REQUEST = new CustomPayload.Id<>(Identifier.of(DeadeyeMod.MOD_ID, "deadeye_shot_request"));

    public static void initializeServerPayloads() {
        PayloadTypeRegistry.playS2C().register(RELOAD_CONFIG, ReloadConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_METER, DeadeyeMeterPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_CORE, DeadeyeCorePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_LEVEL, DeadeyeLevelPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_SKILL, DeadeyeSkillPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(INITIAL_SYNC, InitialSyncPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(DEADEYE_UPDATE, DeadeyeUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_MARK, DeadeyeMarkPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DEADEYE_PHASE, DeadeyePhasePayload.CODEC);
    }

    public static void initializeClientPayloads() {
        PayloadTypeRegistry.playC2S().register(DEADEYE_REQUEST, DeadeyeRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DEADEYE_MARK, DeadeyeMarkPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DEADEYE_PHASE, DeadeyePhasePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DEADEYE_SHOT_REQUEST, DeadeyeShotRequestPayload.CODEC);
    }

    public static void initializeServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_REQUEST, DeadeyeServer::receiveDeadeyeRequest);
        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_MARK, DeadeyeServer::receiveMarkRequest);
        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_PHASE, DeadeyeServer::receivePhaseUpdate);
        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_SHOT_REQUEST, DeadeyeServer::receiveShot);
    }

    public static void initializeClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.RELOAD_CONFIG, (ReloadConfigPayload payload, ClientPlayNetworking.Context context) -> {ConfigHandler.ReloadConfigClient();});
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_METER, DeadeyeClient::deadeyeMeterUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_CORE, DeadeyeClient::deadeyeCoreUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_LEVEL, DeadeyeClient::deadeyeLevelUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_SKILL, DeadeyeClient::deadeyeSkillUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.INITIAL_SYNC, DeadeyeClient::receiveInitialSync);

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_UPDATE, DeadeyeClient::receiveDeadeyeUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_MARK, DeadeyeClient::receiveDeadeyeMark);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_PHASE, DeadeyeClient::receivePhaseUpdate);
    }
}
