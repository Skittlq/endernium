package com.skittlq.endernium.item;

import com.skittlq.endernium.item.armor.EnderniumBoots;
import com.skittlq.endernium.item.armor.EnderniumChestplate;
import com.skittlq.endernium.item.armor.EnderniumHelmet;
import com.skittlq.endernium.item.armor.EnderniumLeggings;
import com.skittlq.endernium.item.armor.ModArmorMaterial;
import com.skittlq.endernium.item.tools.EnderniumAxe;
import com.skittlq.endernium.item.tools.EnderniumHoe;
import com.skittlq.endernium.item.tools.EnderniumPickaxe;
import com.skittlq.endernium.item.tools.EnderniumShovel;
import com.skittlq.endernium.item.tools.EnderniumSword;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public enum EnderniumItems {
    ENDERNIUM_DUST("endernium_dust", Item::new),
    ENDERNIUM_SHARD("endernium_shard", Item::new),
    ENDERNIUM_INGOT("endernium_ingot", properties -> new Item(properties.fireResistant())),
    ENDERNIUM_UPGRADE_SMITHING_TEMPLATE("endernium_upgrade_smithing_template", EnderniumItems::createEnderniumUpgradeTemplate),
    ENDERNIUM_SWORD("endernium_sword", properties -> new EnderniumSword(properties.fireResistant())),
    ENDERNIUM_SPEAR("endernium_spear", properties -> new Item(properties.spear(
            ModToolTiers.ENDERNIUM,
            1.15F,
            1.2F,
            0.4F,
            2.5F,
            9.0F,
            5.5F,
            5.1F,
            8.75F,
            4.6F
    ).fireResistant())),
    ENDERNIUM_SHOVEL("endernium_shovel", properties -> new EnderniumShovel(properties.fireResistant())),
    ENDERNIUM_PICKAXE("endernium_pickaxe", properties -> new EnderniumPickaxe(properties.fireResistant())),
    ENDERNIUM_AXE("endernium_axe", properties -> new EnderniumAxe(properties.fireResistant())),
    ENDERNIUM_HOE("endernium_hoe", properties -> new EnderniumHoe(properties.fireResistant())),
    ENDERNIUM_HELMET("endernium_helmet", EnderniumHelmet::new),
    ENDERNIUM_CHESTPLATE("endernium_chestplate", EnderniumChestplate::new),
    ENDERNIUM_LEGGINGS("endernium_leggings", EnderniumLeggings::new),
    ENDERNIUM_BOOTS("endernium_boots", EnderniumBoots::new),
    ENDERNIUM_HORSE_ARMOR("endernium_horse_armor", properties -> new Item(
            properties.horseArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL).fireResistant()
    )),
    ENDERNIUM_NAUTILUS_ARMOR("endernium_nautilus_armor", properties -> new Item(
            properties.nautilusArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL).fireResistant()
    ));

    private final String id;
    private final Function<Item.Properties, Item> factory;
    private Supplier<? extends Item> supplier = () -> {
        throw new IllegalStateException("Endernium item has not been bound to a loader registry yet");
    };

    EnderniumItems(String id, Function<Item.Properties, Item> factory) {
        this.id = id;
        this.factory = factory;
    }

    public String id() {
        return id;
    }

    public Item create(Item.Properties properties) {
        return factory.apply(properties);
    }

    public void bind(Supplier<? extends Item> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    public Item get() {
        return supplier.get();
    }

    public static SmithingTemplateItem createEnderniumUpgradeTemplate(Item.Properties properties) {
        return new SmithingTemplateItem(
                Component.translatable("upgrade.minecraft.endernium_upgrade.applies_to"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.ingredients"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.base_slot_description"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.additions_slot_description"),
                List.of(
                        Identifier.withDefaultNamespace("container/slot/helmet"),
                        Identifier.withDefaultNamespace("container/slot/chestplate"),
                        Identifier.withDefaultNamespace("container/slot/leggings"),
                        Identifier.withDefaultNamespace("container/slot/boots"),
                        Identifier.withDefaultNamespace("container/slot/horse_armor"),
                        Identifier.withDefaultNamespace("container/slot/nautilus_armor"),
                        Identifier.withDefaultNamespace("container/slot/sword"),
                        Identifier.withDefaultNamespace("container/slot/spear"),
                        Identifier.withDefaultNamespace("container/slot/axe"),
                        Identifier.withDefaultNamespace("container/slot/shovel"),
                        Identifier.withDefaultNamespace("container/slot/pickaxe"),
                        Identifier.withDefaultNamespace("container/slot/hoe")
                ),
                List.of(
                        Identifier.withDefaultNamespace("container/slot/ingot")
                ),
                properties
        );
    }
}
