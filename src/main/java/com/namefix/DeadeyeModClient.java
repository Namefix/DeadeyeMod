package com.namefix;

import com.namefix.deadeye.Deadeye;
import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.handlers.CommandHandler;
import com.namefix.handlers.KeybindHandler;
import com.namefix.integrations.PointBlankIntegration;
import com.namefix.network.DeadeyeNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;

public class DeadeyeModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeybindHandler.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(Deadeye::deadeyeListener);
        ClientTickEvents.END_CLIENT_TICK.register(Deadeye::tick);
        WorldRenderEvents.START.register(Deadeye::render);

        HudRenderCallback.EVENT.register(DeadeyeEffects::renderGraphics);
        ShaderEffectRenderCallback.EVENT.register(DeadeyeEffects::renderShader);

        DeadeyeNetworking.initializeClientReceivers();

        Deadeye.initializeBowProperties();
        if(FabricLoader.getInstance().isModLoaded("pointblank")) PointBlankIntegration.initialize();

        CommandHandler.initializeClient();
    }
}
