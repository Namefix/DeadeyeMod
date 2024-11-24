package com.namefix;

import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.handlers.BlockHandler;
import com.namefix.handlers.CommandHandler;
import com.namefix.handlers.KeybindHandler;
import com.namefix.integrations.PointBlankIntegration;
import com.namefix.network.DeadeyeNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;

public class DeadeyeModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeybindHandler.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(DeadeyeClient::deadeyeListener);
        ClientTickEvents.END_CLIENT_TICK.register(DeadeyeClient::tick);
        ClientPlayConnectionEvents.DISCONNECT.register(DeadeyeClient::disconnect);

        WorldRenderEvents.START.register(DeadeyeClient::render);

        HudRenderCallback.EVENT.register(DeadeyeEffects::renderGraphics);
        ShaderEffectRenderCallback.EVENT.register(DeadeyeEffects::renderShader);

        DeadeyeNetworking.initializeClientReceivers();

        DeadeyeClient.initializeBowProperties();
        if(FabricLoader.getInstance().isModLoaded("pointblank")) PointBlankIntegration.initialize();

        CommandHandler.initializeClient();

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BlockHandler.TOBACCO_CROP);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BlockHandler.WILD_TOBACCO);
    }
}
