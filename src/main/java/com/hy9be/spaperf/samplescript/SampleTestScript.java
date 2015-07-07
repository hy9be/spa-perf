package com.hy9be.spaperf.samplescript;

import com.hy9be.spaperf.driver.SPAPerfChromeDriver;
import com.hy9be.spaperf.output.ConsolePrinter;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;

import java.net.MalformedURLException;

public class SampleTestScript {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/Users/hyou/Documents/chromedriver");

        try {
            TestMstrVI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void TestGoogleSearch() throws MalformedURLException {
        //google.com
        SPAPerfChromeDriver driver = new SPAPerfChromeDriver();

        driver.get("https://news.google.com/");

        WebElement element = driver.findElement(By.name("q"));
        element.sendKeys("Selenium Conference");
        element.sendKeys(Keys.ENTER);

        ConsolePrinter printer = new ConsolePrinter();

        printer.printLogToConsole(driver, LogType.PERFORMANCE);

        //driver.detach();
        driver.close();
    }

    public static void TestUnsplashCom () {
        //https://730.unsplash.com/#/intro
    }

    public static void TestMstrVI() throws Exception {
        SPAPerfChromeDriver driver = new SPAPerfChromeDriver();
        Actions action = new Actions(driver);

        //preScript: login, loading VI shell and e.g. (performance counters are not recorded)
        driver.get("http://10.15.69.127/MicroStrategy_10_GA/servlet/mstrWeb");
        WebElement element = driver.findElement(By.cssSelector("td:nth-child(1) > div > table > tbody > tr > td.mstrLargeIconViewItemText > a.mstrLargeIconViewItemLink"));
        action.click(element).perform();
        element = driver.findElement(By.cssSelector("#Uid"));
        action.sendKeys(element,"administrator");
        element =  driver.findElement(By.cssSelector("input[class='mstrButton'][value='Login']"));
        action.click(element).perform();
        element = driver.findElement(By.cssSelector("a[class='mstrLink'][title='My Reports']"));
        action.click(element).perform();
        Thread.sleep(2000);

        element = driver.findElement(By.cssSelector("a[class='mstrLink'][title='VIPerformance']"));
        action.click(element).perform();
        Thread.sleep(2000);

        element = driver.findElement(By.cssSelector("div[class='item ic-VIHeatMapVisualizationStyle']> div > div"));
        action.click(element).perform();

        String fileName = "/Users/hyou/Documents/perfData.csv";
        // Enable performance log/profiling
        driver.enablePerfProfiling(fileName);

        //actionScript
        //action 1
        String action1 = "Add Origin to Grouping";
        element = driver.findElement(By.xpath("//span[contains(.,'Origin')]/.."));
        action.doubleClick(element).perform();

        //collect perf counters
        driver.collectPerfData(action1);

        //action 2
        String action2 = "Add Depdelay to Color By";
        element = driver.findElement(By.xpath("//span[contains(.,'Depdelay')]/.."));
        action.doubleClick(element).perform();

        driver.collectPerfData(action2);
        //driver.stopPerformanceProfiling();

        //action 3
        String action3 = "Add Arrdelay to Size By";
        element = driver.findElement(By.xpath("//span[contains(.,'Arrdelay')]/.."));
        action.doubleClick(element).perform();

        //collect perf counters
        driver.collectPerfData(action3);

        //action 4
        String action4 = "Add Fightnumber to Grouping";
        element = driver.findElement(By.xpath("//span[contains(.,'Flightnum')]/.."));
        WebElement ele2 = driver.findElement(By.cssSelector("div[class='mstrmojo-VIPanel mstrmojo-VIPanelPortlet']>div:nth-child(2)>div>div"));
        action.dragAndDrop(element, ele2).perform();

        Thread.sleep(25000);


        //collect perf counters
        driver.collectPerfData(action4);

        //driver.detach();
        driver.close();
    }
}