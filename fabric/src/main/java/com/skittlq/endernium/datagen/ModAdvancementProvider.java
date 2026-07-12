package com.skittlq.endernium.datagen;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.advancement.EnderniumSwordSweepTrigger;
import com.skittlq.endernium.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends FabricAdvancementProvider {
    public ModAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        AdvancementHolder freeTheEnd = createPlaceholder(Identifier.fromNamespaceAndPath("minecraft", "end/kill_dragon"));

        AdvancementHolder getIngot = Advancement.Builder.advancement()
                .parent(freeTheEnd)
                .display(
                        ModItems.ENDERNIUM_INGOT,
                        Component.translatable("advancements.endernium.get_ingot.title"),
                        Component.translatable("advancements.endernium.get_ingot.desc"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_INGOT))
                .save(saver, Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "get_ingot"));

        Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        ModItems.ENDERNIUM_HOE,
                        Component.translatable("advancements.endernium.obtain_hoe.title"),
                        Component.translatable("advancements.endernium.obtain_hoe.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("obtain_hoe", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_HOE))
                .save(saver, Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "obtain_hoe"));

        Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        ModItems.ENDERNIUM_CHESTPLATE,
                        Component.translatable("advancements.endernium.full_armor.title"),
                        Component.translatable("advancements.endernium.full_armor.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("has_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_HELMET))
                .addCriterion("has_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_CHESTPLATE))
                .addCriterion("has_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_LEGGINGS))
                .addCriterion("has_boots", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_BOOTS))
                .requirements(AdvancementRequirements.allOf(List.of("has_helmet", "has_chestplate", "has_leggings", "has_boots")))
                .save(saver, Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "full_armor"));

        Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        ModItems.ENDERNIUM_SWORD,
                        Component.translatable("advancements.endernium.sword_ability.title"),
                        Component.translatable("advancements.endernium.sword_ability.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("kill_15_with_ability", EnderniumSwordSweepTrigger.swept(15))
                .save(saver, Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "sword_ability"));
    }
}

