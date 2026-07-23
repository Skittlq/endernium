package com.skittlq.endernium.datagen;

import com.skittlq.endernium.EnderniumConstants;
import com.skittlq.endernium.advancement.EnderniumSwordSweepTrigger;
import com.skittlq.endernium.item.EnderniumItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;

public final class EnderniumAdvancements {
    private EnderniumAdvancements() {
    }

    public static void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
                                PlaceholderFactory placeholderFactory) {
        AdvancementHolder freeTheEnd = placeholderFactory.create("minecraft:end/kill_dragon");

        AdvancementHolder getIngot = Advancement.Builder.advancement()
                .parent(freeTheEnd)
                .display(
                        EnderniumItems.ENDERNIUM_INGOT.get(),
                        Component.translatable("advancements.endernium.get_ingot.title"),
                        Component.translatable("advancements.endernium.get_ingot.desc"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(EnderniumItems.ENDERNIUM_INGOT.get()))
                .save(saver, id("get_ingot"));

        Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        EnderniumItems.ENDERNIUM_HOE.get(),
                        Component.translatable("advancements.endernium.obtain_hoe.title"),
                        Component.translatable("advancements.endernium.obtain_hoe.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("obtain_hoe", InventoryChangeTrigger.TriggerInstance.hasItems(EnderniumItems.ENDERNIUM_HOE.get()))
                .save(saver, id("obtain_hoe"));

        Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        EnderniumItems.ENDERNIUM_CHESTPLATE.get(),
                        Component.translatable("advancements.endernium.full_armor.title"),
                        Component.translatable("advancements.endernium.full_armor.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("has_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(EnderniumItems.ENDERNIUM_HELMET.get()))
                .addCriterion("has_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(EnderniumItems.ENDERNIUM_CHESTPLATE.get()))
                .addCriterion("has_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(EnderniumItems.ENDERNIUM_LEGGINGS.get()))
                .addCriterion("has_boots", InventoryChangeTrigger.TriggerInstance.hasItems(EnderniumItems.ENDERNIUM_BOOTS.get()))
                .requirements(AdvancementRequirements.allOf(List.of("has_helmet", "has_chestplate", "has_leggings", "has_boots")))
                .save(saver, id("full_armor"));

        Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        EnderniumItems.ENDERNIUM_SWORD.get(),
                        Component.translatable("advancements.endernium.sword_ability.title"),
                        Component.translatable("advancements.endernium.sword_ability.desc"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("kill_15_with_ability", EnderniumSwordSweepTrigger.swept(15))
                .save(saver, id("sword_ability"));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EnderniumConstants.MOD_ID, path);
    }

    @FunctionalInterface
    public interface PlaceholderFactory {
        AdvancementHolder create(String id);
    }
}
