package com.skittlq.endernium.particles.custom;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

//    @Override
//    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
//        // Interpolated position
//        float x = (float) (Mth.lerp(partialTicks, xo, this.x));
//        float y = (float) (Mth.lerp(partialTicks, yo, this.y));
//        float z = (float) (Mth.lerp(partialTicks, zo, this.z));
//
//        // Quad size
//        float size = getQuadSize(partialTicks);
//
//        // Texture UVs
//        float u0 = getU0();
//        float u1 = getU1();
//        float v0 = getV0();
//        float v1 = getV1();
//
//        // Colour and alpha
//        int light = getLightColor(partialTicks);
//        float r = rCol;
//        float g = gCol;
//        float b = bCol;
//        float alpha = this.alpha; // Correct usage
//
//        // Quaternion for rotation
//        Quaternionf quaternion = new Quaternionf();
//        camera.rotation().get(quaternion); // Get camera rotation
//
//        // Custom roll rotation
//        float roll = (this.age + partialTicks) * 0.2f;
//        quaternion.rotateAxis(roll, 0.0f, 0.0f, 1.0f); // Rotate around Z
//
//        // Prepare corners
//        Vec3[] corners = new Vec3[]{
//                new Vec3(-1, -1, 0),
//                new Vec3(-1,  1, 0),
//                new Vec3( 1,  1, 0),
//                new Vec3( 1, -1, 0)
//        };
//
//        float halfSize = size / 2.0F;
//        for (int i = 0; i < 4; ++i) {
//            Vec3 v = corners[i].scale(halfSize);
//            Vector3f vec = new Vector3f((float)v.x, (float)v.y, (float)v.z);
//            vec.rotate(quaternion);
//            corners[i] = new Vec3(vec.x() + x, vec.y() + y, vec.z() + z);
//        }
//
//        buffer.vertex(corners[0].x, corners[0].y, corners[0].z).uv(u1, v1).color(r, g, b, alpha).uv2(light).endVertex();
//        buffer.vertex(corners[1].x, corners[1].y, corners[1].z).uv(u1, v0).color(r, g, b, alpha).uv2(light).endVertex();
//        buffer.vertex(corners[2].x, corners[2].y, corners[2].z).uv(u0, v0).color(r, g, b, alpha).uv2(light).endVertex();
//        buffer.vertex(corners[3].x, corners[3].y, corners[3].z).uv(u0, v1).color(r, g, b, alpha).uv2(light).endVertex();
//    }

}