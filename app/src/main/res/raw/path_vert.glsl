#version 320 es
layout (location = 0) in vec3 a_Position; // Position of user or destination node

void main()
{
    gl_Position = vec4(a_Position, 1.0);
}