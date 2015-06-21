package com.hy9be.spaperf.output;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;

import java.util.Date;
import java.util.List;

/**
 * Created by hyou on 6/20/15.
 */
public class ConsolePrinter {
    public void printLogToConsole(ChromeDriver driver, String type) {
        // Get the logs
        // ChromeDriver - format: message: "[{method:'Tracing.dataCollected', params:{cat:...,pid:...}}]"
        List<LogEntry> entries = driver.manage().logs().get(type).getAll();

        System.out.println(entries.size() + " " + type + " log entries found");
        for (LogEntry entry : entries) {
            System.out.println(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
        }
    }
}
