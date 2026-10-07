package com.skittlq.endernium.combat;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;

public final class EnderniumDamageTypes {
    public static final ResourceKey<DamageType> SPEAR_STRAIN = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            Identifier.fromNamespaceAndPath(EnderniumConstants.MOD_ID, "spear_strain")
    );

    private EnderniumDamageTypes() {
    }

    public static DamageSource spearStrain(ServerLevel level, Player player) {
        return new DamageSource(level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(SPEAR_STRAIN), player);
    }

    public static boolean isSpearStrain(DamageSource source) {
        return source.is(SPEAR_STRAIN);
    }
}
