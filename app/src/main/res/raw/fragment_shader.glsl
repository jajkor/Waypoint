#version 300 es
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
    float NdotL = max(dot(norm, lightDir), 0.0);

    // Gooch shading - interpolation between warm and cool colors
    vec3 kCool = min(coolColor + diffuseCool * surfaceColor, 1.0f);
    vec3 kWarm = max(warmColor + diffuseWarm * surfaceColor, 1.0f);
    vec3 kFinal = mix(kCool, kWarm, NdotL);

    // Specular highlights (Blinn-Phong)
    float specularStrength = 0.5;
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular = specularStrength * spec * lightColor;

    // Combine Gooch shading and specular highlights with object color
    //vec3 result = (goochColor + specular) * objectColor;

    // Output the final fragment color
    if (gl_FrontFacing) {
        fragColor = vec4(min(kFinal + spec, 1.0), 1.0);
    } else {
        fragColor = vec4(0, 0, 0, 1);
    }
}
