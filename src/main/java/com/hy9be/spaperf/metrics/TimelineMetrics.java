package com.hy9be.spaperf.metrics;

import org.json.JSONObject;

/**
 * The class parsing the timeline log info
 * Reference (in node.js): https://github.com/axemclion/browser-perf/blob/master/lib/metrics/TimelineMetrics.js
 * Created by hyou on 6/20/15.
 */
public class TimelineMetrics {
    private void processTimelineRecords() {

    }

    /*
    private void processTimelineRecord(JSONObject ) {
        switch (e.ph) {
            case 'I': // Instant Event
            case 'X': // Duration Event
                var duration = e.dur || e.tdur || 0;
                this.addData_(e.name, duration / 1000);
                this.runtimePerfMetrics.processRecord({
                        type: e.name,
                    data: e.args ? e.args.data : {},
                    startTime: e.ts / 1000,
                    endTime: (e.ts + duration) / 1000
                }, 'tracing');
                break;
            case 'B': // Begin Event
                if (typeof this.eventStacks[e.tid] === 'undefined') {
                this.eventStacks[e.tid] = [];
            }
            this.eventStacks[e.tid].push(e);
            break;
            case 'E': // End Event
                if (typeof this.eventStacks[e.tid] === 'undefined' || this.eventStacks[e.tid].length === 0) {
                debug('Encountered an end event that did not have a start event', e);
            } else {
                var b = this.eventStacks[e.tid].pop();
                if (b.name !== e.name) {
                    debug('Start and end events dont have the same name', e, b);
                }
                this.addData_(e.name, (e.ts - b.ts) / 1000);
                this.runtimePerfMetrics.processRecord({
                        type: e.name,
                        data: helpers.extend(e.args.endData, b.args.beginData),
                        startTime: b.ts / 1000,
                        endTime: e.ts / 1000
                }, 'tracing');
            }
            break;
        }
    }
    */
}
