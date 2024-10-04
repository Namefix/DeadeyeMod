package com.namefix.handlers;

import com.namefix.DeadeyeMod;
import com.namefix.deadeye.Deadeye;
import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.deadeye.DeadeyeServer;
import com.namefix.network.payload.ReloadConfigPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {
    public static void ReloadConfigServer(MinecraftServer server) {
        DeadeyeMod.LOGGER.info("Reloading server config...");
        DeadeyeServer.deadeyeMarkableEntities = LoadDeadeyeMarkableEntities();
        DeadeyeServer.deadeyeItems = LoadDeadeyeMarkingItems();
        DeadeyeMod.CONFIG.load();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new ReloadConfigPayload());
        }

    }

    public static void ReloadConfigClient() {
        DeadeyeMod.LOGGER.info("Reloading client config...");
        DeadeyeMod.CONFIG.load();
        Deadeye.deadeyeMarkableEntities = LoadDeadeyeMarkableEntities();
        Deadeye.deadeyeMarkingItems = LoadDeadeyeMarkingItems();
        Deadeye.markLimit = DeadeyeMod.CONFIG.server.maxMarks();
        Deadeye.markFocusSpeed = DeadeyeMod.CONFIG.server.markFocusSpeed();
        Deadeye.deadeyeConsumeRate = DeadeyeMod.CONFIG.server.deadeyeIdleConsumeAmount();

        DeadeyeEffects.renderDisabled = DeadeyeMod.CONFIG.client.disableDeadeyeEffects();
        DeadeyeEffects.vignetteStrength = DeadeyeMod.CONFIG.client.deadeyeVignetteStrength();
        Deadeye.soundVolume = DeadeyeMod.CONFIG.client.deadeyeVolume()/100;
        DeadeyeEffects.setMarkSize(DeadeyeMod.CONFIG.client.deadeyeMarkSize());
    }


    public static List<EntityType<?>> LoadDeadeyeMarkableEntities() {
        List<String> entities = DeadeyeMod.CONFIG.server.markableEntities();
        List<EntityType<?>> entityTypes = new ArrayList<>();

        // TODO: Support wildcard in entity types (minecraft:*)
        for (String entity : entities) {
            if(!Registries.ENTITY_TYPE.containsId(Identifier.tryParse(entity))) {
                DeadeyeMod.LOGGER.warn("Error while parsing entity type: {}", entity);
                continue;
            }
            entityTypes.add(Registries.ENTITY_TYPE.get(Identifier.of(entity)));
        }

        return entityTypes;
    }

    public static List<Item> LoadDeadeyeMarkingItems() {
        List<String> itemStrings = DeadeyeMod.CONFIG.server.markingItems();
        List<Item> items = new ArrayList<>();

        // TODO: Support wildcard in item types (minecraft:*)
        for (String item : itemStrings) {
            if(!Registries.ITEM.containsId(Identifier.tryParse(item))) {
                DeadeyeMod.LOGGER.warn("Error while parsing item: {}", item);
                continue;
            }
            items.add(Registries.ITEM.get(Identifier.of(item)));
        }

        return items;
    }
}
