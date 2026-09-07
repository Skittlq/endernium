package com.skittlq.endernium.particles.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * A short-lived, full-bright trail mote used only by the dragon blessing.
 * It deliberately has no player-seeking or camera-distance fade behavior.
 */
public final class EnderniumBlessingBit extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float baseSize;
    private final float spinSpeed;
    private final float curlPhase;

    private EnderniumBlessingBit(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());
        this.sprites = sprites;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.lifetime = 8 + this.random.nextInt(5);
        this.baseSize = 0.025F + this.random.nextFloat() * 0.025F;
        this.quadSize = this.baseSize;
        this.spinSpeed = (this.random.nextBoolean() ? 1.0F : -1.0F)
                * (0.16F + this.random.nextFloat() * 0.22F);
        this.curlPhase = this.random.nextFloat() * (float)(Math.PI * 2.0);
        this.alpha = 0.0F;
        this.hasPhysics = false;

        this.setColor(EnderniumBit.COLOR_RED, EnderniumBit.COLOR_GREEN, EnderniumBit.COLOR_BLUE);
        this.setSpriteFromAge(sprites);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public float getQuadSize(float partialTick) {
        float progress = Math.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
        float appear = smoothStep(Math.min(1.0F, progress / 0.12F));
        float vanish = smoothStep(Math.min(1.0F, (1.0F - progress) / 0.32F));
        float shimmer = 0.82F + 0.18F * (float)Math.sin(this.curlPhase + progress * Math.PI * 9.0);
        return this.baseSize * appear * vanish * shimmer;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float)this.age / this.lifetime;
        float appear = smoothStep(Math.min(1.0F, progress / 0.12F));
        float vanish = smoothStep(Math.min(1.0F, (1.0F - progress) / 0.32F));
        float shimmer = 0.82F + 0.18F * (float)Math.sin(this.curlPhase + this.age * 0.78F);
        this.alpha = Math.clamp(appear * vanish * shimmer, 0.0F, 1.0F);
        this.roll += this.spinSpeed * (0.65F + vanish * 0.35F);
        this.setSpriteFromAge(this.sprites);

    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed,
                RandomSource random
        ) {
            return new EnderniumBlessingBit(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
