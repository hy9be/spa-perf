package com.hy9be.spaperf.metrics;

/**
 * Created by hyou on 6/23/15.
 */
public class PerfLogFeatures {

    boolean render = false;
    boolean gc = false;
    boolean frameCapture = false;

    public PerfLogFeatures(boolean render, boolean gc, boolean frameCapture) {
        this.render = render;
        this.gc = gc;
        this.frameCapture = frameCapture;
    }
}
