#version 330

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:sample_lightmap.glsl>
#endif

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
in ivec2 UV2;
#endif

uniform sampler2D Sampler0;

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
uniform sampler2D Sampler2;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
#endif

#ifdef IS_GUI 
out float effect;
out vec3 identifier;
#endif

out vec4 vertexColor;
out vec2 texCoord0;

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
    int corner = gl_VertexID % 4;
    if (corner == 1) {
        topLeft -= vec2(0.0, 512.0);
    } else if (corner == 2) {
        topLeft -= vec2(512.0, 512.0);
    } else if (corner == 3) {
        topLeft -= vec2(512.0, 0.0);
    }
    vec2 center = topLeft + vec2(9.0, 9.0);
    return vec3(center + (offsetPosition - center) * scale, Position.z);
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
#ifdef IS_GUI
    identifier = texelFetch(Sampler0, ivec2(0, 0), 0).rgb;
    effect = 0.0;
    if (identifier == vec3(0, 0, 1)) {
        effect = 1.0;
    } else if (identifier == vec3(0, 1, 0)) {
        effect = 2.0;
    } else if (identifier.rg == vec2(143.0 / 255.0, 206.0 / 255.0)) {
        effect = 100.0 + identifier.b * 255.0;
    }
    if (effect > 0.0 && effect < 100.0) {
        gl_Position = vec4((vec2(UV0.x, 1 - UV0.y) * 2) - 1, 1, 1);
    } else if (effect > 100.0 && effect < 200.0) {
        float scale;
        if (effect == 101.0) {
            scale = getHeartbeatScale(40.0, 1.0);
        } else if (effect == 102.0) {
            scale = getHeartbeatScale(60.0, 1.05);
        } else if (effect == 103.0) {
            scale = getHeartbeatScale(120.0, 1.25);
        } else {
            scale = 1.0;
        }
        gl_Position = ProjMat * ModelViewMat * vec4(scaleHeartbeatTexture(scale), 1.0);
    }
#endif

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = Color * sample_lightmap(Sampler2, UV2);
#else
    vertexColor = Color;
#endif
    texCoord0 = UV0;

    

}
