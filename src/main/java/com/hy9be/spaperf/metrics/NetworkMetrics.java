package com.hy9be.spaperf.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hyou on 7/7/15.
 */
public class NetworkMetrics extends BaseMetricGroup {
    public static String[] selectedPerformanceCounters = {"xmlhttprequest"};

    public static int previousNetworkActivityCounts = 0;

    public NetworkMetrics()  {
    }

    public void getResult(List entries) {
        performanceResult = new HashMap<String, double[]>();
        HashMap<String, Double> startTime = new HashMap<>();
        for (int i = 0; i < selectedPerformanceCounters.length; i++) {
            performanceResult.put(selectedPerformanceCounters[i], new double[]{0.0, 0.0, 0.0});
            startTime.put(selectedPerformanceCounters[i], 0.0);
        }

        double dur = 0.0;
        List<Map<String, Object>> networkEntries = new ArrayList<Map<String, Object>>();
        int currentNetworkActivityCounts = entries.size();
        for (int i= previousNetworkActivityCounts; i<currentNetworkActivityCounts; i++)
            networkEntries.add(i-previousNetworkActivityCounts, (Map) entries.get(i));
        for(int i=0; i<currentNetworkActivityCounts - previousNetworkActivityCounts; i++)
        {
            Map<String, Object> ele = networkEntries.get(i);
            for (int j=0; j < selectedPerformanceCounters.length; j++)
                if(ele.get("initiatorType").equals(selectedPerformanceCounters[j])) {
                    dur = Double.parseDouble(ele.get("duration").toString());
                    double[] curr = performanceResult.get(selectedPerformanceCounters[j]);
                    curr[0] += dur;
                    curr[1] = curr[1] > dur ? curr[1] : dur;
                    curr[2]++;
                    performanceResult.put(selectedPerformanceCounters[j], curr);
                }
        }
        previousNetworkActivityCounts = currentNetworkActivityCounts;
    }
}
