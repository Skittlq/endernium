package com.skittlq.endernium.client.vfx;

import com.skittlq.endernium.client.vfx.EnderniumVfxManager.BlessingState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.BuildupState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.BurstState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.CometPath;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.DistantCometPath;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.DistantImpactState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.ExtractedFrame;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.PillarPulse;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.vfx.DragonDeathVfxTiming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/** Shader-pack-safe reconstruction of the synchronized Endernium VFX timeline. */
public final class EnderniumParticleFallback {
    static final int MAX_PARTICLES_PER_TICK = 120;
    static final List<EmissionCategory> EMISSION_PRIORITY = List.of(
            EmissionCategory.BLESSING,
            EmissionCategory.WAVE_ARRIVAL,
            EmissionCategory.DETONATION,
            EmissionCategory.BUILDUP,
            EmissionCategory.PILLARS,
            EmissionCategory.COMETS
    );
    private static long emissionEpoch;

    private EnderniumParticleFallback() {
    }

    public static void reset() {
        emissionEpoch = 0L;
    }

    public static void tick(Minecraft client, ExtractedFrame frame) {
        if (client.level == null || client.player == null || frame.empty()) {
            return;
        }
        int limit = particleLimit(client.options.particles().get());
        if (limit <= 0) {
            return;
        }
        EmissionBudget budget = new EmissionBudget(limit);
        long tickSeed = samplingSeed(client.level.getGameTime(), emissionEpoch++);

        // Priority order is intentional: personal story beats survive reduced particle settings.
        for (EmissionCategory category : activeCategories(frame)) {
            switch (category) {
                case BLESSING -> emitBlessings(client.level, client.player.position(), frame.blessings(), budget, tickSeed);
                case WAVE_ARRIVAL -> emitWaveArrival(client.level, client.player.position(), frame, budget, tickSeed);
                case DETONATION -> emitDetonation(client.level, client.player.position(), frame.burst(), budget, tickSeed);
                case BUILDUP -> emitBuildup(client.level, client.player.position(), frame.buildup(), budget, tickSeed);
                case PILLARS -> emitPillars(client.level, client.player.position(), frame, budget, tickSeed);
                case COMETS -> emitComets(client.level, client.player.position(), frame.burst(), budget, tickSeed);
            }
        }
    }

    static long samplingSeed(long gameTime, long epoch) {
        return gameTime ^ epoch * 0x9E3779B97F4A7C15L;
    }

    static List<EmissionCategory> activeCategories(ExtractedFrame frame) {
        return EMISSION_PRIORITY.stream().filter(category -> switch (category) {
            case BLESSING -> !frame.blessings().isEmpty();
            case WAVE_ARRIVAL -> frame.postIntensity() > 0.01F
                    || frame.atmosphereIntensity() > 0.01F
                    || !frame.distantImpacts().isEmpty()
                    || frame.burst() != null;
            case DETONATION -> frame.burst() != null && frame.burst().age() <= 6.0F;
            case BUILDUP -> frame.buildup() != null;
            case PILLARS -> frame.burst() != null && !frame.pillars().isEmpty();
            case COMETS -> frame.burst() != null;
        }).toList();
    }

    static int particleLimit(Object status) {
        String name = status instanceof Enum<?> value ? value.name() : String.valueOf(status);
        return switch (name.toUpperCase(java.util.Locale.ROOT)) {
            case "MINIMAL" -> 32;
            case "DECREASED" -> 72;
            default -> MAX_PARTICLES_PER_TICK;
        };
    }

    static int distanceScaledCount(int requested, double distance) {
        if (requested <= 0 || distance > 768.0) {
            return 0;
        }
        double scale = distance <= 96.0 ? 1.0
                : distance <= 256.0 ? 0.72
                : distance <= 512.0 ? 0.42
                : 0.20;
        return Math.max(1, (int)Math.floor(requested * scale));
    }

