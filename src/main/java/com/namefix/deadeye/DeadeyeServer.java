package com.namefix.deadeye;

import com.namefix.DeadeyeMod;
import com.namefix.DeadeyeMod.TargetingInteractionType;
import com.namefix.data.DeadeyeTarget;
import com.namefix.data.PlayerSaveData;
import com.namefix.data.PlayerServerData;
import com.namefix.data.StateSaverAndLoader;
import com.namefix.handlers.ConfigHandler;
import com.namefix.network.payload.*;
import com.namefix.utils.Utils;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.*;

public class DeadeyeServer {
    public static boolean deadeyeEnabled = true;

    public static Map<UUID, PlayerServerData> deadeyeUsers = new HashMap<>();
    public static List<EntityType<?>> deadeyeMarkableEntities = ConfigHandler.LoadDeadeyeMarkableEntities();
    public static List<Item> deadeyeItems = ConfigHandler.LoadDeadeyeMarkingItems();

    // Receiving deadeye toggle request packet from a player
    public static void receiveDeadeyeRequest(DeadeyeRequestPayload payload, ServerPlayNetworking.Context context) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(context.player());

        if(payload.status()) {  // Player wants to enable deadeye
            if(!deadeyeEnabled) return;
            if(playerState.deadeyeMeter <= 0f) {
                ServerPlayNetworking.send(context.player(), new DeadeyeUpdatePayload(DeadeyeMod.DeadeyeStatus.EMPTY.ordinal()));
                return;
            }

            updateDeadeyeStatus(context.server(), context.player(), DeadeyeMod.DeadeyeStatus.ENABLED);

        } else {                // Player wants to disable deadeye
            updateDeadeyeStatus(context.server(), context.player(), DeadeyeMod.DeadeyeStatus.DISABLED);
        }
    }

    public static void updateDeadeyeStatus(MinecraftServer server, ServerPlayerEntity player, DeadeyeMod.DeadeyeStatus status) {
        if (status == DeadeyeMod.DeadeyeStatus.ENABLED) {
            deadeyeUsers.put(player.getUuid(), new PlayerServerData());
            if (DeadeyeMod.CONFIG.server.deadeyeSlowdown()) server.getTickManager().setTickRate(5.0f);
            ServerPlayNetworking.send(player, new DeadeyeUpdatePayload(DeadeyeMod.DeadeyeStatus.ENABLED.ordinal()));
        } else {
            deadeyeUsers.remove(player.getUuid());
            if (DeadeyeMod.CONFIG.server.deadeyeSlowdown() && deadeyeUsers.isEmpty())
                server.getTickManager().setTickRate(20.0f);
            ServerPlayNetworking.send(player, new DeadeyeUpdatePayload(status.ordinal()));
        }
    }

    public static void receiveMarkRequest(DeadeyeMarkPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        if(!deadeyeUsers.containsKey(player.getUuid())) return;
        PlayerServerData data = deadeyeUsers.get(player.getUuid());
        if(data.markList.size() >= DeadeyeMod.CONFIG.server.maxMarks()) return;

        ItemStack heldItem = player.getMainHandStack();
        if(heldItem == null) return;
        TargetingInteractionType interactionType = Utils.getTargetingInteractionType(heldItem);
        if(interactionType == TargetingInteractionType.BOW) {
            if(!player.isInCreativeMode() && !player.getInventory().contains(((RangedWeaponItem) heldItem.getItem()).getProjectiles())) {
                updateDeadeyeStatus(context.server(), player, DeadeyeMod.DeadeyeStatus.DISABLED);
                return;
            }
        }
        if(!deadeyeItems.contains(heldItem.getItem())) return;

        Entity ent = player.getWorld().getEntityById(payload.entityId());
        if(ent == null) return;
        if(!(ent instanceof LivingEntity) && !deadeyeMarkableEntities.contains(ent.getType())) return;

        data.shootingPhase = PlayerServerData.ShootingPhase.MARKED;
        data.markList.add(new DeadeyeTarget(ent, new Vec3d(payload.pos())));
        ServerPlayNetworking.send(player, payload);

        if(data.markList.size() >= DeadeyeMod.CONFIG.server.maxMarks()) updatePhase(player, PlayerServerData.ShootingPhase.SHOOTING);
    }

    public static void receivePhaseUpdate(DeadeyePhasePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        if(!deadeyeUsers.containsKey(player.getUuid())) return;
        PlayerServerData data = deadeyeUsers.get(player.getUuid());

        if(data.shootingPhase.ordinal() < payload.phase()) data.shootingPhase = PlayerServerData.ShootingPhase.values()[payload.phase()];
    }

    public static void receiveShot(DeadeyeShotRequestPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        if(!deadeyeUsers.containsKey(player.getUuid())) return;
        PlayerServerData data = deadeyeUsers.get(player.getUuid());

        TargetingInteractionType interactionType = TargetingInteractionType.values()[payload.interactionType()];
        Vec3d pos = context.player().getEyePos();
        ItemStack item = context.player().getMainHandStack();

        if(data.shootingItem != null && !data.shootingItem.getItem().equals(item.getItem())) {
            updateDeadeyeStatus(context.server(), player, DeadeyeMod.DeadeyeStatus.DISABLED);
            return;
        }

        if (!context.player().isInCreativeMode() && item.getItem() instanceof RangedWeaponItem ranged) {
            if (!context.player().getInventory().contains(ranged.getProjectiles())) return;
            context.player().getProjectileType(item).setCount(context.player().getProjectileType(item).getCount() - 1);
        }

        data.shootingPhase = PlayerServerData.ShootingPhase.SHOOTING;
        switch(interactionType) {
            case BOW -> {
                ItemStack projectileItem = context.player().getProjectileType(item).copyWithCount(1);
                ArrowEntity arrow = new ArrowEntity(context.player().getWorld(), pos.x, pos.y, pos.z, projectileItem, item);
                arrow.setOwner(context.player());
                Vector3f motion = payload.shootPos().sub(context.player().getEyePos().toVector3f());
                arrow.addVelocity(new Vec3d(motion.mul(DeadeyeMod.CONFIG.server.markProjectileSpeedMultiplier())));
                context.player().getWorld().spawnEntity(arrow);

                context.player().getWorld().playSound(context.player(), pos.x, pos.y, pos.z, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }

        data.markList.removeFirst();

        if(data.markList.isEmpty()) updateDeadeyeStatus(context.server(), player, DeadeyeMod.DeadeyeStatus.DISABLED);
    }

    public static void updatePhase(ServerPlayerEntity player, PlayerServerData.ShootingPhase phase) {
        if(!deadeyeUsers.containsKey(player.getUuid())) return;
        deadeyeUsers.get(player.getUuid()).shootingPhase = phase;
        ServerPlayNetworking.send(player, new DeadeyePhasePayload(phase.ordinal()));
    }

    public static void onPlayerConnect(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(serverPlayNetworkHandler.player);
        ServerPlayNetworking.send(serverPlayNetworkHandler.player, new InitialSyncPayload(playerState.deadeyeMeter));
    }

    public static void onPlayerDisconnect(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer minecraftServer) {
        deadeyeUsers.remove(serverPlayNetworkHandler.player.getUuid());
    }

    // Give deadeye meter to a player after kill
    public static void awardDeadeyeMeter(LivingEntity livingEntity, DamageSource damageSource) {
        if (!(damageSource.getAttacker() instanceof PlayerEntity player)) return;
        if (DeadeyeServer.deadeyeUsers.get(player.getUuid()) != null) return;
        PlayerSaveData playerData = StateSaverAndLoader.getPlayerState(player);
        playerData.deadeyeMeter = MathHelper.clamp(playerData.deadeyeMeter+DeadeyeMod.CONFIG.server.deadeyeKillRefillAmount(), 0f, 100f);

        ServerPlayNetworking.send((ServerPlayerEntity) player, new DeadeyeMeterPayload(DeadeyeMod.CONFIG.server.deadeyeKillRefillAmount()));
    }

    public static void onTick(MinecraftServer minecraftServer) {
        Iterator<Map.Entry<UUID, PlayerServerData>> iterator = deadeyeUsers.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, PlayerServerData> entry = iterator.next();
            UUID user = entry.getKey();
            PlayerServerData data = entry.getValue();

            ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(user);
            assert player != null;
            PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);

            if (player.isDead()) {
                updateDeadeyeStatus(minecraftServer, player, DeadeyeMod.DeadeyeStatus.DISABLED);
            }

            if (data.shootingPhase != PlayerServerData.ShootingPhase.SHOOTING) {
                playerState.deadeyeMeter = MathHelper.clamp(playerState.deadeyeMeter - DeadeyeMod.CONFIG.server.deadeyeIdleConsumeAmount(), 0f, 100f);
                if (playerState.deadeyeMeter == 0f) {
                    if (data.shootingPhase != PlayerServerData.ShootingPhase.MARKED) {
                        updateDeadeyeStatus(minecraftServer, player, DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY);
                    } else {
                        updatePhase(player, PlayerServerData.ShootingPhase.SHOOTING);
                    }
                }
            } else {
                if (data.shootingItem != null && !data.shootingItem.getItem().equals(player.getMainHandStack().getItem())) {
                    updateDeadeyeStatus(minecraftServer, player, DeadeyeMod.DeadeyeStatus.DISABLED);
                }
            }
        }
    }
}