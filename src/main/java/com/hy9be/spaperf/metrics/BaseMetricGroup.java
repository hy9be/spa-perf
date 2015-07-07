package com.hy9be.spaperf.metrics;

import com.hy9be.spaperf.driver.SPAPerfChromeDriver;

import java.util.HashMap;

/**
 * Created by hyou on 7/7/15.
 */
public abstract class BaseMetricGroup {
    public HashMap<String, double[]> performanceResult;

    public BaseMetricGroup(){
    }

    public abstract void getResult(SPAPerfChromeDriver driver);
}
