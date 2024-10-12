#version 320 es
layout(location = 0) in vec3 a_Position;
layout(location = 1) in vec3 a_Normal;

out vec3 FragPos;
out vec3 Normal;

uniform float u_Outline;
uniform mat4 u_Model;
uniform mat4 u_View;
uniform mat4 u_Projection;

void main() {
    FragPos = vec3(u_Model * vec4(a_Position, 1.0));
    Normal = a_Normal;
    gl_Position = u_Projection * u_View * u_Model * vec4(a_Position + a_Normal * u_Outline, 1.0);
}
