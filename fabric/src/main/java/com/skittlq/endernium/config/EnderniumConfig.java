package com.skittlq.endernium.config;

public class EnderniumConfig {
    public boolean enderniumArmorAbility = true;
    public int enderniumArmorAbilityThreshold = 4;
    public long enderniumArmorAbilityCooldown = 90L;
    public boolean enderniumSwordAbility = true;
    public int enderniumSwordAbilityBaseCooldown = EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS;
    public int enderniumSwordAbilityPerMobCooldown = EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_PER_MOB_COOLDOWN_SECONDS;
    public boolean enderniumToolsVeinMining = true;

    public EnderniumConfig copy() {
        EnderniumConfig copy = new EnderniumConfig();
        copy.enderniumArmorAbility = this.enderniumArmorAbility;
        copy.enderniumArmorAbilityThreshold = this.enderniumArmorAbilityThreshold;
        copy.enderniumArmorAbilityCooldown = this.enderniumArmorAbilityCooldown;
        copy.enderniumSwordAbility = this.enderniumSwordAbility;
        copy.enderniumSwordAbilityBaseCooldown = this.enderniumSwordAbilityBaseCooldown;
        copy.enderniumSwordAbilityPerMobCooldown = this.enderniumSwordAbilityPerMobCooldown;
        copy.enderniumToolsVeinMining = this.enderniumToolsVeinMining;
        return copy;
    }
}
