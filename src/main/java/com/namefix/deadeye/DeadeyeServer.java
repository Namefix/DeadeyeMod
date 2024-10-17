package com.namefix.deadeye;

import com.namefix.DeadeyeMod;
import com.namefix.DeadeyeMod.TargetingInteractionType;
import com.namefix.data.PlayerSaveData;
import com.namefix.data.PlayerServerData;
import com.namefix.data.StateSaverAndLoader;
import com.namefix.handlers.ConfigHandler;
import com.namefix.network.payload.*;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
    public static Map<UUID, PlayerServerData> deadeyeUsers = new HashMap<>();
    public static List<EntityType<?>> deadeyeMarkableEntities = ConfigHandler.LoadDeadeyeMarkableEntities();
    public static List<Item> deadeyeItems = ConfigHandler.LoadDeadeyeMarkingItems();

    // Receiving deadeye toggle packet from a player
    public static void deadeyeToggle(DeadeyeTogglePayload payload, ServerPlayNetworking.Context context) {
        if (payload.status()) {
            deadeyeUsers.put(context.player().getUuid(), new PlayerServerData());
            if (DeadeyeMod.CONFIG.server.deadeyeSlowdown()) context.server().getTickManager().setTickRate(5.0f);
        } else {
            deadeyeUsers.remove(context.player().getUuid());
            if (DeadeyeMod.CONFIG.server.deadeyeSlowdown() && deadeyeUsers.isEmpty())
                context.server().getTickManager().setTickRate(20.0f);
        }
    }

    public static void onPlayerConnect(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(serverPlayNetworkHandler.player);
        ServerPlayNetworking.send(serverPlayNetworkHandler.player, new InitialSyncPayload(playerState.deadeyeMeter));
    }

    public static void onPlayerDisconnect(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer minecraftServer) {
        deadeyeUsers.remove(serverPlayNetworkHandler.player.getUuid());
    }

    public static void updateMarkingStatus(DeadeyeMarkingPayload payload, ServerPlayNetworking.Context context) {
        deadeyeUsers.get(context.player().getUuid()).hasMarkedTargets = payload.status();
    }

    public static void updateShootingStatus(DeadeyeShootingPayload payload, ServerPlayNetworking.Context context) {
        deadeyeUsers.get(context.player().getUuid()).isShootingTargets = payload.status();
    }

    // Receive deadeye target shooting packet
    public static void receiveDeadeyeTargetShoot(DeadeyeShootPayload payload, ServerPlayNetworking.Context context) {
        TargetingInteractionType interactionType = TargetingInteractionType.valueOf(payload.interactionType());
        Vec3d pos = context.player().getEyePos();
        ItemStack item = context.player().getMainHandStack();

        if (!context.player().isInCreativeMode() && item.getItem() instanceof RangedWeaponItem ranged) {
            if (!context.player().getInventory().contains(ranged.getProjectiles())) return;
            context.player().getProjectileType(item).setCount(context.player().getProjectileType(item).getCount() - 1);
        }

        switch(interactionType) {
            case BOW -> {
                ItemStack projectileItem = context.player().getProjectileType(item).copyWithCount(1);
                ArrowEntity arrow = new ArrowEntity(context.player().getWorld(), pos.x, pos.y, pos.z, projectileItem, item);
                arrow.setOwner(context.player());
                Vector3f motion = payload.shootTarget().sub(context.player().getEyePos().toVector3f());
                arrow.addVelocity(new Vec3d(motion.mul(DeadeyeMod.CONFIG.server.markProjectileSpeedMultiplier())));
                context.player().getWorld().spawnEntity(arrow);

                context.player().getWorld().playSound(context.player(), pos.x, pos.y, pos.z, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }

        deadeyeUsers.get(context.player().getUuid()).isShootingTargets = !payload.isLast();
        DeadeyeMod.LOGGER.info(Boolean.toString(!payload.isLast()));
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
        deadeyeUsers.forEach((user, data) -> {
            ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(user);
            assert player != null;
            PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);

            if(!data.isShootingTargets) {
                playerState.deadeyeMeter = MathHelper.clamp(playerState.deadeyeMeter - DeadeyeMod.CONFIG.server.deadeyeIdleConsumeAmount(), 0f, 100f);
                if (playerState.deadeyeMeter == 0f) {
                    if(!data.hasMarkedTargets) ServerPlayNetworking.send(player, new DeadeyeForceTogglePayload(false, playerState.deadeyeMeter));
                    else ServerPlayNetworking.send(player, new DeadeyeForceShootPayload());
                }
            }
        });
    }
}