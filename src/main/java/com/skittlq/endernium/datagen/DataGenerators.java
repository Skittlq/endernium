package com.skittlq.endernium.datagen;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Endernium.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper efh = event.getExistingFileHelper();

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        if (event.includeClient()) {
            generator.addProvider(true, new ModItemModelProvider(packOutput, efh));
            generator.addProvider(true, new ModBlockStateProvider(packOutput, efh));
            // Any other client providers here.
        }

        if (event.includeServer()) {
            generator.addProvider(true, new ModDatapackProvider(packOutput, lookupProvider));
            generator.addProvider(event.includeServer(),
                    new ModBlockTagGenerator(packOutput, lookupProvider, efh));
            generator.addProvider(event.includeServer(),
                    new ModItemTagProvider(packOutput, lookupProvider, CompletableFuture.completedFuture(null)));

        }
    }
}
