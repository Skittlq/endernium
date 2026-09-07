#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

// Java reserves negative U ranges for non-ribbon materials; normal ribbons use non-negative U.
const float FACET_MATERIAL_MAX_U = -1.0;
const float HEAD_MATERIAL_MAX_U = 0.0;
const float HEAD_MATERIAL_U = -0.9;
const float HEAD_MATERIAL_U_SPAN = 0.8;
const float CLEAN_RIBBON_MATERIAL_MAX_U = -3.0;
const float CLEAN_RIBBON_MATERIAL_U = -5.0;

void main() {
    if (texCoord.x < CLEAN_RIBBON_MATERIAL_MAX_U) {
        float along = clamp(texCoord.x - CLEAN_RIBBON_MATERIAL_U, 0.0, 1.0);
        float across = abs(texCoord.y * 2.0 - 1.0);
        float body = 1.0 - smoothstep(0.62, 1.0, across);
        float hotCore = pow(max(0.0, 1.0 - across), 3.0);
        float endFade = smoothstep(0.0, 0.08, along)
                * (1.0 - smoothstep(0.92, 1.0, along));
        vec3 color = mix(vertexColor.rgb, vec3(0.965, 0.925, 1.0), hotCore * 0.62);
        float alpha = vertexColor.a * body * endFade;
        if (alpha <= 0.005) {
            discard;
        }
        fragColor = vec4(color * ColorModulator.rgb, alpha * ColorModulator.a);
        return;
    }

    if (texCoord.x < FACET_MATERIAL_MAX_U) {
        float facetLight = 0.72 + clamp(texCoord.y, 0.0, 1.0) * 0.32;
        vec3 facetColor = vertexColor.rgb * facetLight;
        fragColor = vec4(facetColor * ColorModulator.rgb, vertexColor.a * ColorModulator.a);
        return;
    }

    if (texCoord.x < HEAD_MATERIAL_MAX_U) {
        vec2 local = vec2(
                (texCoord.x - HEAD_MATERIAL_U) / HEAD_MATERIAL_U_SPAN,
                texCoord.y
        ) * 2.0 - 1.0;
        float diamondDistance = abs(local.x) + abs(local.y);
        float body = 1.0 - smoothstep(0.82, 1.0, diamondDistance);
        float hotCore = 1.0 - smoothstep(0.18, 0.68, diamondDistance);
        vec3 violetEdge = mix(vec3(0.455, 0.153, 1.0), vertexColor.rgb, 0.28);
        vec3 color = mix(violetEdge, vec3(0.985, 0.965, 1.0), hotCore);
        float alpha = vertexColor.a * body;
        if (alpha <= 0.005) {
            discard;
        }
        fragColor = vec4(color * ColorModulator.rgb, alpha * ColorModulator.a);
        return;
    }

    float across = abs(texCoord.y * 2.0 - 1.0);
    float body = 1.0 - smoothstep(0.48, 1.0, across);
    float core = pow(max(0.0, 1.0 - across), 4.0);
    float broken = 0.78 + 0.22 * sin(texCoord.x * 31.0 + texCoord.y * 11.0);
    vec3 hot = vec3(0.949, 0.910, 1.0);
    vec3 color = mix(vertexColor.rgb, hot, core * 0.82);
    float alpha = vertexColor.a * body * broken;
    if (alpha <= 0.005) {
        discard;
    }
    fragColor = vec4(color * ColorModulator.rgb, alpha * ColorModulator.a);
}
