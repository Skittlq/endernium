package com.skittlq.endernium.util;

import com.skittlq.endernium.Endernium;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = Endernium.MODID)
public final class EnderniumUtilsEvents {
    private EnderniumUtilsEvents() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        EnderniumUtils.onAutoCollectToolBlockBreak(
                event.getPlayer().level(),
                event.getPlayer(),
                event.getPos().immutable(),
                event.getState(),
                false
        );
    }
}