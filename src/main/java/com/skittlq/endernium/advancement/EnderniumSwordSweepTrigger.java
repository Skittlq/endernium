package com.skittlq.endernium.advancement;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class EnderniumSwordSweepTrigger extends SimpleCriterionTrigger<EnderniumSwordSweepTrigger.Instance> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("endernium", "sword_sweep");
    public static final EnderniumSwordSweepTrigger INSTANCE = new EnderniumSwordSweepTrigger();

    private EnderniumSwordSweepTrigger() {}

    @Override
    public Codec<Instance> codec() { return Instance.CODEC; }

    // Call this when the ability is used
    public void trigger(ServerPlayer player, int mobsKilled) {
        this.trigger(player, instance -> instance.matches(mobsKilled));
    }

    // For advancement criterion
    public static Criterion<Instance> swept(int count) {
        return INSTANCE.createCriterion(new Instance(Optional.empty(), count));
    }

    public record Instance(Optional<ContextAwarePredicate> player, int count)
            implements SimpleCriterionTrigger.SimpleInstance {

        // Codec for data gen (no predicates, just the count)
        public static final Codec<Instance> CODEC =
                Codec.INT.fieldOf("count").xmap(
                        val -> new Instance(Optional.empty(), val),
                        Instance::count
                ).codec();

        public boolean matches(int mobsKilled) {
            return mobsKilled >= count;
        }
    }
}
