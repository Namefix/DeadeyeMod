package com.namefix;

import com.namefix.deadeye.DeadeyeProfiles;
import com.namefix.deadeye.DeadeyeServer;
import com.namefix.handlers.*;
import com.namefix.integrations.PointBlankIntegration;
import com.namefix.network.DeadeyeNetworking;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadeyeMod implements ModInitializer {
	public static final String MOD_ID = "deadeye-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final com.namefix.DeadeyeModConfig CONFIG = com.namefix.DeadeyeModConfig.createAndLoad();

	public enum TargetingInteractionType {
		DEFAULT,
		BOW,
		THROWABLE,
		POINT_BLANK_GUN
	}

	public enum DeadeyeStatus {
		ENABLED,
		DISABLED,
		EMPTY,
		DISABLED_EMPTY,
	}

	@Override
	public void onInitialize() {
		LOGGER.info("People don't forget. Nothing gets forgiven.");

		SoundHandler.initialize();
		CommandHandler.initialize();
		GameruleHandler.initialize();
		ItemHandler.initialize();
		FeatureHandler.initialize();
		DeadeyeProfiles.initialize();

		ServerTickEvents.END_SERVER_TICK.register(DeadeyeServer::onTick);
		ServerLivingEntityEvents.AFTER_DEATH.register(DeadeyeServer::deadeyeMeterKillReward);
		ServerPlayConnectionEvents.JOIN.register(DeadeyeServer::onPlayerConnect);
		ServerPlayConnectionEvents.DISCONNECT.register(DeadeyeServer::onPlayerDisconnect);

		DeadeyeNetworking.initializeServerPayloads();
		DeadeyeNetworking.initializeClientPayloads();
		DeadeyeNetworking.initializeServerReceivers();

		if(FabricLoader.getInstance().isModLoaded("pointblank")) PointBlankIntegration.initialize();
	}
}