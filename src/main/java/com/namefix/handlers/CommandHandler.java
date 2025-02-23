package com.namefix.handlers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.namefix.data.PlayerSaveData;
import com.namefix.data.StateSaverAndLoader;
import com.namefix.deadeye.DeadeyeServer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CommandHandler {
    private static void setMeter(ServerPlayerEntity player, int amount) {
        PlayerSaveData playerData = StateSaverAndLoader.getPlayerState(player);
        if(amount < 0) amount = playerData.deadeyeLevel * 10;
        DeadeyeServer.setDeadeyeMeter(player, amount);
    }

    private static void setCore(ServerPlayerEntity player, int amount) {
        if(amount < 0) amount = 20;
        DeadeyeServer.setDeadeyeCore(player, amount);
    }

    private static void setLevel(ServerPlayerEntity player, int level) {
        if(level < 0) level = 5;
        DeadeyeServer.setDeadeyeLevel(player, level);
    }

    private static void setSkill(ServerPlayerEntity player, int skill) {
        if(skill < 0) skill = 0;
        DeadeyeServer.setDeadeyeSkill(player, skill);
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("deadeye")
                    .then(literal("reload").requires(source -> source.hasPermissionLevel(2)).executes((CommandContext<ServerCommandSource> context) -> {
                        ConfigHandler.ReloadConfigServer(context.getSource().getServer());
                        context.getSource().sendMessage(Text.literal("Reloaded server config."));
                        return 1;
                    }))
                    .then(literal("setmeter").requires(source -> source.hasPermissionLevel(2))
                            .then(argument("player", EntityArgumentType.player())
                                    .then(argument("amount", IntegerArgumentType.integer())
                                            .executes((CommandContext<ServerCommandSource> context) -> {
                                                if (context.getSource().getPlayer() == null) return 0;

                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                                setMeter(player, amount);
                                                context.getSource().sendFeedback(() -> Text.translatable("text.command.deadeye-mod.setmeter.success", player.getName().getString(), amount), true);

                                                return 1;
                                            })
                                    )
                                    .executes((CommandContext<ServerCommandSource> context) -> {
                                        if (context.getSource().getPlayer() == null) return 0;

                                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                        setMeter(player, -1);
                                        context.getSource().sendFeedback(() -> Text.translatable("text.command.deadeye-mod.setmeter.default", player.getName().getString()), true);

                                        return 1;
                                    })
                            )
                    )
                    .then(literal("setcore").requires(source -> source.hasPermissionLevel(2))
                            .then(argument("player", EntityArgumentType.player())
                                    .then(argument("amount", IntegerArgumentType.integer())
                                            .executes((CommandContext<ServerCommandSource> context) -> {
                                                if (context.getSource().getPlayer() == null) return 0;

                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                                setCore(player, amount);
                                                context.getSource().sendFeedback(() -> Text.translatable("text.command.deadeye-mod.setcore.success", player.getName().getString(), amount), true);

                                                return 1;
                                            })
                                    )
                                    .executes((CommandContext<ServerCommandSource> context) -> {
                                        if (context.getSource().getPlayer() == null) return 0;

                                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                        setCore(player, -1);
                                        context.getSource().sendFeedback(() -> Text.translatable("text.command.deadeye-mod.setcore.default", player.getName().getString()), true);

                                        return 1;
                                    })
                            )
                    )
                    .then(literal("setlevel").requires(source -> source.hasPermissionLevel(2))
                            .then(argument("player", EntityArgumentType.player())
                                    .then(argument("level", IntegerArgumentType.integer())
                                            .executes((CommandContext<ServerCommandSource> context) -> {
                                                if (context.getSource().getPlayer() == null) return 0;

                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                int level = IntegerArgumentType.getInteger(context, "level");
                                                setLevel(player, level);
                                                context.getSource().sendFeedback(() -> Text.translatable("text.command.deadeye-mod.setlevel.success", player.getName().getString(), level), true);

                                                return 1;
                                            })
                                    )
                                    .executes((CommandContext<ServerCommandSource> context) -> {
                                        if (context.getSource().getPlayer() == null) return 0;

                                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                        setLevel(player, -1);
                                        context.getSource().sendFeedback(() -> Text.translatable("text.command.deadeye-mod.setlevel.success", player.getName().getString(), 5), true);

                                        return 1;
                                    })
                            )
                    )
                    .then(literal("setskill").requires(source -> source.hasPermissionLevel(2))
                            .then(argument("player", EntityArgumentType.player())
                                    .then(argument("skill", IntegerArgumentType.integer())
                                            .executes((CommandContext<ServerCommandSource> context) -> {
                                                if (context.getSource().getPlayer() == null) return 0;

                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                int skill = IntegerArgumentType.getInteger(context, "skill");
                                                setLevel(player, skill);
                                                context.getSource().sendFeedback(() -> Text.translatable("text.command.deadeye-mod.setskill.success", player.getName().getString(), skill), true);

                                                return 1;
                                            })
                                    )
                                    .executes((CommandContext<ServerCommandSource> context) -> {
                                        if (context.getSource().getPlayer() == null) return 0;

                                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                        setLevel(player, 3);
                                        context.getSource().sendFeedback(() -> Text.translatable("text.command.deadeye-mod.setskill.success", player.getName().getString(), 3), true);

                                        return 1;
                                    })
                            )
                    )
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
