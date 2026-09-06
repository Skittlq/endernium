package com.skittlq.endernium.progression;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Operator tools for repairing or inspecting a player's awakening state. */
public final class EnderniumAwakeningCommand {
    private EnderniumAwakeningCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("endernium")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("awakening")
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
        EnderniumAwakeningSavedData.get(player.level().getServer()).remove(player.getUUID());
        EnderniumAwakening.setAwakened(player, true, false);
        source.sendSuccess(
                () -> Component.literal("Granted Endernium awakening to " + player.getScoreboardName() + "."),
                true
        );
        return 1;
    }

    private static int revoke(CommandSourceStack source, ServerPlayer player) {
        EnderniumAwakeningSavedData.get(player.level().getServer()).remove(player.getUUID());
        EnderniumAwakening.setAwakened(player, false, false);
        source.sendSuccess(
                () -> Component.literal("Revoked Endernium awakening from " + player.getScoreboardName() + "."),
                true
        );
        return 1;
    }

    private static int check(CommandSourceStack source, ServerPlayer player) {
        boolean awakened = EnderniumAwakening.isAwakened(player);
        boolean pending = EnderniumAwakeningSavedData.get(player.level().getServer()).contains(player.getUUID());
        String state = awakened ? "awakened" : pending ? "awaiting blessing" : "dormant";
        source.sendSuccess(
                () -> Component.literal(player.getScoreboardName() + " is " + state + "."),
                false
        );
        return awakened ? 1 : 0;
    }
}