    private static void emitBlessings(ClientLevel level, Vec3 observer, List<BlessingState> blessings,
                                        EmissionBudget budget, long tickSeed) {
        for (BlessingState state : blessings) {
            int count = distanceScaledCount(10, observer.distanceTo(state.chest()));
            Random random = random(tickSeed ^ state.seed() ^ (long)state.age());
            Vec3 core = state.corePosition(state.age());
            spawn(level, budget, EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(), core,
                    new Vec3(0.0, 0.018, 0.0));
            for (int i = 0; i < count - 1; i++) {
                double angle = state.age() * 0.32 + i * Mth.TWO_PI / Math.max(1, count - 1);
                double radius = 0.34 + 0.22 * Math.sin(state.age() * 0.13 + i);
                Vec3 position = state.chest().add(
                        Math.cos(angle) * radius,
                        Math.sin(angle * 1.7) * state.playerHeight() * 0.22,
                        Math.sin(angle) * radius
                );
                spawn(level, budget, i % 4 == 0 ? ParticleTypes.END_ROD : EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(),
                        position, jitter(random, 0.018).add(0.0, 0.025, 0.0));
            }
            if (state.pulse() > 0.72F) {
                spawn(level, budget, EnderniumParticles.ENDERNIUM_SWEEP.get(), state.chest(), Vec3.ZERO);
                spawn(level, budget, ParticleTypes.END_ROD, state.chest(), Vec3.ZERO);
            }
        }
    }

    private static void emitWaveArrival(ClientLevel level, Vec3 observer, ExtractedFrame frame,
                                         EmissionBudget budget, long tickSeed) {
        Random random = random(tickSeed ^ 0x57415645L);
        BurstState burst = frame.burst();
        if (burst != null) {
            if (burst.distantObserver() && burst.distantWave() != null) {
                emitDistantWaveFront(level, observer, burst, budget, random);
            } else if (!burst.distantObserver()) {
                emitIslandWaveArc(level, observer, burst, budget, random);
            }
        }
        if (frame.postIntensity() > 0.01F || frame.atmosphereIntensity() > 0.01F) {
            int count = 22;
            for (int i = 0; i < count; i++) {
                double angle = i * Mth.TWO_PI / count + random.nextDouble() * 0.1;
                double radius = 3.0 + frame.postPhase() * 5.5 + random.nextDouble() * 3.0;
                Vec3 at = observer.add(Math.cos(angle) * radius, 0.15 + random.nextDouble() * 1.2,
                        Math.sin(angle) * radius);
                spawn(level, budget, i % 5 == 0 ? EnderniumParticles.ENDERNIUM_SWEEP.get()
                        : EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(), at,
                        new Vec3(Math.cos(angle) * 0.12, 0.03, Math.sin(angle) * 0.12));
            }
        }
        for (DistantImpactState impact : frame.distantImpacts()) {
            int count = distanceScaledCount(12, observer.distanceTo(impact.position()));
            for (int i = 0; i < count; i++) {
                Vec3 velocity = randomHorizontal(random).scale(0.12 + random.nextDouble() * 0.32)
                        .add(0.0, 0.08 + random.nextDouble() * 0.24, 0.0);
                spawn(level, budget, i == 0 ? EnderniumParticles.ENDERNIUM_SWEEP.get()
                        : EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(), impact.position(), velocity);
            }
        }
    }

    private static void emitIslandWaveArc(ClientLevel level, Vec3 observer, BurstState burst,
                                           EmissionBudget budget, Random random) {
        double radius = burst.age() * DragonDeathVfxTiming.WAVE_SPEED_BLOCKS_PER_TICK;
        double observerRadius = DragonDeathVfxTiming.horizontalDistance(burst.origin(), observer);
        if (radius <= 0.0 || radius > 1700.0 || Math.abs(observerRadius - radius) > 160.0) {
            return;
        }
        double centerAngle = Math.atan2(observer.z - burst.origin().z, observer.x - burst.origin().x);
        int count = distanceScaledCount(16, Math.abs(observerRadius - radius));
        for (int i = 0; i < count; i++) {
            double offset = count == 1 ? 0.0 : Mth.lerp(i / (double)(count - 1), -0.55, 0.55);
            double angle = centerAngle + offset;
            double noisyRadius = radius + (random.nextDouble() - 0.5) * 5.5;
            Vec3 at = burst.origin().add(Math.cos(angle) * noisyRadius,
                    (random.nextDouble() - 0.5) * 1.3, Math.sin(angle) * noisyRadius);
            spawn(level, budget, i % 5 == 0 ? EnderniumParticles.ENDERNIUM_SWEEP.get()
                    : EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(), at,
                    new Vec3(Math.cos(angle) * 0.18, 0.025, Math.sin(angle) * 0.18));
        }
    }

