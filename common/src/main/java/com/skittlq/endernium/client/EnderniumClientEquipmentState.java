package com.skittlq.endernium.client;

/** Client-maintained equipment state exposed without referencing client-only game classes. */
public final class EnderniumClientEquipmentState {
    private static boolean fullEnderniumSetEquipped;

    private EnderniumClientEquipmentState() {
    }

    public static boolean isFullEnderniumSetEquipped() {
        return fullEnderniumSetEquipped;
    }

    public static void setFullEnderniumSetEquipped(boolean equipped) {
        fullEnderniumSetEquipped = equipped;
    }

    public static void reset() {
        fullEnderniumSetEquipped = false;
    }
}
