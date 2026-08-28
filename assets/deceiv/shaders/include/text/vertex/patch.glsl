#ifdef IS_GUI
    identifier = texelFetch(Sampler0, ivec2(0, 0), 0).rgb;
    effect = 0.0;
    int corner = gl_VertexID % 4;
    localX = (corner == 2 || corner == 3) ? 1.0 : 0.0;
    localY = (corner == 1 || corner == 2) ? 1.0 : 0.0;
    if (identifier.rg == DECEIV_IDENTIFIER) {
        effect = identifier.b * 255.0;
    }

    if (effect > 0.0 && effect < 10.0) {
        // transitions
        gl_Position = vec4((vec2(UV0.x, 1 - UV0.y) * 2) - 1, 1, 1);
    } else if (effect > 10.0 && effect < 20.0) {
        // moods
        float scale;
        if (effect == 11.0) {
            scale = getHeartbeatScale(40.0, 1.0);
        } else if (effect == 12.0) {
            scale = getHeartbeatScale(60.0, 1.05);
        } else if (effect == 13.0) {
            scale = getHeartbeatScale(120.0, 1.25);
        } else {
            scale = 1.0;
        }
        gl_Position = ProjMat * ModelViewMat * vec4(scaleHeartbeatTexture(scale), 1.0);
    }
#endif
