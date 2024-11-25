package com.namefix.handlers;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class GameruleHandler {

    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_DEADEYE =
            GameRuleRegistry.register("disableDeadeye", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_DEADEYE_LEVELING =
            GameRuleRegistry.register("disableDeadeyeLeveling", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static void initialize() {}
}
