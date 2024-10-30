package com.namefix.deadeye;

import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.DeadeyeMod;
import me.x150.renderer.util.RendererUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
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

    private static final Identifier DEADEYE_MARK = Identifier.of(DeadeyeMod.MOD_ID, "textures/cross.png");
    static int markWidth = DeadeyeMod.CONFIG.client.deadeyeMarkSize();
    static int markHeight = DeadeyeMod.CONFIG.client.deadeyeMarkSize();;
    static int halfMarkWidth = markWidth / 2;
    static int halfMarkHeight = markHeight / 2;

    public static void setMarkSize(int size) {
        markWidth = size;
        markHeight = size;
        halfMarkWidth = markWidth / 2;
        halfMarkHeight = markHeight / 2;
    }

    private static final ManagedShaderEffect DEADEYE_SHADER = ShaderEffectManager.getInstance().manage(Identifier.of(DeadeyeMod.MOD_ID, "shaders/post/deadeye.json"));
    public static float vignetteStrength = DeadeyeMod.CONFIG.client.deadeyeVignetteStrength();
    public static boolean renderDisabled = DeadeyeMod.CONFIG.client.disableDeadeyeEffects();

    public static void renderShader(float tickDelta) {
        if(DeadeyeClient.isEnabled && !renderDisabled) {
            DEADEYE_SHADER.setUniformValue("TickDelta", tickDelta);
            DEADEYE_SHADER.setUniformValue("VignetteStrength", vignetteStrength);
            DEADEYE_SHADER.setUniformValue("DeadeyeEndValue", DeadeyeClient.deadeyeEnding);
            DEADEYE_SHADER.render(tickDelta);
        }
    }

    public static void renderGraphics(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if(!DeadeyeMod.CONFIG.client.disableDeadeyeEffects()) renderLightleak(drawContext, renderTickCounter);

        if(DeadeyeClient.isEnabled) {
            // Render dead eye marks
            DeadeyeClient.marks.forEach((mark) -> {
                mark.renderTick++;
                Vec3d markPos = RendererUtils.worldSpaceToScreenSpace(mark.getCurrentOffset());
                if (!RendererUtils.screenSpaceCoordinateIsVisible(markPos)) return;

                if(mark.renderTick < 10) drawContext.setShaderColor(1f, 1f, 1f, 1.0f);
                else drawContext.setShaderColor(0.78f, 0.09f, 0.09f, 1.0f);

                int sizeModifier = 0;
                if(mark.renderTick < 5) sizeModifier = markWidth;
                else if(mark.renderTick < 10) sizeModifier = markWidth/2;

                int posX = (int) Math.round(markPos.x) - halfMarkWidth-sizeModifier/2;
                int posY = (int) Math.round(markPos.y) - halfMarkHeight-sizeModifier/2;

                drawContext.drawTexture(
                        DEADEYE_MARK, posX, posY, markWidth+sizeModifier, markHeight+sizeModifier,
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