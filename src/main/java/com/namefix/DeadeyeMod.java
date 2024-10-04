package com.namefix;

import com.namefix.deadeye.DeadeyeServer;
import com.namefix.handlers.CommandHandler;
import com.namefix.handlers.SoundHandler;
import com.namefix.network.DeadeyeNetworking;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadeyeMod implements ModInitializer {
	public static final String MOD_ID = "deadeye-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final com.namefix.DeadeyeModConfig CONFIG = com.namefix.DeadeyeModConfig.createAndLoad();

	@Override
	public void onInitialize() {
		LOGGER.info("People don't forget. Nothing gets forgiven.");

		CommandHandler.initialize();

		ServerTickEvents.END_SERVER_TICK.register(DeadeyeServer::onTick);
		ServerLivingEntityEvents.AFTER_DEATH.register(DeadeyeServer::awardDeadeyeMeter);
		ServerPlayConnectionEvents.JOIN.register(DeadeyeServer::onPlayerConnect);
		ServerPlayConnectionEvents.DISCONNECT.register(DeadeyeServer::onPlayerDisconnect);

		DeadeyeNetworking.registerC2SPackets();
		ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_TOGGLE, DeadeyeServer::deadeyeToggle);
		ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_SHOOT, DeadeyeServer::spawnDeadeyeProjectile);
		ServerPlayNetworking.registerGlobalReceiver(DeadeyeNetworking.DEADEYE_MARKING, DeadeyeServer::updateMarkingStatus);

		DeadeyeServer.initializeBowProperties();
	}
}