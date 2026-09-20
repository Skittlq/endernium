package com.skittlq.endernium.mixin.neoforge;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.neoforged.neoforge.client.model.item.TrimmedArmorModel;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TrimmedArmorModel.class, remap = false)
public abstract class TrimmedArmorModelMixin {
    @Shadow
    @Final
    private Object2ObjectMap<String, ItemModel> itemsWithTrims;

    @Invoker("createTrimLayer")
    protected abstract ItemModel endernium$createTrimLayer(String suffix);

    @Inject(method = "update", at = @At("TAIL"))
    private void endernium$renderTrim(
            ItemStackRenderState state,
            ItemStack stack,
            ItemModelResolver resolver,
            ItemDisplayContext context,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed,
            CallbackInfo callbackInfo
    ) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (trim == null || equippable == null || equippable.assetId().isEmpty()) {
            return;
        }

        Identifier paletteId = trim.material().value().paletteId();
        String palettePath = paletteId.getPath();
        String suffix = palettePath.substring(palettePath.lastIndexOf('/') + 1);

        boolean sameMaterial = trim.material().unwrapKey()
                .map(material -> material.identifier().equals(equippable.assetId().get().identifier()))
                .orElse(false);
        if (sameMaterial) {
            suffix += "_darker";
        }

        this.itemsWithTrims
                .computeIfAbsent(suffix, this::endernium$createTrimLayer)
                .update(state, stack, resolver, context, level, owner, seed);
    }
}
