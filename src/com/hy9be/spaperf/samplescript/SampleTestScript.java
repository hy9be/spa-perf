package com.hy9be.spaperf.samplescript;

import com.hy9be.spaperf.driver.SPAPerfChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class SampleTestScript {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/Users/hyou/Documents/chromedriver");

        TestGoogleSearch();
    }

    public static void TestGoogleSearch() {
        //google.com
        SPAPerfChromeDriver driver = new SPAPerfChromeDriver();
        driver.get("https://news.google.com/");
        WebElement element = driver.findElement(By.name("q"));
        element.sendKeys("Selenium Conference");
        element.sendKeys(Keys.ENTER);
        driver.detach();
    }

    public static void TestUnsplashCom () {
        //https://730.unsplash.com/#/intro
    }
}