package com.namefix.deadeye;

import com.namefix.DeadeyeMod;
import com.namefix.DeadeyeMod.TargetingInteractionType;
import com.namefix.data.PlayerSaveData;
import com.namefix.handlers.ConfigHandler;
import com.namefix.handlers.KeybindHandler;
import com.namefix.handlers.SoundHandler;
import com.namefix.integrations.PointBlankIntegration;
import com.namefix.network.payload.*;
import com.namefix.sound.SoundBackgroundLoop;
import com.namefix.utils.Utils;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeadeyeClient {
    private DeadeyeClient() {}
    public static PlayerSaveData playerData = new PlayerSaveData();

    public static boolean isEnabled = false;
    public static float deadeyeEnding = 0.0f;
    public static float deadeyeConsumeRate = DeadeyeMod.CONFIG.server.deadeyeIdleConsumeAmount();

    public static List<EntityType<?>> deadeyeMarkableEntities = ConfigHandler.LoadDeadeyeMarkableEntities();
    public static List<Item> deadeyeMarkingItems = ConfigHandler.LoadDeadeyeMarkingItems();

    static ArrayList<DeadeyeTarget> marks = new ArrayList<>();
    public static int markLimit = DeadeyeMod.CONFIG.server.maxMarks();
    public static float markFocusSpeed = DeadeyeMod.CONFIG.server.markFocusSpeed();

    public static boolean shootingMarks = false;
    static long lerpWait = 0;
    static long shootWait = 0;
    static long startLerpingTime = 0;
    static Item shootStartItem = null;

    static SoundBackgroundLoop soundBackground;
    static SoundBackgroundLoop soundBackground2;
    public static float soundVolume = DeadeyeMod.CONFIG.client.deadeyeVolume()/100;

    static boolean heartbeat = false;
    static long lastHeartbeat = System.currentTimeMillis();
    static int heartbeatInDuration = 1150;
    static int heartbeatOutDuration = 350;

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
            toggle();
        }

        while (KeybindHandler.keyDeadeyeMark.wasPressed()) mark(client);

        while (KeybindHandler.keyDeadeyeShootTargets.wasPressed() && !marks.isEmpty()) {
            assert client.player != null;
            startShootingTargets(client.player.getInventory().getMainHandStack().getItem());
        }
    }

    public static void render(WorldRenderContext worldRenderContext) {
        shootingTick(worldRenderContext);
        heartbeatTick();
    }

    // Processing heartbeats
    private static void heartbeatTick() {
        if(!isEnabled || MinecraftClient.getInstance().isPaused()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        if(!heartbeat) {    // beat in
            if(System.currentTimeMillis() - lastHeartbeat > heartbeatInDuration-(heartbeatInDuration*(deadeyeEnding/1.5))) {
                client.player.playSound(SoundHandler.DEADEYE_JOHN_HEARTBEAT_IN, soundVolume, 1.0f);
                lastHeartbeat = System.currentTimeMillis();
                heartbeat = true;
            }
        } else {            // beat out
            if(System.currentTimeMillis() - lastHeartbeat > heartbeatOutDuration-(heartbeatOutDuration*(deadeyeEnding/1.5))) {
                client.player.playSound(SoundHandler.DEADEYE_JOHN_HEARTBEAT_OUT, soundVolume, 1.0f);
                lastHeartbeat = System.currentTimeMillis();
                heartbeat = false;
            }
        }
    }

    // All logic related to shooting marked targets
    private static void shootingTick(WorldRenderContext worldRenderContext) {
        if(MinecraftClient.getInstance().isPaused() || !isEnabled) return;
        if(marks.isEmpty() || !shootingMarks) return;
        if (System.currentTimeMillis() < lerpWait) return;

        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

         ItemStack item = client.player.getMainHandStack();
        TargetingInteractionType targetingType;

        if(item == null) {
            toggle();
            return;
        }
        if(!item.getItem().equals(shootStartItem)) {
            toggle();
            return;
        }

        DeadeyeTarget mark = marks.getFirst();

        float pPitch = client.player.getPitch();
        float pYaw = client.player.getYaw();

        float interpolationFactor = markFocusSpeed * worldRenderContext.tickCounter().getLastFrameDuration();
        if(System.currentTimeMillis() - startLerpingTime > 3_000) interpolationFactor *= 4;
        if(PointBlankIntegration.getGunFiremode(item) == FireMode.AUTOMATIC) interpolationFactor *= 8;

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

            targetingType = getTargetingInteractionType(item);
            if(targetingType == TargetingInteractionType.BOW) {
                if(!client.player.isInCreativeMode() && !client.player.getInventory().contains(((RangedWeaponItem) item.getItem()).getProjectiles())) {
                    toggle();
                    return;
                }
            }
            if(targetingType == TargetingInteractionType.POINT_BLANK_GUN) {
                if(PointBlankIntegration.getGunAmmo(item) == 0) {
                    toggle();
                    return;
                }

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
            ClientPlayNetworking.send(new DeadeyeShootPayload(targetingType.toString(), mark.getCurrentOffset().toVector3f(), marks.size()-1<=0));

            marks.removeFirst();
            lerpWait = System.currentTimeMillis() + 100;
            shootWait = 0;
            startLerpingTime = System.currentTimeMillis();

            if (marks.isEmpty()) {
                toggle();
            }
        }
    }

    private static TargetingInteractionType getTargetingInteractionType(ItemStack item) {
        if(item.getItem() instanceof RangedWeaponItem) return TargetingInteractionType.BOW;
        if(item.getItem() instanceof ProjectileItem) return TargetingInteractionType.THROWABLE;
        if(PointBlankIntegration.isLoaded && item.getItem() instanceof GunItem) return TargetingInteractionType.POINT_BLANK_GUN;
        return TargetingInteractionType.DEFAULT;
    }

    private static void startShootingTargets(Item startItem) {
        shootStartItem = startItem;
        shootingMarks = true;
        startLerpingTime = System.currentTimeMillis();
        lerpWait = System.currentTimeMillis() + 250;

        ClientPlayNetworking.send(new DeadeyeShootingPayload(true));
    }

    // Marking targets
    public static void mark(MinecraftClient client) {
        if(DeadeyeClient.isEnabled && !shootingMarks) {
            if(marks.size() >= markLimit) return;
            assert client.player != null;
            ItemStack item = client.player.getMainHandStack();
            if(item == null) return;
            if(!deadeyeMarkingItems.contains(item.getItem())) return;
            TargetingInteractionType interactionType = TargetingInteractionType.DEFAULT;

            if(item.getItem() instanceof RangedWeaponItem ranged) {
                if(!client.player.isInCreativeMode() && !client.player.getInventory().contains(ranged.getProjectiles())) return;
                interactionType = TargetingInteractionType.BOW;
            }
            if(item.getItem() instanceof GunItem) {
                if(!PointBlankIntegration.canMarkTargets(item, marks.size()) && marks.isEmpty()) return;
                interactionType = TargetingInteractionType.POINT_BLANK_GUN;
            }

            double maxDistance = 1000;
            HitResult hit = Utils.raycastEntity(client.player, maxDistance);
            if(hit == null) return;

            EntityHitResult eHit = (EntityHitResult) hit;
            if (Objects.requireNonNull(hit.getType()) == HitResult.Type.ENTITY) {
                Entity ent = null;
                if(eHit.getEntity() instanceof LivingEntity) ent = eHit.getEntity();
                if(deadeyeMarkableEntities.contains(eHit.getEntity().getType())) ent = eHit.getEntity();
                if(ent == null) return;

                marks.add(new DeadeyeTarget(ent, hit.getPos()));
                client.player.playSound(SoundHandler.DEADEYE_ARTHUR_PAINT, soundVolume, 1.0f); // placeholder for now

                ClientPlayNetworking.send(new DeadeyeMarkingPayload(true));
            }
            if(interactionType == TargetingInteractionType.POINT_BLANK_GUN && marks.size() >= PointBlankIntegration.getGunAmmo(item)) startShootingTargets(client.player.getMainHandStack().getItem());
            if(marks.size() >= markLimit) startShootingTargets(client.player.getMainHandStack().getItem());
        }
    }

    // Toggling deadeye
    public static void toggle() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        if(!isEnabled) {
            client.player.playSound(SoundHandler.DEADEYE_JOHN_ENTER, soundVolume, 1.0f);

            soundBackground = new SoundBackgroundLoop(SoundHandler.DEADEYE_JOHN_BACKGROUND, SoundCategory.AMBIENT, client.player, soundVolume/2, true);
            client.getSoundManager().play(soundBackground);
            soundBackground2 = new SoundBackgroundLoop(SoundHandler.DEADEYE_JOHN_BACKGROUND2, SoundCategory.AMBIENT, client.player, soundVolume/20, false);
            client.getSoundManager().play(soundBackground2);

            isEnabled = true;
        }
        else {
            client.player.playSound(SoundHandler.DEADEYE_JOHN_EXIT, soundVolume, 1.0f);

            ClientPlayNetworking.send(new DeadeyeMarkingPayload(false));
            soundBackground.setDone();
            soundBackground2.setDone();
            if(playerData.deadeyeMeter == 0.0f) client.player.playSound(SoundHandler.DEADEYE_JOHN_BACKGROUND2_END, soundVolume/20, 1.0f);
            marks.clear();
            shootingMarks = false;
            startLerpingTime = 0;
            isEnabled = false;
        }
        ClientPlayNetworking.send(new DeadeyeTogglePayload(isEnabled));
    }

    public static void tick(MinecraftClient client) {
        if(MinecraftClient.getInstance().isPaused()) return;
        assert client.player != null;
        if(isEnabled) {
            if(client.player.isDead()) toggle();
            if(!shootingMarks) {
                playerData.deadeyeMeter = MathHelper.clamp(playerData.deadeyeMeter - deadeyeConsumeRate, 0.0f, 100.0f);
                deadeyeEnding = MathHelper.clamp(1f - (playerData.deadeyeMeter / 20f), 0f, 1f);
            }
            if(playerData.deadeyeMeter == 0.0f && !shootingMarks) {
                if(!marks.isEmpty()) startShootingTargets(client.player.getInventory().getMainHandStack().getItem());
                else toggle();
            }
        }
    }

    public static void deadeyeMeterUpdate(DeadeyeMeterPayload payload, ClientPlayNetworking.Context context) {
        playerData.deadeyeMeter = MathHelper.clamp(playerData.deadeyeMeter+payload.amount(),0f,100f);
    }

    public static void receiveInitialSync(InitialSyncPayload payload, ClientPlayNetworking.Context context) {
        playerData.deadeyeMeter = payload.deadeyeMeter();
    }

    public static void deadeyeForceUpdate(DeadeyeForceTogglePayload payload, ClientPlayNetworking.Context context) {
        if(payload.status() != isEnabled) toggle();
        playerData.deadeyeMeter = payload.meter();
    }

    public static void deadeyeForceShoot(DeadeyeForceShootPayload payload, ClientPlayNetworking.Context context) {
        startShootingTargets(context.player().getInventory().getMainHandStack().getItem());
    }
}
