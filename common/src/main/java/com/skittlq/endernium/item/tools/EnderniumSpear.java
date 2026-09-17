package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.entity.EnderniumThrownSpear;
import com.skittlq.endernium.client.EnderniumKeyBindings;
import com.skittlq.endernium.item.ModToolTiers;
import com.skittlq.endernium.progression.EnderniumBlessing;
import com.skittlq.endernium.network.EnderniumNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class EnderniumSpear extends Item {
    private static final float THROW_VELOCITY = 2.75F;
    public static final int RETURN_COOLDOWN_TICKS = 8 * 20;
    private static final Map<net.minecraft.server.MinecraftServer, Map<UUID, Long>> COOLDOWN_END_TICKS =
            new WeakHashMap<>();

    public EnderniumSpear(Properties properties) {
        super(properties.spear(
                ModToolTiers.ENDERNIUM,
                1.15F,
                1.2F,
                0.4F,
                2.5F,
                9.0F,
                5.5F,
                5.1F,
                8.75F,
                4.6F
        ).fireResistant());
    }

    public InteractionResult activateAbility(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || !EnderniumBlessing.isBlessed(serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.getItem() != this) {
            return InteractionResult.PASS;
        }

        Map<UUID, Long> cooldowns = COOLDOWN_END_TICKS.computeIfAbsent(
                serverLevel.getServer(), ignored -> new HashMap<>());
        long gameTime = serverLevel.getGameTime();
        if (cooldowns.getOrDefault(serverPlayer.getUUID(), 0L) > gameTime) {
            return InteractionResult.SUCCESS;
        }

        float meleeDamage = calculateMeleeDamage(serverPlayer, heldStack, hand);
        ItemStack thrownStack = heldStack.copyAndClear();
        EnderniumThrownSpear spear = new EnderniumThrownSpear(
                serverLevel, serverPlayer, thrownStack, hand, meleeDamage);
        spear.setPos(serverPlayer.getX(), serverPlayer.getEyeY() - 0.1, serverPlayer.getZ());
        Vec3 look = serverPlayer.getLookAngle();
        spear.shoot(look.x, look.y, look.z, THROW_VELOCITY, 0.0F);
        Projectile.spawnProjectile(spear, serverLevel, thrownStack);
        serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.1F);
        return InteractionResult.SUCCESS;
    }

    public static void beginReturnCooldown(ServerPlayer player) {
        long cooldownEndTick = player.level().getGameTime() + RETURN_COOLDOWN_TICKS;
        COOLDOWN_END_TICKS.computeIfAbsent(
                player.level().getServer(), ignored -> new HashMap<>())
                .put(player.getUUID(), cooldownEndTick);
        EnderniumNetworking.sendSpearCooldownSync(
                player, cooldownEndTick, RETURN_COOLDOWN_TICKS);
    }

    private static float calculateMeleeDamage(
            ServerPlayer player,
            ItemStack spearStack,
            InteractionHand hand
    ) {
        if (hand == InteractionHand.MAIN_HAND) {
            return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }

        AttributeInstance currentDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (currentDamage == null) {
            return 1.0F;
        }

        AttributeInstance simulatedDamage = new AttributeInstance(
                Attributes.ATTACK_DAMAGE, ignored -> {
                });
        simulatedDamage.replaceFrom(currentDamage);
        player.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
                simulatedDamage.removeModifier(modifier.id());
            }
        });
        spearStack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
                simulatedDamage.addOrUpdateTransientModifier(modifier);
            }
        });
        return (float) simulatedDamage.getValue();
    }

    public static boolean isHeldBy(Player player) {
        return player.getMainHandItem().getItem() instanceof EnderniumSpear
                || player.getOffhandItem().getItem() instanceof EnderniumSpear;
    }

    public static void syncCooldownOnLogin(ServerPlayer player) {
        Map<UUID, Long> cooldowns = COOLDOWN_END_TICKS.get(player.level().getServer());
        long endTick = cooldowns == null ? 0L : cooldowns.getOrDefault(player.getUUID(), 0L);
        long remaining = endTick - player.level().getGameTime();
        int duration = remaining > 0L ? RETURN_COOLDOWN_TICKS : 0;
        EnderniumNetworking.sendSpearCooldownSync(player, duration == 0 ? 0L : endTick, duration);
    }

    public static void clearServerState(net.minecraft.server.MinecraftServer server) {
        COOLDOWN_END_TICKS.remove(server);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        if (!EnderniumBlessing.isClientBlessed()) {
            tooltipAdder.accept(Component.translatable("endernium.tooltip.ability.locked")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltipAdder.accept(Component.translatable("endernium.tooltip.spear.title")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.empty());
            tooltipAdder.accept(Component.translatable(
                    "endernium.tooltip.spear.activate",
                    EnderniumKeyBindings.abilityKeyName()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable("endernium.tooltip.spear.cooldown", 8)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable("endernium.tooltip.spear.description")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
