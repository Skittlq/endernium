package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.util.EnderniumUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class EnderniumVeinMiningToolHelper {
    static final String VEIN_MINING_KEY = "VeinMiningEnabled";
    private static final String VEIN_MINING_NOTIFIED_KEY = "VeinMiningNotified";

    private EnderniumVeinMiningToolHelper() {
    }

    public static boolean isVeinMiningEnabled(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        return tag.getBooleanOr(VEIN_MINING_KEY, false);
    }

    static void setVeinMiningEnabled(ItemStack stack, boolean enabled) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        tag.remove(VEIN_MINING_NOTIFIED_KEY);
        tag.putByte(VEIN_MINING_KEY, (byte) (enabled ? 1 : 0));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    static InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            boolean enabled = !isVeinMiningEnabled(stack);
            setVeinMiningEnabled(stack, enabled);

            if (!level.isClientSide()) {
                player.sendOverlayMessage(
                        Component.literal("Vein Mining: " + (enabled ? "Enabled" : "Disabled"))
                                .withStyle(enabled ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY)
                );
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS, 0.25F, enabled ? 1.4F : 0.8F);
            }
            return InteractionResult.SUCCESS;
        }

        boolean hadActiveOperation = EnderniumUtils.hasActiveVeinMiningOperation(stack);
        EnderniumUtils.cancelVeinMining(stack);

        if (!level.isClientSide()) {
            if (hadActiveOperation) {
                player.sendOverlayMessage(
                        Component.literal("Cancelled all vein mining operations")
                                .withStyle(ChatFormatting.GRAY)
                );
            }
            level.playSound(null, player.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS, 0.25F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }

    static boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide() && isVeinMiningEnabled(stack) && entity instanceof Player player && !player.isCreative()) {
            EnderniumUtils.veinMineBlocks(stack, level, pos, state, player, EnderniumUtils.DEFAULT_MAX_BLOCKS);
        }
        return false;
    }

    static void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        boolean enabled = isVeinMiningEnabled(stack);
        tooltipAdder.accept(Component.literal("Vein Mining: " + (enabled ? "Enabled" : "Disabled"))
                .withStyle(enabled ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY));
        tooltipAdder.accept(Component.literal(""));
        tooltipAdder.accept(Component.literal("\u00A75Sneak + Right-click to toggle vein mining."));
        tooltipAdder.accept(Component.literal("\u00A75Right-click to cancel all vein mining."));
        tooltipAdder.accept(Component.literal("\u00A77Works on blocks that the tool can mine."));
    }

    static void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }

        boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        boolean notified = tag.getBooleanOr(VEIN_MINING_NOTIFIED_KEY, false);

        if (!inHand || !isVeinMiningEnabled(stack)) {
            if (notified) {
                tag.remove(VEIN_MINING_NOTIFIED_KEY);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
            return;
        }

        if (!notified) {
            player.sendOverlayMessage(
                    Component.literal("Vein Mining is Enabled").withStyle(ChatFormatting.LIGHT_PURPLE)
            );
            tag.putByte(VEIN_MINING_NOTIFIED_KEY, (byte) 1);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static CompoundTag getOrCreateCustomDataTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }
}