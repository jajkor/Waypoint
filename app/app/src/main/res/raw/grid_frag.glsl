#version 300 es
precision mediump float;

float near = 0.1;
float far = 10.0;

in vec3 nearPoint;
in vec3 farPoint;
in mat4 fragModel;
in mat4 fragView;
in mat4 fragProjection;

out vec4 fragColor;

vec4 grid(vec3 fragPos3D, float scale, bool drawAxis) {
    vec2 coord = fragPos3D.xz * scale;
    vec2 derivative = fwidth(coord);
    vec2 grid = abs(fract(coord - 0.5) - 0.5) / derivative;
    float line = min(grid.x, grid.y);
    float minimumz = min(derivative.y, 1.0);
    float minimumx = min(derivative.x, 1.0);
    vec4 color = vec4(0.2, 0.2, 0.2, 1.0 - min(line, 1.0));
    // z axis
    if(fragPos3D.x > -0.1 * minimumx && fragPos3D.x < 0.1 * minimumx)
        color = vec4(0.0, 0.0, 1.0, 1.0);
    // x axis
    if(fragPos3D.z > -0.1 * minimumz && fragPos3D.z < 0.1 * minimumz)
        color = vec4(1.0, 0.0, 0.0, 1.0);
    return color;
}

float computeDepth(vec3 pos) {
    vec4 clip_space_pos = vec4(fragProjection * fragView * vec4(pos.xyz, 1.0));
    return (clip_space_pos.z / clip_space_pos.w);
}

float computeLinearDepth(vec3 pos) {
    vec4 clip_space_pos = vec4(fragProjection * fragView * vec4(pos.xyz, 1.0));
    float clip_space_depth = (clip_space_pos.z / clip_space_pos.w) * 2.0 - 1.0; // put back between -1 and 1
    float linearDepth = (2.0 * near * far) / (far + near - clip_space_depth * (far - near)); // get linear value between 0.01 and 100
    return linearDepth / far; // normalize
}

void main() {
    float t = -nearPoint.y / (farPoint.y - nearPoint.y);
    vec3 fragPos3D = nearPoint + t * (farPoint - nearPoint);

    // OpenGL test the depth by having values between -1 and 1, whereas DirectX 12 does it between 0 and 1.
    gl_FragDepth = ((gl_DepthRange.diff * computeDepth(fragPos3D)) + gl_DepthRange.near + gl_DepthRange.far) / 2.0;

    float linearDepth = computeLinearDepth(fragPos3D);
    float fading = max(0.0, (0.5 - linearDepth));

    fragColor = (grid(fragPos3D, 10.0, true) + grid(fragPos3D, 1.0, true)) * float(t > 0.0); // adding multiple resolution for the grid
    fragColor.a *= fading;
}