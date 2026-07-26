package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.item.ModToolTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EnderniumAxe extends AxeItem {
    public EnderniumAxe(Properties properties) {
        super(ModToolTiers.ENDERNIUM, 5.0F, -3.0F, properties);
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

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        EnderniumVeinMiningToolHelper.inventoryTick(stack, level, entity, slot);
    }
}
