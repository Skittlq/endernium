package com.skittlq.endernium.progression;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Shared deterministic path for the blessing's server particles and shader-rendered energy core. */
public final class DragonBlessingVfxMath {
    public static final double ORBIT_RADIUS = 1.75;
    public static final double ORBIT_TURNS = 3.0;
    public static final double ORBIT_END_PROGRESS = 0.74;
    public static final double ABSORPTION_SETTLE_END_PROGRESS = 0.12;
    public static final double ABSORPTION_PULLBACK_DISTANCE = 0.55;
    public static final double ABSORPTION_PULLBACK_END_PROGRESS = 0.42;
    public static final float FACING_FOLLOW_FACTOR = 0.14F;
    public static final int EMERGENCE_END_TICK = 12;
    public static final int GATHERING_END_TICK = 24;
    public static final int SEEKING_END_TICK = 70;
    public static final int LINGER_END_TICK = 84;
    public static final int BLESSING_END_TICK = 140;
    public static final int PULSE_END_TICK = 152;

    private DragonBlessingVfxMath() {
    }

    public static double phase(long seed) {
        return ((seed >>> 11) * 0x1.0p-53) * Mth.TWO_PI;
    }

    public static float followFacingYaw(float currentYaw, float targetYaw) {
        return currentYaw + Mth.wrapDegrees(targetYaw - currentYaw) * FACING_FOLLOW_FACTOR;
    }

    public static Vec3 frontTarget(Vec3 chestTarget, float yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        Vec3 horizontalLook = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        return chestTarget.add(horizontalLook.scale(2.1)).add(0.0, 0.24, 0.0);
    }

    public static Vec3 orbitEntry(Vec3 chestTarget, Vec3 frontTarget) {
        return chestTarget.add(frontDirection(chestTarget, frontTarget).scale(ORBIT_RADIUS));
    }

