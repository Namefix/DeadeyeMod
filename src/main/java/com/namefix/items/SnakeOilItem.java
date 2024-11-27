package com.namefix.items;

import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.deadeye.DeadeyeServer;
import com.namefix.handlers.SoundHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

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
            DeadeyeEffects.startTonicEffect();
            user.playSound(SoundHandler.CONSUME_TONIC, 0.5f, 1.0f);
        }

        return super.finishUsing(stack, world, user);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        switch(level) {
            case 1 -> tooltip.add(Text.translatable("item.deadeye-mod.snake_oil.tooltip").formatted(Formatting.GRAY));
            case 2 -> tooltip.add(Text.translatable("item.deadeye-mod.potent_snake_oil.tooltip").formatted(Formatting.GRAY));
            case 3 -> tooltip.add(Text.translatable("item.deadeye-mod.special_snake_oil.tooltip").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }
}
