package com.skittlq.endernium.datagen;
import com.skittlq.endernium.advancement.EnderniumSwordSweepTrigger;
import com.skittlq.endernium.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<Advancement> consumer) {
        ResourceLocation freeTheEnd = ResourceLocation.fromNamespaceAndPath("minecraft", "end/kill_dragon");

        // Endernium Ingot
        Advancement getIngot = Advancement.Builder.advancement()
                .display(
                        ModItems.ENDERNIUM_INGOT.get().getDefaultInstance(),
                        Component.translatable("advancements.endernium.get_ingot.title"),
                        Component.translatable("advancements.endernium.get_ingot.desc"),
                        null, // background
                        FrameType.TASK, // FrameType: TASK, GOAL, or CHALLENGE
                        true, true, false
                ).parent(Advancement.Builder.advancement().build(freeTheEnd))
                .addCriterion("get_ingot",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_INGOT.get()))
                .save(consumer, "endernium:get_ingot");

        // Endernium Hoe
        Advancement obtainHoe = Advancement.Builder.advancement()
                .display(
                        ModItems.ENDERNIUM_HOE.get().getDefaultInstance(),
                        Component.translatable("advancements.endernium.obtain_hoe.title"),
                        Component.translatable("advancements.endernium.obtain_hoe.desc"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false
                )
                .parent(getIngot) // Link to the ingot advancement
                .addCriterion("obtain_hoe",
                        InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_HOE.get()))
                .save(consumer, "endernium:obtain_hoe");

        // Endernium Armor
        Advancement fullArmor = Advancement.Builder.advancement()
                .display(
                        ModItems.ENDERNIUM_CHESTPLATE.get().getDefaultInstance(),
                        Component.translatable("advancements.endernium.full_armor.title"),
                        Component.translatable("advancements.endernium.full_armor.desc"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false
                ).parent(getIngot) // Link to the ingot advancement
                .addCriterion("has_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_HELMET.get()))
                .addCriterion("has_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_CHESTPLATE.get()))
                .addCriterion("has_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_LEGGINGS.get()))
                .addCriterion("has_boots", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ENDERNIUM_BOOTS.get()))
                // requirements all-of: see below
                .save(consumer, "endernium:full_armor");

        // Endernium Sword Ability
        Advancement swordAbility = Advancement.Builder.advancement()
                .display(
                        ModItems.ENDERNIUM_SWORD.get().getDefaultInstance(),
                        Component.translatable("advancements.endernium.sword_ability.title"),
                        Component.translatable("advancements.endernium.sword_ability.desc"),
                        null,
                        FrameType.CHALLENGE,
                        true, true, false
                )
                .parent(getIngot) // Link to the ingot advancement
                .addCriterion("kill_15_with_ability",
                        EnderniumSwordSweepTrigger.swept(15))
                .save(consumer, "endernium:sword_ability");
    }

}
