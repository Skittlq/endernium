#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

// Material routing is encoded in U by EnderniumShaderRenderer.
const float MANGA_MATERIAL_MAX_U = -4.0;
const float DISTANT_WAVE_MATERIAL_MAX_U = -2.0;
const float DISTANT_WAVE_MATERIAL_U = -3.8;
const float DISTANT_WAVE_MATERIAL_U_SPAN = 0.6;

float hash(float value) {
    return fract(sin(value * 91.713) * 43758.5453);
}

void main() {
    // World-space manga strokes used only during the central-island impact frames.
    if (texCoord.x < MANGA_MATERIAL_MAX_U) {
        float across = abs(texCoord.y * 2.0 - 1.0);
        float body = 1.0 - smoothstep(0.78, 1.0, across);
        float whiteCore = 1.0 - smoothstep(0.12, 0.48, across);
        vec3 hot = vec3(1.0);
        vec3 color = mix(vertexColor.rgb, hot, whiteCore);
        float alpha = vertexColor.a * body;
        if (alpha <= 0.004) {
            discard;
        }
        fragColor = vec4(color * ColorModulator.rgb, alpha * ColorModulator.a);
        return;
    }

    // Observer-local curved sheet used for the distant presentation.
    if (texCoord.x < DISTANT_WAVE_MATERIAL_MAX_U) {
        float strip = (texCoord.x - DISTANT_WAVE_MATERIAL_U) / DISTANT_WAVE_MATERIAL_U_SPAN;
        float irregularity = (hash(floor(strip * 83.0)) - 0.5) * 0.11;
        float wake = smoothstep(0.0, 0.28, texCoord.y)
                * (1.0 - smoothstep(0.72, 1.0, texCoord.y) * 0.28);
        float leading = smoothstep(0.70 + irregularity, 0.96, texCoord.y);
        float razor = 1.0 - smoothstep(0.955 + irregularity * 0.22, 1.0, texCoord.y);
        vec3 violet = vec3(0.20, 0.012, 0.34);
        vec3 magenta = vec3(0.878, 0.322, 1.0);
        vec3 hot = vec3(1.0, 0.965, 1.0);
        vec3 color = mix(violet, magenta, wake * 0.72);
        color = mix(color, hot, leading * razor);
        float flicker = 0.84 + 0.16 * hash(floor(strip * 127.0));
        float alpha = vertexColor.a * (0.34 * wake + 0.96 * leading * razor) * flicker;
        if (alpha <= 0.004) {
            discard;
        }
        fragColor = vec4(color * ColorModulator.rgb, alpha * ColorModulator.a);
        return;
    }

    // Real radial pressure wave used near the central island.
    float brokenEdge = (hash(floor(texCoord.x * 19.0)) - 0.5) * 0.12;
    float leading = smoothstep(0.58 + brokenEdge, 0.97, texCoord.y);
    float razor = 1.0 - smoothstep(0.91 + brokenEdge, 1.0, texCoord.y);
    float wake = smoothstep(0.0, 0.38, texCoord.y) * (1.0 - leading * 0.42);
    float flicker = 0.82 + 0.18 * sin(texCoord.x * 39.0);
    vec3 violet = vec3(0.455, 0.153, 1.0);
    vec3 magenta = vec3(0.878, 0.322, 1.0);
    vec3 hot = vec3(0.949, 0.910, 1.0);
    vec3 color = mix(violet, magenta, wake);
    color = mix(color, hot, leading * razor);
    float alpha = vertexColor.a * (0.24 * wake + 0.92 * leading * razor) * flicker;
    if (alpha <= 0.004) {
        discard;
    }
    fragColor = vec4(color * ColorModulator.rgb, alpha * ColorModulator.a);
}