    private static void emitDistantWaveFront(ClientLevel level, Vec3 observer, BurstState burst,
                                              EmissionBudget budget, Random random) {
        EnderniumVfxManager.DistantWaveState wave = burst.distantWave();
        float localAge = burst.age() - wave.arrivalTick();
        if (localAge < -5.0F || localAge > 10.0F) {
            return;
        }
        Vec3 lead = wave.anchor().add(wave.direction().scale(
                localAge * DragonDeathVfxTiming.WAVE_SPEED_BLOCKS_PER_TICK));
        Vec3 lateral = new Vec3(-wave.direction().z, 0.0, wave.direction().x);
        int count = 20;
        for (int i = 0; i < count; i++) {
            double side = Mth.lerp(i / (double)(count - 1), -72.0, 72.0);
            Vec3 at = lead.add(lateral.scale(side)).add(0.0,
                    wave.originY() - lead.y + 0.4 + random.nextDouble() * 1.6, 0.0);
            spawn(level, budget, i % 6 == 0 ? EnderniumParticles.ENDERNIUM_SWEEP.get()
                    : EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(), at,
                    wave.direction().scale(0.2).add(0.0, 0.025, 0.0));
        }
    }

    private static void emitDetonation(ClientLevel level, Vec3 observer, BurstState burst,
                                        EmissionBudget budget, long tickSeed) {
        if (burst == null || burst.age() > 6.0F) {
            return;
        }
        int count = distanceScaledCount(burst.age() < 2.0F ? 34 : 18, observer.distanceTo(burst.origin()));
        Random random = random(tickSeed ^ burst.seed());
        if (burst.age() < 2.0F) {
            spawn(level, budget, ParticleTypes.END_ROD, burst.origin(), Vec3.ZERO);
            spawn(level, budget, EnderniumParticles.ENDERNIUM_SWEEP.get(), burst.origin(), Vec3.ZERO);
        }
        for (int i = 0; i < count; i++) {
            Vec3 direction = randomUnit(random);
            double speed = 0.35 + random.nextDouble() * 1.55;
            spawn(level, budget, i % 4 == 0 ? ParticleTypes.PORTAL
                    : EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(),
                    burst.origin().add(direction.scale(1.5)), direction.scale(speed));
        }
    }

    private static void emitBuildup(ClientLevel level, Vec3 observer, BuildupState buildup,
                                     EmissionBudget budget, long tickSeed) {
        if (buildup == null) {
            return;
        }
        float charge = Mth.clamp((buildup.deathTicks() - 120.0F) / 80.0F, 0.08F, 1.0F);
        int count = distanceScaledCount(3 + (int)(charge * 13.0F), observer.distanceTo(buildup.origin()));
        Random random = random(tickSeed ^ buildup.seed());
        for (int i = 0; i < count; i++) {
            double angle = buildup.deathTicks() * 0.16 + i * Mth.TWO_PI / Math.max(1, count);
            double radius = 1.5 + (1.0 - charge) * 5.5 + random.nextDouble() * 1.5;
            Vec3 at = buildup.origin().add(Math.cos(angle) * radius,
                    (random.nextDouble() - 0.5) * (3.0 + radius * 0.35), Math.sin(angle) * radius);
            Vec3 pull = buildup.origin().subtract(at).normalize().scale(0.08 + charge * 0.17);
            spawn(level, budget, i % 3 == 0 ? ParticleTypes.PORTAL
                    : EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(), at, pull);
        }
        if (charge > 0.65F) {
            spawn(level, budget, ParticleTypes.END_ROD, buildup.origin(), jitter(random, 0.035));
        }
    }

