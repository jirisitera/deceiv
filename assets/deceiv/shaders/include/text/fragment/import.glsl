#ifdef IS_GUI
#moj_import <minecraft:globals.glsl>

#define DECEIV_CANVAS_SIZE 256.0
#define DECEIV_PROGRESS_SIZE 40.0

in float effect;
in vec3 identifier;
in float localX;
in float localY;

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
    // wrap position and UV
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
    // calculate progress based on opacity
    float progress = vertexColor.a * 1.5 - 0.5;
    float diagonal = (cellUV.x + (1.0 - cellUV.y)) / 2.0;
    float waveProgress = (progress - diagonal + 0.5) * 2.0;
    // check dot radius bounds
    vec2 local = fract(dotCoord) - 0.5;
    float radius = clamp(waveProgress * 1.5, 0.0, 1.5);
    return length(local) * 2.0 >= radius;
}
vec4 renderProgressBar(vec3 color, float opacity) {
    vec3 fill = texelFetch(Sampler0, ivec2(1, 0), 0).rgb;
    if (opacity < 0.1 || color == identifier || color == fill) {
        discard;
    }
    // protection against different text rendering order
    float currentLocalX = (abs(dFdx(localX)) > abs(dFdx(localY))) ? localX : localY;
    // fill based on progress
    bool isFilled = currentLocalX * DECEIV_CANVAS_SIZE < vertexColor.a * DECEIV_PROGRESS_SIZE;
    return vec4(isFilled ? fill : color, opacity * 0.75);
}
vec4 renderProgressBarReady(vec4 texColor, vec4 color) {
    if (texColor.a < 0.1 || texColor.rgb == identifier) {
        discard;
    }
    float pulse = 0.5 + 0.5 * sin(GameTime * 10000.0);
    return vec4(color.rgb * (0.85 + 0.15 * pulse), color.a * (0.75 + 0.25 * pulse));
}
float getHeartbeatScale(float speed, float base) {
    float time = fract(GameTime * 1200.0 * (speed / 60.0));
    // combine beat waves to simulate realistic heartbeat
    float p = exp(-pow((time - 0.15) * 20.0, 2.0)) * 0.15;
    float q = exp(-pow((time - 0.26) * 50.0, 2.0)) * 0.15;
    float r = exp(-pow((time - 0.30) * 50.0, 2.0)) * 0.5;
    float s = exp(-pow((time - 0.34) * 50.0, 2.0)) * 0.3;
    float t = exp(-pow((time - 0.55) * 15.0, 2.0)) * 0.3;
    return 1.0 + (p - q + r - s + t) * 0.2 * base;
}
vec4 scaleHeartbeat(float scale) {
    float canvasSize = DECEIV_CANVAS_SIZE * 2.0;
    float textureSize = 18.0;
    // calculate local coordinates
    float currentX = abs(dFdx(localX));
    float currentY = abs(dFdx(localY));
    float currentLocalX = (currentX > currentY) ? localX : localY;
    float currentLocalY = (currentX > currentY) ? localY : localX;
    // center texture
    bool invertY = dFdy(currentLocalY) > 0.0;
    float centerPixels = 10.0 + textureSize / 2.0;
    vec2 centerLocal = vec2(centerPixels / canvasSize, invertY ? (1.0 - centerPixels / canvasSize) : (centerPixels / canvasSize));
    vec2 currentLocal = vec2(currentLocalX, currentLocalY);
    vec2 newLocal = centerLocal + (currentLocal - centerLocal) / scale;
    // check texture bounds
    float textureMinX = 10.0 / canvasSize;
    float textureMaxX = (10.0 + textureSize) / canvasSize;
    float textureMinY = invertY ? (1.0 - (10.0 + textureSize) / canvasSize) : (10.0 / canvasSize);
    float textureMaxY = invertY ? (1.0 - 10.0 / canvasSize) : ((10.0 + textureSize) / canvasSize);
    if (newLocal.x < textureMinX || newLocal.x > textureMaxX || newLocal.y < textureMinY || newLocal.y > textureMaxY) {
        discard;
    }
    float textureX = dFdx(texCoord0.x) / dFdx(currentLocalX);
    float textureY = dFdy(texCoord0.y) / dFdy(currentLocalY);
    // UV scaling shift
    vec2 localShift = newLocal - currentLocal;
    vec2 finalUV = texCoord0 + vec2(localShift.x * textureX, localShift.y * textureY);
    // undo vertex shift
    finalUV -= vec2(abs(textureX) / canvasSize * 10.0, abs(textureY) / canvasSize * 10.0);
    // build color and remove identifier
    vec4 color = texture(Sampler0, finalUV) * vertexColor * ColorModulator;
    if (color.a < 0.1 || color.rgb == identifier) {
        discard;
    }
    return color;
}
#endif
