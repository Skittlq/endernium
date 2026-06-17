package com.skittlq.endernium;

import com.skittlq.endernium.datagen.ModAdvancementProvider;
import com.skittlq.endernium.datagen.ModBlockTagProvider;
import com.skittlq.endernium.datagen.ModDynamicRegistryProvider;
import com.skittlq.endernium.datagen.ModItemTagProvider;
import com.skittlq.endernium.datagen.ModModelProvider;
import com.skittlq.endernium.datagen.ModPlacedFeatureProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class EnderniumDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		var blockTagProvider = pack.addProvider(ModBlockTagProvider::new);

		pack.addProvider(ModModelProvider::new);
		pack.addProvider((output, registriesFuture) -> new ModItemTagProvider(output, registriesFuture, blockTagProvider));
		pack.addProvider(ModAdvancementProvider::new);
		pack.addProvider(ModPlacedFeatureProvider::new);
		pack.addProvider(ModDynamicRegistryProvider::new);
	}
}
