package com.skittlq.endernium.datagen;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;

import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        EnderniumAdvancements.generate(registries, saver, AdvancementSubProvider::createPlaceholder);
    }
}