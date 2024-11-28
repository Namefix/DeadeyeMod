package com.namefix.data;

import net.minecraft.sound.SoundEvent;

public class DeadeyeSoundProfile {
    public final SoundEvent backgroundSound;
    public final SoundEvent background2Sound;
    public final SoundEvent enterSound;
    public final SoundEvent exitSound;
    public final SoundEvent exitEmptySound;
    public final SoundEvent heartbeatInSound;
    public final SoundEvent heartbeatOutSound;
    public final SoundEvent paintTargetSound;

    public DeadeyeSoundProfile(SoundEvent enterSound, SoundEvent backgroundSound, SoundEvent background2Sound, SoundEvent exitSound, SoundEvent exitEmptySound, SoundEvent heartbeatInSound, SoundEvent heartbeatOutSound, SoundEvent paintTargetSound) {
        this.enterSound = enterSound;
        this.backgroundSound = backgroundSound;
        this.background2Sound = background2Sound;
        this.exitSound = exitSound;
        this.exitEmptySound = exitEmptySound;
        this.heartbeatInSound = heartbeatInSound;
        this.heartbeatOutSound = heartbeatOutSound;
        this.paintTargetSound = paintTargetSound;
    }
}
