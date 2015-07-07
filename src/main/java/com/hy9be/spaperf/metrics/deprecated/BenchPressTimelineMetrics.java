package com.hy9be.spaperf.metrics.deprecated;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.hy9be.spaperf.util.RegExpWrapper;
import com.hy9be.spaperf.util.isXXX;

import java.util.*;

/**
 * The class parsing the timeline log info
 * Reference https://github.com/angular/angular/blob/master/modules/benchpress/src/metric/perflog_metric.ts
 * Created by hyou on 6/20/15.
 */
public class BenchPressTimelineMetrics {
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
    public BenchPressTimelineMetrics() {

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

    private JsonObject aggregateEvents(List<JsonObject> events, String markName) {
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

        JsonObject markStartEvent = null;
        JsonObject markEndEvent = null;
        int gcTimeInScript = 0;
        int renderTimeInScript = 0;
        JsonObject intervalStarts = new JsonObject();

        for(JsonObject event : events) {
            String ph = event.get("ph").asString();
            String name = event.get("name").asString();
            int microIterations = 1;
            List<String> microIterationsMatch = RegExpWrapper.firstMatch(new JsonObject(), name); //JsonObject "_MICRO_ITERATIONS_REGEX"

            if (microIterationsMatch.size() != 0) {
                name = microIterationsMatch.get(0);
                microIterations = Integer.parseInt(microIterationsMatch.get(1), 10);
            }

            if (ph == "b" && name == markName) {
                markStartEvent = event;
            } else if (ph == "e" && name == markName) {
                markEndEvent = event;
            }

            if (isXXX.isPresent(markStartEvent) && isXXX.isBlank(markEndEvent) && event.get("pid").asString() == markStartEvent.get("pid").asString()) {
                if (ph == "B" || ph == "b") {
                    intervalStarts.add(name, event);
                } else if (ph == "E" || ph == "e" && (intervalStarts.get(name) != null)) {
                    JsonObject startEvent = intervalStarts.get(name).asObject();
                    long duration = event.get("ts").asLong() - startEvent.get("ts").asLong();
                    intervalStarts.remove(name);

                    if (name == "gc") {
                        result.set("gcTime", result.get("gcTime").asLong() + duration);

                        int amount = (startEvent.get("args").asObject().get("usedHeapSize").asInt() - event.get("args").asObject().get("usedHeapSize").asInt()) / 1000;
                        result.set("gcAmount", result.get("gcAmount").asLong() + amount);

                        String majorGc = event.get("args").asObject().get("majorGc").asString();
                        if (majorGc != null) {
                            result.set("majorGcTime", result.get("majorGcTime").asLong() + duration);
                        }
                        if (intervalStarts.get("script") != null) {
                            gcTimeInScript += duration;
                        }
                    } else if (name == "render") {
                        result.set("renderTime", result.get("renderTime").asLong() + duration);
                        if (intervalStarts.get("script") != null) {
                            renderTimeInScript += duration;
                        }
                    } else if (name == "script") {
                        result.set("scriptTime", result.get("scriptTime").asLong() + duration);
                    } else if (isXXX.isPresent(microMetrics.get(name))) {
                        result.set(name, result.get(name).asLong() + duration / microIterations);
                    }
                }
            }
        }

        result.set("pureScriptTime", result.get("scriptTime").asLong() - gcTimeInScript - renderTimeInScript);
        return isXXX.isPresent(markStartEvent) && isXXX.isPresent(markEndEvent) ? result : null;

    }
}
