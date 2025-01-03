package com.namefix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.namefix.DeadeyeMod;
import com.namefix.utils.PostEffectProcessorInterface;
import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.deadeye.DeadeyeShader;
import com.namefix.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", shift = At.Shift.AFTER))
    public void deadeyemod_renderDeadeyeShaders(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        PostEffectProcessor deadeyeProcessor = DeadeyeShader.getDeadeyeProcessor();
        PostEffectProcessor tonicProcessor = DeadeyeShader.getTonicProcessor();

        // deadeye shader
        if(DeadeyeEffects.deadeyeFade == 0.0f && deadeyeProcessor != null) DeadeyeShader.clearDeadeyeProcessor();
        if(deadeyeProcessor != null && DeadeyeEffects.deadeyeFade > 0.0f && !DeadeyeMod.CONFIG.client.disableDeadeyeEffects()) {
            PostEffectProcessorInterface deadeyeInterface = ((PostEffectProcessorInterface) deadeyeProcessor);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();

            deadeyeInterface.deadeyemod_setUniforms("TickDelta", client.getLastFrameDuration());
            deadeyeInterface.deadeyemod_setUniforms("VignetteStrength", DeadeyeMod.CONFIG.client.deadeyeVignetteStrength());
            deadeyeInterface.deadeyemod_setUniforms("DeadeyeEndValue", DeadeyeClient.deadeyeEnding);
            deadeyeInterface.deadeyemod_setUniforms("Fade", DeadeyeEffects.deadeyeFade);
            deadeyeProcessor.render(tickDelta);
        }

        // tonic shader
        if(DeadeyeEffects.tonicDuration == 0.0f && tonicProcessor != null) DeadeyeShader.clearTonicProcessor();
        if(tonicProcessor != null && DeadeyeEffects.tonicDuration > 0.0f) {
            PostEffectProcessorInterface tonicInterface = ((PostEffectProcessorInterface) tonicProcessor);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            tonicInterface.deadeyemod_setUniforms("TickDelta", client.getLastFrameDuration());
            tonicInterface.deadeyemod_setUniforms("TonicDuration", DeadeyeEffects.tonicDuration);
            tonicProcessor.render(tickDelta);
        }
    }

    @WrapOperation(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V"))
    private void deadeyemod_postWorldRender(WorldRenderer instance, MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, Operation<Void> original) {
        original.call(instance, matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix);

        Utils.lastProjMat.set(RenderSystem.getProjectionMatrix());
        Utils.lastModMat.set(RenderSystem.getModelViewMatrix());
        Utils.lastWorldSpaceMatrix.set(matrices.peek().getPositionMatrix());
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, Utils.lastViewport);
    }
}
