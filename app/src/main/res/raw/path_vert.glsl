#version 320 es
layout (location = 0) in vec3 a_Position; // Position of user or destination node

uniform vec3 offsets[100];

void main()
{
    vec3 offset = offsets[gl_InstanceID];
    gl_Position = vec4(a_Position + offset, 1.0);
}