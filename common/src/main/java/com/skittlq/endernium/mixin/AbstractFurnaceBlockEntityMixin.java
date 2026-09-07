package com.skittlq.endernium.mixin;

import com.skittlq.endernium.item.EnderniumItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
    private static final int ENDERNIUM_DUST_PER_SHARD = 4;

    @Unique
    private static final ThreadLocal<Boolean> ENDERNIUM_CRAFT = ThreadLocal.withInitial(() -> false);

    @Redirect(
            method = "burn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
            )
    )
    private static void endernium$consumeFourDust(ItemStack input, int amount) {
        input.shrink(ENDERNIUM_CRAFT.get() ? ENDERNIUM_DUST_PER_SHARD : amount);
    }

    @Inject(method = "burn", at = @At("HEAD"))
    private static void endernium$captureRecipe(
            NonNullList<ItemStack> items,
            ItemStack input,
            ItemStack result,
            CallbackInfo callbackInfo
    ) {
        ENDERNIUM_CRAFT.set(input.is(EnderniumItems.ENDERNIUM_DUST.get())
                && result.is(EnderniumItems.ENDERNIUM_SHARD.get()));
    }

    @Inject(method = "burn", at = @At("RETURN"))
    private static void endernium$clearCapturedRecipe(
            NonNullList<ItemStack> items,
            ItemStack input,
            ItemStack result,
            CallbackInfo callbackInfo
    ) {
        ENDERNIUM_CRAFT.remove();
    }
}
