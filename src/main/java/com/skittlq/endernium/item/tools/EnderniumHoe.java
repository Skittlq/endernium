package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.item.ModToolTiers;
import com.skittlq.endernium.util.EnderniumUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EnderniumHoe extends HoeItem {
    public static final String VEIN_MINING_KEY = "VeinMiningEnabled";
    public static final String VEIN_MINING_NOTIFIED_KEY = "VeinMiningNotified";
    public static final int MAX_BLOCKS = 64;

    public EnderniumHoe(Properties properties) {
        super(ModToolTiers.ENDERNIUM, -4.0F, 0.0F, properties);
    }

    public static boolean isVeinMiningEnabled(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        return tag.getBooleanOr(VEIN_MINING_KEY, false);
    }

    public static void setVeinMiningEnabled(ItemStack stack, boolean enabled) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        tag.remove(VEIN_MINING_NOTIFIED_KEY);
        tag.putByte(VEIN_MINING_KEY, (byte) (enabled ? 1 : 0));
        stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }

    private static CompoundTag getOrCreateCustomDataTag(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            boolean enabled = !isVeinMiningEnabled(stack);
            setVeinMiningEnabled(stack, enabled);

            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.literal("Vein Mining: " + (enabled ? "Enabled" : "Disabled"))
                                .withStyle(enabled ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY),
                        true
                );
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS, 0.25f, enabled ? 1.4f : 0.8f);
            }
            return InteractionResult.PASS;
        }

        // If not shift, cancel all vein mining operations
        cancelVeinMining(stack);

        if (!level.isClientSide()) {
            player.displayClientMessage(
                    Component.literal("Cancelled all vein mining operations")
                            .withStyle(ChatFormatting.GRAY),
                    true
            );
            level.playSound(null, player.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS, 0.25f, 1.0f);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (isVeinMiningEnabled(stack) && entity instanceof Player player && !player.isCreative()) {
            EnderniumUtils.veinMineBlocks(stack, level, pos, player, MAX_BLOCKS);
            EnderniumUtils.handleBlockMine(stack, level, state, pos, entity);
            return true;
        }

        EnderniumUtils.handleBlockMine(stack, level, state, pos, entity);
        return super.mineBlock(stack, level, state, pos, entity);
    }

    // Cancel method just calls utility now:
    public static void cancelVeinMining(ItemStack stack) {
        EnderniumUtils.cancelVeinMining(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        boolean enabled = isVeinMiningEnabled(stack);
        tooltipAdder.accept(Component.literal("Vein Mining: " +
                        (enabled ? "Enabled" : "Disabled"))
                .withStyle(enabled ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("§5Sneak + Right-click to toggle vein mining."));
        tooltipAdder.accept(Component.literal("§5Right-click to cancel all vein mining."));
        tooltipAdder.accept(Component.literal("§7Works on blocks that the tool can mine."));

        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (level.isClientSide() || !(entity instanceof Player player)) return;

        boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        boolean notified = tag.getBooleanOr(VEIN_MINING_NOTIFIED_KEY, false);

        if (!inHand || !isVeinMiningEnabled(stack)) {
            if (notified) {
                tag.remove(VEIN_MINING_NOTIFIED_KEY);
                stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            }
            return;
        }

        if (!notified) {
            player.displayClientMessage(
                    Component.literal("Vein Mining is Enabled").withStyle(ChatFormatting.LIGHT_PURPLE),
                    true
            );
            tag.putByte(VEIN_MINING_NOTIFIED_KEY, (byte) 1);
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        }
    }
}
