package com.namefix.data;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerServerData {
    public enum ShootingPhase {
        NONE,
        MARKED,
        SHOOTING
    }

    public ShootingPhase shootingPhase = ShootingPhase.NONE;
    public ItemStack shootingItem;
    public List<DeadeyeTarget> markList = new ArrayList<>();
}
