package com.skittlq.endernium;

import com.skittlq.endernium.client.vfx.EnderniumVfxRenderMode;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-local visual settings; deliberately separate from synchronized gameplay configuration. */
public final class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<EnderniumVfxRenderMode> VFX_RENDER_MODE = BUILDER
            .comment("AUTO uses safe particles with an active Iris shader pack; FULL forces custom shaders; PARTICLES always uses the fallback.")
            .defineEnum("vfxRenderMode", EnderniumVfxRenderMode.AUTO);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {
    }
}
