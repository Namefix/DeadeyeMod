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

void main() {
    vec4 baseColor = texture(DiffuseSampler, texCoord);
    vec4 texColor = texture(DiffuseSampler, texCoord);

    texColor = clamp(vec4(texColor.r + 0.3, texColor.g + 0.2, texColor.b, texColor.a), 0.0, 1.0);

    fragColor = mix(texColor*2, texColor, Fade);
}
