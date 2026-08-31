package com.skittlq.endernium.client.vfx;

import com.skittlq.endernium.config.EnderniumVisualConfig;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** Owns the deterministic client timeline and immutable extracted state for Endernium VFX. */
public final class EnderniumVfxManager {
    public static final int DRAGON_DEATH_DURATION = 200;
    public static final int BURST_DURATION = 72;
    public static final float WAVE_SPEED = 24.0F;
    private static final int MAX_WAVE_ARRIVAL_TICKS = 60;

    private static ClientLevel lastLevel;
    private static DragonTimeline dragonTimeline;
    private static ExtractedFrame extractedFrame = ExtractedFrame.EMPTY;

    private EnderniumVfxManager() {
    }

    public static void onDragonDeathVfx(DragonDeathVfxPayload payload) {
        if (!EnderniumVisualConfig.enabled()) {
            return;
        }

        switch (payload.action()) {
            case START -> {
                if (dragonTimeline == null || !dragonTimeline.dragonId.equals(payload.dragonId())) {
                    dragonTimeline = DragonTimeline.start(payload);
                } else {
                    dragonTimeline.syncStart(payload);
                }
            }
            case BURST -> {
                if (dragonTimeline == null || !dragonTimeline.dragonId.equals(payload.dragonId())) {
                    dragonTimeline = DragonTimeline.start(payload);
                }
                dragonTimeline.beginBurst(payload.origin(), payload.sequenceSeed());
            }
            case CANCEL -> {
                if (dragonTimeline != null && dragonTimeline.dragonId.equals(payload.dragonId())) {
                    dragonTimeline = null;
                    extractedFrame = ExtractedFrame.EMPTY;
                }
            }
        }
    }

    public static void tick(Minecraft client) {
        if (client.level != lastLevel) {
            clear();
            lastLevel = client.level;
        }
        if (client.level == null || client.player == null || !EnderniumVisualConfig.enabled()) {
            if (!EnderniumVisualConfig.enabled()) {
                dragonTimeline = null;
                extractedFrame = ExtractedFrame.EMPTY;
            }
            return;
        }
        if (dragonTimeline != null && !dragonTimeline.tick(client)) {
            dragonTimeline = null;
            extractedFrame = ExtractedFrame.EMPTY;
        }
    }

    public static void extract(Minecraft client) {
        if (dragonTimeline == null || client.level == null || !EnderniumVisualConfig.enabled()) {
            extractedFrame = ExtractedFrame.EMPTY;
            return;
        }
        float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        extractedFrame = dragonTimeline.extract(client, partialTick);
    }

    public static ExtractedFrame frame() {
        return extractedFrame;
    }

    public static void debugBuildup(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        Vec3 origin = client.player.position().add(client.player.getLookAngle().scale(18.0)).add(0.0, 8.0, 0.0);
        UUID id = new UUID(0x454e4445524e4955L, 0x4d56465844454255L);
        DragonDeathVfxPayload payload = new DragonDeathVfxPayload(
                DragonDeathVfxPayload.Action.START,
                id,
                -1,
                origin,
                130,
                0x5EED_DA7A_2026L
        );
        dragonTimeline = DragonTimeline.start(payload);
    }

