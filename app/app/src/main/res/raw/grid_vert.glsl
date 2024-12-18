#version 300 es
layout(location = 0) in vec3 a_Position;
layout(location = 1) in vec3 a_Normal;

out vec3 nearPoint;
out vec3 farPoint;
out mat4 fragModel;
out mat4 fragView;
out mat4 fragProjection;

uniform mat4 u_Model;
uniform mat4 u_View;
uniform mat4 u_Projection;

vec3 UnprojectPoint(float x, float y, float z, mat4 view, mat4 projection) {
    mat4 viewInv = inverse(view);
    mat4 projInv = inverse(projection);
    vec4 unprojectedPoint =  vec4(viewInv * projInv * vec4(x, y, z, 1.0));
    return unprojectedPoint.xyz / unprojectedPoint.w;
}

void main() {
    vec3 p = a_Position;
    fragModel = u_Model;
    fragView = u_View;
    fragProjection = u_Projection;
    nearPoint = UnprojectPoint(p.x, p.y, 0.0, u_View, u_Projection).xyz; // unprojecting on the near plane
    farPoint = UnprojectPoint(p.x, p.y, 1.0, u_View, u_Projection).xyz; // unprojecting on the far plane
    gl_Position = vec4(p, 1.0); // using directly the clipped coordinates
}