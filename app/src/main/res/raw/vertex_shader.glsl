#version 300 es
layout(location = 0) in vec4 a_Position;
uniform mat4 uMVPMatrix;

void main() {
    gl_Position = a_Position;
}
