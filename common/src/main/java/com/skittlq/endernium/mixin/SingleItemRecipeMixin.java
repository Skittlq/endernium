package com.skittlq.endernium.mixin;

import com.skittlq.endernium.item.EnderniumItems;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SingleItemRecipe.class)
public abstract class SingleItemRecipeMixin {
    private static final int ENDERNIUM_DUST_PER_SHARD = 4;

    @Inject(
            method = "matches(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void endernium$requireFourDust(
            SingleRecipeInput input,
            Level level,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        if ((Object) this instanceof AbstractCookingRecipe
                && input.item().is(EnderniumItems.ENDERNIUM_DUST.get())
                && input.item().getCount() < ENDERNIUM_DUST_PER_SHARD) {
            callbackInfo.setReturnValue(false);
        }
    }
}
