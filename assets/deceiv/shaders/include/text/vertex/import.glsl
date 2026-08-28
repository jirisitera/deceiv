#ifdef IS_GUI 
#moj_import <minecraft:globals.glsl>

#define DECEIV_IDENTIFIER vec2(137.0 / 255.0, 237.0 / 255.0)
#define DECEIV_CANVAS_SIZE 256.0

out float effect;
out vec3 identifier;
out float localX;
out float localY;

float getHeartbeatScale(float speed, float base) {
    float time = fract(GameTime * 1200.0 * (speed / 60.0));
    // graph formula: y = 1.0 + (0.15*exp(-((mod(x,1)-0.15)*20)^2) - 0.15*exp(-((mod(x,1)-0.26)*50)^2) + 1.0*exp(-((mod(x,1)-0.30)*50)^2) - 0.3*exp(-((mod(x,1)-0.34)*50)^2) + 0.3*exp(-((mod(x,1)-0.55)*15)^2)) * 0.2
    float p = exp(-pow((time - 0.15) * 20.0, 2.0)) * 0.15;
    float q = exp(-pow((time - 0.26) * 50.0, 2.0)) * 0.15;
    float r = exp(-pow((time - 0.30) * 50.0, 2.0)) * 0.5;
    float s = exp(-pow((time - 0.34) * 50.0, 2.0)) * 0.3;
    float t = exp(-pow((time - 0.55) * 15.0, 2.0)) * 0.3;
    return 1.0 + (p - q + r - s + t) * 0.2 * base;
}
vec3 scaleHeartbeatTexture(float scale) {
    vec2 offsetPosition = Position.xy + vec2(-0.5, 0.0);
    vec2 topLeft = offsetPosition;
    float cornerEdge = DECEIV_CANVAS_SIZE * 2.0;
    int corner = gl_VertexID % 4;
    if (corner == 1) {
        topLeft -= vec2(0.0, cornerEdge);
    } else if (corner == 2) {
        topLeft -= vec2(cornerEdge, cornerEdge);
    } else if (corner == 3) {
        topLeft -= vec2(cornerEdge, 0.0);
    }
    vec2 center = topLeft + vec2(9.0, 9.0);
    return vec3(center + (offsetPosition - center) * scale, Position.z);
}
#endif
