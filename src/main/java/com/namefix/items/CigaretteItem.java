package com.namefix.items;

import com.namefix.deadeye.DeadeyeServer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class CigaretteItem extends PotionItem {
    public CigaretteItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {  // server side
            DeadeyeServer.addDeadeyeCore((ServerPlayerEntity) user, 10, true);
        }
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity)user : null;

        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            stack.decrementUnlessCreative(1, playerEntity);
        }


        if (playerEntity == null || !playerEntity.isInCreativeMode()) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.AIR);
            }
        }

        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.deadeye-mod.cigarette.tooltip").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
