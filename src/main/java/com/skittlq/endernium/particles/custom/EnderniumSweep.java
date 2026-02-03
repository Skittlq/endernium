package com.skittlq.endernium.particles.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class EnderniumSweep extends SingleQuadParticle {

    private final SpriteSet sprites;

    protected EnderniumSweep(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());

        this.sprites = sprites;

        this.friction = 0.0F;
        this.hasPhysics = false;

        this.lifetime = 60;
        this.quadSize = 2.0F;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    protected Layer getLayer() {
        // Replacement for PARTICLE_SHEET_TRANSLUCENT
        return Layer.TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = ((float) this.age + partialTicks) / (float) this.lifetime;
        float growTime = 3.5f / 60f;
        float scale;

        if (t < growTime) {
            float localT = t / growTime;
            scale = (float) Math.pow(localT, 5);
        } else {
            float localT = (t - growTime) / (1.0f - growTime);
            scale = (float) Math.pow(1.0f - localT, 15);
        }

        scale = Math.max(0.0f, Math.min(1.0f, scale));
        return this.quadSize * scale;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Drive rotation (replaces renderRotatedQuad override)
        this.oRoll = this.roll;
        this.roll += 0.2F;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Animated spritesheets
        this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed,
                RandomSource random
        ) {
            return new EnderniumSweep(
                    level,
                    x, y, z,
                    xSpeed, ySpeed, zSpeed,
                    this.sprites
            );
        }
    }
}
