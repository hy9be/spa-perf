package com.hy9be.spaperf.probe;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by hyou on 6/20/15.
 */
public class ChromeDriverPerformanceProbe {
    public ChromeDriverPerformanceProbe() {
        ChromeDriver driver = new ChromeDriver();

        DesiredCapabilities caps = DesiredCapabilities.chrome();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("traceCategories", "browser,devtools.timeline,devtools"); // comma-separated trace categories
        prefs.put("enableTimeline", true);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("perfLoggingPrefs", prefs);
        caps.setCapability(ChromeOptions.CAPABILITY, options);

        for (LogEntry entry : driver.manage().logs().get(LogType.PERFORMANCE)) {
            System.out.println(entry.toString());
        }
    }
}
