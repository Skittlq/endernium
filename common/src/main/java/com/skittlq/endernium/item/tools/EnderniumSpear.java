package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.entity.EnderniumThrownSpear;
import com.skittlq.endernium.client.EnderniumKeyBindings;
import com.skittlq.endernium.client.EnderniumClientGameplaySettings;
import com.skittlq.endernium.combat.EnderniumDamageTypes;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.item.EnderniumTooltipProvider;
import com.skittlq.endernium.item.EnderniumTooltipNumbers;
import com.skittlq.endernium.item.ModToolTiers;
import com.skittlq.endernium.progression.EnderniumBlessing;
import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.util.EnderniumAbilityMath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import java.util.Objects;

public final class EnderniumSpear extends Item implements EnderniumTooltipProvider {
    private static final float THROW_VELOCITY = 2.75F;
    private static StrainStore strainStore = new StrainStore() {
        @Override public long getEndGameTime(Player player) { return 0L; }
        @Override public void setEndGameTime(Player player, long gameTime) { }
    };

    public static void bindStrainStore(StrainStore store) {
        strainStore = Objects.requireNonNull(store);
    }

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

    public static void completeTeleport(ServerPlayer player) {
        long now = player.level().getGameTime();
        int durationTicks = EnderniumGameplayConfig.spearStrainDurationSeconds() * 20;
        long previousEnd = strainStore.getEndGameTime(player);
        long remainingTicks = Math.max(0L, Math.min(durationTicks, previousEnd - now));
        float damage = strainDamage(player.getMaxHealth(), remainingTicks, durationTicks,
                (float) (EnderniumGameplayConfig.spearMaximumHealthCostPercent() / 100.0D));

        long newEnd = now + durationTicks;
        strainStore.setEndGameTime(player, newEnd);
        EnderniumNetworking.sendSpearCooldownSync(player, newEnd, durationTicks);
        if (damage > 0.0F && player.isAlive()) {
            player.hurtServer(player.level(), EnderniumDamageTypes.spearStrain(player.level(), player), damage);
        }
    }

    public static float strainDamage(float maxHealth, long remainingTicks, int durationTicks,
                                     float maximumHealthCostFraction) {
        return EnderniumAbilityMath.spearStrainDamage(maxHealth, remainingTicks, durationTicks,
                maximumHealthCostFraction);
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

    public static void syncStrainOnLogin(ServerPlayer player) {
        long endTick = strainStore.getEndGameTime(player);
        long remaining = endTick - player.level().getGameTime();
        int durationTicks = EnderniumGameplayConfig.spearStrainDurationSeconds() * 20;
        if (remaining <= 0L) {
            strainStore.setEndGameTime(player, 0L);
            EnderniumNetworking.sendSpearCooldownSync(player, 0L, 0);
            return;
        }
        EnderniumNetworking.sendSpearCooldownSync(player, endTick, durationTicks);
    }

    public static void tickStrainExpiry(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            long endTick = strainStore.getEndGameTime(player);
            if (endTick > 0L && endTick <= player.level().getGameTime()) {
                strainStore.setEndGameTime(player, 0L);
                EnderniumNetworking.sendSpearCooldownSync(player, 0L, 0);
            }
        }
    }

    public static void clearStrain(ServerPlayer player) {
        strainStore.setEndGameTime(player, 0L);
        EnderniumNetworking.sendSpearCooldownSync(player, 0L, 0);
    }

    public static void clearServerState(MinecraftServer server) {
        // Strain is persisted on each player rather than held in server-global memory.
    }

    @Override
    public void appendEnderniumTooltip(
            ItemStack stack,
            Consumer<Component> tooltipAdder
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
            EnderniumGameplayConfig.Snapshot settings = EnderniumClientGameplaySettings.get();
            tooltipAdder.accept(Component.translatable("endernium.tooltip.spear.strain",
                            settings.spearStrainDurationSeconds(),
                            EnderniumTooltipNumbers.compact(
                                    settings.spearMaximumHealthCostPercent()))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable("endernium.tooltip.spear.description")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public interface StrainStore {
        long getEndGameTime(Player player);

        void setEndGameTime(Player player, long gameTime);
    }
}
