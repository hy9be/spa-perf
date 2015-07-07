package com.hy9be.spaperf.driver;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.hy9be.spaperf.metrics.BaseMetricGroup;
import com.hy9be.spaperf.metrics.NetworkMetrics;
import com.hy9be.spaperf.metrics.TimelineMetrics;
import com.hy9be.spaperf.metrics.deprecated.BenchPressTimelineMetrics;
import com.hy9be.spaperf.output.CSVPersistor;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Reference to the extension functions:
 * https://github.com/angular/angular/blob/master/modules/benchpress/src/webdriver/chrome_driver_extension.ts
 * Created by hyou on 6/20/15.
 */
public class SPAPerfChromeDriver extends ChromeDriver {
    private String fileName;
    private String[] selectedPerformanceCounters;

    public SPAPerfChromeDriver() {
        super(getPerformanceLoggingCapabilities());
    }

    // Use static method to generate the capabilities object to initiate the driver
    private static DesiredCapabilities getPerformanceLoggingCapabilities() {
        DesiredCapabilities caps = DesiredCapabilities.chrome();

        // Enable performance logging
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        // Enable timeline tracing
        Map<String, Object> chromeOptions = new HashMap<String, Object>();
        Map<String, String> perfLoggingPrefs = new HashMap<String, String>();
        // Tracing categories, please note NO SPACE NEEDED after the commas
        perfLoggingPrefs.put("traceCategories", "blink.console,disabled-by-default-devtools.timeline");
        chromeOptions.put("perfLoggingPrefs", perfLoggingPrefs);
        //chromeOptions.put("debuggerAddress", "127.0.0.1:10134");
        caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        return caps;
    }

    public List<LogEntry> getPerformanceLog ()
    {
        return this.manage().logs().get(LogType.PERFORMANCE).getAll();
    }

    public List<Map> getNetworkActivityLog ()
    {
        return (List<Map>)((JavascriptExecutor) this).executeScript("return window.performance.getEntries()");
    }

    public void startPerformanceProfiling(String fName)
    {
        this.manage().logs().get(LogType.PERFORMANCE).getAll();
        List<Map> currentNetworkActivity = (List<Map>)((JavascriptExecutor) this).executeScript("return window.performance.getEntries()");
        NetworkMetrics.previousNetworkActivityCounts = currentNetworkActivity.size();

        // generate row header for the csvOutput
        fileName = fName;
        selectedPerformanceCounters = ArrayUtils.addAll(TimelineMetrics.selectedPerformanceCounters, NetworkMetrics.selectedPerformanceCounters);
        CSVPersistor csvFile = new CSVPersistor(fileName, selectedPerformanceCounters);
    }

    public void performProfiling(String actionName){
        TimelineMetrics timelineData = new TimelineMetrics();
        timelineData.getResult(getPerformanceLog());
        NetworkMetrics networkData = new NetworkMetrics();
        networkData.getResult(getNetworkActivityLog());

        List<BaseMetricGroup> perfMetrics = new ArrayList<>();
        perfMetrics.add(timelineData);
        perfMetrics.add(networkData);
        CSVPersistor.csvResult(perfMetrics, selectedPerformanceCounters, actionName, fileName);
    }

    // Detach the driver but do not close the browser session (vs. this.close())
    public void detach() {
        this.close();
    }

    public void stopPerformanceProfiling(){

    }

    // Get the log: ported from benchpress@AngularJS
    public List<JsonObject> readPerfLog() throws Exception {
        List<LogEntry> entries = this.manage().logs().get(LogType.PERFORMANCE).getAll();

        List<JsonObject> events = new ArrayList<>();

        for (LogEntry entry : entries) {
            JsonObject message = JsonObject.readFrom(entry.getMessage()).get("message").asObject();

            if (message.get("method").asString() == "Tracing.dataCollected") {
                events.add(message.get("params").asObject());
            }
            if (message.get("method").asString() == "Tracing.bufferUsage") {
                throw new Exception("The DevTools trace buffer filled during the test!");
            }
        }

        return convertPerfRecordsToEvents(events);
    }

