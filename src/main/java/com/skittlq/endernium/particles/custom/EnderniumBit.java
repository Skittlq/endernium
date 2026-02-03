package com.skittlq.endernium.particles.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class EnderniumBit extends SingleQuadParticle {
    public double xStart;
    public double yStart;
    public double zStart;
    public double burstXd, burstYd, burstZd;
    public final Entity targetEntity; // may be null
    public final int burstTicks;
    public final SpriteSet sprites;

    public EnderniumBit(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            Entity target,
            SpriteSet sprites
    ) {
        // SingleQuadParticle needs an initial TextureAtlasSprite up-front.
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());

        this.sprites = sprites;

        this.xStart = x;
        this.yStart = y;
        this.zStart = z;

        this.burstXd = xSpeed;
        this.burstYd = ySpeed;
        this.burstZd = zSpeed;

        this.targetEntity = target;

        this.lifetime = (int) (Math.random() * 10.0) + 40;
        this.burstTicks = (int) (this.lifetime * 0.1); // 10% burst phase

        this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);

        // Colour (white) + fullbright-ish light value below.
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;

        // Optional: particles usually ignore physics unless you want collisions.
        this.hasPhysics = false;

        // Ensure sprite UVs are correct from frame 0.
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    protected Layer getLayer() {
        // Replacement for PARTICLE_SHEET_TRANSLUCENT
        return Layer.TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        // Same as your original: bright.
        return 0xF000F0;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float f = ((float) this.age + scaleFactor) / (float) this.lifetime;
        f = 1.0F - f;
        f *= f;
        f = 1.0F - f;
        return this.quadSize * f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Drive rotation here (instead of renderRotatedQuad override)
        this.oRoll = this.roll;
        this.roll += 0.2F;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Update sprite for animated spritesheets
        this.setSpriteFromAge(this.sprites);

        Entity target = this.targetEntity;
        if (target != null && !target.isRemoved()) {
            if (this.age < this.burstTicks) {
                // Phase 1: burst outward
                this.x = this.xStart + this.burstXd * this.age;
                this.y = this.yStart + this.burstYd * this.age;
                this.z = this.zStart + this.burstZd * this.age;
            } else {
                // Phase 2: home in, reaching target at expiry
                double burstEndX = this.xStart + this.burstXd * this.burstTicks;
                double burstEndY = this.yStart + this.burstYd * this.burstTicks;
                double burstEndZ = this.zStart + this.burstZd * this.burstTicks;

                double targetX = target.getX();
                double targetY = target.getY() + target.getBbHeight() * 0.6;
                double targetZ = target.getZ();

                float lerpT = (float) (this.age - this.burstTicks) / (float) (this.lifetime - this.burstTicks);
                if (lerpT > 1.0F) lerpT = 1.0F;

                this.x = burstEndX + (targetX - burstEndX) * lerpT;
                this.y = burstEndY + (targetY - burstEndY) * lerpT;
                this.z = burstEndZ + (targetZ - burstEndZ) * lerpT;
            }

            this.setPos(this.x, this.y, this.z);
        } else {
            // No target (or it vanished) — drift normally
            this.x += this.burstXd;
            this.y += this.burstYd;
            this.z += this.burstZd;
            this.setPos(this.x, this.y, this.z);
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed,
                RandomSource random
        ) {
            Player player = level.getNearestPlayer(x, y, z, 32.0, false);
            return new EnderniumBit(
                    level,
                    x, y, z,
                    xSpeed, ySpeed, zSpeed,
                    player,
                    this.sprites
            );
        }

    }
}