    public static Vec3 corePosition(
            Vec3 origin,
            Vec3 chestTarget,
            Vec3 frontTarget,
            long seed,
            int recipientIndex,
            int recipientCount,
            float age
    ) {
        double phase = phase(seed);
        double clusterAngle = phase + recipientIndex * Mth.TWO_PI / Math.max(1, recipientCount);
        Vec3 clusterDirection = new Vec3(Math.cos(clusterAngle), 0.0, Math.sin(clusterAngle));
        Vec3 gatherStart = origin.add(clusterDirection.scale(0.35)).add(0.0, 0.35, 0.0);
        Vec3 seekStart = origin.add(clusterDirection.scale(2.55)).add(0.0, 0.8, 0.0);
        if (age < EMERGENCE_END_TICK) {
            double progress = Mth.clamp(age / (EMERGENCE_END_TICK - 1.0), 0.0, 1.0);
            Vec3 emergenceStart = origin.add(
                    Math.cos(phase) * 1.15,
                    Math.sin(phase * 1.4) * 0.62,
                    Math.sin(phase) * 1.15
            );
            Vec3 base = emergenceStart.lerp(gatherStart, smootherStep(progress));
            double arc = Math.sin(progress * Math.PI) * 0.46;
            Vec3 side = horizontalPerpendicular(gatherStart.subtract(emergenceStart), phase);
            return base
                    .add(side.scale(Math.sin(progress * Math.PI * 1.35) * arc))
                    .add(0.0, Math.sin(progress * Math.PI) * 0.34, 0.0);
        }
        if (age < GATHERING_END_TICK) {
            double progress = (age - EMERGENCE_END_TICK)
                    / (GATHERING_END_TICK - EMERGENCE_END_TICK - 1.0);
            Vec3 base = gatherStart.lerp(seekStart, smootherStep(progress));
            Vec3 awayFromTarget = seekStart.subtract(frontTarget);
            if (awayFromTarget.lengthSqr() > 1.0E-6) {
                awayFromTarget = awayFromTarget.normalize();
            }
            double anticipationProgress = Mth.clamp((progress - 0.48) / 0.52, 0.0, 1.0);
            double anticipation = Math.sin(anticipationProgress * Math.PI) * 0.38;
            return base
                    .add(awayFromTarget.scale(anticipation))
                    .add(0.0, Math.sin(progress * Math.PI * 2.0) * 0.09, 0.0);
        }
        if (age < SEEKING_END_TICK) {
            double rawProgress = (age - GATHERING_END_TICK)
                    / (SEEKING_END_TICK - GATHERING_END_TICK - 1.0);
            Vec3 direct = frontTarget.subtract(seekStart);
            double distance = direct.length();
            Vec3 sideways = horizontalPerpendicular(direct, clusterAngle);
            double curveDirection = (recipientIndex & 1) == 0 ? 1.0 : -1.0;
            double curveScale = 4.5 + Math.min(14.0, distance * 0.18);
            Vec3 controlOne = seekStart
                    .add(sideways.scale(curveScale * curveDirection))
                    .add(0.0, 5.0 + Math.min(9.0, distance * 0.14), 0.0);
            Vec3 controlTwo = frontTarget
                    .add(sideways.scale(-curveScale * 0.72 * curveDirection))
                    .add(0.0, 3.0 + Math.min(5.5, distance * 0.07), 0.0);
            double t = pacedSeekingProgress(Mth.clamp(rawProgress, 0.0, 1.0));
            Vec3 base = cubicBezier(seekStart, controlOne, controlTwo, frontTarget, t);
            Vec3 tangent = cubicBezierTangent(seekStart, controlOne, controlTwo, frontTarget, t);
            Vec3 flightSide = horizontalPerpendicular(tangent, clusterAngle);
            double motionEnvelope = Math.sin(t * Math.PI);
            double lateralFlutter = Math.sin(phase + rawProgress * Math.PI * 5.0)
                    * 0.16 * motionEnvelope;
            double verticalFlutter = Math.sin(phase * 0.73 + rawProgress * Math.PI * 7.0)
                    * 0.10 * motionEnvelope;
            return base.add(flightSide.scale(lateralFlutter)).add(0.0, verticalFlutter, 0.0);
        }
        if (age < LINGER_END_TICK) {
            double progress = (age - SEEKING_END_TICK)
                    / (LINGER_END_TICK - SEEKING_END_TICK - 1.0);
            Vec3 direct = frontTarget.subtract(seekStart);
            double distance = direct.length();
            Vec3 sideways = horizontalPerpendicular(direct, clusterAngle);
            double curveDirection = (recipientIndex & 1) == 0 ? 1.0 : -1.0;
            double curveScale = 4.5 + Math.min(14.0, distance * 0.18);
            Vec3 controlTwo = frontTarget
                    .add(sideways.scale(-curveScale * 0.72 * curveDirection))
                    .add(0.0, 3.0 + Math.min(5.5, distance * 0.07), 0.0);
            Vec3 arrivalDirection = frontTarget.subtract(controlTwo);
            if (arrivalDirection.lengthSqr() > 1.0E-6) {
                arrivalDirection = arrivalDirection.normalize();
            }
            Vec3 forward = frontDirection(chestTarget, frontTarget);
            Vec3 orbitSide = new Vec3(-forward.z, 0.0, forward.x);
            double settleEnvelope = Math.sin(progress * Math.PI);
            double overshoot = settleEnvelope * (1.0 - progress * 0.35) * 0.42;
            double secondary = Math.sin(phase + progress * Math.PI * 3.0) * settleEnvelope * 0.09;
            return frontTarget.lerp(orbitEntry(chestTarget, frontTarget), smootherStep(progress))
                    .add(arrivalDirection.scale(overshoot))
                    .add(orbitSide.scale(secondary))
                    .add(0.0, Math.sin(progress * Math.PI * 2.0) * settleEnvelope * 0.10, 0.0);
        }
        if (age < BLESSING_END_TICK) {
            double progress = (age - LINGER_END_TICK)
                    / (BLESSING_END_TICK - LINGER_END_TICK - 1.0);
            progress = Mth.clamp(progress, 0.0, 1.0);
            double orbitProgress = orbitEase(Mth.clamp(progress / ORBIT_END_PROGRESS, 0.0, 1.0));
            double absorptionProgress = Mth.clamp(
                    (progress - ORBIT_END_PROGRESS) / (1.0 - ORBIT_END_PROGRESS),
                    0.0,
                    1.0
            );
            Vec3 forward = frontDirection(chestTarget, frontTarget);
            double frontAngle = Math.atan2(forward.z, forward.x);
            double orbitAngle = orbitProgress * Mth.TWO_PI * ORBIT_TURNS;
            double absorptionSway = Math.sin(absorptionProgress * Math.PI) * 0.12;
            double angle = frontAngle + orbitAngle + absorptionSway;
            double orbitBreath = 1.0
                    + Math.sin(orbitAngle * 2.0) * Math.sin(orbitProgress * Math.PI) * 0.035;
            double radius;
            if (absorptionProgress < ABSORPTION_SETTLE_END_PROGRESS) {
                radius = ORBIT_RADIUS;
            } else if (absorptionProgress < ABSORPTION_PULLBACK_END_PROGRESS) {
                double pullbackProgress = smootherStep(
                        (absorptionProgress - ABSORPTION_SETTLE_END_PROGRESS)
                                / (ABSORPTION_PULLBACK_END_PROGRESS - ABSORPTION_SETTLE_END_PROGRESS)
                );
                radius = ORBIT_RADIUS * orbitBreath
                        + ABSORPTION_PULLBACK_DISTANCE * pullbackProgress;
            } else {
                double inwardProgress = smootherStep(
                        (absorptionProgress - ABSORPTION_PULLBACK_END_PROGRESS)
                                / (1.0 - ABSORPTION_PULLBACK_END_PROGRESS)
                );
                radius = (ORBIT_RADIUS + ABSORPTION_PULLBACK_DISTANCE) * (1.0 - inwardProgress);
            }
            return chestTarget.add(
                    Math.cos(angle) * radius,
                    (Math.sin(orbitAngle) * radius * 0.38)
                            + Math.sin(absorptionProgress * Math.PI) * 0.08,
                    Math.sin(angle) * radius
            );
        }
        return chestTarget;
    }

