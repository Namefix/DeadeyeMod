package com.namefix.handlers;

import com.mojang.brigadier.context.CommandContext;
import com.namefix.data.PlayerSaveData;
import com.namefix.data.StateSaverAndLoader;
import com.namefix.network.payload.DeadeyeMeterPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandHandler {

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("deadeye")
                    .then(CommandManager.literal("reload").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        ConfigHandler.ReloadConfigServer(context.getSource().getServer());
                        context.getSource().sendMessage(Text.literal("Reloaded server config."));
                        return 1;
                    }))
                    .then(CommandManager.literal("clientreload").executes((CommandContext<ServerCommandSource> context) -> {
                        ConfigHandler.ReloadConfigClient();
                        context.getSource().sendMessage(Text.literal("Reloaded client config."));
                        return 1;
                    }))
                    .then(CommandManager.literal("refill").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        if(context.getSource().getPlayer() == null) return 0;
                        PlayerSaveData playerData = StateSaverAndLoader.getPlayerState(context.getSource().getPlayer());
                        playerData.deadeyeMeter = 100f;
                        ServerPlayNetworking.send(context.getSource().getPlayer(), new DeadeyeMeterPayload(100f));
                        context.getSource().sendMessage(Text.literal("Refilled your deadeye meter to 100."));
                        return 1;
                    }))
            );

        });
    }
}
