package com.hy9be.spaperf.metrics;

import com.eclipsesource.json.JsonObject;
import org.openqa.selenium.logging.LogEntry;

import java.util.List;

/**
 * The class parsing the timeline log info
 * Reference (in node.js): https://github.com/axemclion/browser-perf/blob/master/lib/metrics/TimelineMetrics.js
 * Created by hyou on 6/20/15.
 */
public class TimelineMetrics {
    private void processTimelineRecords() {

    }

    private void normalizeTimelineRecord(List<LogEntry> logEntries) {
        for (LogEntry logEntry : logEntries) {
            JsonObject logEntryJsonWrapper = JsonObject.readFrom(logEntry.getMessage());

            JsonObject logEntryJsonParams = logEntryJsonWrapper.get("message").asObject().get("params").asObject();

            String cat = logEntryJsonParams.get("cat").asString();
            String name = logEntryJsonParams.get("name").asString();
            JsonObject args = logEntryJsonParams.get("args").asObject();
            String pid = logEntryJsonParams.get("pid").asString();
            String ph = logEntryJsonParams.get("ph").asString();

            if (cat == "disabled-by-default-devtools.timeline")) {
                if ((name == "FunctionCall")
                        && (args != null
                        || args.get("data").asString().length() == 0
                        || args.get("data").asObject().get("scriptName").asString() != "InjectedScript")) {
                    ListWrapper.push(normalizedEvents, normalizeEvent(event, {
                            'name': 'script'
                    }));
                } else if ((name == "RecalculateStyles")
                        || (name == "Layout")
                        || (name == "UpdateLayerTree")
                        || (name == "Paint")
                        || (name == "Rasterize")
                        || (name == "CompositeLayers")) {
                    ListWrapper.push(normalizedEvents, normalizeEvent(event, {
                            'name': 'render'
                    }));
                } else if (name == "GCEvent") {
                    var normArgs = {
                            'usedHeapSize': isPresent(args['usedHeapSizeAfter']) ? args['usedHeapSizeAfter'] : args['usedHeapSizeBefore']
                    };
                    if (StringWrapper.equals(event['ph'], 'E')) {
                        normArgs['majorGc'] = isPresent(majorGCPids[pid]) && majorGCPids[pid];
                    }
                    majorGCPids[pid] = false;
                    ListWrapper.push(normalizedEvents, normalizeEvent(event, {
                            'name': 'gc',
                            'args': normArgs
                    }));
                }
            } else if (cat == "blink.console") {
                ListWrapper.push(normalizedEvents, normalizeEvent(event, {
                        'name': name
                }));
            } else if (cat == "v8") {
                if (name == "majorGC") {
                    if (ph == "B'") {
                        majorGCPids[pid] = true;
                    }
                }
            }

        return normalizedEvents;
        }
    }

    private void processTimelineRecord() {
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

        /*switch (logEntry.getString(Config.traceLogging_eventType)) {
            case "I": // Instant Event
            case "X": // Duration Event
                var duration = e.dur || e.tdur || 0;
                this.addData_(e.name, duration / 1000);
                this.runtimePerfMetrics.processRecord({
                        type: e.name,
                    data: e.args ? e.args.data : {},
                    startTime: e.ts / 1000,
                    endTime: (e.ts + duration) / 1000
                }, "tracing");
                break;
            case "B": // Begin Event
                    if (typeof this.eventStacks[e.tid] === "undefined") {
                    this.eventStacks[e.tid] = [];
                }
                this.eventStacks[e.tid].push(e);
                break;
            case "E": // End Event
                if (typeof this.eventStacks[e.tid] === "undefined" || this.eventStacks[e.tid].length === 0) {
                    debug("Encountered an end event that did not have a start event", e);
                } else {
                    var b = this.eventStacks[e.tid].pop();
                    if (b.name !== e.name) {
                        debug("Start and end events dont have the same name", e, b);
                    }
                    this.addData_(e.name, (e.ts - b.ts) / 1000);
                    this.runtimePerfMetrics.processRecord({
                            type: e.name,
                            data: helpers.extend(e.args.endData, b.args.beginData),
                            startTime: b.ts / 1000,
                            endTime: e.ts / 1000
                    }, "tracing");
                }
                break;
        }*/
    }
}
