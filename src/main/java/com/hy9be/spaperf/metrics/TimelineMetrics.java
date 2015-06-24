package com.hy9be.spaperf.metrics;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.apache.commons.collections.map.HashedMap;
import org.openqa.selenium.logging.LogEntry;

import java.util.*;

/**
 * The class parsing the timeline log info
 * Reference (in node.js): https://github.com/axemclion/browser-perf/blob/master/lib/metrics/TimelineMetrics.js
 * Created by hyou on 6/20/15.
 */
public class TimelineMetrics {
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
    */

    int measureCount;
    List remainingEvents;
    Map<String, String> microMetrics;
    PerfLogFeatures perfLogFeatures;

    /**
     * Constructor
     */
    public TimelineMetrics() {

    }

    private List<JsonObject> convertPerfRecordsToEvents(List<LogEntry> logEntries) {
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
                    if (ph == "B") {
                        majorGCPids.put(pid, true);
                    }
                }
            }
        }

        return normalizedEvents;
    }

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

    private void addEvents(List<JsonObject> events) {
        boolean needSort = false;

        for (JsonObject event : events) {
            if (event.get("ph").asString() == "X") {
                needSort = true;
                JsonObject startEvent = new JsonObject();
                JsonObject endEvent = new JsonObject();

                for(JsonObject.Member member : event) {
                    String prop = member.getName();
                    JsonValue value = member.getValue();

                    startEvent.add(prop, value);
                    endEvent.add(prop, value);
                }

                startEvent.add("ph", "B");
                endEvent.add("ph", "E");
                endEvent.add("ts", startEvent.get("ts").asLong() + startEvent.get("dur").asLong());

                remainingEvents.add(startEvent);
                remainingEvents.add(endEvent);
            } else {
                remainingEvents.add(event);
            }
        }

        if (needSort) {
            // Need to sort because of the ph==='X' events
            Collections.sort(remainingEvents, new Comparator<JsonObject>() {
                public int compare(JsonObject a, JsonObject b) {
                    Long diff = a.get("ts").asLong() - b.get("ts").asLong();
                    return diff > 0
                            ? 1
                            : diff < 0
                            ? -1
                            : 0;
                }
            });
        }
    }

    private void aggregateEvents(List<JsonObject> events) {
        JsonObject result = new JsonObject()
                .add("scriptTime", 0)
                .add("pureScriptTime", 0);

        if (perfLogFeatures.gc) {
            result.add("gcTime", 0);
            result.add("majorGcTime", 0);
            result.add("gcAmount", 0);
        }
        if (perfLogFeatures.render) {
            result.add("renderTime", 0);
        }

        for(String key : microMetrics.keySet()) {
            result.add(microMetrics.get(key), 0);
        }

        var markStartEvent = null;
        var markEndEvent = null;
        var gcTimeInScript = 0;
        var renderTimeInScript = 0;

        var intervalStarts = {};
        for(JsonObject event : events) {
            String ph = event.get("ph").asString();
            String name = event.get("name").asString();
            int microIterations = 1;

        }
        events.forEach( (event) => {
                var ph = event["ph"];
            var name = event["name"];
            var microIterations = 1;
            var microIterationsMatch = RegExpWrapper.firstMatch(_MICRO_ITERATIONS_REGEX, name);
            if (isPresent(microIterationsMatch)) {
                name = microIterationsMatch[1];
                microIterations = NumberWrapper.parseInt(microIterationsMatch[2], 10);
            }

            if (StringWrapper.equals(ph, "b") && StringWrapper.equals(name, markName)) {
                markStartEvent = event;
            } else if (StringWrapper.equals(ph, "e") && StringWrapper.equals(name, markName)) {
                markEndEvent = event;
            }
            if (isPresent(markStartEvent) && isBlank(markEndEvent) && event["pid"] === markStartEvent["pid"]) {
                if (StringWrapper.equals(ph, "B") || StringWrapper.equals(ph, "b")) {
                    intervalStarts[name] = event;
                } else if ((StringWrapper.equals(ph, "E") || StringWrapper.equals(ph, "e")) && isPresent(intervalStarts[name])) {
                    var startEvent = intervalStarts[name];
                    var duration = (event["ts"] - startEvent["ts"]);
                    intervalStarts[name] = null;
                    if (StringWrapper.equals(name, "gc")) {
                        result["gcTime"] += duration;
                        var amount = (startEvent["args"]["usedHeapSize"] - event["args"]["usedHeapSize"]) / 1000;
                        result["gcAmount"] += amount;
                        var majorGc = event["args"]["majorGc"];
                        if (isPresent(majorGc) && majorGc) {
                            result["majorGcTime"] += duration;
                        }
                        if (isPresent(intervalStarts["script"])) {
                            gcTimeInScript += duration;
                        }
                    } else if (StringWrapper.equals(name, "render")) {
                        result["renderTime"] += duration;
                        if (isPresent(intervalStarts["script"])) {
                            renderTimeInScript += duration;
                        }
                    } else if (StringWrapper.equals(name, "script")) {
                        result["scriptTime"] += duration;
                    } else if (isPresent(this._microMetrics[name])) {
                        result[name] += duration / microIterations;
                    }
                }
            }
        });
        result["pureScriptTime"] = result["scriptTime"] - gcTimeInScript - renderTimeInScript;
        return isPresent(markStartEvent) && isPresent(markEndEvent) ? result : null;
    }
}
