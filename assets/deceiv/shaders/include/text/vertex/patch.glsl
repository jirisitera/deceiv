#ifdef IS_GUI
    identifier = texelFetch(Sampler0, ivec2(0, 0), 0).rgb;
    effect = 0.0;
    int corner = gl_VertexID % 4;
    localX = (corner == 2 || corner == 3) ? 1.0 : 0.0;
    localY = (corner == 1 || corner == 2) ? 1.0 : 0.0;
    if (identifier.rg == DECEIV_IDENTIFIER) {
        // determine effect
        effect = identifier.b * 255.0;
    }
    // vertex changes for seperate effect groups
    if (effect > 0.0 && effect < 10.0) {
        // transitions
        gl_Position = vec4((vec2(UV0.x, 1.0 - UV0.y) * 2.0) - 1.0, 1.0, 1.0);
    } else if (effect > 10.0 && effect < 20.0) {
        // moods
        gl_Position = ProjMat * ModelViewMat * vec4(Position + vec3(-10.0, -10.0, 0.0), 1.0);
    }
#endif
