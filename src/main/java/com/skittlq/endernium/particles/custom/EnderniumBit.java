package com.skittlq.endernium.particles.custom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

public class EnderniumBit extends TextureSheetParticle {
    private final double xStart, yStart, zStart;
    private final double burstXd, burstYd, burstZd;
    private final Entity targetEntity; // The player or other entity to follow
    private final int burstTicks;

    public EnderniumBit(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Player player) {
        super(level, x, y, z);
        this.xStart = x;
        this.yStart = y;
        this.zStart = z;
        this.burstXd = xSpeed;
        this.burstYd = ySpeed;
        this.burstZd = zSpeed;
        this.targetEntity = player;
        this.lifetime = (int)(Math.random() * 10.0) + 40;
        this.burstTicks = (int) (this.lifetime * 0.1); // 30% of lifetime bursting outward

        this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.lifetime = (int)(Math.random() * 10.0) + 40;

    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    public float getQuadSize(float scaleFactor) {
        float f = ((float)this.age + scaleFactor) / (float)this.lifetime;
        f = 1.0F - f;
        f *= f;
        f = 1.0F - f;
        return this.quadSize * f;
    }

    @Override
    protected void renderRotatedQuad(VertexConsumer buffer, Camera camera, Quaternionf quaternion, float partialTicks) {
        float roll = (this.age + partialTicks) * 0.2f;
        quaternion.rotateZ(roll);
        super.renderRotatedQuad(buffer, camera, quaternion, partialTicks);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }


    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        if (this.targetEntity != null) {
            if (this.age < this.burstTicks) {
                // Phase 1: burst outwards
                this.x = xStart + burstXd * this.age;
                this.y = yStart + burstYd * this.age;
                this.z = zStart + burstZd * this.age;
            } else {
                // Phase 2: home in, always reaching player at expiry
                double burstEndX = xStart + burstXd * burstTicks;
                double burstEndY = yStart + burstYd * burstTicks;
                double burstEndZ = zStart + burstZd * burstTicks;

                double targetX = targetEntity.getX();
                double targetY = targetEntity.getY() + targetEntity.getBbHeight() * 0.6;
                double targetZ = targetEntity.getZ();

                float lerpT = (float)(this.age - burstTicks) / (float)(this.lifetime - burstTicks);
                lerpT = Math.min(lerpT, 1.0f); // Clamp just in case

                this.x = burstEndX + (targetX - burstEndX) * lerpT;
                this.y = burstEndY + (targetY - burstEndY) * lerpT;
                this.z = burstEndZ + (targetZ - burstEndZ) * lerpT;
            }
            this.setPos(this.x, this.y, this.z);
        } else {
            // No player—just float along as usual
            this.x += burstXd;
            this.y += burstYd;
            this.z += burstZd;
            this.setPos(this.x, this.y, this.z);
        }
    }
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            Player player = level.getNearestPlayer(x, y, z, 32, null); // 32 blocks radius, no filter
            EnderniumBit bit = new EnderniumBit(level, x, y, z, xSpeed, ySpeed, zSpeed, player);
            bit.pickSprite(this.sprite);
            return bit;
        }

    }

}