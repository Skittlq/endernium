package com.skittlq.endernium.client.vfx;

import com.skittlq.endernium.config.EnderniumVisualConfig;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.vfx.DragonDeathVfxTiming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Owns the deterministic client timeline and immutable extracted state for Endernium VFX. */
public final class EnderniumVfxManager {
    public static final int DRAGON_DEATH_DURATION = 200;
    public static final int BURST_DURATION = 88;
    public static final double DISTANT_PRESENTATION_DISTANCE = 640.0;
    static final float IMPACT_FRAME_FLIP_AGE = 1.6F;
    static final float IMPACT_FULL_INTENSITY_END_AGE = 3.2F;
    static final float IMPACT_END_AGE = 4.4F;
    private static final float DETONATION_SHAKE_DURATION = 3.0F;
    private static final int MAX_CRACK_PATCHES = 128;
    private static final double MAIN_ISLAND_IMPACT_RADIUS = 512.0;

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

    public static void clear() {
        dragonTimeline = null;
        extractedFrame = ExtractedFrame.EMPTY;
        lastLevel = null;
    }

    private static final class DragonTimeline {
        private final UUID dragonId;
        private final List<CometPath> comets;
        private final List<CrackPatch> crackPatches = new ArrayList<>();
        private final Set<Long> crackedColumns = new HashSet<>();
        private final List<PillarPulse> pillarPulses = new ArrayList<>();
        private final List<DistantImpactFlash> distantImpactFlashes = new ArrayList<>();
        private final Set<Integer> triggeredDistantImpacts = new HashSet<>();
        private int entityId;
        private long sequenceSeed;
        private Vec3 origin;
        private int deathTicks;
        private int burstAge = -1;
        private boolean waveHit;
        private int waveHitAge = -1;
        private int chargeSoundStage = -1;
        private boolean pillarsScanned;
        private boolean observerModeLocked;
        private boolean distantObserver;
        private int observerArrivalTick = -1;
        private DistantWaveState distantWave;

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
                lockObserverMode(Minecraft.getInstance());
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
                lockObserverMode(client);
                tickDistantPresentation(client);
                tickWaveArrival(client);
                expireCracks();
                distantImpactFlashes.removeIf(flash -> burstAge - flash.bornAge() > 4);
                scanPillars(client.level);
                int requiredDuration = observerArrivalTick < 0
                        ? BURST_DURATION
                        : Math.max(BURST_DURATION, observerArrivalTick + 28);
                return burstAge <= requiredDuration;
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

        private void lockObserverMode(Minecraft client) {
            if (observerModeLocked || client.player == null) {
                return;
            }
            observerModeLocked = true;
            distantObserver = horizontalDistance(origin, client.player.position()) > DISTANT_PRESENTATION_DISTANCE;
            observerArrivalTick = DragonDeathVfxTiming.waveArrivalTick(origin, client.player.position());
        }

