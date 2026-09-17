package com.skittlq.endernium.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.advancements.AdvancementSubProvider;

public class ModAdvancementProvider extends AdvancementSubProvider {
    public ModAdvancementProvider(BootstrapContext<Advancement> output) {
        super(output);
    }

    @Override
    public void generate() {
        EnderniumAdvancements.generate(advancement -> advancement.register(output));
    }
}
