package com.skittlq.endernium.mixin;

import com.skittlq.endernium.item.EnderniumItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
    private static final int ENDERNIUM_DUST_PER_SHARD = 4;

    @WrapOperation(
            method = "burn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
            )
    )
    private static void endernium$consumeFourDust(
            ItemStack stackToShrink,
            int amount,
            Operation<Void> original,
            NonNullList<ItemStack> items,
            ItemStack input,
            ItemStack result
    ) {
        int consumed = input.is(EnderniumItems.ENDERNIUM_DUST.get())
                && result.is(EnderniumItems.ENDERNIUM_SHARD.get())
                ? ENDERNIUM_DUST_PER_SHARD
                : amount;
        original.call(stackToShrink, consumed);
    }
}