    private static void emitPillars(ClientLevel level, Vec3 observer, ExtractedFrame frame,
                                     EmissionBudget budget, long tickSeed) {
        BurstState burst = frame.burst();
        if (burst == null) {
            return;
        }
        Random random = random(tickSeed ^ 0x50494C4C415253L);
        for (PillarPulse pillar : frame.pillars()) {
            float localAge = burst.age() - pillar.arrivalAge();
            if (localAge < -1.0F || localAge > 10.0F) {
                continue;
            }
            int count = distanceScaledCount(5, observer.distanceTo(pillar.bottom()));
            for (int i = 0; i < count; i++) {
                double height = random.nextDouble() * Math.max(1.0, pillar.top().y - pillar.bottom().y);
                double angle = random.nextDouble() * Mth.TWO_PI;
                double radius = Math.min(1.2, pillar.surfaceRadius() * 0.35);
                Vec3 at = pillar.bottom().add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
                spawn(level, budget, EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(), at,
                        new Vec3(0.0, 0.14 + random.nextDouble() * 0.22, 0.0));
            }
        }
    }

    private static void emitComets(ClientLevel level, Vec3 observer, BurstState burst,
                                    EmissionBudget budget, long tickSeed) {
        if (burst == null) {
            return;
        }
        Random random = random(tickSeed ^ 0x434F4D455453L);
        if (burst.distantObserver() && burst.distantWave() != null) {
            for (DistantCometPath path : burst.distantWave().comets()) {
                if (burst.age() < path.startAge() || burst.age() > path.endAge()) {
                    continue;
                }
                emitTrail(level, observer, budget, random, path.position(burst.age()),
                        path.position(Math.max(path.startAge(), burst.age() - 1.2F)));
            }
            return;
        }
        for (CometPath path : burst.comets()) {
            if (burst.age() < path.delay() || burst.age() > path.delay() + path.lifetime()) {
                continue;
            }
            Vec3 head = path.position(burst.origin(), burst.age());
            Vec3 tail = path.position(burst.origin(), Math.max(path.delay(), burst.age() - 0.8F));
            emitTrail(level, observer, budget, random, head, tail);
        }
    }

    private static void emitTrail(ClientLevel level, Vec3 observer, EmissionBudget budget, Random random,
                                  Vec3 head, Vec3 tail) {
        int count = distanceScaledCount(3, observer.distanceTo(head));
        for (int i = 0; i < count; i++) {
            double t = count == 1 ? 0.0 : i / (double)(count - 1);
            Vec3 at = head.lerp(tail, t).add(jitter(random, 0.08));
            spawn(level, budget, i == 0 ? ParticleTypes.END_ROD
                    : EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(), at, jitter(random, 0.025));
        }
    }

    private static void spawn(ClientLevel level, EmissionBudget budget, ParticleOptions particle,
                              Vec3 position, Vec3 velocity) {
        if (!budget.tryTake()) {
            return;
        }
        level.addParticle(particle, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
    }

    private static Random random(long seed) {
        return new Random(seed * 0xD1342543DE82EF95L + 0x9E3779B97F4A7C15L);
    }

    private static Vec3 jitter(Random random, double amount) {
        return new Vec3((random.nextDouble() - 0.5) * amount,
                (random.nextDouble() - 0.5) * amount,
                (random.nextDouble() - 0.5) * amount);
    }

    private static Vec3 randomHorizontal(Random random) {
        double angle = random.nextDouble() * Mth.TWO_PI;
        return new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
    }

    private static Vec3 randomUnit(Random random) {
        Vec3 result;
        do {
            result = jitter(random, 2.0);
        } while (result.lengthSqr() < 1.0E-5);
        return result.normalize();
    }

    static final class EmissionBudget {
        private int remaining;

        EmissionBudget(int limit) {
            remaining = Math.max(0, Math.min(MAX_PARTICLES_PER_TICK, limit));
        }

        boolean tryTake() {
            if (remaining <= 0) {
                return false;
            }
            remaining--;
            return true;
        }

        int remaining() {
            return remaining;
        }
    }

    enum EmissionCategory {
        BLESSING,
        WAVE_ARRIVAL,
        DETONATION,
        BUILDUP,
        PILLARS,
        COMETS
    }
}
