package com.namefix.deadeye;

import com.google.gson.JsonSyntaxException;
import com.namefix.DeadeyeMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class DeadeyeShader {
    public enum ShaderType {
        DEADEYE,
        TONIC
    }

    public enum DeadeyeShaderType {
        RDR2,
        RDR1,
    }

    private static final Identifier DEADEYE_PROCESSOR_ID = Identifier.of(DeadeyeMod.MOD_ID, "shaders/post/deadeye.json");
    private static final Identifier DEADEYE_REDEMPTION_PROCESSOR_ID = Identifier.of(DeadeyeMod.MOD_ID, "shaders/post/deadeye_redemption.json");
    private static PostEffectProcessor DEADEYE_PROCESSOR;

    private static final Identifier TONIC_PROCESSOR_ID = Identifier.of(DeadeyeMod.MOD_ID, "shaders/post/tonic.json");
    private static PostEffectProcessor TONIC_PROCESSOR;

    public static void loadDeadeyeProcessor(ShaderType type) {
        if(type == ShaderType.DEADEYE) {
            if(DeadeyeMod.CONFIG.client.deadeyeStyle().equals(DeadeyeShaderType.RDR2)) DEADEYE_PROCESSOR = loadProcessor(DEADEYE_PROCESSOR_ID);
            else DEADEYE_PROCESSOR = loadProcessor(DEADEYE_REDEMPTION_PROCESSOR_ID);
        } else {
            TONIC_PROCESSOR = loadProcessor(TONIC_PROCESSOR_ID);
        }
    }

    public static PostEffectProcessor loadProcessor(Identifier id) {
        MinecraftClient client = MinecraftClient.getInstance();
        PostEffectProcessor processor;

        try {
            processor = new PostEffectProcessor(client.getTextureManager(), client.getResourceManager(), client.getFramebuffer(), id);
            processor.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
            return processor;
        } catch (IOException e) {
            DeadeyeMod.LOGGER.warn("Failed to load shader: {}", id, e);
        } catch (JsonSyntaxException e) {
            DeadeyeMod.LOGGER.warn("Failed to parse shader: {}", id, e);
        }

        return null;
    }

    public static void clearDeadeyeProcessor() {
        if (DEADEYE_PROCESSOR != null) {
            DEADEYE_PROCESSOR.close();
        }
        DEADEYE_PROCESSOR = null;
    }

    public static void clearTonicProcessor() {
        if (TONIC_PROCESSOR != null) {
            TONIC_PROCESSOR.close();
        }
        TONIC_PROCESSOR = null;
    }

    public static PostEffectProcessor getDeadeyeProcessor() {
        return DEADEYE_PROCESSOR;
    }

    public static PostEffectProcessor getTonicProcessor() {
        return TONIC_PROCESSOR;
    }
}
