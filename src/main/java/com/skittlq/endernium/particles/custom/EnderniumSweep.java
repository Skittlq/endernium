package com.skittlq.endernium.particles.custom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class EnderniumSweep extends TextureSheetParticle {
    protected EnderniumSweep(ClientLevel level, double x, double y, double z, SpriteSet spriteSet,
                             double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.friction = 0f;

        this.lifetime = 60;
        this.quadSize = 2f;
        this.setSpriteFromAge(spriteSet);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel,
                                       double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new EnderniumSweep(clientLevel, pX, pY, pZ, this.spriteSet, pXSpeed, pYSpeed, pZSpeed);
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = ((float)this.age + partialTicks) / (float)this.lifetime;
        float growTime = 3.5f / 60f;
        float scale;

        if (t < growTime) {
            float localT = t / growTime;
            scale = (float)Math.pow(localT, 5);
        } else {
            float localT = (t - growTime) / (1 - growTime);
            scale = (float)Math.pow(1 - localT, 15);
        }

        scale = Math.max(0, Math.min(1, scale));
        return this.quadSize * scale;
    }

    @Override
    protected void renderRotatedQuad(VertexConsumer buffer, Camera camera, Quaternionf quaternion, float partialTicks) {
        float roll = (this.age + partialTicks) * 0.2f;
        quaternion.rotateZ(roll);
        super.renderRotatedQuad(buffer, camera, quaternion, partialTicks);
    }

}