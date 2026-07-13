package com.skittlq.endernium.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.worldgen.ModPlacedFeatures;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModPlacedFeatureProvider implements DataProvider {
    private static final Identifier ENDERNIUM_ORE_ID = Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_ore");

    private final PackOutput.PathProvider pathProvider;

    public ModPlacedFeatureProvider(FabricPackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "worldgen/placed_feature");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject root = new JsonObject();
        root.addProperty("feature", ENDERNIUM_ORE_ID.toString());

        JsonArray placement = new JsonArray();
        placement.add(countPlacement());
        placement.add(typeOnlyPlacement("minecraft:in_square"));
        placement.add(heightRangePlacement());
        placement.add(typeOnlyPlacement("endernium:dragon_defeated"));
        placement.add(typeOnlyPlacement("minecraft:biome"));
        root.add("placement", placement);

        return DataProvider.saveStable(cachedOutput, root, pathProvider.json(ModPlacedFeatures.ENDERNIUM_ORE_PLACED_KEY));
    }

    @Override
    public String getName() {
        return Endernium.MOD_ID + " Placed Feature Provider";
    }

    private static JsonObject countPlacement() {
        JsonObject count = new JsonObject();
        count.addProperty("type", "minecraft:count");
        count.addProperty("count", 9);
        return count;
    }

    private static JsonObject heightRangePlacement() {
        JsonObject placement = new JsonObject();
        placement.addProperty("type", "minecraft:height_range");

        JsonObject height = new JsonObject();
        height.addProperty("type", "minecraft:uniform");

        JsonObject minInclusive = new JsonObject();
        minInclusive.addProperty("absolute", -64);
        height.add("min_inclusive", minInclusive);

        JsonObject maxInclusive = new JsonObject();
        maxInclusive.addProperty("absolute", 80);
        height.add("max_inclusive", maxInclusive);

        placement.add("height", height);
        return placement;
    }

    private static JsonObject typeOnlyPlacement(String type) {
        JsonObject placement = new JsonObject();
        placement.addProperty("type", type);
        return placement;
    }
}
