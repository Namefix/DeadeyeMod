package com.namefix.handlers;

import com.namefix.DeadeyeMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundHandler {
    private SoundHandler() {
    }

    public static final SoundEvent TONIC_DRINK = registerSound("tonic_drink");

    public static final SoundEvent DEADEYE_ARTHUR_BACKGROUND = registerSound("deadeye_arthur_background");
    public static final SoundEvent DEADEYE_ARTHUR_PAINT = registerSound("deadeye_arthur_paint");

    public static final SoundEvent DEADEYE_JOHN_ENTER = registerSound("deadeye_john_enter");
    public static final SoundEvent DEADEYE_JOHN_BACKGROUND = registerSound("deadeye_john_background");
    public static final SoundEvent DEADEYE_JOHN_BACKGROUND2 = registerSound("deadeye_john_background2");
    public static final SoundEvent DEADEYE_JOHN_BACKGROUND2_END = registerSound("deadeye_john_background2_end");
    public static final SoundEvent DEADEYE_JOHN_HEARTBEAT_IN = registerSound("deadeye_john_heartbeat_in");
    public static final SoundEvent DEADEYE_JOHN_HEARTBEAT_OUT = registerSound("deadeye_john_heartbeat_out");
    public static final SoundEvent DEADEYE_JOHN_EXIT  = registerSound("deadeye_john_exit");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of(DeadeyeMod.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void initialize() {}
}
