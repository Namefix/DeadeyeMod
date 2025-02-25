package com.namefix.deadeye;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.DeadeyeMod;
import com.namefix.data.DeadeyeSoundProfile;
import com.namefix.sound.SoundBackgroundLoop;
import com.namefix.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.IntStream;

public class DeadeyeEffects {
    DeadeyeEffects() {}

    // load every meter related textures
    private static final Identifier DEADEYE_CORE_BG = Identifier.of(DeadeyeMod.MOD_ID, "textures/deadeye-core/core-background.png");
    private static final List<Identifier> DEADEYE_CORE = IntStream.rangeClosed(1, 16)
            .mapToObj(i -> Identifier.of(DeadeyeMod.MOD_ID, String.format("textures/deadeye-core/core%02d.png", i)))
            .toList();
    private static final List<Identifier> DEADEYE_METER_TRACK = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> Identifier.of(DeadeyeMod.MOD_ID, String.format("textures/meter-track/track%02d.png", i)))
            .toList();
    private static final List<Identifier> DEADEYE_METER = IntStream.rangeClosed(1, 100)
            .mapToObj(i -> Identifier.of(DeadeyeMod.MOD_ID, String.format("textures/meter/meter%02d.png", i)))
            .toList();

    // load every lightleak texture
    private static final List<Identifier> DEADEYE_LIGHTLEAK = IntStream.rangeClosed(1, 15)
            .mapToObj(i -> Identifier.of(DeadeyeMod.MOD_ID, String.format("textures/lightleak/lightleak%02d.png", i)))
            .toList();

    // meter variables
    public enum MeterPosition {
        NEAR_HOTBAR,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        NONE
    }

    private static final List<Vector3f> meterFortification = Lists.newArrayList(
            new Vector3f(1f, 0.969f, 0.776f),
            new Vector3f(1f, 0.969f, 0.659f),
            new Vector3f(0.976f, 0.925f, 0.412f)
    );

    public static int meterX = 0;
    public static int meterY = 0;
    public static int meterSize = 16;

    private static int meterCoreLastIndex = 0;
    private static boolean meterCoreEffect = false;
    private static float meterCoreEffectTime = 0.0f;

    private static float meterCoreLastAmount = 0.0f;
    private static float meterCoreBlink = 0.0f;

    private static float meterLastAmount = 0.0f;
    private static float meterBlink = 0.0f;

    // lightleak variables
    public static long lightleakTimer = 0;
    public static int lightleakStatus = 0;
    public static boolean lightleakDirection = false;

    // sounds
    static SoundBackgroundLoop soundBackground;
    static SoundBackgroundLoop soundBackground2;

    // heartbeat and clock tick variables
    static boolean heartbeat = false;
    static long lastHeartbeat = System.currentTimeMillis();
    static int heartbeatInDuration = 1150;
    static int heartbeatOutDuration = 350;

    private static final Identifier DEADEYE_MARK = Identifier.of(DeadeyeMod.MOD_ID, "textures/cross.png");
    static int markSize = DeadeyeMod.CONFIG.client.deadeyeMarkSize();

    // shader values
    public static float deadeyeFade = 0.0f;

    public static float tonicDuration = 0.0f;

    // Processing heartbeats
    public static void heartbeatTick() {
        if(!DeadeyeClient.isEnabled || MinecraftClient.getInstance().isPaused()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        DeadeyeSoundProfile profile = DeadeyeProfiles.getSelectedSoundProfile();
        assert client.player != null;
        if(!heartbeat) {    // beat in
            if(System.currentTimeMillis() - lastHeartbeat > heartbeatInDuration-(heartbeatInDuration*(DeadeyeClient.deadeyeEnding/1.5))) {
                client.player.playSound(profile.heartbeatInSound, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f);
                lastHeartbeat = System.currentTimeMillis();
                heartbeat = true;
            }
        } else {            // beat out
            if(System.currentTimeMillis() - lastHeartbeat > heartbeatOutDuration-(heartbeatOutDuration*(DeadeyeClient.deadeyeEnding/1.5))) {
                client.player.playSound(profile.heartbeatOutSound, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f);
                lastHeartbeat = System.currentTimeMillis();
                heartbeat = false;
            }
        }
    }

    public static void updateVariables(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        DeadeyeShader.DeadeyeShaderType shaderType = DeadeyeMod.CONFIG.client.deadeyeStyle();
        int increment = shaderType.equals(DeadeyeShader.DeadeyeShaderType.RDR2) ? 16 : 8;

        if(DeadeyeClient.isEnabled) deadeyeFade = MathHelper.clamp(deadeyeFade + (client.getLastFrameDuration() / 20.0f)*increment, 0.0f, 1.0f);
        else {
            if(!shaderType.equals(DeadeyeShader.DeadeyeShaderType.RDR1)) deadeyeFade = MathHelper.clamp(deadeyeFade - (client.getLastFrameDuration() / 20.0f)*increment, 0.0f, 1.0f);
            else deadeyeFade = 0.0f;
        }

        tonicDuration = MathHelper.clamp(tonicDuration-(client.getLastFrameDuration() / 20.0f), 0.0f, 1.0f);
    }

    // Update visual and sound effects after deadeye status changes
    public static void updateEffects(DeadeyeMod.DeadeyeStatus status) {
        MinecraftClient client = MinecraftClient.getInstance();
        DeadeyeSoundProfile profile = DeadeyeProfiles.getSelectedSoundProfile();
        DeadeyeShader.DeadeyeShaderType shaderType = DeadeyeMod.CONFIG.client.deadeyeStyle();

        if(status == DeadeyeMod.DeadeyeStatus.ENABLED) {
            client.player.playSound(profile.enterSound, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f);

            lightleakDirection = client.player.getRandom().nextBoolean();
            lightleakTimer = System.currentTimeMillis();
            lightleakStatus = 0;

            soundBackground = new SoundBackgroundLoop(profile.backgroundSound, SoundCategory.AMBIENT, client.player, (DeadeyeMod.CONFIG.client.deadeyeVolume()/100)/2, true);
            client.getSoundManager().play(soundBackground);
            soundBackground2 = new SoundBackgroundLoop(profile.background2Sound, SoundCategory.AMBIENT, client.player, (DeadeyeMod.CONFIG.client.deadeyeVolume()/100)/20, false);
            client.getSoundManager().play(soundBackground2);

            DeadeyeShader.loadDeadeyeProcessor(DeadeyeShader.ShaderType.DEADEYE);
        } else if(status == DeadeyeMod.DeadeyeStatus.DISABLED || status == DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY) {
            client.player.playSound(profile.exitSound, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f);
            if(status == DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY && shaderType.equals(DeadeyeShader.DeadeyeShaderType.RDR2)) client.player.playSound(profile.exitEmptySound, (DeadeyeMod.CONFIG.client.deadeyeVolume()/100)/20, 1.0f);

            soundBackground.setDone();
            soundBackground2.setDone();
        }
    }

    public static void startTonicEffect() {
        DeadeyeEffects.tonicDuration = 1.0f;
        DeadeyeShader.loadDeadeyeProcessor(DeadeyeShader.ShaderType.TONIC);
    }

    public static void renderGraphics(DrawContext drawContext, float v) {
        if(DeadeyeMod.CONFIG.client.deadeyeStyle().equals(DeadeyeShader.DeadeyeShaderType.RDR2) && !DeadeyeMod.CONFIG.client.disableDeadeyeEffects() && !DeadeyeMod.CONFIG.client.disableLightleakEffect()) renderLightleak(drawContext);

        if(DeadeyeClient.isEnabled) renderMarks(drawContext);

        if(!MinecraftClient.getInstance().options.hudHidden && DeadeyeMod.CONFIG.client.meterPosition() != MeterPosition.NONE && DeadeyeClient.playerData.deadeyeSkill > 0) {
            Vector2i meterCoords = getMeterCoordinates(drawContext, DeadeyeMod.CONFIG.client.meterPosition());
            meterX = meterCoords.x;
            meterY = meterCoords.y;
            renderCore(drawContext);
            renderMeter(drawContext);
            drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static void renderMarks(DrawContext drawContext) {
        DeadeyeClient.marks.forEach((mark) -> {
            mark.renderTick++;
            markSize = DeadeyeMod.CONFIG.client.deadeyeMarkSize();
            Vec3d markPos = Utils.worldSpaceToScreenSpace(mark.getCurrentOffset());
            if (!Utils.screenSpaceCoordinateIsVisible(markPos)) return;

            if(mark.renderTick < 10) drawContext.setShaderColor(1f, 1f, 1f, 1.0f);
            else drawContext.setShaderColor(0.78f, 0.09f, 0.09f, 1.0f);

            int sizeModifier = 0;
            if(mark.renderTick < 5) sizeModifier = markSize;
            else if(mark.renderTick < 10) sizeModifier = markSize/2;

            int posX = (int) Math.round(markPos.x) - markSize/2-sizeModifier/2;
            int posY = (int) Math.round(markPos.y) - markSize/2-sizeModifier/2;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            drawContext.drawTexture(
                    DEADEYE_MARK, posX, posY, markSize+sizeModifier, markSize+sizeModifier,
                    0, 0, 64, 64, 64, 64
            );

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        });
        drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void renderLightleak(DrawContext drawContext) {
        if (DeadeyeClient.isEnabled) {
            if (lightleakStatus == 15) return;
            int width = drawContext.getScaledWindowWidth();
            int height = drawContext.getScaledWindowHeight();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            drawContext.getMatrices().push();
            drawContext.getMatrices().translate((float) width / 2, (float) height / 2, 0.0F);
            drawContext.getMatrices().scale(1.0F, 1.0F, 1.0F);
            drawContext.getMatrices().translate((float) -width / 2, (float) -height / 2, 0.0F);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawContext.drawTexture(
                    DEADEYE_LIGHTLEAK.get(lightleakStatus), 0, 0, -90, 0, 0, width, height, lightleakDirection ? -width : width, height
            );
            drawContext.getMatrices().pop();

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();

            if (System.currentTimeMillis() - lightleakTimer > 40) {
                lightleakStatus = MathHelper.clamp(lightleakStatus + 1, 0, 15);
                lightleakTimer = System.currentTimeMillis();
            }
        }
    }

    private static void renderCore(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        drawContext.drawTexture(
                DEADEYE_CORE_BG, meterX, meterY, meterSize, meterSize, 0, 0, meterSize, meterSize, meterSize, meterSize
        );

        if(meterCoreLastAmount > 20f) {
            float currentCore = DeadeyeClient.playerData.deadeyeCore;

            if((meterCoreLastAmount >= 60f && currentCore < 60f) ||
                (meterCoreLastAmount >= 40f && currentCore < 40f) ||
                (currentCore < 20f)) {
                meterCoreBlink = 1.0f;
            }
        }

        meterCoreLastAmount = DeadeyeClient.playerData.deadeyeCore;

        if(meterCoreBlink > 0.0f) meterCoreBlink -= (client.getLastFrameDuration() / 20.0f)*4;
        if(meterCoreBlink > 0.75f || meterCoreBlink < 0.50f && meterCoreBlink > 0.25f) return;

        int coreIndex = MathHelper.clamp(Math.round(DeadeyeClient.playerData.deadeyeCore), 0, 15);
        if(coreIndex < 4) drawContext.setShaderColor(0.8f, 0.075f, 0.024f, 1.0f);
        else {
            Vector3f color = getCoreColor();
            drawContext.setShaderColor(color.x, color.y, color.z, 1.0f);
        }

        if(meterCoreEffect || meterCoreEffectTime < 0.0f) meterCoreEffectTime += client.getLastFrameDuration() / 20.0f;
        if(meterCoreEffectTime > 1.0f) {
            meterCoreEffect = false;
            meterCoreEffectTime = -1.0f;
        }

        if(meterCoreLastIndex != coreIndex && !meterCoreEffect) {
            if(meterCoreLastIndex > coreIndex && meterCoreEffectTime >= 0.0f) {
                meterCoreEffect = true;
                meterCoreEffectTime = 0.0f;
            }
        }

        meterCoreLastIndex = coreIndex;
        float progress = meterCoreEffectTime*16;
        if(progress > 1.0f) meterCoreEffect = false;
        float scale = 1.0f + (meterCoreEffect ? (progress < 0.5f
                ? -0.1f * (progress / 0.5f)
                : -0.1f * (1.0f - (progress - 0.5f) / 0.5f)) : 0f);
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(meterX + meterSize / 2.0f, meterY + meterSize / 2.0f, 0);
        drawContext.getMatrices().scale(scale, scale, 1.0f);
        drawContext.getMatrices().translate(-meterSize / 2.0f, -meterSize / 2.0f, 0);

        // Draw your texture
        drawContext.drawTexture(DEADEYE_CORE.get(coreIndex), 0, 0, -90, 0, 0, meterSize, meterSize, meterSize, meterSize);

        drawContext.getMatrices().pop();

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void renderMeter(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        float deadeyeMeter = DeadeyeClient.playerData.deadeyeMeter;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        if(DeadeyeClient.playerData.deadeyeLevel > 0) {
            drawContext.setShaderColor(0.33f, 0.31f, 0.31f, 1.0f);
            drawContext.drawTexture(
                    DEADEYE_METER_TRACK.get(MathHelper.clamp(DeadeyeClient.playerData.deadeyeLevel - 1, 0, 9)), meterX, meterY, meterSize, meterSize, 0, 0, meterSize, meterSize, meterSize, meterSize
            );
        }

        boolean tonic = false;
        if(meterLastAmount > DeadeyeClient.getMaxMeter(0)) {
            tonic = true;
            if((meterLastAmount >= DeadeyeClient.getMaxMeter(2) && deadeyeMeter < DeadeyeClient.getMaxMeter(2)) ||
                (meterLastAmount >= DeadeyeClient.getMaxMeter(1) && deadeyeMeter < DeadeyeClient.getMaxMeter(1)) ||
                (deadeyeMeter < DeadeyeClient.getMaxMeter(0))) {
                meterBlink = 1.0f;
            }
        }
        if(deadeyeMeter > DeadeyeClient.getMaxMeter(0) && deadeyeMeter > meterLastAmount) {
            meterBlink = 1.0f;
        }

        meterLastAmount = deadeyeMeter;

        if(meterBlink > 0.0f) meterBlink -= (client.getLastFrameDuration() / 20.0f)*4;
        if(meterBlink > 0.75f || meterBlink < 0.50f && meterBlink > 0.25f) return;

        Vector3f color = getMeterColor();
        drawContext.setShaderColor(color.x, color.y, color.z, 1.0f);

        if(Math.round(deadeyeMeter) > 0) {
            int meterIndex = tonic ? 99 : MathHelper.clamp(Math.round(deadeyeMeter), 0, 99);
            drawContext.drawTexture(
                    DEADEYE_METER.get(meterIndex), meterX, meterY, meterSize, meterSize, 0, 0, meterSize, meterSize, meterSize, meterSize
            );
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static Vector3f getMeterColor() {
        if(DeadeyeClient.playerData.deadeyeMeter > DeadeyeClient.getMaxMeter(2)) return meterFortification.get(meterFortification.size()-1);
        else if(DeadeyeClient.playerData.deadeyeMeter > DeadeyeClient.getMaxMeter(1)) return meterFortification.get(1);
        else if(DeadeyeClient.playerData.deadeyeMeter > DeadeyeClient.getMaxMeter(0)) return meterFortification.get(0);
        else return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    private static Vector3f getCoreColor() {
        if(DeadeyeClient.playerData.deadeyeCore > 60) return meterFortification.get(meterFortification.size()-1);
        else if(DeadeyeClient.playerData.deadeyeCore > 40) return meterFortification.get(1);
        else if(DeadeyeClient.playerData.deadeyeCore > 20) return meterFortification.get(0);
        else return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    private static Vector2i getMeterCoordinates(DrawContext ctx, MeterPosition pos) {
        switch(pos) {
            case TOP_LEFT -> {
                return new Vector2i(4, 4);
            }
            case TOP_RIGHT -> {
                return new Vector2i(ctx.getScaledWindowWidth()-20, 4);
            }
            case BOTTOM_LEFT -> {
                return new Vector2i(4, ctx.getScaledWindowHeight()-20);
            }
            case BOTTOM_RIGHT -> {
                return new Vector2i(ctx.getScaledWindowWidth()-20, ctx.getScaledWindowHeight()-20);
            }
            case NEAR_HOTBAR -> {
                return new Vector2i((ctx.getScaledWindowWidth()/4)-20, ctx.getScaledWindowHeight()-20);
            }
        }
        return new Vector2i(0, 0);
    }
}