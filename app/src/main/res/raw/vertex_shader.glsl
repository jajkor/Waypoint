#version 300 es
layout(location = 0) in vec4 a_Position;
layout(location = 1) in vec3 a_Normal;

out vec3 Normal;

uniform mat4 u_MVPMatrix;

void main() {
    Normal = a_Normal;
    gl_Position = u_MVPMatrix * a_Position;
}
