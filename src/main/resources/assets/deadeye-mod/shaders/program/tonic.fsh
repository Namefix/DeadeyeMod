#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec2 ScreenSize;
uniform float TickDelta;
uniform float TonicDuration;

void main() {
    // Sample the texture color
    vec4 texColor = texture(DiffuseSampler, texCoord);

    // Define the target goldish color
    vec4 goldColor = vec4(1.0, 0.843, 0.0, 1.0); // RGBA for gold

    // Calculate the fade factor using smoothstep
    float fadeFactor = smoothstep(0.0, 1.0, TonicDuration) * (1.0 - TonicDuration);

    // --- Radial Gradient for Washed-Out Effect ---
    // Calculate the distance from the center of the screen
    vec2 screenCenter = vec2(0.5, 0.5);
    vec2 relativeCoord = (texCoord - screenCenter) * ScreenSize;
    float distance = length(relativeCoord / ScreenSize);

    // Modulate the fade factor based on distance (stronger at center, weaker at edges)
    float radialFactor = smoothstep(1.0, 0.6, distance); // Adjust outer/inner falloff
    float modulatedFade = fadeFactor * radialFactor;

    // Blend the texture color with the goldish color using the modulated fade factor
    vec4 blendedColor = mix(texColor, goldColor, modulatedFade);

    // --- Intense Vignette Effect ---
    // Vignette intensity (peaks in the middle of the TonicDuration)
    float vignetteIntensity = smoothstep(0.0, 1.0, TonicDuration) * (1.0 - TonicDuration);

    // Apply a more intense radial vignette effect
    // Adjust inner and outer falloff values to make it more pronounced
    float vignette = smoothstep(0.7, 0.3, distance); // Inner radius is now closer, steeper falloff
    vignette = mix(1.0, vignette, vignetteIntensity * 1.5); // Increase intensity multiplier

    // Combine vignette with the blended color
    fragColor = blendedColor * vignette;
}
