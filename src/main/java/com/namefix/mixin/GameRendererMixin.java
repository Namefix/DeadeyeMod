package com.namefix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.deadeye.DeadeyeShader;
import com.namefix.utils.Utils;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", shift = At.Shift.AFTER))
    public void renderDeadeyeShaders(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        PostEffectProcessor deadeyeProcessor = DeadeyeShader.getDeadeyeProcessor();
        PostEffectProcessor tonicProcessor = DeadeyeShader.getTonicProcessor();

        // deadeye shader
        if(DeadeyeEffects.deadeyeFade == 0.0f && deadeyeProcessor != null) DeadeyeShader.clearDeadeyeProcessor();
        if(deadeyeProcessor != null && DeadeyeEffects.deadeyeFade > 0.0f && !DeadeyeMod.CONFIG.client.disableDeadeyeEffects()) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            deadeyeProcessor.setUniforms("TickDelta", tickCounter.getLastFrameDuration());
            deadeyeProcessor.setUniforms("VignetteStrength", DeadeyeMod.CONFIG.client.deadeyeVignetteStrength());
            deadeyeProcessor.setUniforms("DeadeyeEndValue", DeadeyeClient.deadeyeEnding);
            deadeyeProcessor.setUniforms("Fade", DeadeyeEffects.deadeyeFade);
            deadeyeProcessor.render(tickCounter.getLastFrameDuration());
        }

        // tonic shader
        if(DeadeyeEffects.tonicDuration == 0.0f && tonicProcessor != null) DeadeyeShader.clearTonicProcessor();
        if(tonicProcessor != null && DeadeyeEffects.tonicDuration > 0.0f) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            tonicProcessor.setUniforms("TickDelta", tickCounter.getLastFrameDuration());
            tonicProcessor.setUniforms("TonicDuration", DeadeyeEffects.tonicDuration);
            tonicProcessor.render(tickCounter.getLastFrameDuration());
        }
    }

    @WrapOperation(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
    private void deadeye_postWorldRender(WorldRenderer instance, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, Operation<Void> original) {
        original.call(instance, tickCounter, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f, matrix4f2);
        MatrixStack matrix = new MatrixStack();
        matrix.multiplyPositionMatrix(matrix4f);

        Utils.lastProjMat.set(RenderSystem.getProjectionMatrix());
        Utils.lastModMat.set(RenderSystem.getModelViewMatrix());
        Utils.lastWorldSpaceMatrix.set(matrix.peek().getPositionMatrix());
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, Utils.lastViewport);
    }
}
