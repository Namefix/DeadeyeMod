package com.namefix;

import com.namefix.deadeye.Deadeye;
import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.handlers.ConfigHandler;
import com.namefix.handlers.KeybindHandler;
import com.namefix.integrations.PointBlankIntegration;
import com.namefix.network.DeadeyeNetworking;
import com.namefix.network.payload.ReloadConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

        DeadeyeNetworking.registerS2CPackets();
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.RELOAD_CONFIG, (ReloadConfigPayload payload, ClientPlayNetworking.Context context) -> {ConfigHandler.ReloadConfigClient();});
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_METER, Deadeye::deadeyeMeterUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.INITIAL_SYNC, Deadeye::receiveInitialSync);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_FORCE, Deadeye::deadeyeForceUpdate);
        ClientPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_FORCE_SHOOT, Deadeye::deadeyeForceShoot);

        if(FabricLoader.getInstance().isModLoaded("pointblank")) PointBlankIntegration.initialize();
    }
}
