#version 320 es
precision mediump float;

in vec3 FragPos;
in vec3 Normal;

out vec4 fragColor;

uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 lightPos;

uniform vec3 ambientColor; // Ka
uniform vec3 diffuseColor; // Kd
uniform vec3 specularColor; // Ks
uniform float specularComponent; // Ns

void main() {
    // Ambient
    vec3 ambient = 1.0 * ambientColor;

    // Diffuse
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(lightDir, norm), 0.0);
    vec3 diffuse = diff * diffuseColor;

    // Specular
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = 0.0;

    vec3 halfwayDir = normalize(lightDir + viewDir);
    spec = pow(max(dot(norm, halfwayDir), 0.0), specularComponent);

    // Output the final fragment color
    if (gl_FrontFacing) {
        vec3 specular = specularColor * spec;
        fragColor = vec4(ambient + diffuse + specular, 1.0);
    } else {
        fragColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }
}
