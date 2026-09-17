package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.item.ModToolTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class EnderniumHoe extends Item {
    public EnderniumHoe(Properties properties) {
        super(properties.hoe(ModToolTiers.ENDERNIUM, -4.0F, 0.0F));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (EnderniumVeinMiningToolHelper.mineBlock(stack, level, state, pos, entity)) {
            return true;
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        EnderniumVeinMiningToolHelper.appendHoverText(stack, context, display, tooltipAdder, flag);
        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }
}
