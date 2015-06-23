package com.hy9be.spaperf.samplescript;

import com.hy9be.spaperf.driver.SPAPerfChromeDriver;
import com.hy9be.spaperf.output.ConsolePrinter;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogType;

import java.net.MalformedURLException;

public class SampleTestScript {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/Users/hyou/Documents/chromedriver");

        try {
            TestGoogleSearch();
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
}