#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

float hash(float value) {
    return fract(sin(value * 91.713) * 43758.5453);
}

void main() {
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
