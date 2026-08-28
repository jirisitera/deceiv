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
    if (color.a < 0.1 || color.rgb == identifier) {
        discard;
    }
    fragColor = color;
} else if (effect == 21.0) {
    fragColor = renderProgressBar(color.rgb, texColor.a * ColorModulator.a);
} else if (effect == 22.0) {
    fragColor = renderProgressBarReady(texColor, color);
}
