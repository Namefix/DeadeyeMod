package com.namefix.sound;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class SoundBackgroundLoop extends AbstractSoundInstance implements TickableSoundInstance {

    private final PlayerEntity player;
    private boolean done;

    public SoundBackgroundLoop(SoundEvent soundEvent, SoundCategory soundCategory, PlayerEntity player, float volume) {
        super(soundEvent, soundCategory, SoundInstance.createRandom());
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = volume;
        this.player = player;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    @Override
    public void tick() {
        if(player == null) {this.setDone();}
        else {
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    public final void setDone() {
        this.done = true;
        this.repeat = false;
    }
}