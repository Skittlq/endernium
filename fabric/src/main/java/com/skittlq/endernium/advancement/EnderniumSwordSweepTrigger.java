package com.skittlq.endernium.advancement;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class EnderniumSwordSweepTrigger extends SimpleCriterionTrigger<EnderniumSwordSweepTrigger.Instance> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("endernium", "sword_sweep");
    public static final EnderniumSwordSweepTrigger INSTANCE = new EnderniumSwordSweepTrigger();

    private EnderniumSwordSweepTrigger() {
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, int mobsKilled) {
        this.trigger(player, instance -> instance.matches(mobsKilled));
    }

    public static Criterion<Instance> swept(int count) {
        return INSTANCE.createCriterion(new Instance(Optional.empty(), count));
    }

    public record Instance(Optional<ContextAwarePredicate> player, int count) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = Codec.INT.fieldOf("count").xmap(
                value -> new Instance(Optional.empty(), value),
                Instance::count
        ).codec();

        public boolean matches(int mobsKilled) {
            return mobsKilled >= count;
        }
    }
}