    public static void debugBurst(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }
        Vec3 origin = client.player.getEyePosition().add(client.player.getLookAngle().scale(16.0)).add(0.0, 4.0, 0.0);
        UUID id = new UUID(0x454e4445524e4955L, 0x4d56465844454255L);
        DragonDeathVfxPayload payload = new DragonDeathVfxPayload(
                DragonDeathVfxPayload.Action.BURST,
                id,
                -1,
                origin,
                DRAGON_DEATH_DURATION,
                0x5EED_DA7A_2026L
        );
        onDragonDeathVfx(payload);
    }

    public static void clear() {
        dragonTimeline = null;
        extractedFrame = ExtractedFrame.EMPTY;
        lastLevel = null;
    }

    private static final class DragonTimeline {
        private final UUID dragonId;
        private final List<CometPath> comets;
        private int entityId;
        private long sequenceSeed;
        private Vec3 origin;
        private int deathTicks;
        private int burstAge = -1;
        private boolean waveHit;
        private int waveHitAge = -1;
        private int chargeSoundStage = -1;

        private DragonTimeline(UUID dragonId, int entityId, Vec3 origin, int deathTicks, long sequenceSeed) {
            this.dragonId = dragonId;
            this.entityId = entityId;
            this.origin = origin;
            this.deathTicks = Mth.clamp(deathTicks, 0, DRAGON_DEATH_DURATION - 1);
            this.sequenceSeed = sequenceSeed;
            this.comets = createComets(sequenceSeed, 72);
        }

        private static DragonTimeline start(DragonDeathVfxPayload payload) {
            DragonTimeline timeline = new DragonTimeline(
                    payload.dragonId(),
                    payload.entityId(),
                    payload.origin(),
                    payload.deathTicks(),
                    payload.sequenceSeed()
            );
            return timeline;
        }

        private void syncStart(DragonDeathVfxPayload payload) {
            entityId = payload.entityId();
            sequenceSeed = payload.sequenceSeed();
            origin = payload.origin();
            deathTicks = Math.max(deathTicks, Math.min(payload.deathTicks(), DRAGON_DEATH_DURATION - 1));
        }

        private void beginBurst(Vec3 finalOrigin, long seed) {
            origin = finalOrigin;
            sequenceSeed = seed;
            deathTicks = DRAGON_DEATH_DURATION;
            if (burstAge < 0) {
                burstAge = 0;
                playBurst(Minecraft.getInstance());
            }
        }

        private boolean tick(Minecraft client) {
            if (burstAge >= 0) {
                if (burstAge > 0) {
                    burstAge++;
                } else {
                    burstAge = 1;
                }
                tickWaveArrival(client);
                return burstAge <= BURST_DURATION;
            }

            Entity entity = client.level.getEntity(entityId);
            if (entity instanceof EnderDragon dragon && dragon.getUUID().equals(dragonId)) {
                origin = dragon.position().add(0.0, 2.0, 0.0);
                deathTicks = Math.max(deathTicks, dragon.dragonDeathTime);
            } else {
                deathTicks = Math.min(DRAGON_DEATH_DURATION - 1, deathTicks + 1);
            }
            tickFinalChargeSound(client);
            return true;
        }

        private void tickFinalChargeSound(Minecraft client) {
            if (deathTicks < 190 || client.level == null) {
                return;
            }
            int stage = deathTicks >= 199 ? 3 : deathTicks >= 197 ? 2 : deathTicks >= 194 ? 1 : 0;
            if (stage <= chargeSoundStage) {
                return;
            }
            chargeSoundStage = stage;
            float volume = 1.15F + stage * 0.18F;
            float pitch = switch (stage) {
                case 0 -> 0.48F;
                case 1 -> 0.64F;
                case 2 -> 0.83F;
                default -> 1.08F;
            };
            client.level.playLocalSound(
                    origin.x, origin.y, origin.z,
                    SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE,
                    volume, pitch, false
            );
        }

        private void tickWaveArrival(Minecraft client) {
            if (waveHit || client.player == null) {
                if (waveHitAge >= 0) {
                    waveHitAge++;
                }
                return;
            }
            double dx = client.player.getX() - origin.x;
            double dz = client.player.getZ() - origin.z;
            int arrival = Math.min(MAX_WAVE_ARRIVAL_TICKS, (int)Math.ceil(Math.sqrt(dx * dx + dz * dz) / WAVE_SPEED));
            if (burstAge >= arrival) {
                waveHit = true;
                waveHitAge = 0;
                client.level.playLocalSound(
                        client.player.getX(), client.player.getY(), client.player.getZ(),
                        SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 0.8F, 0.65F, false
                );
            }
        }

        private ExtractedFrame extract(Minecraft client, float partialTick) {
            Vec3 renderOrigin = origin;
            if (burstAge < 0) {
                Entity entity = client.level.getEntity(entityId);
                if (entity instanceof EnderDragon dragon && dragon.getUUID().equals(dragonId)) {
                    renderOrigin = new Vec3(
                            Mth.lerp(partialTick, dragon.xo, dragon.getX()),
                            Mth.lerp(partialTick, dragon.yo, dragon.getY()) + 2.0,
                            Mth.lerp(partialTick, dragon.zo, dragon.getZ())
                    );
                }
            }

            float postIntensity = 0.0F;
            float postPhase = 0.0F;
            if (waveHitAge >= 0
                    && waveHitAge < 6
                    && EnderniumVisualConfig.screenDistortion()
                    && EnderniumVisualConfig.observerScreenEffects()) {
                postPhase = waveHitAge + partialTick;
                float normalized = postPhase / 6.0F;
                postIntensity = (float)Math.sin(normalized * Math.PI) * 0.85F;
            }

            BuildupState buildup = burstAge < 0
                    ? new BuildupState(renderOrigin, Math.min(199.99F, deathTicks + partialTick), sequenceSeed)
                    : null;
            BurstState burst = burstAge >= 0
                    ? new BurstState(renderOrigin, burstAge + partialTick, sequenceSeed, comets)
                    : null;
            return new ExtractedFrame(buildup, burst, postIntensity, postPhase);
        }

        private static void playBurst(Minecraft client) {
            if (client.level == null || dragonTimeline == null) {
                return;
            }
            Vec3 at = dragonTimeline.origin;
            client.level.playLocalSound(at.x, at.y, at.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 4.0F, 0.55F, false);
            client.level.playLocalSound(at.x, at.y, at.z,
                    SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.HOSTILE, 1.8F, 1.35F, false);

            Random random = new Random(dragonTimeline.sequenceSeed ^ 0xFA11BACCL);
            for (int i = 0; i < 28; i++) {
                Vec3 direction = randomDirection(random);
                double speed = 0.6 + random.nextDouble() * 1.8;
                client.level.addParticle(
                        i % 3 == 0
                                ? ParticleTypes.PORTAL
                                : PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F),
                        at.x + direction.x * 2.0,
                        at.y + direction.y * 2.0,
                        at.z + direction.z * 2.0,
                        direction.x * speed,
                        direction.y * speed,
                        direction.z * speed
                );
            }
        }
    }

    private static List<CometPath> createComets(long seed, int count) {
        Random random = new Random(seed ^ 0xD4A60F5EEDL);
        Vec3[] launchClusters = createLaunchClusters(seed);
        List<CometPath> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double y;
            if (random.nextFloat() < 0.76F) {
                y = -0.18 + random.nextDouble() * 0.66;
            } else {
                y = -0.82 + random.nextDouble() * 1.72;
            }
            Vec3 direction = new Vec3(Math.cos(angle), y, Math.sin(angle)).normalize();
            Vec3 tangent = new Vec3(-direction.z, random.nextDouble() * 0.32 - 0.16, direction.x).normalize();
            float delay = random.nextInt(8);
            float speed = 7.0F + random.nextFloat() * 8.5F;
            float acceleration = 0.08F + random.nextFloat() * 0.22F;
            float curve = (random.nextFloat() - 0.5F) * 0.026F;
            float width = 0.18F + random.nextFloat() * 0.34F;
            float trailLength = 18.0F + random.nextFloat() * 46.0F;
            float lifetime = 28.0F + random.nextFloat() * 28.0F;
            long traits = mixSeed(seed + i * 0x9E3779B97F4A7C15L);
            int clusterIndex = Math.floorMod((int)traits, launchClusters.length);
            int colorRoll = (int)(traits >>> 9) & 15;
            int headVariant = colorRoll == 0 ? 2 : colorRoll < 4 ? 1 : 0;
            result.add(new CometPath(
                    launchClusters[clusterIndex], direction, tangent, delay, speed, acceleration,
                    curve, width, trailLength, lifetime, headVariant
            ));
        }
        return Collections.unmodifiableList(result);
    }

    private static Vec3[] createLaunchClusters(long seed) {
        Random random = new Random(seed ^ 0xC1A57E4D0B1L);
        Vec3[] clusters = new Vec3[6];
        clusters[0] = Vec3.ZERO;
        for (int i = 1; i < clusters.length; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double radius = 0.55 + random.nextDouble() * 1.75;
            clusters[i] = new Vec3(
                    Math.cos(angle) * radius,
                    -1.15 + random.nextDouble() * 2.7,
                    Math.sin(angle) * radius
            );
        }
        return clusters;
    }

    private static long mixSeed(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private static Vec3 randomDirection(Random random) {
        Vec3 direction;
        do {
            direction = new Vec3(
                    random.nextDouble() * 2.0 - 1.0,
                    random.nextDouble() * 2.0 - 1.0,
                    random.nextDouble() * 2.0 - 1.0
            );
        } while (direction.lengthSqr() < 1.0E-4);
        return direction.normalize();
    }

    public record ExtractedFrame(BuildupState buildup, BurstState burst, float postIntensity, float postPhase) {
        public static final ExtractedFrame EMPTY = new ExtractedFrame(null, null, 0.0F, 0.0F);
        public boolean empty() {
            return buildup == null && burst == null && postIntensity <= 0.0F;
        }
    }

    public record BuildupState(Vec3 origin, float deathTicks, long seed) {
    }

    public record BurstState(Vec3 origin, float age, long seed, List<CometPath> comets) {
    }

    public record CometPath(
            Vec3 launchOffset,
            Vec3 direction,
            Vec3 tangent,
            float delay,
            float speed,
            float acceleration,
            float curve,
            float width,
            float trailLength,
            float lifetime,
            int headVariant
    ) {
        public Vec3 position(Vec3 origin, float age) {
            float t = Math.max(0.0F, age - delay);
            double distance = speed * t + acceleration * t * t;
            return origin.add(launchOffset).add(direction.scale(distance)).add(tangent.scale(curve * t * t));
        }
    }
}
