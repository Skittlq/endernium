package com.skittlq.endernium.progression;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Operator tools for repairing or inspecting a player's blessing state. */
public final class EnderniumBlessingCommand {
    private EnderniumBlessingCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("endernium")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("blessing")
                        .then(Commands.literal("grant")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> grant(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))))
                        .then(Commands.literal("revoke")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> revoke(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))))
                        .then(Commands.literal("check")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> check(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))))));
    }

    private static int grant(CommandSourceStack source, ServerPlayer player) {
        EnderniumBlessingSavedData.get(player.level().getServer()).remove(player.getUUID());
        EnderniumBlessing.setBlessed(player, true, false);
        source.sendSuccess(
                () -> Component.literal("Granted Endernium blessing to " + player.getScoreboardName() + "."),
                true
        );
        return 1;
    }

    private static int revoke(CommandSourceStack source, ServerPlayer player) {
        EnderniumBlessingSavedData.get(player.level().getServer()).remove(player.getUUID());
        EnderniumBlessing.setBlessed(player, false, false);
        source.sendSuccess(
                () -> Component.literal("Revoked Endernium blessing from " + player.getScoreboardName() + "."),
                true
        );
        return 1;
    }

    private static int check(CommandSourceStack source, ServerPlayer player) {
        boolean blessed = EnderniumBlessing.isBlessed(player);
        boolean pending = EnderniumBlessingSavedData.get(player.level().getServer()).contains(player.getUUID());
        String state = blessed ? "blessed" : pending ? "awaiting blessing" : "dormant";
        source.sendSuccess(
                () -> Component.literal(player.getScoreboardName() + " is " + state + "."),
                false
        );
        return blessed ? 1 : 0;
    }
}
