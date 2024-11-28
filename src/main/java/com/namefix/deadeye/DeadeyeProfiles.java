package com.namefix.deadeye;

import com.namefix.data.DeadeyeSoundProfile;
import com.namefix.handlers.SoundHandler;

import java.util.HashMap;
import java.util.Map;

public class DeadeyeProfiles {
    public static Map<String, DeadeyeSoundProfile> soundProfiles = new HashMap<>();
    public static String selectedProfile;

    public static void initialize() {
        soundProfiles.put("JOHN_MARSTON", new DeadeyeSoundProfile(
                SoundHandler.DEADEYE_JOHN_ENTER,
                SoundHandler.DEADEYE_JOHN_BACKGROUND,
                SoundHandler.DEADEYE_JOHN_BACKGROUND2,
                SoundHandler.DEADEYE_JOHN_EXIT,
                SoundHandler.DEADEYE_JOHN_BACKGROUND2_END,
                SoundHandler.DEADEYE_JOHN_HEARTBEAT_IN,
                SoundHandler.DEADEYE_JOHN_HEARTBEAT_OUT,
                SoundHandler.DEADEYE_ARTHUR_PAINT // placeholder
        ));

        soundProfiles.put("ARTHUR_MORGAN", new DeadeyeSoundProfile(
                SoundHandler.DEADEYE_JOHN_ENTER, // placeholder
                SoundHandler.DEADEYE_ARTHUR_BACKGROUND,
                SoundHandler.DEADEYE_ARTHUR_BACKGROUND2,
                SoundHandler.DEADEYE_ARTHUR_EXIT,
                SoundHandler.DEADEYE_JOHN_BACKGROUND2_END, // placeholder
                SoundHandler.DEADEYE_ARTHUR_TICK_IN,
                SoundHandler.DEADEYE_ARTHUR_TICK_OUT,
                SoundHandler.DEADEYE_ARTHUR_PAINT
        ));

        selectedProfile = soundProfiles.keySet().iterator().next();
    }

    public static DeadeyeSoundProfile getSelectedSoundProfile() {
        return soundProfiles.get(selectedProfile);
    }
}
