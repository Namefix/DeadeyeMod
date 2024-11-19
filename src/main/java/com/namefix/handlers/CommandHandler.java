package com.namefix.handlers;

import com.mojang.brigadier.context.CommandContext;
import com.namefix.data.PlayerSaveData;
import com.namefix.data.StateSaverAndLoader;
import com.namefix.network.payload.DeadeyeCorePayload;
import com.namefix.network.payload.DeadeyeMeterPayload;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CommandHandler {

    private static void refillDeadeye(CommandContext<ServerCommandSource> context, boolean core, boolean meter, boolean fortify) {
        PlayerSaveData playerData = StateSaverAndLoader.getPlayerState(context.getSource().getPlayer());
        StringBuilder message = new StringBuilder("Refilled");
        if(fortify) message.append(" and fortified ");
        message.append("your Deadeye ");
        if(core) {
            if(fortify) playerData.deadeyeCore = 80.0f;
            else playerData.deadeyeCore = 20.0f;
            ServerPlayNetworking.send(context.getSource().getPlayer(), new DeadeyeCorePayload(playerData.deadeyeCore));

            message.append("core");
            if(meter) message.append(" and ");
        }
        if(meter) {
            if(fortify) playerData.deadeyeMeter = 160f;
            else playerData.deadeyeMeter = 100.0f;
            ServerPlayNetworking.send(context.getSource().getPlayer(), new DeadeyeMeterPayload(playerData.deadeyeMeter));

            message.append("meter");
        }
        message.append(" to default values.");

        context.getSource().sendMessage(Text.literal(message.toString()));
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("deadeye")
                    .then(CommandManager.literal("reload").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        ConfigHandler.ReloadConfigServer(context.getSource().getServer());
                        context.getSource().sendMessage(Text.literal("Reloaded server config."));
                        return 1;
                    }))
                    .then(CommandManager.literal("refill").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        if(context.getSource().getPlayer() == null) return 0;
                        refillDeadeye(context, true, true, false);
                        return 1;
                    }))
                    .then(CommandManager.literal("refillcore").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        if(context.getSource().getPlayer() == null) return 0;
                        refillDeadeye(context, true, false, false);
                        return 1;
                    }))
                    .then(CommandManager.literal("refillmeter").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        if(context.getSource().getPlayer() == null) return 0;
                        refillDeadeye(context, false, true, false);
                        return 1;
                    }))
                    .then(CommandManager.literal("fortify").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        if(context.getSource().getPlayer() == null) return 0;
                        refillDeadeye(context, true, true, true);
                        return 1;
                    }))
                    .then(CommandManager.literal("fortifycore").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        if(context.getSource().getPlayer() == null) return 0;
                        refillDeadeye(context, true, false, true);
                        return 1;
                    }))
                    .then(CommandManager.literal("fortifymeter").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        if(context.getSource().getPlayer() == null) return 0;
                        refillDeadeye(context, false, true, true);
                        return 1;
                    }))
            );

        });
    }

    public static void initializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("deadeye-client")
                .then(ClientCommandManager.literal("reload").executes((CommandContext<FabricClientCommandSource> context) -> {
                    ConfigHandler.ReloadConfigClient();
                    context.getSource().sendFeedback(Text.literal("Reloaded client config."));
                    return 1;
                }))
            );
        });
    }
}
