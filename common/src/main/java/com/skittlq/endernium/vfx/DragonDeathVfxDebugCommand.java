package com.skittlq.endernium.vfx;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Development-only entry point for replaying the complete authoritative dragon eruption. */
public final class DragonDeathVfxDebugCommand {
    private DragonDeathVfxDebugCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("enderniumvfx")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("simulate")
                        .executes(context -> simulate(
                                context.getSource(),
                                context.getSource().getPosition()
                        ))
                        .then(Commands.argument("origin", Vec3Argument.vec3())
                                .executes(context -> simulate(
                                        context.getSource(),
                                        Vec3Argument.getVec3(context, "origin")
                                )))));
    }

    private static int simulate(CommandSourceStack source, Vec3 origin) {
        if (!source.getLevel().dimension().equals(Level.END)) {
            source.sendFailure(Component.literal("The Endernium eruption can only be simulated in The End."));
            return 0;
        }
        if (!DragonDeathVfxTracker.startSimulation((ServerLevel)source.getLevel(), origin)) {
            source.sendFailure(Component.literal("A real dragon-death sequence is already active."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(String.format(
                "Simulating the complete Endernium eruption at %.1f %.1f %.1f",
                origin.x, origin.y, origin.z
        )), true);
        return 1;
    }
}
