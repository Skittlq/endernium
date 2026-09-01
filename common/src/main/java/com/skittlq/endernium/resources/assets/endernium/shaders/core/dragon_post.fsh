#version 330

uniform sampler2D Sampler0;
uniform sampler2D SamplerDepth;

layout(std140) uniform DragonPost {
    vec4 VfxData;
    vec4 AtmosphereData;
};

in vec2 texCoord;
out vec4 fragColor;

float luminanceAt(vec2 uv) {
    return dot(texture(Sampler0, clamp(uv, vec2(0.001), vec2(0.999))).rgb,
               vec3(0.299, 0.587, 0.114));
}

void main() {
    float intensity = clamp(VfxData.x, 0.0, 1.0);
    float atmosphere = clamp(AtmosphereData.x, 0.0, 1.0);
    float impact = clamp(AtmosphereData.y, 0.0, 1.0);
    float impactPhase = AtmosphereData.z;
    float detonationShake = clamp(AtmosphereData.w, 0.0, 1.0);
    float phase = VfxData.y;
    vec2 centered = texCoord - vec2(0.5);
    float distanceFromCenter = length(centered);
    vec2 radial = centered / max(distanceFromCenter, 0.001);
    float wave = sin(distanceFromCenter * 52.0 - phase * 3.4);
    float envelope = smoothstep(0.74, 0.08, distanceFromCenter);
    // Pixel-sized offsets shake only the rendered world. Player rotation, hand, and HUD stay untouched.
    vec2 arrivalImpulse = vec2(sin(phase * 5.9), cos(phase * 4.7) * 0.62)
            * VfxData.zw * 1.9 * intensity;
    vec2 detonationImpulse = vec2(
            sin(impactPhase * 8.7 + 1.1),
            cos(impactPhase * 7.3 + 0.4) * 0.58
    ) * VfxData.zw * 2.4 * detonationShake;
    vec2 impulse = arrivalImpulse + detonationImpulse;
    vec2 warpedUv = clamp(texCoord + radial * wave * envelope * intensity * 0.0045 + impulse, vec2(0.001), vec2(0.999));
    vec2 split = radial * intensity * 0.0018;
    vec3 color;
    color.r = texture(Sampler0, clamp(warpedUv + split, vec2(0.001), vec2(0.999))).r;
    color.g = texture(Sampler0, warpedUv).g;
    color.b = texture(Sampler0, clamp(warpedUv - split, vec2(0.001), vec2(0.999))).b;
    float edge = smoothstep(0.18, 0.78, distanceFromCenter);
    color += vec3(0.22, 0.025, 0.27) * intensity * (0.55 + edge * 0.45);
    color = mix(color, vec3(dot(color, vec3(0.299, 0.587, 0.114))), intensity * 0.10);
    float luminance = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(color, vec3(luminance), atmosphere * 0.16);
    color += vec3(0.115, 0.012, 0.145) * atmosphere * (0.72 + edge * 0.28);
    color = mix(color, color * vec3(1.05, 0.86, 1.13), atmosphere * 0.34);

    if (impact > 0.001) {
        vec2 pixel = VfxData.zw;
        float sourceLuminance = luminanceAt(texCoord);
        float leftLuminance = luminanceAt(texCoord - vec2(pixel.x, 0.0));
        float rightLuminance = luminanceAt(texCoord + vec2(pixel.x, 0.0));
        float downLuminance = luminanceAt(texCoord - vec2(0.0, pixel.y));
        float upLuminance = luminanceAt(texCoord + vec2(0.0, pixel.y));
        float colorEdge = abs(leftLuminance - rightLuminance)
                + abs(downLuminance - upLuminance);

        float sceneDepth = texture(SamplerDepth, texCoord).r;
        float leftDepth = texture(SamplerDepth, texCoord - vec2(pixel.x, 0.0)).r;
        float rightDepth = texture(SamplerDepth, texCoord + vec2(pixel.x, 0.0)).r;
        float downDepth = texture(SamplerDepth, texCoord - vec2(0.0, pixel.y)).r;
        float upDepth = texture(SamplerDepth, texCoord + vec2(0.0, pixel.y)).r;
        float depthEdge = abs(leftDepth - rightDepth) + abs(downDepth - upDepth);
        float geometry = smoothstep(0.000001, 0.00008, sceneDepth);
        float energy = smoothstep(0.42, 0.88, max(color.r, color.b))
                * smoothstep(0.08, 0.62, color.r + color.b - color.g * 1.35);
        float worldSubject = max(geometry, energy);
        float structureEdge = smoothstep(0.00035, 0.004, depthEdge);
        float energyEdge = energy * smoothstep(0.16, 0.48, colorEdge);
        float hardEdge = max(structureEdge, energyEdge);
        float brightSurface = step(0.285, sourceLuminance);
        float darkSurface = 1.0 - step(0.19, sourceLuminance);
        float frameFlip = step(1.6, impactPhase);

        vec3 paper = vec3(1.0);
        vec3 ink = vec3(0.002, 0.0, 0.006);
        vec3 purpleEdge = vec3(0.78, 0.025, 1.0);
        float highInk = clamp(darkSurface + hardEdge, 0.0, 1.0);
        vec3 highSurface = mix(paper, ink, highInk);
        highSurface = mix(highSurface, purpleEdge, hardEdge * 0.82);
        vec3 highFrame = mix(paper, highSurface, worldSubject);

        vec3 voidFrame = ink;
        vec3 hotFrame = vec3(1.0);
        vec3 lowSurface = mix(ink, hotFrame, brightSurface);
        lowSurface = mix(lowSurface, purpleEdge, hardEdge * 0.88);
        vec3 lowFrame = mix(voidFrame, lowSurface, worldSubject);

        vec3 impactColor = mix(highFrame, lowFrame, frameFlip);
        color = mix(color, impactColor, impact);
    }
    fragColor = vec4(color, 1.0);
}
