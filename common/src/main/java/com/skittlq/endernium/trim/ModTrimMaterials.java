package com.skittlq.endernium.trim;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

import java.util.Map;

public final class ModTrimMaterials {
    public static final ResourceKey<TrimMaterial> ENDERNIUM = ResourceKey.create(
            Registries.TRIM_MATERIAL,
            Identifier.fromNamespaceAndPath("endernium", "endernium")
    );

    private ModTrimMaterials() {
    }

    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        register(
                context,
                ENDERNIUM,
                Style.EMPTY.withColor(TextColor.parseColor("#031cfc").getOrThrow())
        );
    }

    private static void register(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> trimKey, Style style) {
        var assetInfo = new MaterialAssetGroup.AssetInfo(trimKey.identifier().getPath());
        var assets = new MaterialAssetGroup(assetInfo, Map.of());

        TrimMaterial material = new TrimMaterial(
                assets,
                Component.translatable(Util.makeDescriptionId("trim_material", trimKey.identifier())).withStyle(style)
        );

        context.register(trimKey, material);
    }
}