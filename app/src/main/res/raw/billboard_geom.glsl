#version 320 es
layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

uniform mat4 u_Model;
uniform mat4 u_View;
uniform mat4 u_Projection;
uniform vec3 u_CamPos;
uniform float u_QuadWidth;  // Width of the billboard quad

void main() {
    // Extract the user position and destination position from gl_in[] (assuming 2 points are passed)
    vec3 userPosition = gl_in[0].gl_Position.xyz;

    vec3 toCamera = normalize(u_CamPos - userPosition);
    vec3 up = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(up, toCamera);

    userPosition -= (right * 0.5);
    gl_Position = u_Projection * u_View * vec4(userPosition, 1.0);
    EmitVertex();

    userPosition.y += 1.0;
    gl_Position = u_Projection * u_View * vec4(userPosition, 1.0);
    EmitVertex();

    userPosition.y -= 1.0;
    userPosition += right;
    gl_Position = u_Projection * u_View * vec4(userPosition, 1.0);
    EmitVertex();

    userPosition.y += 1.0;
    gl_Position = u_Projection * u_View * vec4(userPosition, 1.0);
    EmitVertex();

    EndPrimitive();
}
