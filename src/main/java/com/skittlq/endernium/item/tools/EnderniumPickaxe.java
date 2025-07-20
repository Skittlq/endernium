package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.util.EnderniumUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnderniumPickaxe extends PickaxeItem {
    public static final String VEIN_MINING_KEY = "VeinMiningEnabled";
    public static final String VEIN_MINING_SESSION_ID_KEY = "VeinMiningSessionId";
    public static final int MAX_BLOCKS = 64; // Safety cap

    public EnderniumPickaxe(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    public static boolean isVeinMiningEnabled(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        return tag.getBoolean(VEIN_MINING_KEY);
    }

    public static void setVeinMiningEnabled(ItemStack stack, boolean enabled) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        tag.putBoolean(VEIN_MINING_KEY, enabled);
        stack.setTag(tag);
    }

    private static CompoundTag getOrCreateCustomDataTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundTag();
            stack.setTag(tag);
        }
        return tag;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            boolean enabled = !isVeinMiningEnabled(stack);
            setVeinMiningEnabled(stack, enabled);

            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("Vein Mining: " + (enabled ? "Enabled" : "Disabled"))
                                .withStyle(enabled ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY),
                        true
                );
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS, 0.25f, enabled ? 1.4f : 0.8f);
            }
            return InteractionResultHolder.pass(stack);
        }

        // If not shift, cancel all vein mining operations
        cancelVeinMining(stack);

        if (!level.isClientSide) {
            player.displayClientMessage(
                    Component.literal("Cancelled all vein mining operations")
                            .withStyle(ChatFormatting.GRAY),
                    true
            );
            level.playSound(null, player.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS, 0.25f, 1.0f);
        }

        return InteractionResultHolder.pass(stack);
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
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, java.util.List<Component> components, TooltipFlag tooltipFlag) {
        boolean enabled = isVeinMiningEnabled(stack);
        components.add(Component.literal("Vein Mining: " +
                        (enabled ? "Enabled" : "Disabled"))
                .withStyle(enabled ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY));
        components.add(Component.literal(""));
        components.add(Component.literal("§5Sneak + Right-click to toggle vein mining."));
        components.add(Component.literal("§5Right-click to cancel all vein mining."));
        components.add(Component.literal("§7Works on blocks that the tool can mine."));

        super.appendHoverText(stack, level, components, tooltipFlag);
    }
}
