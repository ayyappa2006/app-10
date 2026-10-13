package com.civicbin.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Logger;

public class BaseAppiumTest {
    protected AppiumDriver driver;
    protected WebDriverWait wait;
    protected Properties properties;
    private static final Logger logger = Logger.getLogger(BaseAppiumTest.class.getName());

    @BeforeMethod
    public void setUp() throws IOException {
        properties = loadProperties();
        UiAutomator2Options options = new UiAutomator2Options()
                .setChromedriverPort(9515)
                .setPlatformName("Android")
                .setAutomationName("UIAutomator2")
                .setPlatformVersion(properties.getProperty("PLATFORM_VERSION"))
                .setDeviceName(properties.getProperty("DEVICE_NAME"))
                .setAppPackage(properties.getProperty("APP_PACKAGE"))
                .setAppActivity(properties.getProperty("APP_ACTIVITY"))
                .setAutoGrantPermissions(true)
                .setAutoLaunchApp(true);

        String appPath = properties.getProperty("APP_PATH");
        if (new File(appPath).exists()) {
            options.setApp(new File(appPath).getAbsolutePath());
        }

        try {
            driver = new AndroidDriver(new URL(properties.getProperty("APPIUM_SERVER_URL")), options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(
                    Long.parseLong(properties.getProperty("EXPLICIT_WAIT", "20"))));
            logger.info("Android Driver initialized successfully");
        } catch (Exception e) {
            logger.severe("Failed to initialize Android Driver: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            logger.info("Driver quit successfully");
        }
    }

    protected Properties loadProperties() throws IOException {
        Properties props = new Properties();
        String configPath = "config/appium.properties";
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
        }
        return props;
    }

    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement waitForClickableElement(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitForClickableElement(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement element = waitForElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForElement(locator).getText();
    }

    protected boolean isElementVisible(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected By resourceId(String id) {
        return By.id(id);
    }

    protected void scrollDown() {
        driver.executeScript("window.scrollBy(0, 250);");
    }

    protected void scrollUp() {
        driver.executeScript("window.scrollBy(0, -250);");
    }

    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void takeScreenshot(String filename) {
        try {
            String screenshotPath = properties.getProperty("REPORT_PATH") + "/screenshots/";
            new File(screenshotPath).mkdirs();
            File screenshot = ((io.appium.java_client.ScreenshotAs) driver)
                    .getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            org.apache.commons.io.FileUtils.copyFile(screenshot, 
                    new File(screenshotPath + filename + ".png"));
            logger.info("Screenshot saved: " + filename);
        } catch (Exception e) {
            logger.warning("Failed to take screenshot: " + e.getMessage());
        }
    }
}
