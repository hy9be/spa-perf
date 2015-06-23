package com.hy9be.spaperf.driver;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;

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
        //LoggingPreferences logPrefs = new LoggingPreferences();
        //logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        //caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        // Enable timeline tracing
        Map<String, Object> chromeOptions = new HashMap<String, Object>();
        Map<String, String> perfLoggingPrefs = new HashMap<String, String>();
        perfLoggingPrefs.put("traceCategories", "blink.console, disabled-by-default-devtools.timeline");
        chromeOptions.put("perfLoggingPrefs", perfLoggingPrefs);
        //chromeOptions.put("debuggerAddress", "127.0.0.1:10134");
        caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        return caps;
    }

    // Detach the driver but do not close the browser session (vs. this.close())
    public void detach() {
        this.close();
    }
}
