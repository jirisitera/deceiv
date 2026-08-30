#elif defined(IS_GUI)
switch (effectGroup) {
    case 1:
        // transitions
        if (isWithinDotRadius(gl_FragCoord.xy) || color.a == 0.0) {
            discard;
        }
        switch (effect) {
            case 1:
                fragColor = renderWorldEffect(gl_FragCoord.xy);
                break;
            case 2:
                fragColor = renderBackground(gl_FragCoord.xy);
                break;
        }
        break;
    case 2:
        // moods
        switch (effect) {
            case 1:
                fragColor = scaleHeartbeat(getHeartbeatScale(40.0, 1.0), 0.0);
                break;
            case 2:
                fragColor = scaleHeartbeat(getHeartbeatScale(60.0, 1.05), 0.0);
                break;
            case 3:
                fragColor = scaleHeartbeat(getHeartbeatScale(120.0, 1.25), 0.0);
                break;
            case 4:
                fragColor = scaleHeartbeat(getHeartbeatScale(40.0, 1.0), 1.0);
                break;
            case 5:
                fragColor = scaleHeartbeat(getHeartbeatScale(40.0, 1.0), 2.0);
                break;
        }
        break;
    case 3:
        // progress bars
        switch (effect) {
            case 1:
                fragColor = renderProgressBar(color.rgb, texColor.a * ColorModulator.a);
                break;
            case 2:
                fragColor = renderProgressBarReady(texColor, color);
                break;
        }
        break;
    default:
        if (color.a < 0.1) {
            discard;
        }
        fragColor = color;
}
