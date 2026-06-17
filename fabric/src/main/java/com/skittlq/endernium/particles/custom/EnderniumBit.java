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

public class EnderniumBit extends SingleQuadParticle {
    protected double xStart;
    protected double yStart;
    protected double zStart;
    protected double burstXd;
    protected double burstYd;
    protected double burstZd;
    protected final Entity targetEntity;
    protected final int burstTicks;
    protected final SpriteSet sprites;

    protected EnderniumBit(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Entity target, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());
        this.sprites = sprites;
        this.targetEntity = target;
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;
        this.burstXd = xSpeed;
        this.burstYd = ySpeed;
        this.burstZd = zSpeed;
        this.lifetime = this.random.nextInt(10) + 40;
        this.burstTicks = Math.max(1, (int) (this.lifetime * 0.1F));
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
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
        this.oRoll = this.roll;
        this.roll += 0.2F;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.setSpriteFromAge(this.sprites);

        Entity target = this.targetEntity;
        if (target != null && !target.isRemoved()) {
            if (this.age < this.burstTicks) {
                this.x = this.xStart + this.burstXd * this.age;
                this.y = this.yStart + this.burstYd * this.age;
                this.z = this.zStart + this.burstZd * this.age;
            } else {
                double burstEndX = this.xStart + this.burstXd * this.burstTicks;
                double burstEndY = this.yStart + this.burstYd * this.burstTicks;
                double burstEndZ = this.zStart + this.burstZd * this.burstTicks;
                double targetX = target.getX();
                double targetY = target.getY() + target.getBbHeight() * 0.6;
                double targetZ = target.getZ();
                float lerpT = (float) (this.age - this.burstTicks) / (float) (this.lifetime - this.burstTicks);
                lerpT = Math.min(1.0F, lerpT);
                this.x = burstEndX + (targetX - burstEndX) * lerpT;
                this.y = burstEndY + (targetY - burstEndY) * lerpT;
                this.z = burstEndZ + (targetZ - burstEndZ) * lerpT;
            }
            this.setPos(this.x, this.y, this.z);
        } else {
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
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            Player player = level.getNearestPlayer(x, y, z, 32.0, false);
            return new EnderniumBit(level, x, y, z, xSpeed, ySpeed, zSpeed, player, this.sprites);
        }
    }
}

