package com.namefix.data;

import com.namefix.DeadeyeMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    public HashMap<UUID, PlayerSaveData> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putFloat("deadeyeCore", playerData.deadeyeCore);
            playerNbt.putFloat("deadeyeMeter", playerData.deadeyeMeter);
            playerNbt.putInt("deadeyeLevel", playerData.deadeyeLevel);
            playerNbt.putFloat("deadeyeXp", playerData.deadeyeXp);
            playerNbt.putInt("deadeyeSkill", playerData.deadeyeSkill);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerSaveData playerData = new PlayerSaveData();

            playerData.deadeyeCore = playersNbt.getCompound(key).getFloat("deadeyeCore");
            playerData.deadeyeMeter = playersNbt.getCompound(key).getFloat("deadeyeMeter");
            playerData.deadeyeLevel = playersNbt.getCompound(key).getInt("deadeyeLevel");
            playerData.deadeyeXp = playersNbt.getCompound(key).getFloat("deadeyeXp");
            playerData.deadeyeSkill = playersNbt.getCompound(key).getInt("deadeyeSkill");

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });
        return state;
    }

    private static Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, DeadeyeMod.MOD_ID);

        state.markDirty();
        return state;
    }

    public static PlayerSaveData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(player.getWorld().getServer());

        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerSaveData());
    }
}
