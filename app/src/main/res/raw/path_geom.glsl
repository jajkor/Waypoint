#version 320 es
layout(lines) in;
layout(triangle_strip, max_vertices = 10) out;

uniform vec3 u_UserPos;
uniform vec3 u_NodePos;
uniform mat4 u_View;
uniform mat4 u_Projection;

void main() {
    // Extract the user position and destination position from gl_in[]
/**    vec3 userPosition = gl_in[0].gl_Position.xyz;
    vec3 destinationPosition = gl_in[1].gl_Position.xyz;*/
    vec3 userPosition = u_UserPos;
    vec3 destinationPosition = u_NodePos;

    // Compute the direction vector from the user to the destination
    vec3 direction = normalize(destinationPosition - userPosition);

    // Calculate the right vector (perpendicular to the direction, using up vector as reference)
    vec3 up = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(up, direction);

    // Set the scale of the quad (you can adjust this depending on how wide you want the path marker)
    float quadScale = 0.1;  // For example, 0.1 units for width
    float quadHeight = 0.2;
    // Create vertices for the quad stretched from userPosition to destinationPosition

    vec3 v1 = userPosition + right * quadScale + quadHeight; // Bottom Left
    vec3 v2 = destinationPosition + right * quadScale + quadHeight; // Top Left
    vec3 v3 = userPosition - right * quadScale + quadHeight; // Bottom Right
    vec3 v4 = destinationPosition - right * quadScale + quadHeight; // Top Left

    // 1
    gl_Position = u_Projection * u_View * vec4(v1.x, v1.y + quadHeight, v1.z, 1.0);
    EmitVertex();

    // 2
    gl_Position = u_Projection * u_View * vec4(v2.x, v2.y + quadHeight, v2.z, 1.0);
    EmitVertex();

    // 3
    gl_Position = u_Projection * u_View * vec4(v3.x, v3.y + quadHeight, v3.z, 1.0);
    EmitVertex();

    // 4
    gl_Position = u_Projection * u_View * vec4(v4.x, v4.y + quadHeight, v4.z, 1.0);
    EmitVertex();

    // 5
    gl_Position = u_Projection * u_View * vec4(v3, 1.0);
    EmitVertex();

    // 6
    gl_Position = u_Projection * u_View * vec4(v4, 1.0);
    EmitVertex();

    // 7
    gl_Position = u_Projection * u_View * vec4(v1, 1.0);
    EmitVertex();

    // 8
    gl_Position = u_Projection * u_View * vec4(v2, 1.0);
    EmitVertex();

    // 9
    gl_Position = u_Projection * u_View * vec4(v1.x, v1.y + quadHeight, v1.z, 1.0);
    EmitVertex();

    // 10
    gl_Position = u_Projection * u_View * vec4(v2.x, v2.y + quadHeight, v2.z, 1.0);
    EmitVertex();

    EndPrimitive();
}