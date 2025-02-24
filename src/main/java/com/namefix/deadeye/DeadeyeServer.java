package com.namefix.deadeye;

import com.namefix.DeadeyeMod;
import com.namefix.DeadeyeMod.TargetingInteractionType;
import com.namefix.data.DeadeyeTarget;
import com.namefix.data.PlayerSaveData;
import com.namefix.data.PlayerServerData;
import com.namefix.data.StateSaverAndLoader;
import com.namefix.handlers.ConfigHandler;
import com.namefix.handlers.GameruleHandler;
import com.namefix.integrations.PointBlankIntegration;
import com.namefix.network.payload.*;
import com.namefix.utils.Utils;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.*;

public class DeadeyeServer {
    public static Map<UUID, PlayerServerData> deadeyeUsers = new HashMap<>();
    public static List<EntityType<?>> deadeyeMarkableEntities = ConfigHandler.LoadDeadeyeMarkableEntities();
    public static List<Item> deadeyeItems = ConfigHandler.LoadDeadeyeMarkingItems();

    // Receiving deadeye toggle request packet from a player
    public static void receiveDeadeyeRequest(DeadeyeRequestPayload payload, ServerPlayNetworking.Context context) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(context.player());

        if(payload.status()) {  // Player wants to enable deadeye
            if(context.player().getWorld().getGameRules().getBoolean(GameruleHandler.DISABLE_DEADEYE) || playerState.deadeyeSkill <= 0) return;
            if(playerState.deadeyeMeter <= 0f && playerState.deadeyeCore <= 0f) {
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

        ItemStack heldItem = player.getMainHandStack();
        if(heldItem == null) return;
        TargetingInteractionType interactionType = Utils.getTargetingInteractionType(heldItem);

        if(interactionType == TargetingInteractionType.POINT_BLANK_GUN && !PointBlankIntegration.canMarkTargets(heldItem, data.markList.size())) return; // if marks more than gun max ammo
        if(interactionType != TargetingInteractionType.POINT_BLANK_GUN && data.markList.size() >= DeadeyeMod.CONFIG.server.maxMarks()) return; // check mark limit
        if(interactionType == TargetingInteractionType.BOW) {
            if(!player.isInCreativeMode() && !player.getInventory().contains(((RangedWeaponItem) heldItem.getItem()).getProjectiles())) {
                updateDeadeyeStatus(context.server(), player, DeadeyeMod.DeadeyeStatus.DISABLED);
                return;
            }
        }
        if(!deadeyeItems.contains(heldItem.getItem()) && interactionType != TargetingInteractionType.POINT_BLANK_GUN) return;

        Entity ent = player.getWorld().getEntityById(payload.entityId());
        if(ent == null) return;
        if(!(ent instanceof LivingEntity) && !deadeyeMarkableEntities.contains(ent.getType())) return;

        updatePhase(player, PlayerServerData.ShootingPhase.MARKED);
        Vec3d pos = new Vec3d(payload.pos());
        DeadeyeTarget target = new DeadeyeTarget(ent, pos);
        data.markList.add(target);
        ServerPlayNetworking.send(player, payload);

        if(!player.isInCreativeMode() && interactionType == TargetingInteractionType.BOW && player.getProjectileType(heldItem).getCount() <= data.markList.size()) updatePhase(player, PlayerServerData.ShootingPhase.SHOOTING);
        if(interactionType != TargetingInteractionType.POINT_BLANK_GUN && data.markList.size() >= DeadeyeMod.CONFIG.server.maxMarks()) updatePhase(player, PlayerServerData.ShootingPhase.SHOOTING);
    }

    public static void receivePhaseUpdate(DeadeyePhasePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        if(!deadeyeUsers.containsKey(player.getUuid())) return;
        PlayerServerData data = deadeyeUsers.get(player.getUuid());

        if(data.shootingPhase.ordinal() < payload.phase()) data.shootingPhase = PlayerServerData.ShootingPhase.values()[payload.phase()];
    }

    public static void receiveShot(DeadeyeShotRequestPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = (ServerWorld) player.getWorld();
        if(!deadeyeUsers.containsKey(player.getUuid())) return;
        PlayerServerData data = deadeyeUsers.get(player.getUuid());
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);

        TargetingInteractionType interactionType = TargetingInteractionType.values()[payload.interactionType()];
        Vec3d pos = player.getEyePos();
        ItemStack item = player.getMainHandStack();

        if(data.shootingItem != null && !data.shootingItem.getItem().equals(item.getItem())) {
            updateDeadeyeStatus(context.server(), player, DeadeyeMod.DeadeyeStatus.DISABLED);
            return;
        }

        if (!player.isInCreativeMode() && item.getItem() instanceof RangedWeaponItem ranged) {
            if (!player.getInventory().contains(ranged.getProjectiles())) return;
        }

        data.shootingPhase = PlayerServerData.ShootingPhase.SHOOTING;
        switch(interactionType) {
            case BOW -> {
                RangedWeaponItem ranged = (RangedWeaponItem) item.getItem();
                ItemStack projectileItem = player.getProjectileType(item).copyWithCount(1);
                ArrowEntity arrow = new ArrowEntity(world, pos.x, pos.y, pos.z, projectileItem, item);
                arrow.setOwner(player);
                Vector3f motion = payload.shootPos().sub(player.getEyePos().toVector3f());
                arrow.addVelocity(new Vec3d(motion.mul(DeadeyeMod.CONFIG.server.markProjectileSpeedMultiplier())));
                world.spawnEntity(arrow);

                SoundEvent shootSound = item.getItem() instanceof BowItem ? SoundEvents.ENTITY_ARROW_SHOOT : SoundEvents.ITEM_CROSSBOW_SHOOT;
                int i = ranged.getMaxUseTime(item, player);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), shootSound, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + BowItem.getPullProgress(i) * 0.5F);
                player.incrementStat(Stats.USED.getOrCreateStat(item.getItem()));
            }
        }

        player.getProjectileType(item).decrementUnlessCreative(1, player);

        data.markList.removeFirst();

        if(data.markList.isEmpty()) updateDeadeyeStatus(context.server(), player, playerState.deadeyeMeter <= 0.0f && playerState.deadeyeCore <= 0.0f ? DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY : DeadeyeMod.DeadeyeStatus.DISABLED);
    }

    public static void updatePhase(ServerPlayerEntity player, PlayerServerData.ShootingPhase phase) {
        if(!deadeyeUsers.containsKey(player.getUuid())) return;
        deadeyeUsers.get(player.getUuid()).shootingPhase = phase;
        ServerPlayNetworking.send(player, new DeadeyePhasePayload(phase.ordinal()));
    }

    public static void onPlayerConnect(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(serverPlayNetworkHandler.player);
        ServerPlayNetworking.send(serverPlayNetworkHandler.player, new InitialSyncPayload(playerState.deadeyeMeter, playerState.deadeyeCore, playerState.deadeyeLevel));
    }

    public static void onPlayerDisconnect(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer minecraftServer) {
        updateDeadeyeStatus(minecraftServer, serverPlayNetworkHandler.player, DeadeyeMod.DeadeyeStatus.DISABLED);
    }

    public static void setDeadeyeMeter(ServerPlayerEntity player, float amount) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);
        if(amount < 0) amount = getMaxDeadeye(playerState);
        playerState.deadeyeMeter = amount;
        ServerPlayNetworking.send(player, new DeadeyeMeterPayload(amount));
    }

    public static void setMeterTonicLevel(ServerPlayerEntity player, int level) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);
        playerState.deadeyeMeter = (playerState.deadeyeLevel*10)+(level*20);
        ServerPlayNetworking.send(player, new DeadeyeMeterPayload(playerState.deadeyeMeter));
    }

    public static void setDeadeyeCore(ServerPlayerEntity player, float amount) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);
        playerState.deadeyeCore = amount;
        ServerPlayNetworking.send(player, new DeadeyeCorePayload(amount));
    }

    public static void addDeadeyeCore(ServerPlayerEntity player, float amount, boolean meterCap) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);
        if(meterCap && playerState.deadeyeCore + amount > 20f) playerState.deadeyeCore = 20f;
        else playerState.deadeyeCore = MathHelper.clamp(playerState.deadeyeCore+amount, 0.0f, 80f);
        ServerPlayNetworking.send(player, new DeadeyeCorePayload(playerState.deadeyeCore));
    }

    public static void setDeadeyeLevel(ServerPlayerEntity player, int level) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);
        playerState.deadeyeLevel = level;
        playerState.deadeyeXp = 0;
        ServerPlayNetworking.send(player, new DeadeyeLevelPayload(level));
    }

    public static void setDeadeyeSkill(ServerPlayerEntity player, int skill) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);
        playerState.deadeyeSkill = skill;
        ServerPlayNetworking.send(player, new DeadeyeSkillPayload(skill));
    }

    public static void addDeadeyeXP(ServerPlayerEntity player, float xp) {
        PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);
        if(playerState.deadeyeLevel >= 10) return;
        playerState.deadeyeXp += xp;
        if(player.getWorld().getGameRules().getBoolean(GameruleHandler.DISABLE_DEADEYE_LEVELING)) return;
        boolean leveledUp = false;
        while(playerState.deadeyeXp >= calculateNeededXP(playerState.deadeyeLevel)) {
            playerState.deadeyeXp -= calculateNeededXP(playerState.deadeyeLevel);
            playerState.deadeyeLevel++;
            leveledUp = true;
        }
        if(leveledUp) {
            player.sendMessage(Text.translatable("text.deadeye-mod.levelup", playerState.deadeyeLevel));
            ServerPlayNetworking.send(player, new DeadeyeLevelPayload(playerState.deadeyeLevel));
        }
    }

    public static float calculateNeededXP(int level) {
        return level * 10f;
    }

    // Give deadeye meter to a player after kill
    public static void deadeyeMeterKillReward(LivingEntity livingEntity, DamageSource damageSource) {
        if (!(damageSource.getAttacker() instanceof ServerPlayerEntity player)) return;
        if (DeadeyeServer.deadeyeUsers.get(player.getUuid()) != null) {
            addDeadeyeXP(player, 0.5f);
            return;
        }
        PlayerSaveData playerData = StateSaverAndLoader.getPlayerState(player);
        if(playerData.deadeyeMeter > playerData.deadeyeLevel*10) return;
        playerData.deadeyeMeter = MathHelper.clamp(playerData.deadeyeMeter+DeadeyeMod.CONFIG.server.deadeyeKillRefillAmount(), 0f, playerData.deadeyeLevel*10);
        ServerPlayNetworking.send(player, new DeadeyeMeterPayload(playerData.deadeyeMeter));
    }

    public static float getMaxDeadeye(PlayerSaveData playerData) {
        return (playerData.deadeyeLevel*10)+60f;
    }

    public static void onTick(MinecraftServer minecraftServer) {
        Iterator<Map.Entry<UUID, PlayerServerData>> iterator = deadeyeUsers.entrySet().iterator();
        List<UUID> toRemove = new ArrayList<>();

        while (iterator.hasNext()) {
            Map.Entry<UUID, PlayerServerData> entry = iterator.next();
            UUID user = entry.getKey();
            PlayerServerData data = entry.getValue();

            ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(user);
            assert player != null;
            PlayerSaveData playerState = StateSaverAndLoader.getPlayerState(player);

            if (player.isDead()) {
                ServerPlayNetworking.send(player, new DeadeyeUpdatePayload(DeadeyeMod.DeadeyeStatus.DISABLED.ordinal()));
                toRemove.add(user);
                continue;
            }

            if (data.shootingPhase != PlayerServerData.ShootingPhase.SHOOTING) {
                float slowdownMultiplier = 20f/minecraftServer.getTickManager().getTickRate();
                float decreaseAmount = DeadeyeMod.CONFIG.server.deadeyeIdleConsumeAmount() * slowdownMultiplier;

                addDeadeyeXP(player, 0.001f*slowdownMultiplier);
                if(playerState.deadeyeMeter > 0)playerState.deadeyeMeter = MathHelper.clamp(playerState.deadeyeMeter - decreaseAmount, 0f, getMaxDeadeye(playerState));
                else playerState.deadeyeCore = MathHelper.clamp(playerState.deadeyeCore - decreaseAmount, 0f, 80f);
                if (playerState.deadeyeMeter == 0f && playerState.deadeyeCore == 0f) {
                    if (data.shootingPhase != PlayerServerData.ShootingPhase.MARKED) {
                        ServerPlayNetworking.send(player, new DeadeyeUpdatePayload(DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY.ordinal()));
                        toRemove.add(user);
                    } else {
                        setDeadeyeMeter(player, 0);
                        setDeadeyeCore(player, 0);
                        updatePhase(player, PlayerServerData.ShootingPhase.SHOOTING);
                    }
                }
            } else {
                if (data.shootingItem != null && !data.shootingItem.getItem().equals(player.getMainHandStack().getItem())) {
                    ServerPlayNetworking.send(player, new DeadeyeUpdatePayload(DeadeyeMod.DeadeyeStatus.DISABLED.ordinal()));
                    toRemove.add(user);
                }
            }
        }

        for (UUID user : toRemove) {
            deadeyeUsers.remove(user);
        }

        if (DeadeyeMod.CONFIG.server.deadeyeSlowdown() && deadeyeUsers.isEmpty()) {
            minecraftServer.getTickManager().setTickRate(20.0f);
        }
    }
}