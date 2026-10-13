package com.flipkart.utils;

import com.aventstack.extentreports.ExtentTest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScreenshotUtil {

    public static void takeAndAttach(WebDriver driver, ExtentTest test, String name, String title) {
        try {
            Path dir = Paths.get(System.getProperty("user.dir"), "target", "screenshots");
            Files.createDirectories(dir);
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path file = dir.resolve(name + ".png");
            Files.write(file, bytes);
            test.addScreenCaptureFromBase64String(Base64.getEncoder().encodeToString(bytes), title);
            Log.info("Screenshot saved: " + file);
        } catch (Exception e) {
            Log.warn("Could not take screenshot " + name + ": " + e.getMessage());
            test.warning("Could not attach screenshot " + name + ": " + e.getMessage());
        }
    }
}
