#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    if (texCoord.x < -1.0) {
        float facetLight = 0.72 + clamp(texCoord.y, 0.0, 1.0) * 0.32;
        vec3 facetColor = vertexColor.rgb * facetLight;
        fragColor = vec4(facetColor * ColorModulator.rgb, vertexColor.a * ColorModulator.a);
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
