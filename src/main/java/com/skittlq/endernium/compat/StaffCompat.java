package com.skittlq.endernium.compat;

public final class StaffCompat {
//    public static void initIfPresent() {
//        if (!net.neoforged.fml.ModList.get().isLoaded("thestaff")) return;
//        // Safe: we only load the compat class when TheStaff exists
//        CompatImpl.hook();
//    }

    // this inner class is ONLY loaded when the above guard passes
//    static final class CompatImpl {
//        static void hook() {
//            // Subscribe to the GAME bus for the event
//            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new Object() {
//                @net.neoforged.bus.api.SubscribeEvent
//                public void onRegister(com.skittlq.thestaff.api.RegisterStaffAbilitiesEvent e) {
//                    e.register(new net.minecraft.resources.Identifier("theirmod:shiny_block"),
//                            new TheirShinyBlockAbility());
//                }
//            });
//        }
//    }
}