        private void tickDistantPresentation(Minecraft client) {
            if (!distantObserver || client.player == null || client.level == null) {
                return;
            }
            if (distantWave == null) {
                int arrival = observerArrivalTick;
                if (burstAge >= arrival - 10) {
                    Vec3 anchor = client.player.position();
                    Vec3 direction = horizontalDirection(origin, anchor, sequenceSeed);
                    distantWave = createDistantWave(client.level, anchor, direction, arrival);
                }
            }
            if (distantWave == null) {
                return;
            }
            List<DistantCometPath> paths = distantWave.comets();
            for (int i = 0; i < paths.size(); i++) {
                DistantCometPath path = paths.get(i);
                if (!path.landing()
                        || triggeredDistantImpacts.contains(i)
                        || burstAge < path.endAge()) {
                    continue;
                }
                triggeredDistantImpacts.add(i);
                triggerDistantImpact(client.level, path, i);
            }
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
                    if (waveHitAge < 4) {
                        spawnTerrainArrival(client, waveHitAge);
                    }
                }
                return;
            }
            int arrival = observerArrivalTick >= 0
                    ? observerArrivalTick
                    : DragonDeathVfxTiming.waveArrivalTick(origin, client.player.position());
            if (burstAge >= arrival) {
                waveHit = true;
                waveHitAge = 0;
                client.level.playLocalSound(
                        client.player.getX(), client.player.getY(), client.player.getZ(),
                        SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 0.8F, 0.65F, false
                );
                spawnTerrainArrival(client, 0);
            }
        }

        private void spawnTerrainArrival(Minecraft client, int arrivalAge) {
            if (client.level == null || client.player == null || arrivalAge < 0 || arrivalAge >= 4) {
                return;
            }
            int patchLimit = EnderniumVisualConfig.cinematic() ? MAX_CRACK_PATCHES : MAX_CRACK_PATCHES / 2;
            int perTick = EnderniumVisualConfig.cinematic() ? 32 : 16;
            int stride = EnderniumVisualConfig.cinematic() ? 2 : 3;
            int radius = 64;
            int centerX = Mth.floor(client.player.getX());
            int centerZ = Mth.floor(client.player.getZ());
            Vec3 direction = horizontalDirection(origin, client.player.position(), sequenceSeed);
            double stripCenter = -24.0 + arrivalAge * 16.0;
            List<TerrainCandidate> candidates = new ArrayList<>();

            for (int x = centerX - radius; x <= centerX + radius; x += stride) {
                for (int z = centerZ - radius; z <= centerZ + radius; z += stride) {
                    double dx = x + 0.5 - client.player.getX();
                    double dz = z + 0.5 - client.player.getZ();
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    double along = dx * direction.x + dz * direction.z;
                    if (Math.abs(along - stripCenter) > 7.5) {
                        continue;
                    }

                    long hash = mixSeed(sequenceSeed ^ BlockPos.asLong(x, 0, z));
                    if ((hash & 15L) > 4L) {
                        continue;
                    }
                    candidates.add(new TerrainCandidate(x, z, hash));
                }
            }
            candidates.sort((left, right) -> Long.compareUnsigned(left.hash(), right.hash()));

            int added = 0;
            for (TerrainCandidate candidate : candidates) {
                if (added >= perTick || crackPatches.size() >= patchLimit) {
                    break;
                }
                int x = candidate.x();
                int z = candidate.z();
                long hash = candidate.hash();
                BlockPos probe = new BlockPos(x, Mth.floor(client.player.getY()), z);
                if (!client.level.hasChunkAt(probe)) {
                    continue;
                }
                int surfaceY = client.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockPos surface = new BlockPos(x, surfaceY, z);
                BlockState state = client.level.getBlockState(surface);
                if (!state.is(Blocks.END_STONE) || !crackedColumns.add(surface.asLong())) {
                    continue;
                }

                float angle = (float)(((hash >>> 11) & 0xFFFFFFL) / (double)0xFFFFFF * Mth.TWO_PI);
                float scale = 0.75F + (float)(((hash >>> 35) & 0xFFFFL) / 65535.0) * 1.45F;
                int lifetime = 16 + Math.floorMod((int)(hash >>> 51), 9);
                crackPatches.add(new CrackPatch(
                        new Vec3(x + 0.5, surfaceY + 1.012, z + 0.5),
                        angle,
                        scale,
                        burstAge,
                        lifetime,
                        hash
                ));
                spawnTerrainParticles(client.level, surface, state, direction, hash, arrivalAge);
                added++;
            }
        }

        private void spawnTerrainParticles(
                ClientLevel level,
                BlockPos surface,
                BlockState state,
                Vec3 direction,
                long seed,
                int arrivalAge
        ) {
            Random random = new Random(seed ^ (arrivalAge * 0x9E3779B97F4A7C15L));
            int dustCount = EnderniumVisualConfig.cinematic() ? 2 : 1;
            for (int i = 0; i < dustCount; i++) {
                double x = surface.getX() + 0.2 + random.nextDouble() * 0.6;
                double y = surface.getY() + 1.04;
                double z = surface.getZ() + 0.2 + random.nextDouble() * 0.6;
                double side = random.nextDouble() * 0.28 - 0.14;
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        x, y, z,
                        direction.x * (0.12 + random.nextDouble() * 0.22) - direction.z * side,
                        0.16 + random.nextDouble() * 0.34,
                        direction.z * (0.12 + random.nextDouble() * 0.22) + direction.x * side
                );
            }
            if ((seed & 3L) == 0L) {
                level.addParticle(
                        EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                        surface.getX() + 0.5,
                        surface.getY() + 1.08,
                        surface.getZ() + 0.5,
                        direction.x * (0.16 + random.nextDouble() * 0.16),
                        0.08 + random.nextDouble() * 0.14,
                        direction.z * (0.16 + random.nextDouble() * 0.16)
                );
            }
        }

        private void expireCracks() {
            crackPatches.removeIf(patch -> burstAge - patch.bornAge() > patch.lifetime());
        }

        private DistantWaveState createDistantWave(
                ClientLevel level,
                Vec3 anchor,
                Vec3 direction,
                int arrivalTick
        ) {
            int landingTarget = EnderniumVisualConfig.cinematic() ? 10 : 4;
            int flybyTarget = EnderniumVisualConfig.cinematic() ? 14 : 8;
            int regionX = Mth.floor(anchor.x / 32.0);
            int regionZ = Mth.floor(anchor.z / 32.0);
            long localSeed = mixSeed(sequenceSeed ^ BlockPos.asLong(regionX, 0, regionZ));
            Random random = new Random(localSeed);
            List<DistantCometPath> localComets = new ArrayList<>(landingTarget + flybyTarget);
            Set<Long> landingColumns = new HashSet<>();
            int convertedFlybys = 0;

            for (int i = 0; i < landingTarget; i++) {
                Vec3 landing = findDistantLanding(level, anchor, direction, random, landingColumns);
                if (landing == null) {
                    convertedFlybys++;
                    continue;
                }
                localComets.add(createLandingComet(anchor, direction, landing, arrivalTick, random, i));
            }
            for (int i = 0; i < flybyTarget + convertedFlybys; i++) {
                localComets.add(createFlybyComet(anchor, direction, arrivalTick, random, i));
            }
            return new DistantWaveState(
                    anchor,
                    direction,
                    origin.y,
                    arrivalTick,
                    localSeed,
                    Collections.unmodifiableList(localComets)
            );
        }

        private Vec3 findDistantLanding(
                ClientLevel level,
                Vec3 anchor,
                Vec3 direction,
                Random random,
                Set<Long> occupiedColumns
        ) {
            Vec3 towardOrigin = direction.scale(-1.0);
            for (int attempt = 0; attempt < 16; attempt++) {
                double angle = (random.nextDouble() - 0.5) * Math.toRadians(200.0);
                Vec3 candidateDirection = rotateHorizontal(towardOrigin, angle);
                double radius = 18.0 + random.nextDouble() * 42.0;
                int x = Mth.floor(anchor.x + candidateDirection.x * radius);
                int z = Mth.floor(anchor.z + candidateDirection.z * radius);
                BlockPos probe = new BlockPos(x, Mth.floor(anchor.y), z);
                if (!level.hasChunkAt(probe)) {
                    continue;
                }
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockPos surface = new BlockPos(x, surfaceY, z);
                BlockPos above = surface.above();
                if (!level.getBlockState(surface).is(Blocks.END_STONE)
                        || !level.getBlockState(above).getCollisionShape(level, above).isEmpty()
                        || !level.getFluidState(above).isEmpty()
                        || !occupiedColumns.add(surface.asLong())) {
                    continue;
                }
                return new Vec3(x + 0.5, surfaceY + 1.08, z + 0.5);
            }
            return null;
        }

        private DistantCometPath createLandingComet(
                Vec3 anchor,
                Vec3 direction,
                Vec3 landing,
                int arrivalTick,
                Random random,
                int index
        ) {
            Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
            Vec3 start = landing
                    .subtract(direction.scale(72.0 + random.nextDouble() * 72.0))
                    .add(side.scale((random.nextDouble() - 0.5) * 54.0))
                    .add(0.0, 58.0 + random.nextDouble() * 66.0, 0.0);
            Vec3 control = start.add(landing).scale(0.5)
                    .add(side.scale((random.nextDouble() - 0.5) * 22.0))
                    .add(0.0, 20.0 + random.nextDouble() * 28.0, 0.0);
            float startAge = Math.max(0.0F, arrivalTick - 10.0F + random.nextFloat() * 4.0F);
            float endAge = arrivalTick - 1.0F + random.nextFloat() * 9.0F;
            return new DistantCometPath(
                    start,
                    control,
                    landing,
                    startAge,
                    Math.max(startAge + 4.0F, endAge),
                    0.18F + random.nextFloat() * 0.26F,
                    index % 5 == 0 ? 1 : 0,
                    true,
                    mixSeed(sequenceSeed ^ (index * 0x9E3779B97F4A7C15L) ^ 0x1A4D1A6L)
            );
        }

        private DistantCometPath createFlybyComet(
                Vec3 anchor,
                Vec3 direction,
                int arrivalTick,
                Random random,
                int index
        ) {
            Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
            double lateral = (random.nextBoolean() ? 1.0 : -1.0) * (8.0 + random.nextDouble() * 37.0);
            double passHeight = -6.0 + random.nextDouble() * 42.0;
            Vec3 pass = anchor.add(side.scale(lateral)).add(0.0, passHeight, 0.0);
            Vec3 start = anchor
                    .subtract(direction.scale(105.0 + random.nextDouble() * 80.0))
                    .add(side.scale(lateral * (0.7 + random.nextDouble() * 0.8)))
                    .add(0.0, 48.0 + random.nextDouble() * 72.0, 0.0);
            Vec3 end = anchor
                    .add(direction.scale(100.0 + random.nextDouble() * 120.0))
                    .add(side.scale(lateral * (0.35 + random.nextDouble() * 0.55)))
                    .add(0.0, -5.0 + random.nextDouble() * 42.0, 0.0);
            Vec3 control = pass.scale(2.0).subtract(start.add(end).scale(0.5));
            float passAge = arrivalTick - 3.0F + random.nextFloat() * 8.0F;
            float startAge = Math.max(0.0F, arrivalTick - 10.0F + random.nextFloat() * 4.0F);
            float endAge = Math.max(passAge + 3.0F, passAge * 2.0F - startAge);
            return new DistantCometPath(
                    start,
                    control,
                    end,
                    startAge,
                    endAge,
                    0.16F + random.nextFloat() * 0.28F,
                    index % 9 == 0 ? 2 : index % 4 == 0 ? 1 : 0,
                    false,
                    mixSeed(sequenceSeed ^ (index * 0xD1B54A32D192ED03L) ^ 0xF17B1L)
            );
        }

        private void triggerDistantImpact(ClientLevel level, DistantCometPath path, int index) {
            Vec3 at = path.end();
            long seed = mixSeed(path.seed() ^ index * 0x9E3779B97F4A7C15L);
            Random random = new Random(seed);
            distantImpactFlashes.add(new DistantImpactFlash(at, burstAge, seed));

            BlockPos surface = BlockPos.containing(at.x, at.y - 0.2, at.z);
            if (crackPatches.size() < MAX_CRACK_PATCHES && crackedColumns.add(surface.asLong())) {
                crackPatches.add(new CrackPatch(
                        at.add(0.0, -0.068, 0.0),
                        (float)Math.atan2(distantWave.direction().z, distantWave.direction().x),
                        1.0F + random.nextFloat() * 0.9F,
                        burstAge,
                        18 + random.nextInt(7),
                        seed
                ));
            }

            int particleCount = 6 + random.nextInt(5);
            for (int i = 0; i < particleCount; i++) {
                double angle = random.nextDouble() * Mth.TWO_PI;
                double speed = 0.08 + random.nextDouble() * 0.28;
                double vx = Math.cos(angle) * speed;
                double vz = Math.sin(angle) * speed;
                double vy = 0.12 + random.nextDouble() * 0.34;
                level.addParticle(
                        i % 3 == 0
                                ? ParticleTypes.PORTAL
                                : PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F),
                        at.x,
                        at.y + 0.08,
                        at.z,
                        vx,
                        vy,
                        vz
                );
            }
        }

        private void scanPillars(ClientLevel level) {
            if (pillarsScanned) {
                return;
            }
            pillarsScanned = true;
            for (int i = 0; i < 10; i++) {
                double angle = 2.0 * (-Math.PI + Math.PI / 10.0 * i);
                int x = Mth.floor(42.0 * Math.cos(angle));
                int z = Mth.floor(42.0 * Math.sin(angle));
                BlockPos probe = new BlockPos(x, 64, z);
                if (!level.hasChunkAt(probe)) {
                    continue;
                }
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                int top = Integer.MIN_VALUE;
                for (int y = surfaceY; y >= level.getMinY(); y--) {
                    if (level.getBlockState(new BlockPos(x, y, z)).is(Blocks.OBSIDIAN)) {
                        top = y;
                        break;
                    }
                }
                if (top == Integer.MIN_VALUE) {
                    continue;
                }
                int bottom = top;
                while (bottom > level.getMinY()
                        && level.getBlockState(new BlockPos(x, bottom - 1, z)).is(Blocks.OBSIDIAN)) {
                    bottom--;
                }
                int sampleY = Math.max(bottom, top - 2);
                double surfaceRadius = 1.55;
                int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] direction : directions) {
                    int run = 0;
                    for (int step = 1; step <= 7; step++) {
                        if (!level.getBlockState(new BlockPos(
                                x + direction[0] * step,
                                sampleY,
                                z + direction[1] * step
                        )).is(Blocks.OBSIDIAN)) {
                            break;
                        }
                        run = step;
                    }
                    surfaceRadius = Math.max(surfaceRadius, run + 0.53);
                }
                int arrival = Math.max(
                        1,
                        DragonDeathVfxTiming.waveArrivalTick(origin, new Vec3(x, origin.y, z))
                );
                pillarPulses.add(new PillarPulse(
                        new Vec3(x + 0.5, bottom, z + 0.5),
                        new Vec3(x + 0.5, top + 1.0, z + 0.5),
                        surfaceRadius,
                        arrival,
                        mixSeed(sequenceSeed ^ BlockPos.asLong(x, 0, z))
                ));
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
            float atmosphereIntensity = 0.0F;
            float impactIntensity = 0.0F;
            float impactPhase = 0.0F;
            float detonationShakeIntensity = 0.0F;
            if (waveHitAge >= 0
                    && waveHitAge < 6
                    && EnderniumVisualConfig.cinematic()) {
                postPhase = waveHitAge + partialTick;
                float normalized = postPhase / 6.0F;
                postIntensity = (float)Math.sin(normalized * Math.PI) * 0.85F;
            }
            if (waveHitAge >= 0
                    && waveHitAge < 18
                    && EnderniumVisualConfig.cinematic()) {
                postPhase = waveHitAge + partialTick;
                float attack = Math.min(1.0F, postPhase / 2.0F);
                float recovery = Math.max(0.0F, 1.0F - Math.max(0.0F, postPhase - 2.0F) / 16.0F);
                atmosphereIntensity = attack * recovery * recovery * 0.62F;
            }
            if (burstAge >= 0
                    && EnderniumVisualConfig.cinematic()
                    && isOnMainEndIsland(client.player.position())) {
                impactPhase = burstAge + partialTick;
                if (impactPhase < IMPACT_END_AGE) {
                    impactIntensity = impactPhase < IMPACT_FULL_INTENSITY_END_AGE
                            ? 1.0F
                            : Math.max(0.0F, 1.0F - (impactPhase - IMPACT_FULL_INTENSITY_END_AGE)
                                    / (IMPACT_END_AGE - IMPACT_FULL_INTENSITY_END_AGE));
                }
            }
            if (burstAge >= 0
                    && EnderniumVisualConfig.cinematic()
                    && isOnMainEndIsland(client.player.position())) {
                float shakeAge = burstAge + partialTick;
                if (shakeAge < DETONATION_SHAKE_DURATION) {
                    float remaining = 1.0F - shakeAge / DETONATION_SHAKE_DURATION;
                    detonationShakeIntensity = remaining * remaining;
                }
            }

            BuildupState buildup = burstAge < 0
                    ? new BuildupState(renderOrigin, Math.min(199.99F, deathTicks + partialTick), sequenceSeed)
                    : null;
            BurstState burst = burstAge >= 0
                    ? new BurstState(
                            renderOrigin,
                            burstAge + partialTick,
                            sequenceSeed,
                            comets,
                            distantObserver,
                            distantWave
                    )
                    : null;
            List<CrackState> cracks = crackPatches.stream()
                    .map(patch -> new CrackState(
                            patch.position(),
                            patch.angle(),
                            patch.scale(),
                            Math.max(0.0F, burstAge - patch.bornAge() + partialTick),
                            patch.lifetime(),
                            patch.seed()
                    ))
                    .toList();
            List<DistantImpactState> distantImpacts = distantImpactFlashes.stream()
                    .map(flash -> new DistantImpactState(
                            flash.position(),
                            Math.max(0.0F, burstAge - flash.bornAge() + partialTick),
                            flash.seed()
                    ))
                    .toList();
            return new ExtractedFrame(
                    buildup,
                    burst,
                    cracks,
                    List.copyOf(pillarPulses),
                    distantImpacts,
                    postIntensity,
                    postPhase,
                    atmosphereIntensity,
                    impactIntensity,
                    impactPhase,
                    detonationShakeIntensity
            );
        }

        private boolean isOnMainEndIsland(Vec3 position) {
            return position.x * position.x + position.z * position.z
                    <= MAIN_ISLAND_IMPACT_RADIUS * MAIN_ISLAND_IMPACT_RADIUS;
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

    private static Vec3 horizontalDirection(Vec3 origin, Vec3 target, long seed) {
        double dx = target.x - origin.x;
        double dz = target.z - origin.z;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length > 1.0E-4) {
            return new Vec3(dx / length, 0.0, dz / length);
        }
        double angle = (mixSeed(seed) >>> 11) * 0x1.0p-53 * Mth.TWO_PI;
        return new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
    }

    private static Vec3 rotateHorizontal(Vec3 vector, double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        return new Vec3(
                vector.x * cos - vector.z * sin,
                0.0,
                vector.x * sin + vector.z * cos
        ).normalize();
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public record ExtractedFrame(
            BuildupState buildup,
            BurstState burst,
            List<CrackState> cracks,
            List<PillarPulse> pillars,
            List<DistantImpactState> distantImpacts,
            float postIntensity,
            float postPhase,
            float atmosphereIntensity,
            float impactIntensity,
            float impactPhase,
            float detonationShakeIntensity
    ) {
        public static final ExtractedFrame EMPTY = new ExtractedFrame(
                null, null, List.of(), List.of(), List.of(),
                0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F
        );
        public boolean empty() {
            return buildup == null
                    && burst == null
                    && cracks.isEmpty()
                    && pillars.isEmpty()
                    && distantImpacts.isEmpty()
                    && postIntensity <= 0.0F
                    && atmosphereIntensity <= 0.0F
                    && impactIntensity <= 0.0F
                    && detonationShakeIntensity <= 0.0F;
        }
    }

    public record BuildupState(Vec3 origin, float deathTicks, long seed) {
    }

    public record BurstState(
            Vec3 origin,
            float age,
            long seed,
            List<CometPath> comets,
            boolean distantObserver,
            DistantWaveState distantWave
    ) {
    }

    public record CrackState(Vec3 position, float angle, float scale, float age, int lifetime, long seed) {
    }

    public record PillarPulse(Vec3 bottom, Vec3 top, double surfaceRadius, int arrivalAge, long seed) {
    }

    public record DistantWaveState(
            Vec3 anchor,
            Vec3 direction,
            double originY,
            int arrivalTick,
            long seed,
            List<DistantCometPath> comets
    ) {
    }

    public record DistantCometPath(
            Vec3 start,
            Vec3 control,
            Vec3 end,
            float startAge,
            float endAge,
            float width,
            int headVariant,
            boolean landing,
            long seed
    ) {
        public Vec3 position(float age) {
            float duration = Math.max(0.001F, endAge - startAge);
            double t = Mth.clamp((age - startAge) / duration, 0.0F, 1.0F);
            double inverse = 1.0 - t;
            return start.scale(inverse * inverse)
                    .add(control.scale(2.0 * inverse * t))
                    .add(end.scale(t * t));
        }
    }

    public record DistantImpactState(Vec3 position, float age, long seed) {
    }

    private record CrackPatch(Vec3 position, float angle, float scale, int bornAge, int lifetime, long seed) {
    }

    private record DistantImpactFlash(Vec3 position, int bornAge, long seed) {
    }

    private record TerrainCandidate(int x, int z, long hash) {
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
