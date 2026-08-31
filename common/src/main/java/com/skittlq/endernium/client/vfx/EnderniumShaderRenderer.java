package com.skittlq.endernium.client.vfx;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.BuildupState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.BurstState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.CometPath;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.ExtractedFrame;
import com.skittlq.endernium.config.EnderniumVisualConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.system.MemoryUtil;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;

/** Blaze3D-only renderer for the first-dragon-death Endernium eruption. */
public final class EnderniumShaderRenderer implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger("endernium/dragon_vfx");
    private static final Identifier ENERGY_SHADER = Identifier.fromNamespaceAndPath("endernium", "core/dragon_energy");
    private static final Identifier WAVE_SHADER = Identifier.fromNamespaceAndPath("endernium", "core/dragon_wave");
    private static final Identifier POST_SHADER = Identifier.fromNamespaceAndPath("endernium", "core/dragon_post");

    private static final BindGroupLayout POST_BINDINGS = BindGroupLayout.builder()
            .withSampler("Sampler0")
            .withUniform("DragonPost", UniformType.UNIFORM_BUFFER)
            .build();

    private static final RenderPipeline DRAGON_ENERGY = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath("endernium", "pipeline/dragon_energy"))
            .withVertexShader(ENERGY_SHADER)
            .withFragmentShader(ENERGY_SHADER)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .build();

    private static final RenderPipeline DRAGON_WAVE = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath("endernium", "pipeline/dragon_wave"))
            .withVertexShader(WAVE_SHADER)
            .withFragmentShader(WAVE_SHADER)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .build();

    private static final RenderPipeline DRAGON_POST = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("endernium", "pipeline/dragon_post"))
            .withVertexShader("core/screenquad")
            .withFragmentShader(POST_SHADER)
            .withBindGroupLayout(POST_BINDINGS)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build();

    private static final List<RenderPipeline> PIPELINES = List.of(DRAGON_ENERGY, DRAGON_WAVE, DRAGON_POST);
    private static final EnderniumShaderRenderer INSTANCE = new EnderniumShaderRenderer();

    private GpuTexture sceneCopy;
    private GpuTextureView sceneCopyView;
    private GpuSampler sceneSampler;
    private GpuBuffer postUniform;
    private int postWidth;
    private int postHeight;
    private boolean worldPipelineEnabled = true;
    private boolean postPipelineEnabled = true;
    private boolean loggedWorldFailure;
    private boolean loggedPostFailure;

    private EnderniumShaderRenderer() {
    }

    public static EnderniumShaderRenderer instance() {
        return INSTANCE;
    }

    public static List<RenderPipeline> pipelines() {
        return PIPELINES;
    }

    public void render(Matrix4fc modelViewMatrix, Vec3 camera) {
        ExtractedFrame frame = EnderniumVfxManager.frame();
        if (!worldPipelineEnabled || frame.empty() || (frame.buildup() == null && frame.burst() == null)) {
            return;
        }

        BuiltMesh energy = null;
        BuiltMesh wave = null;
        try {
            energy = buildEnergyMesh(frame, camera);
            wave = buildWaveMesh(frame.burst(), camera);
            if (energy == null && wave == null) {
                return;
            }

            RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            GpuTextureView colorView = target.getColorTextureView();
            GpuTextureView depthView = target.getDepthTextureView();
            if (colorView == null || depthView == null) {
                return;
            }

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (RenderPass pass = encoder.createRenderPass(
                    () -> "Endernium dragon eruption",
                    colorView,
                    Optional.empty(),
                    depthView,
                    OptionalDouble.empty()
            )) {
                if (energy != null) {
                    pass.setPipeline(DRAGON_ENERGY);
                    RenderSystem.bindDefaultUniforms(pass);
                    pass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewMatrix)));
                    pass.setVertexBuffer(0, energy.buffer().slice());
                    pass.draw(energy.vertexCount(), 1, 0, 0);
                }
                if (wave != null) {
                    pass.setPipeline(DRAGON_WAVE);
                    RenderSystem.bindDefaultUniforms(pass);
                    pass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewMatrix)));
                    pass.setVertexBuffer(0, wave.buffer().slice());
                    pass.draw(wave.vertexCount(), 1, 0, 0);
                }
            }
        } catch (Throwable throwable) {
            worldPipelineEnabled = false;
            if (!loggedWorldFailure) {
                loggedWorldFailure = true;
                LOGGER.error("Disabling Endernium dragon world shaders for this session; fallback particles remain active", throwable);
            }
        } finally {
            if (energy != null) {
                energy.close();
            }
            if (wave != null) {
                wave.close();
            }
        }
    }

    public void renderPost(Vec3 camera) {
        ExtractedFrame frame = EnderniumVfxManager.frame();
        if (!postPipelineEnabled
                || frame.postIntensity() <= 0.001F
                || !EnderniumVisualConfig.screenDistortion()
                || !EnderniumVisualConfig.observerScreenEffects()) {
            return;
        }

        try {
            RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            GpuTexture source = target.getColorTexture();
            GpuTextureView destination = target.getColorTextureView();
            if (source == null || destination == null) {
                return;
            }
            ensurePostResources(target, source);
            if (sceneCopy == null || sceneCopyView == null || sceneSampler == null || postUniform == null) {
                return;
            }

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.copyTextureToTexture(source, sceneCopy, 0, 0, 0, 0, 0, target.width, target.height);

            ByteBuffer data = MemoryUtil.memAlloc(16);
            try {
                data.putFloat(frame.postIntensity());
                data.putFloat(frame.postPhase());
                data.putFloat(1.0F / Math.max(1, target.width));
                data.putFloat(1.0F / Math.max(1, target.height));
                data.flip();
                encoder.writeToBuffer(postUniform.slice(), data);
            } finally {
                MemoryUtil.memFree(data);
            }

            try (RenderPass pass = encoder.createRenderPass(
                    () -> "Endernium dragon shockwave post",
                    destination,
                    Optional.empty()
            )) {
                pass.setPipeline(DRAGON_POST);
                RenderSystem.bindDefaultUniforms(pass);
                pass.bindTexture("Sampler0", sceneCopyView, sceneSampler);
                pass.setUniform("DragonPost", postUniform);
                pass.draw(3, 1, 0, 0);
            }
        } catch (Throwable throwable) {
            postPipelineEnabled = false;
            closePostResources();
            if (!loggedPostFailure) {
                loggedPostFailure = true;
                LOGGER.error("Disabling Endernium dragon post shader for this session; world geometry remains active", throwable);
            }
        }
    }

    private void ensurePostResources(RenderTarget target, GpuTexture source) {
        if (sceneCopy != null && postWidth == target.width && postHeight == target.height) {
            return;
        }
        closePostResources();
        postWidth = target.width;
        postHeight = target.height;
        sceneCopy = RenderSystem.getDevice().createTexture(
                "Endernium dragon scene copy",
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
                source.getFormat(),
                postWidth,
                postHeight,
                1,
                1
        );
        sceneCopyView = RenderSystem.getDevice().createTextureView(sceneCopy);
        sceneSampler = RenderSystem.getDevice().createSampler(
                AddressMode.CLAMP_TO_EDGE,
                AddressMode.CLAMP_TO_EDGE,
                FilterMode.LINEAR,
                FilterMode.LINEAR,
                1,
                OptionalDouble.empty()
        );
        postUniform = RenderSystem.getDevice().createBuffer(
                () -> "Endernium dragon post uniforms",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                16
        );
    }

    private BuiltMesh buildEnergyMesh(ExtractedFrame frame, Vec3 camera) {
        try (ByteBufferBuilder bytes = new ByteBufferBuilder(256 * 1024)) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
            if (frame.buildup() != null) {
                addBuildup(builder, frame.buildup(), camera);
            }
            if (frame.burst() != null) {
                addBurstEnergy(builder, frame.burst(), camera);
            }
            MeshData mesh = builder.build();
            return upload(mesh, "Endernium dragon energy vertices");
        }
    }

    private BuiltMesh buildWaveMesh(BurstState burst, Vec3 camera) {
        if (burst == null || burst.age() <= 0.0F || burst.age() > EnderniumVfxManager.BURST_DURATION) {
            return null;
        }
        try (ByteBufferBuilder bytes = new ByteBufferBuilder(96 * 1024)) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
            addShockwave(builder, burst, camera);
            MeshData mesh = builder.build();
            return upload(mesh, "Endernium dragon wave vertices");
        }
    }

    private BuiltMesh upload(MeshData mesh, String label) {
        if (mesh == null) {
            return null;
        }
        try (mesh) {
            if (!mesh.vertexBuffer().hasRemaining() || mesh.drawState().vertexCount() == 0) {
                return null;
            }
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                    () -> label,
                    GpuBuffer.USAGE_VERTEX,
                    mesh.vertexBuffer()
            );
            return new BuiltMesh(buffer, mesh.drawState().vertexCount());
        }
    }

    private void addBuildup(BufferBuilder builder, BuildupState state, Vec3 camera) {
        float progress = state.deathTicks() / EnderniumVfxManager.DRAGON_DEATH_DURATION;
        float intensity = 0.16F + 0.84F * progress * progress;
        float terminalCompression = Math.max(0.0F, Math.min(1.0F, (state.deathTicks() - 190.0F) / 10.0F));
        int count = EnderniumVisualConfig.cinematic() ? 42 : 21;
        Random random = new Random(state.seed() ^ 0xB017D0A1L);
        for (int i = 0; i < count; i++) {
            float phase = random.nextFloat();
            float speed = 0.018F + random.nextFloat() * 0.035F + progress * 0.025F;
            float cycle = fract(phase + state.deathTicks() * speed);
            double radius = (1.2 + (1.0 - cycle) * (7.0 + random.nextDouble() * 11.0))
                    * (1.0 - terminalCompression * 0.84);
            double angle = random.nextDouble() * Math.PI * 2.0 + state.deathTicks() * (0.025 + random.nextDouble() * 0.04);
            double y = (random.nextDouble() - 0.5) * 10.0 * (0.35 + radius / 18.0);
            Vec3 outer = state.origin().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            Vec3 inward = state.origin().subtract(outer).normalize();
            Vec3 inner = outer.add(inward.scale(0.7 + progress * 2.3));
            int alpha = clampColor((int)(210.0F * intensity * (0.35F + 0.65F * cycle)));
            int color = argb(alpha, 224, 82, 255);
            addRibbon(builder, outer, inner, 0.045 + progress * 0.09, color, camera, phase);
        }

        float pulse = 0.5F + 0.5F * (float)Math.sin(state.deathTicks() * 0.43F);
        float grownCoreRadius = 0.22F + progress * 0.86F + pulse * (0.03F + progress * 0.07F);
        float coreRadius = grownCoreRadius * (1.0F - terminalCompression)
                + (0.34F + pulse * 0.035F) * terminalCompression;
        int coreAlpha = clampColor((int)(105 + progress * 135 + pulse * 15));
        double coreSpin = state.deathTicks() * (0.018 + progress * 0.032);
        addFacetedCore(builder, state.origin(), coreRadius, coreSpin, coreAlpha, camera);

        double shellRadius = (0.72 + progress * 0.92) * (1.0 - terminalCompression * 0.62);
        int shellAlpha = clampColor((int)((42 + progress * 128) * (0.82 + pulse * 0.18)));
        addBrokenCoreRing(builder, state.origin(), shellRadius, 0.025 + progress * 0.035,
                coreSpin * 1.28, 0.24, state.seed() ^ 0x51E11A11L,
                argb(shellAlpha, 224, 82, 255), camera);
        addBrokenCoreRing(builder, state.origin(), shellRadius * 1.28, 0.022 + progress * 0.03,
                -coreSpin * 0.91, 1.08, state.seed() ^ 0xA17E2B05L,
                argb(clampColor((int)(shellAlpha * 0.78F)), 116, 39, 255), camera);

        if (state.deathTicks() >= 120.0F) {
            float filamentProgress = (state.deathTicks() - 120.0F) / 80.0F;
            int filaments = EnderniumVisualConfig.cinematic() ? 12 : 6;
            Random filamentRandom = new Random(state.seed() ^ ((long)(state.deathTicks() / 3.0F) * 0x9E3779B97F4A7C15L));
            for (int i = 0; i < filaments; i++) {
                Vec3 direction = randomUnit(filamentRandom);
                double contraction = 1.0 - terminalCompression * 0.88;
                Vec3 from = state.origin().add(direction.scale((3.0 + filamentRandom.nextDouble() * 8.0) * contraction));
                Vec3 bend = state.origin().add(direction.scale((1.0 + filamentRandom.nextDouble() * 2.5) * contraction))
                        .add(randomUnit(filamentRandom).scale(0.8 * contraction));
                int color = argb(clampColor((int)(110 + filamentProgress * 120)), 116, 39, 255);
                addRibbon(builder, from, bend, 0.05 + filamentProgress * 0.12, color, camera, i / (float)filaments);
                addRibbon(builder, bend, state.origin(), 0.04 + filamentProgress * 0.09, color, camera, i / (float)filaments + 0.5F);
            }
        }
    }

    private void addBurstEnergy(BufferBuilder builder, BurstState state, Vec3 camera) {
        float age = state.age();
        if (age < 2.5F) {
            float flash = 1.0F - age / 2.5F;
            double flashRadius = 1.8 + age * 2.7;
            addFacetedCore(builder, state.origin(), flashRadius, age * 0.62,
                    clampColor((int)(255 * flash)), camera);
        }

        Vec3 cometOrigin = displayOriginForDistance(state.origin(), camera);
        double displayScale = cometOrigin == state.origin() ? 1.0 : 0.42;
        int limit = EnderniumVisualConfig.cinematic() ? state.comets().size() : state.comets().size() / 2;
        int samples = EnderniumVisualConfig.cinematic() ? 7 : 4;
        for (int i = 0; i < limit; i++) {
            CometPath comet = state.comets().get(i);
            float localAge = age - comet.delay();
            if (localAge <= 0.0F || localAge >= comet.lifetime()) {
                continue;
            }
            float fade = Math.min(1.0F, localAge / 2.0F) * Math.min(1.0F, (comet.lifetime() - localAge) / 8.0F);
            float timeStep = Math.max(0.45F, comet.trailLength() / Math.max(1.0F, comet.speed()) / samples);
            Vec3 previous = scaledCometPosition(comet, cometOrigin, state.age(), displayScale);
            for (int sample = 1; sample <= samples; sample++) {
                float sampleAge = state.age() - sample * timeStep;
                if (sampleAge <= comet.delay()) {
                    break;
                }
                Vec3 next = scaledCometPosition(comet, cometOrigin, sampleAge, displayScale);
                float taper = 1.0F - sample / (float)(samples + 1);
                int alpha = clampColor((int)(235 * fade * taper));
                int color = sample <= 2
                        ? cometHeadColor(comet.headVariant(), alpha)
                        : argb(alpha, 224, 82, 255);
                addRibbon(builder, next, previous, comet.width() * taper * displayScale, color, camera, i * 0.071F + sample * 0.13F);
                previous = next;
            }
            Vec3 head = scaledCometPosition(comet, cometOrigin, state.age(), displayScale);
            addCrossedCore(builder, head, comet.width() * 2.3 * displayScale,
                    cometHeadColor(comet.headVariant(), clampColor((int)(255 * fade))), camera);
        }
    }

    private int cometHeadColor(int variant, int alpha) {
        return switch (variant) {
            case 1 -> argb(alpha, 224, 82, 255);
            case 2 -> argb(alpha, 116, 39, 255);
            default -> argb(alpha, 242, 232, 255);
        };
    }

    private void addShockwave(BufferBuilder builder, BurstState state, Vec3 camera) {
        double cameraDistance = horizontalDistance(state.origin(), camera);
        float age = state.age();
        Vec3 center = state.origin();
        float radius = age * EnderniumVfxManager.WAVE_SPEED;

        if (cameraDistance > 1800.0) {
            float arrival = Math.min(60.0F, (float)(cameraDistance / EnderniumVfxManager.WAVE_SPEED));
            float localAge = age - arrival + 5.0F;
            if (localAge <= 0.0F || localAge > 16.0F) {
                return;
            }
            Vec3 away = new Vec3(camera.x - state.origin().x, 0.0, camera.z - state.origin().z).normalize();
            center = camera.subtract(away.scale(110.0)).add(0.0, 22.0, 0.0);
            radius = localAge * EnderniumVfxManager.WAVE_SPEED;
        } else if (radius > 1700.0F) {
            return;
        }

        int segments = EnderniumVisualConfig.cinematic() ? 128 : 64;
        float fade = Math.min(1.0F, age / 2.0F) * Math.min(1.0F, (EnderniumVfxManager.BURST_DURATION - age) / 14.0F);
        float wake = 14.0F + Math.min(18.0F, age * 0.35F);
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 1) / segments;
            float n0 = waveNoise(state.seed(), i, age);
            float n1 = waveNoise(state.seed(), i + 1, age);
            double outer0 = radius + n0 * 2.8;
            double outer1 = radius + n1 * 2.8;
            double inner0 = Math.max(0.0, outer0 - wake - Math.abs(n0) * 5.0);
            double inner1 = Math.max(0.0, outer1 - wake - Math.abs(n1) * 5.0);
            double y0 = center.y + Math.sin(a0 * 5.0 + age * 0.35) * 0.65;
            double y1 = center.y + Math.sin(a1 * 5.0 + age * 0.35) * 0.65;
            Vec3 p00 = center.add(Math.cos(a0) * inner0, y0 - center.y, Math.sin(a0) * inner0);
            Vec3 p01 = center.add(Math.cos(a1) * inner1, y1 - center.y, Math.sin(a1) * inner1);
            Vec3 p11 = center.add(Math.cos(a1) * outer1, y1 - center.y, Math.sin(a1) * outer1);
            Vec3 p10 = center.add(Math.cos(a0) * outer0, y0 - center.y, Math.sin(a0) * outer0);
            int innerColor = argb(clampColor((int)(35 * fade)), 116, 39, 255);
            int outerColor = argb(clampColor((int)(235 * fade)), 242, 232, 255);
            float u0 = i / (float)segments * 12.0F + age * 0.03F;
            float u1 = (i + 1) / (float)segments * 12.0F + age * 0.03F;
            addQuad(builder, p00, p01, p11, p10, innerColor, innerColor, outerColor, outerColor, u0, u1, camera);
        }
    }

    private void addRibbon(BufferBuilder builder, Vec3 start, Vec3 end, double width, int color, Vec3 camera, float u) {
        Vec3 along = end.subtract(start);
        if (along.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 toCamera = camera.subtract(start);
        Vec3 side = along.cross(toCamera);
        if (side.lengthSqr() < 1.0E-6) {
            side = along.cross(new Vec3(0.0, 1.0, 0.0));
        }
        if (side.lengthSqr() < 1.0E-6) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        side = side.normalize().scale(width);
        Vec3 p0 = start.subtract(side);
        Vec3 p1 = end.subtract(side);
        Vec3 p2 = end.add(side);
        Vec3 p3 = start.add(side);
        addQuad(builder, p0, p1, p2, p3, color, color, color, color, u, u + 1.0F, camera);
    }

    private void addFacetedCore(
            BufferBuilder builder,
            Vec3 center,
            double radius,
            double spin,
            int alpha,
            Vec3 camera
    ) {
        Vec3 axisX = rotateY(new Vec3(radius, 0.0, 0.0), spin);
        Vec3 axisZ = rotateY(new Vec3(0.0, 0.0, radius), spin);
        Vec3 axisY = new Vec3(0.0, radius * 1.22, 0.0);
        Vec3 top = center.add(axisY);
        Vec3 bottom = center.subtract(axisY);
        Vec3 east = center.add(axisX);
        Vec3 west = center.subtract(axisX);
        Vec3 south = center.add(axisZ);
        Vec3 north = center.subtract(axisZ);

        int outerAlpha = clampColor((int)(alpha * 0.48F));
        int violet = argb(outerAlpha, 116, 39, 255);
        int magenta = argb(outerAlpha, 224, 82, 255);
        addFacetTriangle(builder, top, east, south, magenta, 0.92F, camera);
        addFacetTriangle(builder, top, south, west, violet, 0.72F, camera);
        addFacetTriangle(builder, top, west, north, magenta, 0.84F, camera);
        addFacetTriangle(builder, top, north, east, violet, 0.66F, camera);
        addFacetTriangle(builder, bottom, south, east, violet, 0.58F, camera);
        addFacetTriangle(builder, bottom, west, south, magenta, 0.76F, camera);
        addFacetTriangle(builder, bottom, north, west, violet, 0.62F, camera);
        addFacetTriangle(builder, bottom, east, north, magenta, 0.81F, camera);

        double innerScale = 0.57;
        Vec3 innerTop = center.add(axisY.scale(innerScale));
        Vec3 innerBottom = center.subtract(axisY.scale(innerScale));
        Vec3 innerEast = center.add(axisX.scale(innerScale));
        Vec3 innerWest = center.subtract(axisX.scale(innerScale));
        Vec3 innerSouth = center.add(axisZ.scale(innerScale));
        Vec3 innerNorth = center.subtract(axisZ.scale(innerScale));
        int hot = argb(alpha, 242, 232, 255);
        addFacetTriangle(builder, innerTop, innerEast, innerSouth, hot, 1.0F, camera);
        addFacetTriangle(builder, innerTop, innerSouth, innerWest, hot, 0.94F, camera);
        addFacetTriangle(builder, innerTop, innerWest, innerNorth, hot, 1.0F, camera);
        addFacetTriangle(builder, innerTop, innerNorth, innerEast, hot, 0.92F, camera);
        addFacetTriangle(builder, innerBottom, innerSouth, innerEast, hot, 0.82F, camera);
        addFacetTriangle(builder, innerBottom, innerWest, innerSouth, hot, 0.9F, camera);
        addFacetTriangle(builder, innerBottom, innerNorth, innerWest, hot, 0.8F, camera);
        addFacetTriangle(builder, innerBottom, innerEast, innerNorth, hot, 0.88F, camera);

        int edgeColor = argb(clampColor((int)(alpha * 0.72F)), 224, 82, 255);
        double edgeWidth = Math.max(0.018, radius * 0.045);
        addRibbon(builder, top, east, edgeWidth, edgeColor, camera, 0.11F);
        addRibbon(builder, top, south, edgeWidth, edgeColor, camera, 0.23F);
        addRibbon(builder, top, west, edgeWidth, edgeColor, camera, 0.37F);
        addRibbon(builder, top, north, edgeWidth, edgeColor, camera, 0.49F);
        addRibbon(builder, bottom, east, edgeWidth, edgeColor, camera, 0.61F);
        addRibbon(builder, bottom, south, edgeWidth, edgeColor, camera, 0.73F);
        addRibbon(builder, bottom, west, edgeWidth, edgeColor, camera, 0.87F);
        addRibbon(builder, bottom, north, edgeWidth, edgeColor, camera, 0.99F);
        addRibbon(builder, east, south, edgeWidth, edgeColor, camera, 1.13F);
        addRibbon(builder, south, west, edgeWidth, edgeColor, camera, 1.27F);
        addRibbon(builder, west, north, edgeWidth, edgeColor, camera, 1.41F);
        addRibbon(builder, north, east, edgeWidth, edgeColor, camera, 1.59F);
    }

    private void addBrokenCoreRing(
            BufferBuilder builder,
            Vec3 center,
            double radius,
            double width,
            double spin,
            double tilt,
            long seed,
            int color,
            Vec3 camera
    ) {
        Vec3 axisA = rotateY(new Vec3(1.0, 0.0, 0.0), spin);
        Vec3 axisB = rotateY(new Vec3(0.0, Math.cos(tilt), Math.sin(tilt)), spin);
        int segments = EnderniumVisualConfig.cinematic() ? 28 : 16;
        for (int i = 0; i < segments; i++) {
            long bits = seed + i * 0x9E3779B97F4A7C15L;
            bits ^= bits >>> 30;
            bits *= 0xBF58476D1CE4E5B9L;
            bits ^= bits >>> 27;
            if ((bits & 7L) < 3L) {
                continue;
            }
            double a0 = Math.PI * 2.0 * i / segments;
            double a1 = Math.PI * 2.0 * (i + 0.72) / segments;
            Vec3 p0 = center.add(axisA.scale(Math.cos(a0) * radius)).add(axisB.scale(Math.sin(a0) * radius));
            Vec3 p1 = center.add(axisA.scale(Math.cos(a1) * radius)).add(axisB.scale(Math.sin(a1) * radius));
            addRibbon(builder, p0, p1, width, color, camera, i * 0.19F);
        }
    }

    private void addFacetTriangle(
            BufferBuilder builder,
            Vec3 p0,
            Vec3 p1,
            Vec3 p2,
            int color,
            float shade,
            Vec3 camera
    ) {
        vertex(builder, p0, color, -2.0F, shade, camera);
        vertex(builder, p1, color, -2.0F, shade, camera);
        vertex(builder, p2, color, -2.0F, shade, camera);
    }

    private Vec3 rotateY(Vec3 vector, double angle) {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        return new Vec3(
                vector.x * cos - vector.z * sin,
                vector.y,
                vector.x * sin + vector.z * cos
        );
    }

    private void addCrossedCore(BufferBuilder builder, Vec3 center, double radius, int color, Vec3 camera) {
        Vec3 view = camera.subtract(center);
        if (view.lengthSqr() < 1.0E-6) {
            view = new Vec3(0.0, 0.0, 1.0);
        }
        view = view.normalize();
        Vec3 right = view.cross(new Vec3(0.0, 1.0, 0.0));
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        }
        right = right.normalize().scale(radius);
        Vec3 up = right.cross(view).normalize().scale(radius);
        addQuad(builder,
                center.subtract(right).subtract(up),
                center.add(right).subtract(up),
                center.add(right).add(up),
                center.subtract(right).add(up),
                color, color, color, color, 0.0F, 1.0F, camera);
    }

    private void addQuad(
            BufferBuilder builder,
            Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
            int c0, int c1, int c2, int c3,
            float u0, float u1,
            Vec3 camera
    ) {
        vertex(builder, p0, c0, u0, 0.0F, camera);
        vertex(builder, p1, c1, u1, 0.0F, camera);
        vertex(builder, p2, c2, u1, 1.0F, camera);
        vertex(builder, p2, c2, u1, 1.0F, camera);
        vertex(builder, p3, c3, u0, 1.0F, camera);
        vertex(builder, p0, c0, u0, 0.0F, camera);
    }

    private void vertex(BufferBuilder builder, Vec3 point, int color, float u, float v, Vec3 camera) {
        builder.addVertex(
                        (float)(point.x - camera.x),
                        (float)(point.y - camera.y),
                        (float)(point.z - camera.z)
                )
                .setColor(color)
                .setUv(u, v);
    }

    private Vec3 displayOriginForDistance(Vec3 origin, Vec3 camera) {
        Vec3 horizontal = new Vec3(origin.x - camera.x, 0.0, origin.z - camera.z);
        if (horizontal.lengthSqr() <= 640.0 * 640.0) {
            return origin;
        }
        Vec3 direction = horizontal.normalize();
        return camera.add(direction.scale(220.0)).add(0.0, 58.0, 0.0);
    }

    private Vec3 scaledCometPosition(CometPath comet, Vec3 origin, float age, double scale) {
        Vec3 position = comet.position(Vec3.ZERO, age).scale(scale);
        return origin.add(position);
    }

    private float waveNoise(long seed, int segment, float age) {
        long bits = seed + segment * 0x9E3779B97F4A7C15L;
        bits ^= bits >>> 30;
        bits *= 0xBF58476D1CE4E5B9L;
        bits ^= bits >>> 27;
        float random = ((bits >>> 40) & 0xFFFFFF) / (float)0xFFFFFF * 2.0F - 1.0F;
        return random * 0.55F + (float)Math.sin(segment * 1.73 + age * 0.47) * 0.45F;
    }

    private static Vec3 randomUnit(Random random) {
        Vec3 vector;
        do {
            vector = new Vec3(random.nextDouble() * 2.0 - 1.0, random.nextDouble() * 2.0 - 1.0, random.nextDouble() * 2.0 - 1.0);
        } while (vector.lengthSqr() < 1.0E-5);
        return vector.normalize();
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static float fract(float value) {
        return value - (float)Math.floor(value);
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public void resetBuffers() {
        closePostResources();
        worldPipelineEnabled = true;
        postPipelineEnabled = true;
        loggedWorldFailure = false;
        loggedPostFailure = false;
    }

    private void closePostResources() {
        if (postUniform != null) {
            postUniform.close();
            postUniform = null;
        }
        if (sceneSampler != null) {
            sceneSampler.close();
            sceneSampler = null;
        }
        if (sceneCopyView != null) {
            sceneCopyView.close();
            sceneCopyView = null;
        }
        if (sceneCopy != null) {
            sceneCopy.close();
            sceneCopy = null;
        }
        postWidth = 0;
        postHeight = 0;
    }

    @Override
    public void close() {
        closePostResources();
    }

    private record BuiltMesh(GpuBuffer buffer, int vertexCount) implements AutoCloseable {
        @Override
        public void close() {
            buffer.close();
        }
    }
}
