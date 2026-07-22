package com.skittlq.endernium.worldgen;
import com.skittlq.endernium.Endernium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
public final class ModPlacementModifiers {
    public static final PlacementModifierType<DragonDefeatedPlacementFilter> DRAGON_DEFEATED = Registry.register(
            BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "dragon_defeated"),
            () -> DragonDefeatedPlacementFilter.CODEC
    );
    private ModPlacementModifiers() {
    }
    public static PlacementModifierType<DragonDefeatedPlacementFilter> dragonDefeatedType() {
        return DRAGON_DEFEATED;
    }
    public static void register() {
    }
}