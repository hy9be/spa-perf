package com.hy9be.spaperf.metrics;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hyou on 7/7/15.
 */
public abstract class BaseMetricGroup {
    public HashMap<String, double[]> performanceResult;

    public BaseMetricGroup(){
    }

    public abstract void getResult(List objList);
}
