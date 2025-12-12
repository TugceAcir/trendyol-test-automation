package utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ScreenshotHelper - Utility for capturing and saving screenshots
 */
public class ScreenshotHelper {
    
    private static final Logger logger = LogManager.getLogger(ScreenshotHelper.class);
    private static final String SCREENSHOT_DIR = ConfigReader.getScreenshotDirectory();
    
    /**
     * Captures screenshot and saves to configured directory
     * @param driver - WebDriver instance
     * @param testName - test name for file naming
     * @return screenshot file path or null if failed
     */
    public static String captureScreenshot(WebDriver driver, String testName) {
        // Ensure screenshot directory exists
        createScreenshotDirectory();
        
        // Generate unique filename
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
        String fileName = testName + "_" + timestamp + ".png";
        String screenshotPath = SCREENSHOT_DIR + fileName;
        
        try {
            // Capture screenshot
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            File destFile = new File(screenshotPath);
            
            // Copy to destination
            FileUtils.copyFile(sourceFile, destFile);
            
            logger.info("Screenshot captured successfully: {}", screenshotPath);
            return screenshotPath;
            
        } catch (IOException e) {
            logger.error("Failed to capture screenshot for test: {}", testName, e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while capturing screenshot", e);
            return null;
        }
    }
    
    /**
     * Captures screenshot with custom file name
     * @param driver - WebDriver instance
     * @param fileName - custom file name (without extension)
     * @return screenshot file path or null if failed
     */
    public static String captureScreenshotWithCustomName(WebDriver driver, String fileName) {
        createScreenshotDirectory();
        
        String screenshotPath = SCREENSHOT_DIR + fileName + ".png";
        
        try {
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            File destFile = new File(screenshotPath);
            
            FileUtils.copyFile(sourceFile, destFile);
            
            logger.info("Screenshot captured: {}", screenshotPath);
            return screenshotPath;
            
        } catch (Exception e) {
            logger.error("Failed to capture screenshot", e);
            return null;
        }
    }
    
    /**
     * Creates screenshot directory if it doesn't exist
     */
    private static void createScreenshotDirectory() {
        File directory = new File(SCREENSHOT_DIR);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                logger.info("Screenshot directory created: {}", SCREENSHOT_DIR);
            } else {
                logger.error("Failed to create screenshot directory: {}", SCREENSHOT_DIR);
            }
        }
    }
    
    /**
     * Captures screenshot as byte array (useful for CI/CD integrations)
     * @param driver - WebDriver instance
     * @return screenshot as byte array
     */
    public static byte[] captureScreenshotAsBytes(WebDriver driver) {
        try {
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            return takesScreenshot.getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot as bytes", e);
            return null;
        }
    }
    
    /**
     * Captures screenshot as Base64 string (useful for embedding in reports)
     * @param driver - WebDriver instance
     * @return screenshot as Base64 string
     */
    public static String captureScreenshotAsBase64(WebDriver driver) {
        try {
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            return takesScreenshot.getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot as Base64", e);
            return null;
        }
    }
}
