package com.skittlq.endernium.mixin;

import com.skittlq.endernium.item.EnderniumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
    private static final int ENDERNIUM_DUST_PER_SHARD = 4;

    @Shadow
    private int cookingTimer;

    @Unique
    private int endernium$cookingTimerAtTickStart;

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void endernium$captureCookingProgress(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo callbackInfo
    ) {
        AbstractFurnaceBlockEntityMixin mixin = (AbstractFurnaceBlockEntityMixin) (Object) furnace;
        mixin.endernium$cookingTimerAtTickStart = mixin.cookingTimer;
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void endernium$depletePausedCookingProgress(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo callbackInfo
    ) {
        AbstractFurnaceBlockEntityMixin mixin = (AbstractFurnaceBlockEntityMixin) (Object) furnace;
        ItemStack input = furnace.getItem(0);

        if (mixin.cookingTimer > 0
                && mixin.cookingTimer == mixin.endernium$cookingTimerAtTickStart
                && input.is(EnderniumItems.ENDERNIUM_DUST.get())
                && input.getCount() < ENDERNIUM_DUST_PER_SHARD) {
            mixin.cookingTimer = Math.max(0, mixin.cookingTimer - 2);
            furnace.setChanged();
        }
    }

    @Redirect(
            method = "burn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
            )
    )
    private static void endernium$consumeFourDust(ItemStack input, int amount) {
        input.shrink(input.is(EnderniumItems.ENDERNIUM_DUST.get()) ? ENDERNIUM_DUST_PER_SHARD : amount);
    }
}