    // Conver the log to events: ported from benchpress@AngularJS
    private List<JsonObject> convertPerfRecordsToEvents(List<JsonObject> events) throws Exception {
        List<JsonObject> normalizedEvents = new ArrayList<>();
        Map<String, Boolean> majorGCPids = new HashedMap();

        for (JsonObject event : events) {

            String cat = event.get("cat").asString();
            String name = event.get("name").asString();
            JsonObject args = event.get("args").asObject();
            String pid = event.get("pid").asString();
            String ph = event.get("ph").asString();

            if (cat == "disabled-by-default-devtools.timeline") {
                if ((name == "FunctionCall")
                        && (args != null
                        || args.get("data").asString().length() == 0
                        || args.get("data").asObject().get("scriptName").asString() != "InjectedScript")) {
                    normalizedEvents.add(normalizeEvent(event, new JsonObject().add("name", "script")));

                } else if ((name == "RecalculateStyles")
                        || (name == "Layout")
                        || (name == "UpdateLayerTree")
                        || (name == "Paint")
                        || (name == "Rasterize")
                        || (name == "CompositeLayers")) {
                    normalizedEvents.add(normalizeEvent(event, new JsonObject().add("name", "render")));
                } else if (name == "GCEvent") {
                    JsonObject normArgs = new JsonObject().add("usedHeapSize",
                            (args.get("usedHeapSizeAfter") != null) ? args.get("usedHeapSizeAfter").asString() : args.get("usedHeapSizeBefore").asString());
                    if (ph == "E") {
                        normArgs.set("majorGc", majorGCPids.get(pid));
                    }
                    majorGCPids.put(pid, false);
                    normalizedEvents.add(normalizeEvent(event, new JsonObject().add("name", "gc").add("args", normArgs)));
                }
            } else if (cat == "blink.console") {
                normalizedEvents.add(normalizeEvent(event, new JsonObject().add("name", name)));
            } else if (cat == "v8") {
                if (name == "majorGC") {
                    if (ph == "B") {
                        majorGCPids.put(pid, true);
                    }
                }
            } else if (cat == "benchmark") {
                // TODO(goderbauer): Instead of BenchmarkInstrumentation::ImplThreadRenderingStats the
                // following events should be used (if available) for more accurate measurments:
                //   1st choice: vsync_before - ground truth on Android
                //   2nd choice: BenchmarkInstrumentation::DisplayRenderingStats - available on systems with
                //               new surfaces framework (not broadly enabled yet)
                //   3rd choice: BenchmarkInstrumentation::ImplThreadRenderingStats - fallback event that is
                //               allways available if something is rendered
                if (name == "BenchmarkInstrumentation::ImplThreadRenderingStats") {
                    int frameCount = event.get("args").asObject().get("data").asObject().get("frame_count").asInt();
                    if (frameCount > 1) {
                        throw new Exception("multi-frame render stats not supported");
                    }
                    if (frameCount == 1) {
                        normalizedEvents.add(normalizeEvent(event, new JsonObject().add("name", "'frame'")));
                    }
                }
            }
        }

        return normalizedEvents;
    }

    // Normalize the event: : ported from benchpress@AngularJS
    private JsonObject normalizeEvent(JsonObject chromeEvent, JsonObject data) {

        String ph = chromeEvent.get("ph").asString();

        if (ph == "S") {
            ph = "b";
        } else if (ph == "F") {
            ph = "e";
        }

        JsonObject result = new JsonObject()
                .add("pid", chromeEvent.get("pid").asString())
                .add("ph", ph)
                .add("cat", "timeline")
                .add("ts", chromeEvent.get("ts").asLong() / 1000);

        if (ph == "X") {
            String dur = chromeEvent.get("dur").asString();
            if (dur == null || dur.length() == 0) {
                dur = chromeEvent.get("tdur").asString();
            }
            result.add("dur", (dur.length() == 0) ? 0.0 : Long.parseLong(dur) / 1000);
        }

        for(JsonObject.Member member : data) {
            String name = member.getName();
            JsonValue value = member.getValue();
            result.add(name, value);
        }

        return result;
    }
}