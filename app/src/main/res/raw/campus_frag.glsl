#version 320 es
precision mediump float;

in vec3 FragPos;
in vec3 Normal;

out vec4 fragColor;

uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 lightPos;

uniform vec3 surfaceColor;
uniform float diffuseWarm;
uniform float diffuseCool;
uniform vec3 warmColor;
uniform vec3 coolColor;

void main() {
    // Normalize the normal vector
    vec3 norm = normalize(Normal);

    // Calculate light direction
    vec3 lightDir = normalize(lightPos - FragPos);

    // Diffuse lighting
    float NdotL = max(dot(norm, lightDir), 0.0f);

    // Gooch shading - interpolation between warm and cool colors
    vec3 kCool = min(coolColor + diffuseCool * surfaceColor, 1.0f);
    vec3 kWarm = min(warmColor + diffuseWarm * surfaceColor, 1.0f);
    vec3 kFinal = mix(kCool, kWarm, NdotL);

    // Calculate the specular component using Blinn-Phong shading
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 halfwayDir = normalize(lightDir + viewDir);  // Halfway vector
    float specAngle = max(dot(norm, halfwayDir), 0.0);
    float specular = pow(specAngle, 32.0f);      // Specular intensity

    // Output the final fragment color
    if (gl_FrontFacing) {
        fragColor = vec4(min(kFinal + specular, 1.0f), 1.0f);
    } else {
        fragColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }
}
