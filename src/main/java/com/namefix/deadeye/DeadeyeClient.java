package com.namefix.deadeye;

import com.namefix.DeadeyeMod;
import com.namefix.DeadeyeMod.TargetingInteractionType;
import com.namefix.data.DeadeyeTarget;
import com.namefix.data.PlayerSaveData;
import com.namefix.data.PlayerServerData;
import com.namefix.handlers.ConfigHandler;
import com.namefix.handlers.KeybindHandler;
import com.namefix.integrations.PointBlankIntegration;
import com.namefix.network.payload.*;
import com.namefix.utils.Utils;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeadeyeClient {
    private DeadeyeClient() {}
    public static PlayerSaveData playerData = new PlayerSaveData();

    public static boolean isEnabled = false;
    public static PlayerServerData.ShootingPhase shootingPhase = PlayerServerData.ShootingPhase.NONE;
    public static float deadeyeEnding = 0.0f;

    public static List<EntityType<?>> deadeyeMarkableEntities = ConfigHandler.LoadDeadeyeMarkableEntities();
    public static List<Item> deadeyeMarkingItems = ConfigHandler.LoadDeadeyeMarkingItems();

    static ArrayList<DeadeyeTarget> marks = new ArrayList<>();

    public static boolean shootingMarks = false;
    static long markWait = 0;
    static long lerpWait = 0;
    static long shootWait = 0;
    static long startLerpingTime = 0;
    static Item shootStartItem = null;

    // Adding fast pull functionality to bows when in deadeye
    public static void initializeBowProperties() {
        ModelPredicateProviderRegistry.register(Items.BOW, Identifier.of("pull"), (itemStack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            }
            if (entity.isUsingItem() && entity.getActiveItem() == itemStack) {
                int useTicks = entity.getItemUseTime();
                float adjustedUseTicks = useTicks * (DeadeyeClient.isEnabled ? 4.0f : 1.0f); // Compensate for slowed time
                return Math.min(adjustedUseTicks / 20.0f, 1.0f);
            }
            return 0.0F;
        });

        ModelPredicateProviderRegistry.register(Items.CROSSBOW, Identifier.of("pull"), (itemStack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            }
            if (entity.isUsingItem() && entity.getActiveItem() == itemStack) {
                int useTicks = entity.getItemUseTime();
                float adjustedUseTicks = useTicks * (DeadeyeClient.isEnabled ? 4.0f : 1.0f); // Compensate for slowed time
                return Math.min(adjustedUseTicks / 20.0f, 1.0f);
            }
            return 0.0F;
        });
    }

    public static void deadeyeListener(MinecraftClient client) {
        while (KeybindHandler.keyDeadeyeToggle.wasPressed()) {
            requestDeadeye();
        }

        if(playerData.deadeyeSkill == 1) {
            mark(client);
        } else {
            while (KeybindHandler.keyDeadeyeMark.wasPressed()) mark(client);
        }

        while (KeybindHandler.keyDeadeyeShootTargets.wasPressed() && !marks.isEmpty()) {
            assert client.player != null;
            startShootingTargets(client.player.getInventory().getMainHandStack().getItem());
        }

        while (KeybindHandler.keyDeadeyeQuickdraw.wasPressed() && !isEnabled) performQuickdraw();

        if(client.options.attackKey.isPressed()) {
            if(isEnabled && (playerData.deadeyeSkill == 1 || playerData.deadeyeSkill == 2)) requestDeadeye();
            if(!marks.isEmpty()) startShootingTargets(client.player.getInventory().getMainHandStack().getItem());
        }
    }

    public static void render(WorldRenderContext worldRenderContext) {
        shootingTick(worldRenderContext);
        DeadeyeEffects.heartbeatTick();
        DeadeyeEffects.updateVariables(worldRenderContext);
    }

    // Quickdraw
    private static void performQuickdraw() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerInventory inventory = client.player.getInventory();

        int targetSlot = -1;
        for (int i = 0; i <= 8; i++) {
            ItemStack stackInSlot = inventory.getStack(i);
            for (Item targetStack : deadeyeMarkingItems) {
                if (ItemStack.areItemsEqual(stackInSlot, targetStack.getDefaultStack())) {
                    targetSlot = i;
                    break;
                }
            }
            if (targetSlot != -1) {
                break;
            }
        }

        requestDeadeye();
        if(targetSlot != -1) {
            inventory.selectedSlot = targetSlot;
        }
    }

    // All logic related to shooting marked targets
    private static void shootingTick(WorldRenderContext worldRenderContext) {
        if(MinecraftClient.getInstance().isPaused() || !isEnabled) return;
        if(marks.isEmpty() || !shootingMarks) return;
        if (System.currentTimeMillis() < lerpWait) return;

        MinecraftClient client = MinecraftClient.getInstance();
        float slowdownMultiplier = 20f/client.world.getTickManager().getTickRate();
        assert client.player != null;

        ItemStack item = client.player.getMainHandStack();
        TargetingInteractionType targetingType;

        if(!item.getItem().equals(shootStartItem)) {
            requestDeadeye();
            return;
        }

        DeadeyeTarget mark = marks.getFirst();

        float pPitch = client.player.getPitch();
        float pYaw = client.player.getYaw();

        float interpolationFactor = DeadeyeMod.CONFIG.server.markFocusSpeed() * worldRenderContext.tickCounter().getLastFrameDuration() * slowdownMultiplier;
        if(System.currentTimeMillis() - startLerpingTime > 3_000) interpolationFactor *= 4;
        if(PointBlankIntegration.isLoaded) if(PointBlankIntegration.getGunFiremode(item) == FireMode.AUTOMATIC) interpolationFactor *= 8;

        Vec2f targetHeading = mark.getCurrentHeading();
        float targetPitch = targetHeading.x;
        float targetYaw = targetHeading.y;

        float shortestYaw = pYaw + MathHelper.wrapDegrees(targetYaw - pYaw);
        float newYaw = MathHelper.lerp(interpolationFactor, pYaw, shortestYaw);

        float shortestPitch = pPitch + MathHelper.wrapDegrees(targetPitch - pPitch);
        float newPitch = MathHelper.lerp(interpolationFactor, pPitch, shortestPitch);

        if(System.currentTimeMillis() - startLerpingTime > 10_000) {
            newPitch = targetPitch;
            newYaw = targetYaw;
        }

        client.player.setPitch(newPitch);
        client.player.setYaw(newYaw);

        float wrappedNewPitch = MathHelper.wrapDegrees(newPitch);
        float wrappedTargetPitch = MathHelper.wrapDegrees(targetPitch);
        float wrappedNewYaw = MathHelper.wrapDegrees(newYaw);
        float wrappedTargetYaw = MathHelper.wrapDegrees(targetYaw);

        if (Math.abs(wrappedTargetPitch - wrappedNewPitch) < 1f && Math.abs(wrappedTargetYaw - wrappedNewYaw) < 1f) {
            if(shootWait == 0) shootWait = System.currentTimeMillis() + 250;
            if(System.currentTimeMillis() < shootWait) return;

            targetingType = Utils.getTargetingInteractionType(item);
            if(targetingType == TargetingInteractionType.POINT_BLANK_GUN) {
                if(!PointBlankIntegration.canGunShoot(item)) return;
            }

            assert client.interactionManager != null;
            switch(targetingType) {
                // BOW: Simulate shooting
                case BOW -> {}
                // THROWABLE/ProjectileItem: Execute right click
                case THROWABLE -> client.interactionManager.interactItem(client.player, client.player.getActiveHand());
                // POINT_BLANK_GUN: Shoot. (Vic's Point Blank integration)
                case POINT_BLANK_GUN -> PointBlankIntegration.shootGun((GunItem) item.getItem(), client.player, mark.target);
                // DEFAULT: Execute left click
                case DEFAULT -> KeyBinding.onKeyPressed(client.options.attackKey.getDefaultKey());
            }
            ClientPlayNetworking.send(new DeadeyeShotRequestPayload(targetingType.ordinal(), mark.getCurrentOffset().toVector3f()));

            marks.removeFirst();
            lerpWait = System.currentTimeMillis() + 100;
            shootWait = 0;
            startLerpingTime = System.currentTimeMillis();
        }
    }

    private static void startShootingTargets(Item startItem) {
        shootStartItem = startItem;
        shootingMarks = true;
        startLerpingTime = System.currentTimeMillis();
        lerpWait = System.currentTimeMillis() + 250;

        shootingPhase = PlayerServerData.ShootingPhase.SHOOTING;
        ClientPlayNetworking.send(new DeadeyePhasePayload(PlayerServerData.ShootingPhase.SHOOTING.ordinal()));
    }

    // Marking targets
    public static void mark(MinecraftClient client) {
        if(DeadeyeClient.isEnabled && !shootingMarks) {
            int waitAmount = playerData.deadeyeSkill == 1 ? 250 : 100;
            if(System.currentTimeMillis() - markWait < waitAmount) return;
            markWait = System.currentTimeMillis();
            assert client.player != null;
            ItemStack item = client.player.getMainHandStack();
            if(item == null) return;

            TargetingInteractionType interactionType = Utils.getTargetingInteractionType(item);
            if(interactionType != TargetingInteractionType.POINT_BLANK_GUN && marks.size() >= DeadeyeMod.CONFIG.server.maxMarks()) return;
            if(!deadeyeMarkingItems.contains(item.getItem()) && interactionType != TargetingInteractionType.POINT_BLANK_GUN) return;

            if(interactionType == TargetingInteractionType.BOW) {
                if(!client.player.isInCreativeMode() && client.player.getProjectileType(item).getCount() <= marks.size()) return;
            }
            if (interactionType == TargetingInteractionType.POINT_BLANK_GUN) {
                if (!PointBlankIntegration.canMarkTargets(item, marks.size()) && marks.isEmpty()) return;
            }

            double maxDistance = DeadeyeMod.CONFIG.server.maxTargetDistance();
            HitResult hit = Utils.raycastEntity(client.player, maxDistance);
            if(hit == null) return;

            EntityHitResult eHit = (EntityHitResult) hit;
            if (Objects.requireNonNull(hit.getType()) == HitResult.Type.ENTITY) {
                Entity ent = null;
                if(eHit.getEntity() instanceof LivingEntity) ent = eHit.getEntity();
                if(deadeyeMarkableEntities.contains(eHit.getEntity().getType())) ent = eHit.getEntity();
                if(ent == null) return;

                ClientPlayNetworking.send(new DeadeyeMarkPayload(hit.getPos().toVector3f(), ent.getId()));
            }
        }
    }

    public static void requestDeadeye() {
        ClientPlayNetworking.send(new DeadeyeRequestPayload(!isEnabled));
    }

    public static void receiveDeadeyeUpdate(DeadeyeUpdatePayload payload, ClientPlayNetworking.Context context) {
        if(payload.status() == DeadeyeMod.DeadeyeStatus.EMPTY.ordinal()) return;
        setDeadeye(DeadeyeMod.DeadeyeStatus.values()[payload.status()]);
    }

    public static void receiveDeadeyeMark(DeadeyeMarkPayload payload, ClientPlayNetworking.Context context) {
        if(!isEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        TargetingInteractionType interactionType = Utils.getTargetingInteractionType(client.player.getMainHandStack());

        Entity ent = context.player().getWorld().getEntityById(payload.entityId());
        if(ent == null) return;
        marks.add(new DeadeyeTarget(ent, new Vec3d(payload.pos())));
        client.player.playSound(DeadeyeProfiles.getSelectedSoundProfile().paintTargetSound, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f); // placeholder for now
        if(interactionType == TargetingInteractionType.POINT_BLANK_GUN && marks.size() >= PointBlankIntegration.getGunAmmo(client.player.getMainHandStack())) startShootingTargets(client.player.getMainHandStack().getItem());
        if(interactionType != TargetingInteractionType.POINT_BLANK_GUN && marks.size() >= DeadeyeMod.CONFIG.server.maxMarks()) startShootingTargets(client.player.getMainHandStack().getItem());
    }

    public static void receivePhaseUpdate(DeadeyePhasePayload payload, ClientPlayNetworking.Context context) {
        shootingPhase = PlayerServerData.ShootingPhase.values()[payload.phase()];
        if(payload.phase() == PlayerServerData.ShootingPhase.SHOOTING.ordinal()) {
            startShootingTargets(context.player().getInventory().getMainHandStack().getItem());
        }
    }

    // Toggling deadeye
    public static void setDeadeye(DeadeyeMod.DeadeyeStatus status) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        if(status == DeadeyeMod.DeadeyeStatus.ENABLED) isEnabled = true;
        else if(status == DeadeyeMod.DeadeyeStatus.DISABLED || status == DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY) isEnabled = false;

        if(isEnabled) {
            calculateDeadeyeEnding();
            DeadeyeEffects.updateEffects(status);
        }
        else {
            DeadeyeEffects.updateEffects(status);

            shootingPhase = PlayerServerData.ShootingPhase.NONE;
            marks.clear();
            shootingMarks = false;
            startLerpingTime = 0;
            if(status == DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY) {
                playerData.deadeyeMeter = 0.0f;
                playerData.deadeyeCore = 0.0f;
            }
            deadeyeEnding = 0f;
        }
    }

    public static void tick(MinecraftClient client) {
        if(MinecraftClient.getInstance().isPaused()) return;
        assert client.player != null;
        if(isEnabled) {
            if(!shootingMarks) {
                float slowdownMultiplier = 20f/client.world.getTickManager().getTickRate();
                float decreaseAmount = DeadeyeMod.CONFIG.server.deadeyeIdleConsumeAmount() * slowdownMultiplier;

                if(playerData.deadeyeMeter > 0) playerData.deadeyeMeter = MathHelper.clamp(playerData.deadeyeMeter - decreaseAmount, 0.0f, getMaxMeter(3));
                else playerData.deadeyeCore = MathHelper.clamp(playerData.deadeyeCore - decreaseAmount, 0.0f, 80.0f);
                calculateDeadeyeEnding();
            }
        }
    }

    public static void calculateDeadeyeEnding() {
        deadeyeEnding = MathHelper.clamp(1f - ((playerData.deadeyeCore/20f)+(playerData.deadeyeMeter/20f)), 0f, 1f);
    }

    public static float getMaxMeter(int tonicLevel) {
        return (playerData.deadeyeLevel*10)+(tonicLevel*20);
    }

    public static void deadeyeMeterUpdate(DeadeyeMeterPayload payload, ClientPlayNetworking.Context context) {
        playerData.deadeyeMeter = MathHelper.clamp(payload.amount(),0f, getMaxMeter(3));
    }

    public static void deadeyeCoreUpdate(DeadeyeCorePayload payload, ClientPlayNetworking.Context context) {
        playerData.deadeyeCore = MathHelper.clamp(payload.amount(),0f,80f);
    }

    public static void deadeyeLevelUpdate(DeadeyeLevelPayload payload, ClientPlayNetworking.Context context) {
        playerData.deadeyeLevel = MathHelper.clamp(payload.level(),0,10);
    }

    public static void deadeyeSkillUpdate(DeadeyeSkillPayload payload, ClientPlayNetworking.Context context) {
        playerData.deadeyeSkill = MathHelper.clamp(payload.skill(), 0, 3);
    }

    public static void receiveInitialSync(InitialSyncPayload payload, ClientPlayNetworking.Context context) {
        playerData.deadeyeMeter = payload.deadeyeMeter();
        playerData.deadeyeCore = payload.deadeyeCore();
        playerData.deadeyeLevel = payload.deadeyeLevel();
    }

    public static void disconnect(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient client) {
        if(isEnabled) setDeadeye(DeadeyeMod.DeadeyeStatus.DISABLED);
    }
}
