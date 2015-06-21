package com.hy9be.spaperf.driver;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("traceCategories", "blink.console, disabled-by-default-devtools.timeline");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("perfLoggingPrefs", prefs);
        caps.setCapability(ChromeOptions.CAPABILITY, options);

        return caps;
    }

    public void detach() {
        this.close();
    }
}
