package com.namefix.network;

import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import com.namefix.handlers.ConfigHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public class DeadeyeNetworking {
    public static final Identifier RELOAD_CONFIG = Identifier.of(DeadeyeMod.MOD_ID, "reload_config");
    public static final Identifier DEADEYE_METER = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_meter");
    public static final Identifier DEADEYE_CORE = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_core");
    public static final Identifier DEADEYE_LEVEL = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_level");
    public static final Identifier DEADEYE_SKILL = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_skill");
    public static final Identifier INITIAL_SYNC = Identifier.of(DeadeyeMod.MOD_ID, "initial_sync");

    public static final Identifier DEADEYE_REQUEST = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_request");
    public static final Identifier DEADEYE_UPDATE = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_update");
    public static final Identifier DEADEYE_MARK = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_mark");
    public static final Identifier DEADEYE_PHASE = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_phase");
    public static final Identifier DEADEYE_SHOT_REQUEST = Identifier.of(DeadeyeMod.MOD_ID, "deadeye_shot_request");

    public static void initializeServerPayloads() {
    }

    public static void initializeClientPayloads() {
    }

    public static void initializeServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_REQUEST, (server, client, handler, buf, responseSender) -> {
            boolean status = buf.readBoolean();
            DeadeyeServer.receiveDeadeyeRequest(client, server, status);
        });

        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_SHOT_REQUEST, (server, client, handler, buf, responseSender) -> {
            int interactionType = buf.readInt();
            Vector3f shootPos = buf.readVector3f();
            DeadeyeServer.receiveShot(server, client, interactionType, shootPos);
        });

        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_MARK, (server, client, handler, buf, responseSender) -> {
            Vector3f pos = buf.readVector3f();
            int entityId = buf.readInt();
            DeadeyeServer.receiveMarkRequest(server, client, pos, entityId);
        });

        ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_PHASE, (server, client, handler, buf, responseSender) -> {
            int phase = buf.readInt();
            DeadeyeServer.receivePhaseUpdate(client, phase);
        });
    }

    public static void initializeClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.RELOAD_CONFIG, (client, handler, buf, responseSender) -> client.execute(ConfigHandler::ReloadConfigClient));

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_METER, (client, handler, buf, responseSender) -> {
            float amount = buf.readFloat();
            client.execute(() -> DeadeyeClient.deadeyeMeterUpdate(amount));
        });

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_CORE, (client, handler, buf, responseSender) -> {
            float amount = buf.readFloat();
            client.execute(() -> DeadeyeClient.deadeyeCoreUpdate(amount));
        });

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_LEVEL, (client, handler, buf, responseSender) -> {
            int level = buf.readInt();
            client.execute(() -> DeadeyeClient.deadeyeLevelUpdate(level));
        });

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_SKILL, (client, handler, buf, response) -> {
            int skill = buf.readInt();
            client.execute(() -> DeadeyeClient.deadeyeSkillUpdate(skill));
        });

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.INITIAL_SYNC, (client, handler, buf, responseSender) -> {
            float meter = buf.readFloat();
            float core = buf.readFloat();
            int level = buf.readInt();
            client.execute(() -> DeadeyeClient.receiveInitialSync(meter, core, level));
        });

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_UPDATE, (client, handler, buf, responseSender) -> {
            int status = buf.readInt();
            client.execute(() -> DeadeyeClient.receiveDeadeyeUpdate(status));
        });

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_MARK, (client, handler, buf, responseSender) -> {
            Vector3f pos = buf.readVector3f();
            int entityId = buf.readInt();
            client.execute(() -> DeadeyeClient.receiveDeadeyeMark(pos, entityId));
        });

        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_PHASE, (client, handler, buf, responseSender) -> {
            int phase = buf.readInt();
            client.execute(() -> DeadeyeClient.receivePhaseUpdate(phase));
        });
    }
}
