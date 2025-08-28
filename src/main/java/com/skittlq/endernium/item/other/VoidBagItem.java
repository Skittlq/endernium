package com.skittlq.endernium.item.other;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;

public class VoidBagItem extends BundleItem {
    // This is a special variation of the bundle that allows for 4 times the storage capacity of a normal bundle. (256 items)
    public VoidBagItem(Properties properties) {
        super(properties.stacksTo(1).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack p_150742_, ItemStack p_150743_, Slot p_150744_, ClickAction p_150745_, Player p_150746_, SlotAccess p_150747_) {
        return super.overrideOtherStackedOnMe(p_150742_, p_150743_, p_150744_, p_150745_, p_150746_, p_150747_);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack p_150733_, Slot p_150734_, ClickAction p_150735_, Player p_150736_) {
        return super.overrideStackedOnOther(p_150733_, p_150734_, p_150735_, p_150736_);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return super.canFitInsideContainerItems();
    }

}
