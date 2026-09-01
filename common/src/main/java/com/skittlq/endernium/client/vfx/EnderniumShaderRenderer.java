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
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.CrackState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.DistantCometPath;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.DistantImpactState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.DistantWaveState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.ExtractedFrame;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.PillarPulse;
import com.skittlq.endernium.config.EnderniumVisualConfig;
import com.skittlq.endernium.vfx.DragonDeathVfxTiming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
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

    /*
     * POSITION_TEX_COLOR gives these procedural meshes one spare vec2. The U coordinate doubles
     * as a tiny material channel; matching branches live at the top of the fragment shaders.
     */
    private static final float ENERGY_FACET_MATERIAL_U = -2.0F;
    private static final float ENERGY_HEAD_MATERIAL_U = -0.9F;
    private static final float ENERGY_HEAD_MATERIAL_U_SPAN = 0.8F;
    private static final float ENERGY_CLEAN_RIBBON_MATERIAL_U = -5.0F;
    private static final float WAVE_MANGA_MATERIAL_U = -8.0F;
    private static final float WAVE_DISTANT_MATERIAL_U = -3.8F;
    private static final float WAVE_DISTANT_MATERIAL_U_SPAN = 0.6F;

    private static final float DISTANT_WAVE_VISIBLE_BEFORE_ARRIVAL = 5.0F;
    private static final float DISTANT_WAVE_VISIBLE_AFTER_ARRIVAL = 10.0F;
    private static final double DISTANT_WAVE_HALF_WIDTH = 192.0;
    private static final double DISTANT_WAVE_WAKE_LENGTH = 32.0;
    private static final double DISTANT_WAVE_CURVATURE_RADIUS = 900.0;

    private static final BindGroupLayout POST_BINDINGS = BindGroupLayout.builder()
            .withSampler("Sampler0")
            .withSampler("SamplerDepth")
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
    private GpuSampler depthSampler;
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
                || (frame.postIntensity() <= 0.001F
                && frame.atmosphereIntensity() <= 0.001F
                && frame.impactIntensity() <= 0.001F
                && frame.detonationShakeIntensity() <= 0.001F)) {
            return;
        }

        try {
            RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            GpuTexture source = target.getColorTexture();
            GpuTextureView destination = target.getColorTextureView();
            GpuTextureView depth = target.getDepthTextureView();
            if (source == null || destination == null || depth == null) {
                return;
            }
            ensurePostResources(target, source);
            if (sceneCopy == null
                    || sceneCopyView == null
                    || sceneSampler == null
                    || depthSampler == null
                    || postUniform == null) {
                return;
            }

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.copyTextureToTexture(source, sceneCopy, 0, 0, 0, 0, 0, target.width, target.height);

            ByteBuffer data = MemoryUtil.memAlloc(32);
            try {
                data.putFloat(frame.postIntensity());
                data.putFloat(frame.postPhase());
                data.putFloat(1.0F / Math.max(1, target.width));
                data.putFloat(1.0F / Math.max(1, target.height));
                data.putFloat(frame.atmosphereIntensity());
                data.putFloat(frame.impactIntensity());
                data.putFloat(frame.impactPhase());
                data.putFloat(frame.detonationShakeIntensity());
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
                pass.bindTexture("SamplerDepth", depth, depthSampler);
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
        depthSampler = RenderSystem.getDevice().createSampler(
                AddressMode.CLAMP_TO_EDGE,
                AddressMode.CLAMP_TO_EDGE,
                FilterMode.NEAREST,
                FilterMode.NEAREST,
                1,
                OptionalDouble.empty()
        );
        postUniform = RenderSystem.getDevice().createBuffer(
                () -> "Endernium dragon post uniforms",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                32
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
                addTerrainCracks(builder, frame.cracks(), camera);
                addPillarPulses(builder, frame.pillars(), frame.burst(), camera);
            }
            addDistantImpactFlashes(builder, frame.distantImpacts(), camera);
            MeshData mesh = builder.build();
            return upload(mesh, "Endernium dragon energy vertices");
        }
    }

    private BuiltMesh buildWaveMesh(BurstState burst, Vec3 camera) {
        if (burst == null || burst.age() <= 0.0F) {
            return null;
        }
        try (ByteBufferBuilder bytes = new ByteBufferBuilder(192 * 1024)) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
            addShockwave(builder, burst, camera);
            addMangaImpactLines(builder, burst, camera);
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
        float age = state.deathTicks();
        float progress = Mth.clamp(age / EnderniumVfxManager.DRAGON_DEATH_DURATION, 0.0F, 1.0F);
        float fadeIn = smoothProgress(age / 36.0F);
        float acceleratedAge = EnderniumVfxManager.DRAGON_DEATH_DURATION
                * exponentialProgress(progress, 2.3F);
        float filamentProgress = smoothProgress((age - 120.0F) / 60.0F);
        float terminalCompression = (float)Math.pow(Mth.clamp((age - 160.0F) / 40.0F, 0.0F, 1.0F), 2.2);
        float pulsePhase = acceleratedAge * 0.22F;
        float pulse = 0.5F + 0.5F * (float)Math.sin(pulsePhase);
        float intensity = 0.14F + 0.86F * progress * progress;

        // The primary read is a broken End-crystal-like cage that tightens around the core.
        int shellCount = EnderniumVisualConfig.cinematic() ? 3 : 2;
        for (int shell = 0; shell < shellCount; shell++) {
            double relaxedRadius = 2.15 + shell * 1.48 + progress * (0.42 + shell * 0.16);
            double compressedRadius = 0.82 + shell * 0.57;
            double radius = relaxedRadius + (compressedRadius - relaxedRadius) * terminalCompression;
            double spin = acceleratedAge * (0.018 + shell * 0.006);
            double pitch = 0.42 + shell * 0.73 + Math.sin(acceleratedAge * 0.016 + shell) * 0.12;
            double roll = (shell % 2 == 0 ? 1.0 : -1.0) * spin * (0.62 + shell * 0.13);
            int alpha = clampColor((int)((48 + progress * 142 + terminalCompression * 42)
                    * (0.82F + pulse * 0.18F) * fadeIn));
            int color = switch (shell) {
                case 0 -> argb(alpha, 242, 232, 255);
                case 1 -> argb(alpha, 224, 82, 255);
                default -> argb(alpha, 116, 39, 255);
            };
            addBrokenDiamondShell(
                    builder,
                    state.origin(),
                    radius,
                    0.025 + progress * 0.038 + terminalCompression * 0.028,
                    spin,
                    pitch,
                    roll,
                    state.seed() ^ (0x51E11A11L + shell * 0x2D35A4C7L),
                    color,
                    camera
            );
        }

        // Short fragments spiral inward. Their tails point away from the core, making travel direction legible.
        int fragmentCount = EnderniumVisualConfig.cinematic() ? 48 : 24;
        Random fragmentRandom = new Random(state.seed() ^ 0xB017D0A1L);
        for (int i = 0; i < fragmentCount; i++) {
            float phase = fragmentRandom.nextFloat();
            float speed = 0.018F + fragmentRandom.nextFloat() * 0.022F;
            float cycle = fract(phase + acceleratedAge * speed);
            float approach = smoothProgress(cycle);
            double farRadius = 8.0 + fragmentRandom.nextDouble() * 10.0;
            double nearRadius = 1.05 + fragmentRandom.nextDouble() * 1.15;
            farRadius *= 1.0 - terminalCompression * 0.68;
            nearRadius *= 1.0 - terminalCompression * 0.34;
            double radius = farRadius + (nearRadius - farRadius) * approach;
            double baseAngle = fragmentRandom.nextDouble() * Math.PI * 2.0;
            double orbitDirection = fragmentRandom.nextBoolean() ? 1.0 : -1.0;
            double angle = baseAngle + orbitDirection * (acceleratedAge * 0.026 + cycle * 0.72);
            double verticalBias = fragmentRandom.nextDouble() * 2.0 - 1.0;
            double y = verticalBias * radius * 0.48
                    + Math.sin(angle * 1.7 + phase * 9.0) * (0.35 + radius * 0.035);
            Vec3 position = state.origin().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            Vec3 outward = position.subtract(state.origin()).normalize();
            double trailLength = 0.38 + progress * 0.52 + fragmentRandom.nextDouble() * 0.62;
            Vec3 tail = position.add(outward.scale(trailLength));
            Vec3 head = position.subtract(outward.scale(0.08 + progress * 0.14));
            int alpha = clampColor((int)(220.0F * intensity * (0.38F + cycle * 0.62F) * fadeIn));
            int color = i % 7 == 0
                    ? argb(alpha, 242, 232, 255)
                    : argb(alpha, 224, 82, 255);
            addRibbon(builder, tail, head, 0.035 + progress * 0.055, color, camera,
                    ENERGY_CLEAN_RIBBON_MATERIAL_U);
            if (i % 6 == 0) {
                addBillboardCore(builder, head, 0.055 + progress * 0.045, color, camera);
            }
        }

        // A few local filaments snap in different pulse groups instead of forming one uniform starburst.
        if (filamentProgress > 0.0F) {
            int filaments = EnderniumVisualConfig.cinematic() ? 14 : 7;
            Random filamentRandom = new Random(state.seed() ^ 0xF11A6E47L);
            for (int i = 0; i < filaments; i++) {
                float phase = filamentRandom.nextFloat() * Mth.TWO_PI;
                float snap = Math.max(0.0F, (float)Math.sin(
                        acceleratedAge * 0.35F + phase
                ));
                snap *= snap * snap;
                if (snap < 0.07F) {
                    randomUnit(filamentRandom);
                    randomUnit(filamentRandom);
                    filamentRandom.nextDouble();
                    continue;
                }
                Vec3 direction = randomUnit(filamentRandom);
                Vec3 lateral = randomUnit(filamentRandom);
                double relaxedReach = 3.2 + filamentRandom.nextDouble() * 4.8;
                double reach = relaxedReach * (1.0 - terminalCompression * 0.72);
                double innerReach = (0.62 + i % 3 * 0.24) * (1.0 - terminalCompression * 0.28);
                Vec3 from = state.origin().add(direction.scale(reach));
                Vec3 bend = state.origin().add(direction.scale(reach * 0.52))
                        .add(lateral.scale((0.25 + i % 4 * 0.11) * (1.0 - terminalCompression * 0.7)));
                Vec3 inner = state.origin().add(direction.scale(innerReach));
                int alpha = clampColor((int)((85 + filamentProgress * 145) * snap * fadeIn));
                int color = i % 4 == 0
                        ? argb(alpha, 242, 232, 255)
                        : argb(alpha, 116, 39, 255);
                addRibbon(builder, from, bend, 0.035 + filamentProgress * 0.075, color, camera,
                        ENERGY_CLEAN_RIBBON_MATERIAL_U);
                addRibbon(builder, bend, inner, 0.028 + filamentProgress * 0.06, color, camera,
                        ENERGY_CLEAN_RIBBON_MATERIAL_U);
            }
        }

        float grownCoreRadius = 0.24F + progress * 0.78F + pulse * (0.025F + progress * 0.055F);
        float compressedCoreRadius = 0.42F + pulse * 0.025F;
        float coreRadius = grownCoreRadius + (compressedCoreRadius - grownCoreRadius) * terminalCompression;
        int coreAlpha = clampColor((int)((105 + progress * 125 + terminalCompression * 25 + pulse * 12) * fadeIn));
        double coreSpin = acceleratedAge * 0.048;
        addFacetedCore(builder, state.origin(), coreRadius, coreSpin, coreAlpha, camera);

        if (terminalCompression > 0.0F) {
            int hotAlpha = clampColor((int)(245 * terminalCompression * (0.86F + pulse * 0.14F)));
            addBillboardCore(
                    builder,
                    state.origin(),
                    0.16 + terminalCompression * 0.30,
                    argb(hotAlpha, 242, 232, 255),
                    camera
            );
        }
    }

    private void addBurstEnergy(BufferBuilder builder, BurstState state, Vec3 camera) {
        if (state.distantObserver()) {
            if (state.distantWave() != null) {
                addDistantComets(builder, state.distantWave(), state.age(), camera);
            }
            return;
        }

        float age = state.age();

        if (age < 2.5F) {
            float flash = 1.0F - age / 2.5F;
            double flashRadius = 1.8 + age * 2.7;
            addFacetedCore(builder, state.origin(), flashRadius, age * 0.62,
                    clampColor((int)(255 * flash)), camera);
        }

        Vec3 cometOrigin = state.origin();
        double displayScale = 1.0;
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
            Vec3 previous = scaledCometPosition(comet, cometOrigin, age, displayScale);
            for (int sample = 1; sample <= samples; sample++) {
                float sampleAge = age - sample * timeStep;
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
            Vec3 head = scaledCometPosition(comet, cometOrigin, age, displayScale);
            addBillboardCore(builder, head, comet.width() * 2.3 * displayScale,
                    cometHeadColor(comet.headVariant(), clampColor((int)(255 * fade))), camera);
        }
    }

    private void addDistantComets(
            BufferBuilder builder,
            DistantWaveState distant,
            float age,
            Vec3 camera
    ) {
        int samples = EnderniumVisualConfig.cinematic() ? 7 : 4;
        for (int i = 0; i < distant.comets().size(); i++) {
            DistantCometPath comet = distant.comets().get(i);
            if (age < comet.startAge() || age >= comet.endAge()) {
                continue;
            }
            float fadeIn = Math.min(1.0F, (age - comet.startAge()) / 1.5F);
            float fadeOut = Math.min(1.0F, (comet.endAge() - age) / 1.5F);
            float fade = Math.max(0.0F, Math.min(fadeIn, fadeOut));
            float duration = comet.endAge() - comet.startAge();
            float sampleStep = Math.max(0.45F, duration / (samples * 2.8F));
            Vec3 previous = comet.position(age);
            for (int sample = 1; sample <= samples; sample++) {
                float sampleAge = age - sample * sampleStep;
                if (sampleAge <= comet.startAge()) {
                    break;
                }
                Vec3 next = comet.position(sampleAge);
                float taper = 1.0F - sample / (float)(samples + 1);
                int alpha = clampColor((int)(245 * fade * taper));
                int color = sample <= 2
                        ? cometHeadColor(comet.headVariant(), alpha)
                        : argb(alpha, 224, 82, 255);
                addRibbon(builder, next, previous, comet.width() * taper, color, camera,
                        i * 0.083F + sample * 0.17F);
                previous = next;
            }
            Vec3 head = comet.position(age);
            addBillboardCore(
                    builder,
                    head,
                    comet.width() * 2.45,
                    cometHeadColor(comet.headVariant(), clampColor((int)(255 * fade))),
                    camera
            );
        }
    }

    private void addDistantImpactFlashes(
            BufferBuilder builder,
            List<DistantImpactState> impacts,
            Vec3 camera
    ) {
        for (DistantImpactState impact : impacts) {
            if (impact.age() < 0.0F || impact.age() > 4.0F) {
                continue;
            }
            float fade = Math.max(0.0F, 1.0F - impact.age() / 4.0F);
            double radius = 0.32 + impact.age() * 0.22;
            int alpha = clampColor((int)(255 * fade));
            addBillboardCore(
                    builder,
                    impact.position().add(0.0, 0.12, 0.0),
                    radius,
                    argb(alpha, 242, 232, 255),
                    camera
            );
            addBrokenCoreRing(
                    builder,
                    impact.position().add(0.0, 0.08, 0.0),
                    radius * 1.7,
                    0.035 + impact.age() * 0.008,
                    impact.age() * 0.31,
                    0.08,
                    impact.seed(),
                    argb(clampColor((int)(220 * fade)), 224, 82, 255),
                    camera
            );
        }
    }

    private int cometHeadColor(int variant, int alpha) {
        return switch (variant) {
            case 1 -> argb(alpha, 224, 82, 255);
            case 2 -> argb(alpha, 116, 39, 255);
            default -> argb(alpha, 242, 232, 255);
        };
    }

    private void addTerrainCracks(BufferBuilder builder, List<CrackState> cracks, Vec3 camera) {
        for (CrackState crack : cracks) {
            if (crack.age() < 0.0F || crack.age() >= crack.lifetime()) {
                continue;
            }
            float appear = Math.min(1.0F, crack.age() * 1.4F);
            float fade = Math.min(1.0F, (crack.lifetime() - crack.age()) / 7.0F);
            float alpha = appear * fade;
            if (alpha <= 0.01F) {
                continue;
            }

            Random random = new Random(crack.seed());
            Vec3 direction = new Vec3(Math.cos(crack.angle()), 0.0, Math.sin(crack.angle()));
            Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
            Vec3 cursor = crack.position().subtract(direction.scale(crack.scale() * 0.58));
            int segments = EnderniumVisualConfig.cinematic() ? 4 : 3;
            for (int segment = 0; segment < segments; segment++) {
                double length = crack.scale() * (0.28 + random.nextDouble() * 0.22);
                Vec3 next = cursor.add(direction.scale(length))
                        .add(side.scale((random.nextDouble() - 0.5) * crack.scale() * 0.26));
                int violet = argb(clampColor((int)(132 * alpha)), 64, 10, 118);
                int magenta = argb(clampColor((int)(225 * alpha)), 224, 82, 255);
                addGroundSegment(builder, cursor, next, 0.105 * crack.scale(), violet, camera, segment * 0.31F);
                addGroundSegment(builder, cursor, next, 0.035 * crack.scale(), magenta, camera, segment * 0.31F + 0.11F);

                if (segment > 0 && random.nextFloat() < 0.72F) {
                    double branchAngle = (random.nextBoolean() ? 1.0 : -1.0) * (0.62 + random.nextDouble() * 0.48);
                    Vec3 branchDirection = rotateY(direction, branchAngle);
                    Vec3 branchEnd = next.add(branchDirection.scale(crack.scale() * (0.22 + random.nextDouble() * 0.34)));
                    addGroundSegment(builder, next, branchEnd, 0.052 * crack.scale(), violet, camera, segment * 0.43F);
                    addGroundSegment(builder, next, branchEnd, 0.019 * crack.scale(), magenta, camera, segment * 0.43F + 0.17F);
                }
                cursor = next;
            }

            if (crack.age() < 3.5F) {
                double lead = Math.min(1.0, crack.age() / 3.5F);
                Vec3 spark = crack.position()
                        .subtract(direction.scale(crack.scale() * 0.58))
                        .add(direction.scale(crack.scale() * 1.45 * lead));
                addBillboardCore(builder, spark.add(0.0, 0.035, 0.0), 0.075 + crack.scale() * 0.035,
                        argb(clampColor((int)(245 * alpha)), 242, 232, 255), camera);
            }
        }
    }

    private void addPillarPulses(
            BufferBuilder builder,
            List<PillarPulse> pillars,
            BurstState burst,
            Vec3 camera
    ) {
        for (PillarPulse pillar : pillars) {
            float localAge = burst.age() - pillar.arrivalAge();
            if (localAge < 0.0F || localAge > 20.0F) {
                continue;
            }
            float climb = Math.min(1.0F, localAge / 8.0F);
            float fade = localAge <= 8.0F ? 1.0F : Math.max(0.0F, 1.0F - (localAge - 8.0F) / 12.0F);
            double height = pillar.top().y - pillar.bottom().y;
            double leadingY = pillar.bottom().y + height * climb;
            double wakeBottom = Math.max(pillar.bottom().y, leadingY - 18.0 - localAge * 0.8);
            int streaks = EnderniumVisualConfig.cinematic() ? 5 : 2;
            Random random = new Random(pillar.seed());
            for (int streak = 0; streak < streaks; streak++) {
                double angle = Math.PI * 2.0 * streak / streaks + random.nextDouble() * 0.72;
                double radius = pillar.surfaceRadius() + 0.025 + random.nextDouble() * 0.055;
                Vec3 offset = new Vec3(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
                double segmentLength = 2.4 + random.nextDouble() * 2.6;
                double gap = 0.55 + random.nextDouble() * 1.05;
                int segment = 0;
                for (double y = wakeBottom; y < leadingY; y += segmentLength + gap) {
                    double endY = Math.min(leadingY, y + segmentLength);
                    double twist = Math.sin(y * 0.19 + streak * 1.7 + localAge * 0.28) * 0.24;
                    Vec3 start = new Vec3(pillar.bottom().x, y, pillar.bottom().z)
                            .add(rotateY(offset, twist));
                    Vec3 end = new Vec3(pillar.bottom().x, endY, pillar.bottom().z)
                            .add(rotateY(offset, twist + 0.12));
                    int alpha = clampColor((int)(205 * fade * (0.72 + 0.28 * Math.sin(segment * 2.1 + localAge))));
                    int color = (streak & 1) == 0
                            ? argb(alpha, 224, 82, 255)
                            : argb(alpha, 116, 39, 255);
                    addRibbon(builder, start, end, 0.055 + streak * 0.008, color, camera, streak * 0.37F + segment * 0.21F);
                    segment++;
                }
                Vec3 head = new Vec3(pillar.bottom().x, leadingY, pillar.bottom().z).add(offset);
                addBillboardCore(builder, head, 0.10,
                        argb(clampColor((int)(235 * fade)), 242, 232, 255), camera);
            }
        }
    }

    private void addShockwave(BufferBuilder builder, BurstState state, Vec3 camera) {
        if (state.distantObserver()) {
            if (state.distantWave() != null) {
                addDistantWaveSheet(builder, state.distantWave(), state.age(), camera);
            }
            return;
        }

        float age = state.age();
        Vec3 center = state.origin();
        float radius = (float)(age * DragonDeathVfxTiming.WAVE_SPEED_BLOCKS_PER_TICK);

        if (radius > 1700.0F) {
            return;
        }

        int segments = EnderniumVisualConfig.cinematic() ? 128 : 64;
        float fade = Math.min(1.0F, age / 2.0F)
                * Math.min(1.0F, (EnderniumVfxManager.BURST_DURATION - age) / 14.0F);
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

    private void addDistantWaveSheet(
            BufferBuilder builder,
            DistantWaveState distant,
            float age,
            Vec3 camera
    ) {
        float localAge = age - distant.arrivalTick();
        if (localAge < -DISTANT_WAVE_VISIBLE_BEFORE_ARRIVAL
                || localAge > DISTANT_WAVE_VISIBLE_AFTER_ARRIVAL) {
            return;
        }
        float fadeIn = Math.min(1.0F, (localAge + DISTANT_WAVE_VISIBLE_BEFORE_ARRIVAL) / 2.0F);
        float fadeOut = Math.min(1.0F, (DISTANT_WAVE_VISIBLE_AFTER_ARRIVAL - localAge) / 3.0F);
        float temporalFade = Math.max(0.0F, Math.min(fadeIn, fadeOut));
        Vec3 direction = distant.direction();
        Vec3 side = new Vec3(-direction.z, 0.0, direction.x);
        Vec3 leadingCenter = new Vec3(distant.anchor().x, distant.originY(), distant.anchor().z)
                .add(direction.scale(localAge * DragonDeathVfxTiming.WAVE_SPEED_BLOCKS_PER_TICK));
        int segments = EnderniumVisualConfig.cinematic() ? 64 : 32;

        for (int i = 0; i < segments; i++) {
            double s0 = -DISTANT_WAVE_HALF_WIDTH + DISTANT_WAVE_HALF_WIDTH * 2.0 * i / segments;
            double s1 = -DISTANT_WAVE_HALF_WIDTH + DISTANT_WAVE_HALF_WIDTH * 2.0 * (i + 1) / segments;
            double bow0 = -(s0 * s0) / (2.0 * DISTANT_WAVE_CURVATURE_RADIUS);
            double bow1 = -(s1 * s1) / (2.0 * DISTANT_WAVE_CURVATURE_RADIUS);
            double y0 = Math.sin((s0 + distant.seed() * 0.0001) * 0.075 + age * 0.22) * 0.62;
            double y1 = Math.sin((s1 + distant.seed() * 0.0001) * 0.075 + age * 0.22) * 0.62;
            Vec3 lead0 = leadingCenter.add(side.scale(s0)).add(direction.scale(bow0)).add(0.0, y0, 0.0);
            Vec3 lead1 = leadingCenter.add(side.scale(s1)).add(direction.scale(bow1)).add(0.0, y1, 0.0);
            Vec3 trail0 = lead0.subtract(direction.scale(DISTANT_WAVE_WAKE_LENGTH));
            Vec3 trail1 = lead1.subtract(direction.scale(DISTANT_WAVE_WAKE_LENGTH));
            float lateral0 = lateralSheetFade(s0, DISTANT_WAVE_HALF_WIDTH);
            float lateral1 = lateralSheetFade(s1, DISTANT_WAVE_HALF_WIDTH);
            int inner0 = argb(clampColor((int)(48 * temporalFade * lateral0)), 116, 39, 255);
            int inner1 = argb(clampColor((int)(48 * temporalFade * lateral1)), 116, 39, 255);
            int outer0 = argb(clampColor((int)(245 * temporalFade * lateral0)), 242, 232, 255);
            int outer1 = argb(clampColor((int)(245 * temporalFade * lateral1)), 242, 232, 255);
            float u0 = WAVE_DISTANT_MATERIAL_U
                    + i / (float)segments * WAVE_DISTANT_MATERIAL_U_SPAN;
            float u1 = WAVE_DISTANT_MATERIAL_U
                    + (i + 1) / (float)segments * WAVE_DISTANT_MATERIAL_U_SPAN;
            addQuad(builder, trail0, trail1, lead1, lead0,
                    inner0, inner1, outer1, outer0, u0, u1, camera);
        }
    }

    private float lateralSheetFade(double lateral, double halfWidth) {
        double normalized = Math.abs(lateral) / halfWidth;
        return (float)Mth.clamp((1.0 - normalized) / 0.15, 0.0, 1.0);
    }

    private void addMangaImpactLines(BufferBuilder builder, BurstState state, Vec3 camera) {
        float age = state.age();
        if (age < 0.0F || age >= EnderniumVfxManager.IMPACT_END_AGE
                || camera.x * camera.x + camera.z * camera.z > 512.0 * 512.0) {
            return;
        }

        float fade = age < EnderniumVfxManager.IMPACT_FULL_INTENSITY_END_AGE
                ? 1.0F
                : Math.max(0.0F, 1.0F - (age - EnderniumVfxManager.IMPACT_FULL_INTENSITY_END_AGE)
                        / (EnderniumVfxManager.IMPACT_END_AGE
                        - EnderniumVfxManager.IMPACT_FULL_INTENSITY_END_AGE));
        long frameSeed = state.seed() ^ (age < EnderniumVfxManager.IMPACT_FRAME_FLIP_AGE
                ? 0x1A4AC7F2E11L
                : 0x5EC04D1F4A2L);
        Random random = new Random(frameSeed);
        int clusterCount = EnderniumVisualConfig.cinematic() ? 7 : 5;
        Vec3[] clusters = new Vec3[clusterCount];
        for (int cluster = 0; cluster < clusterCount; cluster++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double vertical = -0.58 + random.nextDouble() * 1.34;
            clusters[cluster] = new Vec3(Math.cos(angle), vertical, Math.sin(angle)).normalize();
        }

        int heroCount = EnderniumVisualConfig.cinematic() ? 9 : 5;
        int lineCount = EnderniumVisualConfig.cinematic() ? 40 : 22;
        for (int line = 0; line < lineCount; line++) {
            boolean heroStroke = line < heroCount;
            Vec3 direction;
            if (!heroStroke && line % 6 == 0) {
                direction = randomUnit(random);
            } else {
                Vec3 cluster = clusters[Math.floorMod(line * 3 + random.nextInt(2), clusterCount)];
                double spread = heroStroke
                        ? 0.025 + random.nextDouble() * 0.055
                        : 0.075 + random.nextDouble() * 0.16;
                direction = cluster.add(randomUnit(random).scale(spread)).normalize();
            }

            double innerRadius = heroStroke
                    ? 2.5 + random.nextDouble() * 7.0
                    : 5.0 + random.nextDouble() * 14.0;
            double length = heroStroke
                    ? 72.0 + random.nextDouble() * 88.0
                    : 34.0 + random.nextDouble() * 90.0;
            int segments = heroStroke ? 1 + random.nextInt(2) : 2 + random.nextInt(3);
            double gap = heroStroke
                    ? 0.012 + random.nextDouble() * 0.025
                    : 0.04 + random.nextDouble() * 0.065;
            double endWidth = heroStroke
                    ? 2.7 + random.nextDouble() * 3.6
                    : 0.32 + random.nextDouble() * 1.0;
            int alpha = clampColor((int)((heroStroke
                    ? 235 + random.nextInt(21)
                    : 190 + random.nextInt(61)) * fade));
            int color = heroStroke
                    ? argb(alpha, 116, 39, 255)
                    : line % 7 == 0
                            ? argb(alpha, 242, 232, 255)
                            : argb(alpha, 224, 82, 255);

            for (int segment = 0; segment < segments; segment++) {
                double t0 = segment / (double)segments + gap * 0.5;
                double t1 = (segment + 1.0) / segments - gap * 0.5;
                if (t1 <= t0) {
                    continue;
                }
                Vec3 start = state.origin().add(direction.scale(innerRadius + length * t0));
                Vec3 end = state.origin().add(direction.scale(innerRadius + length * t1));
                double startWidth = (heroStroke ? 0.12 : 0.03)
                        + endWidth * t0 * t0 * (heroStroke ? 0.58 : 0.68);
                double finishWidth = (heroStroke ? 0.18 : 0.05) + endWidth * t1 * t1;
                addTaperedRibbon(
                        builder,
                        start,
                        end,
                        startWidth,
                        finishWidth,
                        color,
                        camera,
                        WAVE_MANGA_MATERIAL_U + line * 0.0001F
                );
            }
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

    private void addTaperedRibbon(
            BufferBuilder builder,
            Vec3 start,
            Vec3 end,
            double startWidth,
            double endWidth,
            int color,
            Vec3 camera,
            float u
    ) {
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
        side = side.normalize();
        Vec3 startSide = side.scale(startWidth);
        Vec3 endSide = side.scale(endWidth);
        addQuad(
                builder,
                start.subtract(startSide),
                end.subtract(endSide),
                end.add(endSide),
                start.add(startSide),
                color, color, color, color,
                u, u + 0.5F,
                camera
        );
    }

    private void addGroundSegment(
            BufferBuilder builder,
            Vec3 start,
            Vec3 end,
            double width,
            int color,
            Vec3 camera,
            float u
    ) {
        Vec3 along = end.subtract(start);
        double length = Math.sqrt(along.x * along.x + along.z * along.z);
        if (length < 1.0E-5) {
            return;
        }
        Vec3 side = new Vec3(-along.z / length * width, 0.0, along.x / length * width);
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

    private void addBrokenDiamondShell(
            BufferBuilder builder,
            Vec3 center,
            double radius,
            double width,
            double yaw,
            double pitch,
            double roll,
            long seed,
            int color,
            Vec3 camera
    ) {
        Vec3[] localVertices = {
                new Vec3(0.0, radius * 1.12, 0.0),
                new Vec3(0.0, -radius * 1.12, 0.0),
                new Vec3(radius, 0.0, 0.0),
                new Vec3(0.0, 0.0, radius),
                new Vec3(-radius, 0.0, 0.0),
                new Vec3(0.0, 0.0, -radius)
        };
        Vec3[] vertices = new Vec3[localVertices.length];
        for (int i = 0; i < localVertices.length; i++) {
            vertices[i] = center.add(rotateEuler(localVertices[i], yaw, pitch, roll));
        }

        int[][] edges = {
                {0, 2}, {0, 3}, {0, 4}, {0, 5},
                {1, 2}, {1, 3}, {1, 4}, {1, 5},
                {2, 3}, {3, 4}, {4, 5}, {5, 2}
        };
        int pieces = EnderniumVisualConfig.cinematic() ? 3 : 2;
        Random random = new Random(seed);
        for (int edgeIndex = 0; edgeIndex < edges.length; edgeIndex++) {
            Vec3 start = vertices[edges[edgeIndex][0]];
            Vec3 edge = vertices[edges[edgeIndex][1]].subtract(start);
            for (int piece = 0; piece < pieces; piece++) {
                boolean omitted = random.nextFloat() < 0.22F;
                double inset = 0.055 + random.nextDouble() * 0.045;
                double t0 = (piece + inset) / pieces;
                double t1 = (piece + 0.74 - inset * 0.45) / pieces;
                if (omitted || t1 <= t0) {
                    continue;
                }
                Vec3 p0 = start.add(edge.scale(t0));
                Vec3 p1 = start.add(edge.scale(t1));
                addRibbon(builder, p0, p1, width, color, camera, ENERGY_CLEAN_RIBBON_MATERIAL_U);
            }
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
        vertex(builder, p0, color, ENERGY_FACET_MATERIAL_U, shade, camera);
        vertex(builder, p1, color, ENERGY_FACET_MATERIAL_U, shade, camera);
        vertex(builder, p2, color, ENERGY_FACET_MATERIAL_U, shade, camera);
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

    private Vec3 rotateEuler(Vec3 vector, double yaw, double pitch, double roll) {
        double sinPitch = Math.sin(pitch);
        double cosPitch = Math.cos(pitch);
        double pitchedY = vector.y * cosPitch - vector.z * sinPitch;
        double pitchedZ = vector.y * sinPitch + vector.z * cosPitch;

        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);
        double yawedX = vector.x * cosYaw - pitchedZ * sinYaw;
        double yawedZ = vector.x * sinYaw + pitchedZ * cosYaw;

        double sinRoll = Math.sin(roll);
        double cosRoll = Math.cos(roll);
        return new Vec3(
                yawedX * cosRoll - pitchedY * sinRoll,
                yawedX * sinRoll + pitchedY * cosRoll,
                yawedZ
        );
    }

    private void addBillboardCore(BufferBuilder builder, Vec3 center, double radius, int color, Vec3 camera) {
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
                color, color, color, color,
                ENERGY_HEAD_MATERIAL_U,
                ENERGY_HEAD_MATERIAL_U + ENERGY_HEAD_MATERIAL_U_SPAN,
                camera);
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

    private static float fract(float value) {
        return value - (float)Math.floor(value);
    }

    private static float smoothProgress(float value) {
        value = Mth.clamp(value, 0.0F, 1.0F);
        return value * value * (3.0F - 2.0F * value);
    }

    private static float exponentialProgress(float value, float exponent) {
        value = Mth.clamp(value, 0.0F, 1.0F);
        double scale = Math.exp(exponent) - 1.0;
        return (float)((Math.exp(exponent * value) - 1.0) / scale);
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
        if (depthSampler != null) {
            depthSampler.close();
            depthSampler = null;
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
