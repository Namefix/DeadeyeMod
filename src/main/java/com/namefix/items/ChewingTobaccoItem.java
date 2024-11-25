package com.namefix.items;

import com.namefix.deadeye.DeadeyeEffects;
import com.namefix.deadeye.DeadeyeServer;
import com.namefix.handlers.SoundHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class ChewingTobaccoItem extends Item {
    public ChewingTobaccoItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {  // server side
            DeadeyeServer.setMeterTonicLevel((ServerPlayerEntity) user, 1);
            DeadeyeServer.addDeadeyeCore((ServerPlayerEntity) user, -5, false);
        } else {                // client side
            DeadeyeEffects.tonicDuration = 1.0f;
            user.playSound(SoundHandler.CONSUME_TONIC, 0.5f, 1.0f);
        }

        return super.finishUsing(stack, world, user);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.deadeye-mod.chewing_tobacco.tooltip").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
