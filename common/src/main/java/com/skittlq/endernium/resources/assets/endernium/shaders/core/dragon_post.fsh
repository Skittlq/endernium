#version 330

uniform sampler2D Sampler0;

layout(std140) uniform DragonPost {
    vec4 VfxData;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float intensity = clamp(VfxData.x, 0.0, 1.0);
    float phase = VfxData.y;
    vec2 centered = texCoord - vec2(0.5);
    float distanceFromCenter = length(centered);
    vec2 radial = centered / max(distanceFromCenter, 0.001);
    float wave = sin(distanceFromCenter * 52.0 - phase * 3.4);
    float envelope = smoothstep(0.74, 0.08, distanceFromCenter);
    vec2 impulse = vec2(sin(phase * 4.7), cos(phase * 3.9)) * VfxData.zw * 1.8 * intensity;
    vec2 warpedUv = clamp(texCoord + radial * wave * envelope * intensity * 0.0045 + impulse, vec2(0.001), vec2(0.999));
    vec2 split = radial * intensity * 0.0018;
    vec3 color;
    color.r = texture(Sampler0, clamp(warpedUv + split, vec2(0.001), vec2(0.999))).r;
    color.g = texture(Sampler0, warpedUv).g;
    color.b = texture(Sampler0, clamp(warpedUv - split, vec2(0.001), vec2(0.999))).b;
    float edge = smoothstep(0.18, 0.78, distanceFromCenter);
    color += vec3(0.22, 0.025, 0.27) * intensity * (0.55 + edge * 0.45);
    color = mix(color, vec3(dot(color, vec3(0.299, 0.587, 0.114))), intensity * 0.10);
    fragColor = vec4(color, 1.0);
}
