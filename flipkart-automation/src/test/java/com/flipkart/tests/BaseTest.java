package com.flipkart.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.flipkart.utils.ScreenshotUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseTest {

    protected WebDriver driver;
    protected ExtentTest test;
    private static ExtentReports extent;

    @BeforeSuite(alwaysRun = true)
    public void setUpReport() throws Exception {
        Path reportDir = Paths.get("target", "extent-report");
        Files.createDirectories(reportDir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path reportFile = reportDir.resolve("ExtentReport_" + timestamp + ".html");

        ExtentSparkReporter spark = new ExtentSparkReporter(reportFile.toFile());
        spark.config().setDocumentTitle("Flipkart Automation Report");
        spark.config().setReportName("Flipkart Search, Filter, Sort and Cart Validation");
        spark.config().setTheme(com.aventstack.extentreports.reporter.configuration.Theme.STANDARD);

        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--disable-notifications",
                "--start-maximized",
                "--disable-blink-features=AutomationControlled",
                "--disable-dev-shm-usage",
                "--no-sandbox"
        );
        driver = new ChromeDriver(options);
        test = extent.createTest(method.getName());
        test.assignAuthor("Automation Team");
        test.assignCategory("Flipkart");
        test.log(Status.INFO, "Browser launched");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() != ITestResult.SUCCESS) {
                ScreenshotUtil.takeAndAttach(driver, test, "FAILED_or_SKIPPED_" + result.getName(), "Failure Screenshot");
                test.log(Status.FAIL, result.getThrowable() != null ? result.getThrowable().getMessage() : "Test failed");
            } else {
                test.log(Status.PASS, "Test passed");
            }
        } catch (Exception ignored) {
            // browser may already be closed; ignore this diagnostic failure
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownReport() {
        if (extent != null) {
            extent.flush();
        }
    }
}
