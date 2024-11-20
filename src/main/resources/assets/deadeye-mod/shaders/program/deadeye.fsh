#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec2 ScreenSize;
uniform float Time;
uniform float TickDelta;
uniform float VignetteStrength;
uniform float DeadeyeEndValue;
uniform float Fade;

float hash(float n) {
    return fract(sin(n) * 43758.5453);
}

vec4 applyRadialBlur(vec2 uv, vec2 mid, float strength, int samples) {
    vec4 color = vec4(0.0);
    float totalWeight = 0.0;

    for (int i = 0; i < samples; i++) {
        float t = float(i) / float(samples - 1);
        vec2 offset = mix(uv, mid, t * strength);
        color += texture(DiffuseSampler, offset);
        totalWeight += 1.0;
    }

    return color / totalWeight;
}

void main() {
    vec4 baseColor = texture(DiffuseSampler, texCoord);
    vec4 texColor = texture(DiffuseSampler, texCoord);

    vec2 mid = vec2(0.5, 0.5);

    vec2 screenCoord = texCoord * ScreenSize;
    vec2 midScreen = mid * ScreenSize;

    float distanceFromCenter = length(screenCoord - midScreen);

    float maxDistance = length(ScreenSize * 0.5);
    float normalizedDistance = distanceFromCenter / maxDistance;

    if (DeadeyeEndValue > 0.0) {
        float blurStrength = DeadeyeEndValue * normalizedDistance * 0.05;
        int samples = 8;
        texColor = applyRadialBlur(texCoord, mid, blurStrength, samples);
    }

    float distanceFactor = pow(normalizedDistance, VignetteStrength);

    float slowTime = Time * 0.10;
    float intensity = 5.0 + clamp((sin(slowTime*4) - 0.1), -0.5, 0.5);

    texColor = clamp(vec4(texColor.r + 0.3, texColor.g + 0.2, texColor.b, texColor.a), 0.0, 1.0);

    float reduction = distanceFactor / intensity;
    texColor.rgb = clamp(texColor.rgb - reduction * 1.5, 0.0, 1.0);

    fragColor = mix(baseColor, texColor, Fade);
}
