package com.skittlq.endernium.particles.custom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;

public class ReverseEnderniumBit extends EnderniumBit {
    protected ReverseEnderniumBit(ClientLevel p_107590_, double p_107591_, double p_107592_, double p_107593_, double p_107594_, double p_107595_, double p_107596_, Player player) {
        super(p_107590_, p_107591_, p_107592_, p_107593_, p_107594_, p_107595_, p_107596_, player);
        this.quadSize *= 1.5F;
        this.lifetime = (int)(Math.random() * 2.0) + 60;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float t = (this.age + scaleFactor) / this.lifetime;
        float scale = 1.0F - t;
        return this.quadSize * Math.max(0.0F, scale);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float f = (float)this.age / this.lifetime;
            this.x = this.x + this.xd * f;
            this.y = this.y + this.yd * f;
            this.z = this.z + this.zd * f;
            this.setPos(this.x, this.y, this.z); // Neo: update the particle's bounding box
        }
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


    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            Player player = level.getNearestPlayer(x, y, z, 32, null); // 32 blocks radius, no filter
            ReverseEnderniumBit reverseenderniumbit = new ReverseEnderniumBit(level, x, y, z, xSpeed, ySpeed, zSpeed, player);
            reverseenderniumbit.pickSprite(this.sprite);
            return reverseenderniumbit;
        }
    }

}