    public static float playerPulse(float age) {
        if (age < BLESSING_END_TICK - 1 || age >= PULSE_END_TICK) {
            return 0.0F;
        }
        float localAge = age - (BLESSING_END_TICK - 1);
        float attack = Math.min(1.0F, localAge / 2.0F);
        float release = Math.max(0.0F, 1.0F - Math.max(0.0F, localAge - 2.0F) / 11.0F);
        float heartbeat = 0.84F + 0.16F * (float)Math.sin(localAge * 1.55F);
        return attack * release * release * heartbeat;
    }

    private static Vec3 horizontalPerpendicular(Vec3 direction, double fallbackAngle) {
        Vec3 perpendicular = new Vec3(-direction.z, 0.0, direction.x);
        if (perpendicular.lengthSqr() < 1.0E-6) {
            return new Vec3(Math.cos(fallbackAngle), 0.0, Math.sin(fallbackAngle));
        }
        return perpendicular.normalize();
    }

    private static Vec3 frontDirection(Vec3 chestTarget, Vec3 frontTarget) {
        Vec3 direction = new Vec3(
                frontTarget.x - chestTarget.x,
                0.0,
                frontTarget.z - chestTarget.z
        );
        return direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
    }

    private static double smoothStep(double value) {
        return value * value * (3.0 - 2.0 * value);
    }

    private static double smootherStep(double value) {
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
    }

    private static double pacedSeekingProgress(double value) {
        if (value < 0.22) {
            return 0.10 * smootherStep(value / 0.22);
        }
        if (value < 0.72) {
            return 0.10 + 0.76 * smootherStep((value - 0.22) / 0.50);
        }
        return 0.86 + 0.14 * smootherStep((value - 0.72) / 0.28);
    }

    private static double orbitEase(double value) {
        double smooth = smootherStep(value);
        return 1.0 - Math.pow(1.0 - smooth, 1.4);
    }

    private static Vec3 cubicBezier(Vec3 a, Vec3 b, Vec3 c, Vec3 d, double t) {
        double inverse = 1.0 - t;
        return a.scale(inverse * inverse * inverse)
                .add(b.scale(3.0 * inverse * inverse * t))
                .add(c.scale(3.0 * inverse * t * t))
                .add(d.scale(t * t * t));
    }

    private static Vec3 cubicBezierTangent(Vec3 a, Vec3 b, Vec3 c, Vec3 d, double t) {
        double inverse = 1.0 - t;
        return b.subtract(a).scale(3.0 * inverse * inverse)
                .add(c.subtract(b).scale(6.0 * inverse * t))
                .add(d.subtract(c).scale(3.0 * t * t));
    }
}
