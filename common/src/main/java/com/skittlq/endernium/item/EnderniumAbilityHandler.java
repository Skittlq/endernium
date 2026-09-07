package com.skittlq.endernium.item;

import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.item.tools.EnderniumAxe;
import com.skittlq.endernium.item.tools.EnderniumHoe;
import com.skittlq.endernium.item.tools.EnderniumPickaxe;
import com.skittlq.endernium.item.tools.EnderniumShovel;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.item.tools.EnderniumVeinMiningToolHelper;
import com.skittlq.endernium.progression.EnderniumAwakening;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class EnderniumAbilityHandler {
    private static final Map<net.minecraft.server.MinecraftServer, Map<UUID, Integer>> LAST_PACKET_TICKS =
            new WeakHashMap<>();
    private EnderniumAbilityHandler() {
    }

    public static void activateHeldAbility(Level level, Player player) {
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            Map<UUID, Integer> playerTicks = LAST_PACKET_TICKS.computeIfAbsent(
                    serverPlayer.level().getServer(), ignored -> new HashMap<>());
            int currentTick = serverPlayer.level().getServer().getTickCount();
            Integer previousTick = playerTicks.put(player.getUUID(), currentTick);
            if (previousTick != null && previousTick == currentTick) {
                return;
            }
        }
        if (tryActivate(level, player, InteractionHand.MAIN_HAND)) {
            return;
        }
        tryActivate(level, player, InteractionHand.OFF_HAND);
    }

    public static void clearPlayerState(MinecraftServer server, UUID playerId) {
        Map<UUID, Integer> playerTicks = LAST_PACKET_TICKS.get(server);
        if (playerTicks != null) {
            playerTicks.remove(playerId);
            if (playerTicks.isEmpty()) {
                LAST_PACKET_TICKS.remove(server);
            }
        }
    }

    public static void clearServerState(MinecraftServer server) {
        LAST_PACKET_TICKS.remove(server);
    }

    private static boolean tryActivate(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (item instanceof EnderniumSword sword) {
            if (!EnderniumGameplayConfig.swordAbilityEnabled()) {
                return false;
            }
            if (!EnderniumAwakening.isAwakened(player)) {
                return true;
            }
            sword.activateAbility(level, player, hand);
            return true;
        }

        if (item instanceof EnderniumPickaxe
                || item instanceof EnderniumShovel
                || item instanceof EnderniumAxe
                || item instanceof EnderniumHoe) {
            if (!EnderniumGameplayConfig.toolsVeinMiningEnabled()) {
                return false;
            }
            if (!EnderniumAwakening.isAwakened(player)) {
                return true;
            }
            EnderniumVeinMiningToolHelper.activate(level, player, hand);
            return true;
        }

        return false;
    }
}
