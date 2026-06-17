package com.skittlq.endernium.trim;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public final class ModTrimMaterials {
    public static final ResourceKey<TrimMaterial> ENDERNIUM = ResourceKey.create(
            Registries.TRIM_MATERIAL,
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium")
    );

    private ModTrimMaterials() {
    }
}
