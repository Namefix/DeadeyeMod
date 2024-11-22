package com.namefix.items;

import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.deadeye.DeadeyeServer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class SnakeOilItem extends PotionItem {
    int level;

    public SnakeOilItem(Settings settings, int level) {
        super(settings);
        this.level = level;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {  // server side
            DeadeyeServer.setMeterTonicLevel((ServerPlayerEntity) user, level);
        } else {                // client side
            DeadeyeEffects.tonicDuration = 1.0f;
        }

        return super.finishUsing(stack, world, user);
    }
}
