package com.skittlq.endernium.config;

import com.skittlq.endernium.client.vfx.EnderniumVfxRenderMode;

public class EnderniumConfig {
    public boolean enderniumArmorAbility = true;
    public int enderniumArmorAbilityThreshold = EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_THRESHOLD;
    public long enderniumArmorAbilityCooldown = EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_COOLDOWN_SECONDS;
    public boolean enderniumSwordAbility = true;
    public int enderniumSwordAbilityBaseCooldown = EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS;
    public int enderniumSwordAbilityPerMobCooldown = EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_PER_MOB_COOLDOWN_SECONDS;
    public boolean enderniumToolsVeinMining = true;
    public EnderniumVfxRenderMode vfxRenderMode = EnderniumVfxRenderMode.AUTO;

    public EnderniumConfig copy() {
        EnderniumConfig copy = new EnderniumConfig();
        copy.enderniumArmorAbility = enderniumArmorAbility;
        copy.enderniumArmorAbilityThreshold = enderniumArmorAbilityThreshold;
        copy.enderniumArmorAbilityCooldown = enderniumArmorAbilityCooldown;
        copy.enderniumSwordAbility = enderniumSwordAbility;
        copy.enderniumSwordAbilityBaseCooldown = enderniumSwordAbilityBaseCooldown;
        copy.enderniumSwordAbilityPerMobCooldown = enderniumSwordAbilityPerMobCooldown;
        copy.enderniumToolsVeinMining = enderniumToolsVeinMining;
        copy.vfxRenderMode = vfxRenderMode;
        return copy;
    }

    public static EnderniumConfig sanitize(EnderniumConfig rawConfig) {
        EnderniumConfig sanitized = rawConfig == null ? new EnderniumConfig() : rawConfig.copy();
        sanitized.enderniumArmorAbilityThreshold = Math.max(1,
                Math.min(2048, sanitized.enderniumArmorAbilityThreshold));
        sanitized.enderniumArmorAbilityCooldown = Math.max(1L,
                Math.min(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS, sanitized.enderniumArmorAbilityCooldown));
        sanitized.enderniumSwordAbilityBaseCooldown = Math.max(0,
                Math.min(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS, sanitized.enderniumSwordAbilityBaseCooldown));
        sanitized.enderniumSwordAbilityPerMobCooldown = Math.max(0,
                Math.min(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS, sanitized.enderniumSwordAbilityPerMobCooldown));
        if (sanitized.vfxRenderMode == null) {
            sanitized.vfxRenderMode = EnderniumVfxRenderMode.AUTO;
        }
        return sanitized;
    }
}
