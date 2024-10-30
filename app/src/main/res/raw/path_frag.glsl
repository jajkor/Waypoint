#version 320 es
precision mediump float;

out vec4 FragColor;

uniform float u_Time;

void main()
{
    FragColor = vec4(mix(1.0, 0.75, abs(sin(u_Time))), 0.0, 0.0, 1.0);
}
