package com.namefix.deadeye;

import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.DeadeyeMod;
import com.namefix.handlers.SoundHandler;
import com.namefix.sound.SoundBackgroundLoop;
import me.x150.renderer.util.RendererUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

import java.util.List;
import java.util.stream.IntStream;

public class DeadeyeEffects {
    DeadeyeEffects() {}

    // load every lightleak texture
    private static final List<Identifier> DEADEYE_LIGHTLEAK = IntStream.rangeClosed(1, 15)
            .mapToObj(i -> Identifier.of(DeadeyeMod.MOD_ID, String.format("textures/lightleak/lightleak%02d.png", i)))
            .toList();

    public static long lightleakTimer = 0;
    public static int lightleakStatus = 0;
    public static boolean lightleakDirection = false;

    static SoundBackgroundLoop soundBackground;
    static SoundBackgroundLoop soundBackground2;

    static boolean heartbeat = false;
    static long lastHeartbeat = System.currentTimeMillis();
    static int heartbeatInDuration = 1150;
    static int heartbeatOutDuration = 350;

    private static final Identifier DEADEYE_MARK = Identifier.of(DeadeyeMod.MOD_ID, "textures/cross.png");
    static int markSize = DeadeyeMod.CONFIG.client.deadeyeMarkSize();

    private static final ManagedShaderEffect DEADEYE_SHADER = ShaderEffectManager.getInstance().manage(Identifier.of(DeadeyeMod.MOD_ID, "shaders/post/deadeye.json"));

    // Processing heartbeats
    public static void heartbeatTick() {
        if(!DeadeyeClient.isEnabled || MinecraftClient.getInstance().isPaused()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        if(!heartbeat) {    // beat in
            if(System.currentTimeMillis() - lastHeartbeat > heartbeatInDuration-(heartbeatInDuration*(DeadeyeClient.deadeyeEnding/1.5))) {
                client.player.playSound(SoundHandler.DEADEYE_JOHN_HEARTBEAT_IN, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f);
                lastHeartbeat = System.currentTimeMillis();
                heartbeat = true;
            }
        } else {            // beat out
            if(System.currentTimeMillis() - lastHeartbeat > heartbeatOutDuration-(heartbeatOutDuration*(DeadeyeClient.deadeyeEnding/1.5))) {
                client.player.playSound(SoundHandler.DEADEYE_JOHN_HEARTBEAT_OUT, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f);
                lastHeartbeat = System.currentTimeMillis();
                heartbeat = false;
            }
        }
    }

    public static void updateEffects(DeadeyeMod.DeadeyeStatus status) {
        MinecraftClient client = MinecraftClient.getInstance();

        if(status == DeadeyeMod.DeadeyeStatus.ENABLED) {
            client.player.playSound(SoundHandler.DEADEYE_JOHN_ENTER, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f);

            lightleakDirection = client.player.getRandom().nextBoolean();
            lightleakTimer = System.currentTimeMillis();
            lightleakStatus = 0;

            soundBackground = new SoundBackgroundLoop(SoundHandler.DEADEYE_JOHN_BACKGROUND, SoundCategory.AMBIENT, client.player, (DeadeyeMod.CONFIG.client.deadeyeVolume()/100)/2, true);
            client.getSoundManager().play(soundBackground);
            soundBackground2 = new SoundBackgroundLoop(SoundHandler.DEADEYE_JOHN_BACKGROUND2, SoundCategory.AMBIENT, client.player, (DeadeyeMod.CONFIG.client.deadeyeVolume()/100)/20, false);
            client.getSoundManager().play(soundBackground2);
        } else if(status == DeadeyeMod.DeadeyeStatus.DISABLED || status == DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY) {
            client.player.playSound(SoundHandler.DEADEYE_JOHN_EXIT, DeadeyeMod.CONFIG.client.deadeyeVolume()/100, 1.0f);
            if(status == DeadeyeMod.DeadeyeStatus.DISABLED_EMPTY) client.player.playSound(SoundHandler.DEADEYE_JOHN_BACKGROUND2_END, (DeadeyeMod.CONFIG.client.deadeyeVolume()/100)/20, 1.0f);

            soundBackground.setDone();
            soundBackground2.setDone();
        }
    }

    public static void renderShader(float tickDelta) {
        if(DeadeyeClient.isEnabled && !DeadeyeMod.CONFIG.client.disableDeadeyeEffects()) {
            DEADEYE_SHADER.setUniformValue("TickDelta", tickDelta);
            DEADEYE_SHADER.setUniformValue("VignetteStrength", DeadeyeMod.CONFIG.client.deadeyeVignetteStrength());
            DEADEYE_SHADER.setUniformValue("DeadeyeEndValue", DeadeyeClient.deadeyeEnding);
            DEADEYE_SHADER.render(tickDelta);
        }
    }

    public static void renderGraphics(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if(!DeadeyeMod.CONFIG.client.disableDeadeyeEffects() && !DeadeyeMod.CONFIG.client.disableLightleakEffect()) renderLightleak(drawContext, renderTickCounter);

        if(DeadeyeClient.isEnabled) {
            // Render dead eye marks
            DeadeyeClient.marks.forEach((mark) -> {
                mark.renderTick++;
                markSize = DeadeyeMod.CONFIG.client.deadeyeMarkSize();
                Vec3d markPos = RendererUtils.worldSpaceToScreenSpace(mark.getCurrentOffset());
                if (!RendererUtils.screenSpaceCoordinateIsVisible(markPos)) return;

                if(mark.renderTick < 10) drawContext.setShaderColor(1f, 1f, 1f, 1.0f);
                else drawContext.setShaderColor(0.78f, 0.09f, 0.09f, 1.0f);

                int sizeModifier = 0;
                if(mark.renderTick < 5) sizeModifier = markSize;
                else if(mark.renderTick < 10) sizeModifier = markSize/2;

                int posX = (int) Math.round(markPos.x) - markSize/2-sizeModifier/2;
                int posY = (int) Math.round(markPos.y) - markSize/2-sizeModifier/2;

                drawContext.drawTexture(
                        DEADEYE_MARK, posX, posY, markSize+sizeModifier, markSize+sizeModifier,
                        0, 0, 64, 64, 64, 64
                );
            });
            drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static void renderLightleak(DrawContext drawContext, RenderTickCounter renderTickCounter) {
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
}