package com.skittlq.endernium.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddItemModifier extends LootModifier {
    public static final MapCodec<AddItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).and(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(e -> e.item))
                    .and(Codec.INT.optionalFieldOf("min_count", 1).forGetter(e -> e.minCount))
                    .and(Codec.INT.optionalFieldOf("max_count", 1).forGetter(e -> e.maxCount))
                    .apply(inst, AddItemModifier::new));
    private final Item item;
    private final int minCount;
    private final int maxCount;

    public AddItemModifier(LootItemCondition[] conditionsIn, int priority, Item item, int minCount, int maxCount) {
        super(conditionsIn, priority);
        this.item = item;
        this.minCount = Math.max(1, minCount);
        this.maxCount = Math.max(this.minCount, maxCount);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext lootContext) {
        int count = this.minCount == this.maxCount
                ? this.minCount
                : Mth.nextInt(lootContext.getRandom(), this.minCount, this.maxCount);
        generatedLoot.add(new ItemStack(this.item, count));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
