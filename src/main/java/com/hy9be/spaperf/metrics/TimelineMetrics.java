package com.hy9be.spaperf.metrics;

import com.eclipsesource.json.JsonObject;
import com.hy9be.spaperf.driver.SPAPerfChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hyou on 7/7/15.
 */
public class TimelineMetrics extends BaseMetricGroup {
    public static String [] selectedPerformanceCounters = {"Program", "FunctionCall", "GCEvent", "InvalidateLayout", "Layout",
            "RecalculateStyles", "Scroll", "CompositeLayers", "ImageDecode", "ImageResize", "Paint"};

    public TimelineMetrics(){
    }

    private List getPerfLogFromDriver(SPAPerfChromeDriver driver) {
        return driver.manage().logs().get(LogType.PERFORMANCE).getAll();
    }

    public void getResult(SPAPerfChromeDriver driver) {
        List entries = getPerfLogFromDriver(driver);

        performanceResult = new HashMap<String, double[]>();
        HashMap<String, Double> startTime = new HashMap<>();
        for (int i = 0; i < selectedPerformanceCounters.length; i++) {
            performanceResult.put(selectedPerformanceCounters[i], new double[]{0.0, 0.0, 0.0});
            startTime.put(selectedPerformanceCounters[i], 0.0);
        }

        for (Object entryObj : entries) {
            LogEntry entry = (LogEntry) entryObj;
            try {
                JsonObject message = JsonObject.readFrom(entry.getMessage()).get("message").asObject();
                if (message.get("method").asString().equals("Tracing.dataCollected")) {
                    //System.out.println(entry.getLevel() + " " + entry.getMessage());
                    JsonObject event = message.get("params").asObject();
                    String name = event.get("name").asString();
                    if (performanceResult.containsKey(name)) {
                        String eventStatus = event.get("ph").asString();
                        double[] currentValue = performanceResult.get(name);
                        Double dur = 0.0;
                        switch (eventStatus) {
                            case "X":
                                dur = event.get("dur").asDouble() / 1000.0;
                                break;
                            case "B":  //add start time for this event
                                startTime.put(name, event.get("ts").asDouble());
                                continue;
                            case "E":
                                if (startTime.get(name) < 0.001)  // this means no beginning time for this event
                                    //throw new Exception();
                                    continue;
                                else {
                                    dur = (event.get("ts").asDouble() - startTime.get(name)) / 1000.0;
                                    startTime.put(name, 0.0);  //reset the startTime
                                }
                                break;
                            default:
                                break;
                        }
                        currentValue[0] = currentValue[0] + dur;
                        currentValue[1] = currentValue[1] > dur ? currentValue[1] : dur;
                        currentValue[2]++;
                        performanceResult.put(name, currentValue); //calculate event duration
                    }
                }
            } catch (Exception ex) {
                continue;
            }
        }
    }
}
