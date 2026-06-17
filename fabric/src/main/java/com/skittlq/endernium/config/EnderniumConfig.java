package com.skittlq.endernium.config;

public class EnderniumConfig {
    public boolean enderniumArmorAbility = true;
    public int enderniumArmorAbilityThreshold = 4;
    public long enderniumArmorAbilityCooldown = 120L;

    public EnderniumConfig copy() {
        EnderniumConfig copy = new EnderniumConfig();
        copy.enderniumArmorAbility = this.enderniumArmorAbility;
        copy.enderniumArmorAbilityThreshold = this.enderniumArmorAbilityThreshold;
        copy.enderniumArmorAbilityCooldown = this.enderniumArmorAbilityCooldown;
        return copy;
    }
}
