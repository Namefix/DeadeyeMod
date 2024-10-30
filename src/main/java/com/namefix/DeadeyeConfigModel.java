package com.namefix;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Modmenu(modId = "deadeye-mod")
@Config(name = "deadeye-mod", wrapperName = "DeadeyeModConfig")
public class DeadeyeConfigModel {
    @Nest
    @Expanded
    public ServerNest server = new ServerNest();

    public static class ServerNest {
        // Server
        public boolean deadeyeSlowdown = true;

        @Expanded
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public List<String> markableEntities = new ArrayList<>();

        @Expanded
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public List<String> markingItems = new ArrayList<>(List.of("minecraft:bow", "minecraft:crossbow"));

        @RangeConstraint(min = 0.5f, max = 5.0f)
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public float markFocusSpeed = 2.0f;

        @RangeConstraint(min = 0, max = 50)
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public int maxMarks = 6;

        @RangeConstraint(min = 0.1f, max = 4f)
        public float markProjectileSpeedMultiplier = .25f;

        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        public float deadeyeIdleConsumeAmount = 1.0f;
        public float deadeyeKillRefillAmount = 5f;
    }

    @Nest
    @Expanded
    public ClientNest client = new ClientNest();

    public static class ClientNest {
        // Client
        public boolean disableDeadeyeEffects = false;
        public boolean disableLightleakEffect = false;
        @RangeConstraint(min = 0f, max = 10.0f)
        public float deadeyeVignetteStrength = 3.0f;
        @RangeConstraint(min = 0, max = 100)
        public float deadeyeVolume = 100.0f;
        @RangeConstraint(min = 0, max = 64)
        public int deadeyeMarkSize = 4;
    }
}
