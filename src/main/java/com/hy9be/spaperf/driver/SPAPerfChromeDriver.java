package com.hy9be.spaperf.driver;

import com.eclipsesource.json.JsonObject;
import org.apache.commons.collections.map.HashedMap;
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
 * Created by hyou on 6/20/15.
 */
public class SPAPerfChromeDriver extends ChromeDriver {
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

    // Get the log
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

    // Conver the log to events
    private List<JsonObject> convertPerfRecordsToEvents(List<JsonObject> events) {
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
                    normalizedEvents.add(new JsonObject()
                            .add("name", "script")
                            .add("events", event));
                } else if ((name == "RecalculateStyles")
                        || (name == "Layout")
                        || (name == "UpdateLayerTree")
                        || (name == "Paint")
                        || (name == "Rasterize")
                        || (name == "CompositeLayers")) {
                    normalizedEvents.add(new JsonObject()
                            .add("name", "render")
                            .add("events", event));
                } else if (name == "GCEvent") {
                    JsonObject normArgs = new JsonObject().add("usedHeapSize",
                            (args.get("usedHeapSizeAfter") != null) ? args.get("usedHeapSizeAfter").asString() : args.get("usedHeapSizeBefore").asString());
                    if (ph == "E") {
                        normArgs.set("majorGc", majorGCPids.get(pid));
                    }
                    majorGCPids.put(pid, false);
                    normalizedEvents.add(new JsonObject()
                            .add("name", "gc")
                            .add("args", normArgs));
                }
            } else if (cat == "blink.console") {
                normalizedEvents.add(new JsonObject()
                        .add("name", name)
                        .add("events", event));
            } else if (cat == "v8") {
                if (name == "majorGC") {
                    if (ph == "B") {
                        majorGCPids.put(pid, true);
                    }
                }
            }
        }

        return normalizedEvents;
    }

    // Detach the driver but do not close the browser session (vs. this.close())
    public void detach() {
        this.close();
    }
}
