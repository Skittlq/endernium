package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.item.ModToolTiers;
import com.skittlq.endernium.util.EnderniumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EnderniumShovel extends Item {
    public static final int MAX_BLOCKS = 64;

    public EnderniumShovel(Properties properties) {
        super(properties.shovel(ModToolTiers.ENDERNIUM, 1.5F, -3.0F));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return EnderniumVeinMiningToolHelper.use(level, player, hand);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (EnderniumVeinMiningToolHelper.isVeinMiningEnabled(stack) && entity instanceof Player player && !player.isCreative()) {
            EnderniumUtils.veinMineBlocks(stack, level, pos, player, MAX_BLOCKS);
            EnderniumUtils.handleBlockMine(stack, level, state, pos, entity);
            return true;
        }

        EnderniumUtils.handleBlockMine(stack, level, state, pos, entity);
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

