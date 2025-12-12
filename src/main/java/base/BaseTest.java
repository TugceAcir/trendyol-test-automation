package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.ConfigReader;
import utils.ScreenshotHelper;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * BaseTest - Parent class for all test classes
 * Provides:
 * - WebDriver setup and teardown
 * - ExtentReports integration
 * - Screenshot on failure
 * - Logging
 */
public class BaseTest {
    
    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;
    
    // ExtentReports - thread-safe for parallel execution
    private static ExtentReports extent;
    protected static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    
    /**
     * BeforeSuite - Runs once before entire test suite
     * Initializes ExtentReports
     */
    @BeforeSuite
    public void beforeSuite() {
        logger.info("=== TEST SUITE STARTED ===");
        initializeExtentReports();
        logger.info("Configuration loaded:");
        logger.info("Browser: {}", ConfigReader.getBrowser());
        logger.info("Base URL: {}", ConfigReader.getBaseUrl());
        logger.info("Headless: {}", ConfigReader.isHeadless());
    }
    
    /**
     * BeforeClass - Runs once before each test class
     */
    @BeforeClass
    public void beforeClass() {
        logger.info("=== TEST CLASS STARTED: {} ===", this.getClass().getSimpleName());
    }
    
    /**
     * BeforeMethod - Runs before each test method
     * Initializes WebDriver and navigates to base URL
     */
    @BeforeMethod
    public void beforeMethod(Method method) {
        logger.info("=== TEST STARTED: {} ===", method.getName());
        
        // Create ExtentTest for this test
        ExtentTest test = extent.createTest(
            method.getName(),
            method.getAnnotation(Test.class).description()
        );
        extentTest.set(test);
        
        // Initialize WebDriver
        driver = DriverManager.getDriver();
        
        // Navigate to base URL
        DriverManager.navigateToBaseUrl();
        
        logger.info("Driver initialized and navigated to base URL");
        extentTest.get().info("Test execution started");
        extentTest.get().info("Browser: " + ConfigReader.getBrowser());
        extentTest.get().info("URL: " + ConfigReader.getBaseUrl());
    }
    
    /**
     * AfterMethod - Runs after each test method
     * Captures screenshot on failure and quits driver
     */
    @AfterMethod
    public void afterMethod(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        // Check test result
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("TEST FAILED: {}", testName);
            extentTest.get().fail("Test Failed: " + result.getThrowable());
            
            // Capture screenshot on failure
            if (ConfigReader.shouldTakeScreenshotOnFailure()) {
                String screenshotPath = ScreenshotHelper.captureScreenshot(driver, testName);
                if (screenshotPath != null) {
                    try {
                        extentTest.get().addScreenCaptureFromPath(screenshotPath);
                        logger.info("Screenshot captured: {}", screenshotPath);
                    } catch (Exception e) {
                        logger.error("Failed to attach screenshot to report", e);
                    }
                }
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("TEST PASSED: {}", testName);
            extentTest.get().pass("Test Passed");
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.warn("TEST SKIPPED: {}", testName);
            extentTest.get().skip("Test Skipped: " + result.getThrowable());
        }
        
        // Quit driver
        DriverManager.quitDriver();
        logger.info("=== TEST ENDED: {} ===", testName);
    }
    
    /**
     * AfterClass - Runs once after each test class
     */
    @AfterClass
    public void afterClass() {
        logger.info("=== TEST CLASS ENDED: {} ===", this.getClass().getSimpleName());
    }
    
    /**
     * AfterSuite - Runs once after entire test suite
     * Flushes ExtentReports
     */
    @AfterSuite
    public void afterSuite() {
        if (extent != null) {
            extent.flush();
            logger.info("ExtentReports flushed");
        }
        logger.info("=== TEST SUITE ENDED ===");
    }
    
    /**
     * Initializes ExtentReports
     */
    private void initializeExtentReports() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String reportPath = ConfigReader.getReportDirectory() + "Trendyol_Test_Report_" + timestamp + ".html";
        
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        
        // Report configuration
        sparkReporter.config().setDocumentTitle("Trendyol Automation Test Report");
        sparkReporter.config().setReportName("Trendyol Test Execution Results");
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        sparkReporter.config().setEncoding("UTF-8");
        
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        
        // System information
        extent.setSystemInfo("Application", "Trendyol.com");
        extent.setSystemInfo("Environment", "Production");
        extent.setSystemInfo("Browser", ConfigReader.getBrowser());
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Tester", System.getProperty("user.name"));
        
        logger.info("ExtentReports initialized: {}", reportPath);
    }
    
    /**
     * Helper method to get ExtentTest instance
     * @return current thread's ExtentTest
     */
    protected ExtentTest getExtentTest() {
        return extentTest.get();
    }
    
    /**
     * Helper method to log info to both logger and ExtentReports
     */
    protected void logInfo(String message) {
        logger.info(message);
        extentTest.get().info(message);
    }
    
    /**
     * Helper method to log pass to both logger and ExtentReports
     */
    protected void logPass(String message) {
        logger.info("PASS: {}", message);
        extentTest.get().pass(message);
    }
    
    /**
     * Helper method to log fail to both logger and ExtentReports
     */
    protected void logFail(String message) {
        logger.error("FAIL: {}", message);
        extentTest.get().fail(message);
    }
}
