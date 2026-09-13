package com.skittlq.endernium.mixin;

import com.skittlq.endernium.item.armor.EnderniumHorseArmorAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void endernium$allowTwoBlockHorseStep(CallbackInfoReturnable<Float> cir) {
        Object self = this;
        if (self instanceof AbstractHorse horse && EnderniumHorseArmorAbility.canStepUp(horse)) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), EnderniumHorseArmorAbility.STEP_HEIGHT));
        }
    }

}
