#elif defined(IS_GUI)
if (effect <= 0.0) {
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color;
} else if (effect > 0.0 && effect < 10.0) {
    // transitions
    if (isWithinDotRadius(gl_FragCoord.xy) || color.a == 0.0) {
        discard;
    }
    if (effect == 1.0) {
        fragColor = renderWorldEffect(gl_FragCoord.xy);
    } else if (effect == 2.0) {
        fragColor = renderBackground(gl_FragCoord.xy);
    }
} else if (effect > 10.0 && effect < 20.0) {
    // moods
    float scale = 1.0;
    if (effect == 11.0) {
        scale = getHeartbeatScale(40.0, 1.0);
    } else if (effect == 12.0) {
        scale = getHeartbeatScale(60.0, 1.05);
    } else if (effect == 13.0) {
        scale = getHeartbeatScale(120.0, 1.25);
    }
    fragColor = scaleHeartbeat(scale);
} else if (effect == 21.0) {
    fragColor = renderProgressBar(color.rgb, texColor.a * ColorModulator.a);
} else if (effect == 22.0) {
    fragColor = renderProgressBarReady(texColor, color);
}
