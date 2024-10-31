package com.namefix.handlers;

import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeClient;
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
        DeadeyeClient.deadeyeMarkableEntities = LoadDeadeyeMarkableEntities();
        DeadeyeClient.deadeyeMarkingItems = LoadDeadeyeMarkingItems();
        DeadeyeClient.markLimit = DeadeyeMod.CONFIG.server.maxMarks();
        DeadeyeClient.markFocusSpeed = DeadeyeMod.CONFIG.server.markFocusSpeed();
        DeadeyeClient.deadeyeConsumeRate = DeadeyeMod.CONFIG.server.deadeyeIdleConsumeAmount();
    }


    public static List<EntityType<?>> LoadDeadeyeMarkableEntities() {
        List<String> entities = DeadeyeMod.CONFIG.server.markableEntities();
        List<EntityType<?>> entityTypes = new ArrayList<>();

        for (String entity : entities) {
            if (entity.endsWith(":*")) {
                String namespace = entity.split(":")[0];
                Registries.ENTITY_TYPE.stream()
                        .filter(entityType -> namespace.equals(Registries.ENTITY_TYPE.getId(entityType).getNamespace()))
                        .forEach(entityTypes::add);
            } else {
                Identifier id = Identifier.tryParse(entity);
                if (id == null || !Registries.ENTITY_TYPE.containsId(id)) {
                    DeadeyeMod.LOGGER.warn("Error while parsing entity type: {}", entity);
                    continue;
                }
                entityTypes.add(Registries.ENTITY_TYPE.get(id));
            }
        }

        return entityTypes;
    }

    public static List<Item> LoadDeadeyeMarkingItems() {
        List<String> itemStrings = DeadeyeMod.CONFIG.server.markingItems();
        List<Item> items = new ArrayList<>();

        for (String item : itemStrings) {
            if (item.endsWith(":*")) {
                String namespace = item.split(":")[0];
                Registries.ITEM.stream()
                        .filter(itemType -> namespace.equals(Registries.ITEM.getId(itemType).getNamespace()))
                        .forEach(items::add);
            } else {
                Identifier id = Identifier.tryParse(item);
                if (id == null || !Registries.ITEM.containsId(id)) {
                    DeadeyeMod.LOGGER.warn("Error while parsing item: {}", item);
                    continue;
                }
                items.add(Registries.ITEM.get(id));
            }
        }

        return items;
    }
}
