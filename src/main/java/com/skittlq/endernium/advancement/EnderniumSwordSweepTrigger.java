package com.skittlq.endernium.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class EnderniumSwordSweepTrigger extends SimpleCriterionTrigger<EnderniumSwordSweepTrigger.Instance> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("endernium", "sword_sweep");
    public static final EnderniumSwordSweepTrigger INSTANCE = new EnderniumSwordSweepTrigger();

    private EnderniumSwordSweepTrigger() {}

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        int count = GsonHelper.getAsInt(json, "count", 15); // Default 15 if missing
        return new Instance(player, count);
    }

    public void trigger(ServerPlayer player, int mobsKilled) {
        this.trigger(player, instance -> instance.matches(mobsKilled));
    }

    public static Criterion swept(int count) {
        return new Criterion(new Instance(ContextAwarePredicate.ANY, count));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final int count;

        public Instance(ContextAwarePredicate player, int count) {
            super(ID, player);
            this.count = count;
        }

        public boolean matches(int mobsKilled) {
            return mobsKilled >= count;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("count", count);
            return json;
        }
    }
}
