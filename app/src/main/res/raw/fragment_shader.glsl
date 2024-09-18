#version 300 es
precision mediump float;

in vec3 Normal;

out vec4 fragColor;

void main() {
    fragColor = vec4(Normal, 1.0);  // White color
}
