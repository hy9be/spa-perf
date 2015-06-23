package com.hy9be.spaperf.metrics;

import com.eclipsesource.json.JsonObject;
import org.apache.commons.collections.map.HashedMap;
import org.openqa.selenium.logging.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The class parsing the timeline log info
 * Reference (in node.js): https://github.com/axemclion/browser-perf/blob/master/lib/metrics/TimelineMetrics.js
 * Created by hyou on 6/20/15.
 */
public class TimelineMetrics {
    private void processTimelineRecords() {

    }

    private List<JsonObject> processTimelineRecord(List<LogEntry> logEntries) {
        List<JsonObject> normalizedEvents = new ArrayList<>();
        Map<String, Boolean> majorGCPids = new HashedMap();

        for (LogEntry logEntry : logEntries) {
            JsonObject logEntryJsonWrapper = JsonObject.readFrom(logEntry.getMessage());

            JsonObject logEntryJsonParams = logEntryJsonWrapper.get("message").asObject().get("params").asObject();

            String cat = logEntryJsonParams.get("cat").asString();
            String name = logEntryJsonParams.get("name").asString();
            JsonObject args = logEntryJsonParams.get("args").asObject();
            String pid = logEntryJsonParams.get("pid").asString();
            String ph = logEntryJsonParams.get("ph").asString();

            if (cat == "disabled-by-default-devtools.timeline") {
                if ((name == "FunctionCall")
                        && (args != null
                        || args.get("data").asString().length() == 0
                        || args.get("data").asObject().get("scriptName").asString() != "InjectedScript")) {
                    normalizedEvents.add(new JsonObject()
                            .add("name", "script")
                            .add("events", logEntryJsonParams));
                } else if ((name == "RecalculateStyles")
                        || (name == "Layout")
                        || (name == "UpdateLayerTree")
                        || (name == "Paint")
                        || (name == "Rasterize")
                        || (name == "CompositeLayers")) {
                    normalizedEvents.add(new JsonObject()
                            .add("name", "render")
                            .add("events", logEntryJsonParams));
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
                        .add("events", logEntryJsonParams));
            } else if (cat == "v8") {
                if (name == "majorGC") {
                    if (ph == "B'") {
                        majorGCPids.put(pid, true);
                    }
                }
            }
        }

        return normalizedEvents;
    }

    private void aggregateData() {
        /*

        {"message":{
            "method":"Tracing.dataCollected",
            "params":{
                "args":{"number":8},
                "cat":"__metadata",
                "name":"num_cpus",
                "ph":"M",
                "pid":37587,
                "tid":0,
                "ts":0}},
            "webview":"browser"}
        {"message":{
            "method":"Tracing.dataCollected",
            "params":{
                "args":{"sort_index":-1},
                "cat":"__metadata",
                "name":"process_sort_index",
                "ph":"M",
                "pid":37587,
                "tid":17159,
                "ts":0}},
            "webview":"browser"}
        {"message":{
            "method":"Tracing.dataCollected",
            "params":{
                "args":{"name":"Renderer"},
                "cat":"__metadata",
                "name":"process_name",
                "ph":"M",
                "pid":37592,
                "tid":16151,
                "ts":0}},
            "webview":"browser"}

         */


    }
}
