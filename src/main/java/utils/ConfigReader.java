package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class to read configuration from config.properties
 * Singleton pattern ensures single Properties instance
 */
public class ConfigReader {
    
    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private static Properties properties;
    private static final String CONFIG_FILE_PATH = "src/test/resources/config.properties";
    
    // Private constructor to prevent instantiation
    private ConfigReader() {}
    
    /**
     * Loads properties file
     * Called once during initialization
     */
    private static void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
                properties.load(fis);
                logger.info("Configuration file loaded successfully from: {}", CONFIG_FILE_PATH);
            } catch (IOException e) {
                logger.error("Failed to load configuration file from: {}", CONFIG_FILE_PATH, e);
                throw new RuntimeException("Configuration file not found at: " + CONFIG_FILE_PATH, e);
            }
        }
    }
    
    /**
     * Gets property value by key
     * @param key - property key
     * @return property value
     */
    public static String getProperty(String key) {
        loadProperties();
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Property key '{}' not found in config.properties", key);
        }
        return value;
    }
    
    /**
     * Gets property value by key with default value
     * @param key - property key
     * @param defaultValue - default value if key not found
     * @return property value or default
     */
    public static String getProperty(String key, String defaultValue) {
        loadProperties();
        return properties.getProperty(key, defaultValue);
    }
    
    // ============================================================
    // Convenience methods for specific configurations
    // ============================================================
    
    public static String getBrowser() {
        return getProperty("browser", "chrome");
    }
    
    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless", "false"));
    }
    
    public static String getBaseUrl() {
        return getProperty("base.url", "https://www.trendyol.com/");
    }
    
    public static int getImplicitWait() {
        return Integer.parseInt(getProperty("implicit.wait", "10"));
    }
    
    public static int getExplicitWait() {
        return Integer.parseInt(getProperty("explicit.wait", "20"));
    }
    
    public static int getPageLoadTimeout() {
        return Integer.parseInt(getProperty("page.load.timeout", "30"));
    }
    
    public static boolean shouldMaximizeBrowser() {
        return Boolean.parseBoolean(getProperty("browser.maximize", "true"));
    }
    
    public static int getWindowWidth() {
        return Integer.parseInt(getProperty("window.width", "1920"));
    }
    
    public static int getWindowHeight() {
        return Integer.parseInt(getProperty("window.height", "1080"));
    }
    
    public static String getReportDirectory() {
        return getProperty("report.directory", "./reports/");
    }
    
    public static String getScreenshotDirectory() {
        return getProperty("screenshot.directory", "./screenshots/");
    }
    
    public static boolean shouldTakeScreenshotOnFailure() {
        return Boolean.parseBoolean(getProperty("screenshot.on.failure", "true"));
    }
    
    public static String getTestSearchKeyword() {
        return getProperty("test.search.keyword", "laptop");
    }
    
    public static int getParallelThreadCount() {
        return Integer.parseInt(getProperty("parallel.thread.count", "3"));
    }
    
    public static int getRetryCount() {
        return Integer.parseInt(getProperty("retry.count", "1"));
    }
    
    public static boolean isCiMode() {
        return Boolean.parseBoolean(getProperty("ci.mode", "false"));
    }
    
    public static String getLogLevel() {
        return getProperty("log.level", "INFO");
    }
    
    /**
     * For debugging - prints all loaded properties
     */
    public static void printAllProperties() {
        loadProperties();
        logger.info("=== Loaded Configuration ===");
        properties.forEach((key, value) -> logger.info("{} = {}", key, value));
        logger.info("============================");
    }
}
