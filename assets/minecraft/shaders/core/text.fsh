#version 330

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#moj_import <minecraft:fog.glsl>
#endif

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
#endif

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#ifdef IS_GUI
in float effect;
in vec3 identifier;
#endif

float topologize(float noise) {
    float smoothFloor = noise*12.0;
    vec2 fracU = vec2(smoothFloor,fwidth(smoothFloor)*1.3);
    fracU.x = fract(fracU.x);
    fracU += (1.0 - 2.0*fracU)*step(fracU.y,fracU.x);
    smoothFloor = smoothFloor - clamp(1.0 - fracU.x/fracU.y,0.0,1.0);
    return noise*1.25 + smoothFloor*0.95/09.0;
}
vec3 hash33(vec3 p) {
    p = vec3(dot(p,vec3(227.1,311.7, 74.7)), dot(p,vec3(269.5,183.3,246.1)), dot(p,vec3(333.5,4444271.9,6624.6)));
    return -1.0 + 2.2*fract(sin(p)*43758.5453123);
}
float tetraNoise(vec2 o) {
    vec3 p = vec3(o.x + GameTime * 20, o.y + GameTime * 10, GameTime * 10);
    vec3 i = floor(p + dot(p, vec3(0.33333,0.33333,0.33333)));
    p -= i - dot(i, vec3(0.16666,0.16666,0.16666));
    vec3 i1 = step(p.yzx, p);
    vec3 i2 = max(i1, 1.0-i1.zxy);
    i1 = min(i1, 1.0-i1.zxy);
    vec3 p1 = p - i1 + 0.16666, p2 = p - i2 + 0.33333, p3 = p - 0.5;
    vec4 v = max(0.5 - vec4(dot(p,p), dot(p1,p1), dot(p2,p2), dot(p3,p3)), 0.0);
    vec4 d = vec4(dot(p, hash33(i)), dot(p1, hash33(i + i1)), dot(p2, hash33(i + i2)), dot(p3, hash33(i + 1.0)));
    float n = clamp(dot(d,v*v*v*8.)*1.732 + 0.5, 0., 1.);
    return n;
}
vec2 roundToPixelGrid(vec2 p, int size) {
    ivec2 result = (ivec2(p) / size) * size;
    return vec2(result + (size / 2));
}
float drawStripes(in vec2 uv) {
    vec2 p = uv + vec2(GameTime * 10, GameTime * 450);
    float wave = fract((p.x - p.y) * 0.5);
    float rising_edge  = smoothstep(0.0, 0.5, wave);
    float falling_edge = smoothstep(1.0 - 0.5, 1.0, wave);
    return rising_edge - falling_edge;
}
vec4 renderWorldEffect(in vec2 fragCoord) {
    vec2 iResolution = ScreenSize;
    float aspect = ScreenSize.x / ScreenSize.y;
    vec2 p = roundToPixelGrid(fragCoord, 4) * vec2(aspect, 1);
    p = p / ScreenSize;
    p *= 2.5;
    vec2 e = vec2(15.0/(iResolution.y+iResolution.x), 0.0);
    // left
    float fxl = topologize(tetraNoise(p + e.xy));
    // right
    float fxr = topologize(tetraNoise(p - e.xy));
    // up
    float fyu = topologize(tetraNoise(p + e.yx));
    // down
    float fyd = topologize(tetraNoise(p - e.yx));
    float weight = clamp((max(abs(fxl - fxr), abs(fyu - fyd)) - 0.01) * 12.0, 0.0, 1.0);
    vec3 highlight = vertexColor.xyz * max(drawStripes(p) * 0.5, 0.25);
    return mix(vec4(0, 0, 0, vertexColor.a), vec4(highlight, vertexColor.a), pow(weight, 8));
}
vec4 renderBackground(in vec2 fragCoord) {
    vec2 baseCoord = vec2(fragCoord.x, -fragCoord.y);
    float textureScale = 8.0;
    float time = GameTime * 5000.0;

    vec2 position = baseCoord / textureScale + vec2(-time, time);
    vec2 wrappedPosition = vec2(1.0, 1.0) + mod(position, vec2(16.0, 16.0));
    vec2 wrappedUV = wrappedPosition / vec2(textureSize(Sampler0, 0));

    return texture(Sampler0, wrappedUV) * vertexColor * ColorModulator;
}
bool isWithinDotRadius(in vec2 fragCoord) {
    float dotSize = 40.0;
    vec2 pixelatedCoord = roundToPixelGrid(fragCoord.xy, 4);
    vec2 dotCoord = pixelatedCoord / dotSize;
    vec2 cell = floor(dotCoord);
    vec2 cellUV = (cell + 0.5) * dotSize / ScreenSize.xy;

    float progress = vertexColor.a * 1.5 - 0.5;
    float diagonal = (cellUV.x + (1.0 - cellUV.y)) / 2.0;
    float waveProgress = (progress - diagonal + 0.5) * 2.0;

    vec2 local = fract(dotCoord) - 0.5;
    float radius = clamp(waveProgress * 1.5, 0.0, 1.5);

    return length(local) * 2.0 >= radius;
}

void main() {
#ifdef IS_GRAYSCALE
    vec4 texColor = texture(Sampler0, texCoord0).rrrr;
#else
    vec4 texColor = texture(Sampler0, texCoord0);
#endif

#ifdef IS_SEE_THROUGH
    vec4 color = texColor * vertexColor;
#else
    vec4 color = texColor * vertexColor * ColorModulator;
#endif

#ifndef IS_GUI
    if (color.a < 0.1) {
        discard;
    }
#endif

#ifdef IS_SEE_THROUGH
    fragColor = color * ColorModulator;
#elif defined(IS_GUI)
    if (effect == 0.0) {
        if (color.a < 0.1) {
            discard;
        }
        fragColor = color;
    } else if (effect > 0.0 && effect < 100.0) {
        if (isWithinDotRadius(gl_FragCoord.xy) || color.a == 0.0) {
            discard;
        }
        if (effect == 1.0) {
            fragColor = renderWorldEffect(gl_FragCoord.xy);
        } else if (effect == 2.0) {
            fragColor = renderBackground(gl_FragCoord.xy);
        }
    } else if (effect > 100.0 && effect < 200.0) {
        if (color.a < 0.1 || color.rgb == identifier) {
            discard;
        }
        fragColor = color;
    }
#else
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
#endif

}
