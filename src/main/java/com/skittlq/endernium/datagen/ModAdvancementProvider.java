package com.skittlq.endernium.datagen;

import com.skittlq.endernium.advancement.EnderniumSwordSweepTrigger;
import com.skittlq.endernium.item.ModItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        // Parent: Free the End (end/kill_dragon)
        Identifier freeTheEnd = Identifier.fromNamespaceAndPath("minecraft", "end/kill_dragon");

        // Endernium Ingot
        AdvancementHolder getIngot = Advancement.Builder.advancement()
                .parent(freeTheEnd)
                .display(
                        ModItems.ENDERNIUM_INGOT.get().getDefaultInstance(),
                        Component.translatable("advancements.endernium.get_ingot.title"),
                        Component.translatable("advancements.endernium.get_ingot.desc"),
                        null,
                        AdvancementType.TASK, true, true, false
                )
                .addCriterion("get_ingot",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_INGOT.get()))
                .save(saver, Identifier.fromNamespaceAndPath("endernium", "get_ingot"));

        // Endernium Hoe
        AdvancementHolder obtainHoe = Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        ModItems.ENDERNIUM_HOE.get().getDefaultInstance(),
                        Component.translatable("advancements.endernium.obtain_hoe.title"),
                        Component.translatable("advancements.endernium.obtain_hoe.desc"),
                        null,
                        AdvancementType.CHALLENGE, true, true, false
                )
                .addCriterion("obtain_hoe",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_HOE.get()))
                .save(saver, Identifier.fromNamespaceAndPath("endernium", "obtain_hoe"));

        // Endernium Armor
        AdvancementHolder fullArmor = Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        ModItems.ENDERNIUM_CHESTPLATE.get().getDefaultInstance(),
                        Component.translatable("advancements.endernium.full_armor.title"),
                        Component.translatable("advancements.endernium.full_armor.desc"),
                        null,
                        AdvancementType.CHALLENGE, true, true, false
                )
                .addCriterion("has_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_HELMET.get()))
                .addCriterion("has_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_CHESTPLATE.get()))
                .addCriterion("has_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_LEGGINGS.get()))
                .addCriterion("has_boots", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_BOOTS.get()))
                .requirements(AdvancementRequirements.allOf(List.of(
                        "has_helmet", "has_chestplate", "has_leggings", "has_boots"
                )))
                .save(saver, Identifier.fromNamespaceAndPath("endernium", "full_armor"));

        // Sword Special Ability
        AdvancementHolder swordAbility = Advancement.Builder.advancement()
                .parent(getIngot)
                .display(
                        ModItems.ENDERNIUM_SWORD.get().getDefaultInstance(),
                        Component.translatable("advancements.endernium.sword_ability.title"),
                        Component.translatable("advancements.endernium.sword_ability.desc"),
                        null,
                        AdvancementType.CHALLENGE, true, true, false
                )
                .addCriterion("kill_15_with_ability",
                        EnderniumSwordSweepTrigger.swept(15))
                .save(saver, Identifier.fromNamespaceAndPath("endernium", "sword_ability"));
    }
}
