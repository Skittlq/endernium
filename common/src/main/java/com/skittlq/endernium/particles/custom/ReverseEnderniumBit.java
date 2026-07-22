package com.skittlq.endernium.particles.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class ReverseEnderniumBit extends EnderniumBit {
    protected ReverseEnderniumBit(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Player player, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, player, sprites);
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;
        this.burstXd = xSpeed;
        this.burstYd = ySpeed;
        this.burstZd = zSpeed;
        this.quadSize *= 1F;
        this.lifetime = this.random.nextInt(2) + 60;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float t = ((float) this.age + scaleFactor) / (float) this.lifetime;
        float scale = 1.0F - t;
        return this.quadSize * Math.max(0.0F, scale);
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
        this.x += this.burstXd;
        this.y += this.burstYd;
        this.z += this.burstZd;
        this.setPos(this.x, this.y, this.z);
        this.updateAlphaForNearbyPlayer();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            Player player = level.getNearestPlayer(x, y, z, 32.0, false);
            return new ReverseEnderniumBit(level, x, y, z, xSpeed, ySpeed, zSpeed, player, this.sprites);
        }
    }
}
