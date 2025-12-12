package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.ConfigReader;

import java.time.Duration;

/**
 * DriverManager - Singleton pattern with ThreadLocal for thread-safe parallel execution
 * Handles WebDriver lifecycle: creation, configuration, and cleanup
 */
public class DriverManager {
    
    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    
    // ThreadLocal ensures each thread gets its own WebDriver instance
    // Critical for parallel test execution
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    
    // Private constructor prevents instantiation
    private DriverManager() {}
    
    /**
     * Gets WebDriver instance for current thread
     * Creates new instance if not exists
     * @return WebDriver instance
     */
    public static WebDriver getDriver() {
        if (driver.get() == null) {
            initializeDriver();
        }
        return driver.get();
    }
    
    /**
     * Initializes WebDriver based on configuration
     * Reads browser type from config.properties
     */
    private static void initializeDriver() {
        String browser = ConfigReader.getBrowser().toLowerCase();
        boolean headless = ConfigReader.isHeadless();
        
        logger.info("Initializing WebDriver: Browser={}, Headless={}", browser, headless);
        
        WebDriver webDriver;
        
        switch (browser) {
            case "chrome":
                webDriver = createChromeDriver(headless);
                break;
            case "firefox":
                webDriver = createFirefoxDriver(headless);
                break;
            case "edge":
                webDriver = createEdgeDriver(headless);
                break;
            default:
                logger.warn("Unknown browser '{}'. Defaulting to Chrome.", browser);
                webDriver = createChromeDriver(headless);
        }
        
        driver.set(webDriver);
        configureDriver(webDriver);
        
        logger.info("WebDriver initialized successfully for thread: {}", Thread.currentThread().getName());
    }
    
    /**
     * Creates Chrome WebDriver with options
     */
    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        
        if (headless) {
            options.addArguments("--headless=new");
            logger.info("Chrome running in headless mode");
        }
        
        // Performance & stability options
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-blink-features=AutomationControlled");
        
        // Turkish language support
        options.addArguments("--lang=tr-TR");
        
        // Disable notifications
        options.addArguments("--disable-notifications");
        
        // Set user agent to avoid bot detection
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        
        // Preferences
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        
        logger.info("Chrome options configured");
        return new ChromeDriver(options);
    }
    
    /**
     * Creates Firefox WebDriver with options
     */
    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        
        if (headless) {
            options.addArguments("--headless");
            logger.info("Firefox running in headless mode");
        }
        
        // Turkish language
        options.addPreference("intl.accept_languages", "tr-TR");
        
        // Disable notifications
        options.addPreference("dom.webnotifications.enabled", false);
        
        logger.info("Firefox options configured");
        return new FirefoxDriver(options);
    }
    
    /**
     * Creates Edge WebDriver with options
     */
    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        
        if (headless) {
            options.addArguments("--headless=new");
            logger.info("Edge running in headless mode");
        }
        
        // Similar to Chrome options
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--lang=tr-TR");
        
        logger.info("Edge options configured");
        return new EdgeDriver(options);
    }
    
    /**
     * Configures driver with timeouts and window settings
     */
    private static void configureDriver(WebDriver driver) {
        // Timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeout()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        
        // Window management
        if (ConfigReader.shouldMaximizeBrowser()) {
            driver.manage().window().maximize();
            logger.info("Browser window maximized");
        } else {
            driver.manage().window().setSize(
                new org.openqa.selenium.Dimension(
                    ConfigReader.getWindowWidth(), 
                    ConfigReader.getWindowHeight()
                )
            );
            logger.info("Browser window size set to {}x{}", 
                ConfigReader.getWindowWidth(), 
                ConfigReader.getWindowHeight());
        }
        
        logger.info("Driver configured: ImplicitWait={}s, PageLoadTimeout={}s", 
            ConfigReader.getImplicitWait(), 
            ConfigReader.getPageLoadTimeout());
    }
    
    /**
     * Quits driver and removes from ThreadLocal
     * Should be called in @AfterMethod or @AfterClass
     */
    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                logger.info("Quitting WebDriver for thread: {}", Thread.currentThread().getName());
                driver.get().quit();
                driver.remove(); // Important: clean up ThreadLocal
                logger.info("WebDriver quit successfully");
            } catch (Exception e) {
                logger.error("Error while quitting WebDriver", e);
            }
        }
    }
    
    /**
     * Navigates to URL
     * @param url - target URL
     */
    public static void navigateToUrl(String url) {
        logger.info("Navigating to URL: {}", url);
        getDriver().get(url);
        logger.info("Navigation completed");
    }
    
    /**
     * Navigates to base URL from config
     */
    public static void navigateToBaseUrl() {
        navigateToUrl(ConfigReader.getBaseUrl());
    }
    
    /**
     * Gets current URL
     * @return current URL
     */
    public static String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }
    
    /**
     * Gets page title
     * @return page title
     */
    public static String getPageTitle() {
        return getDriver().getTitle();
    }
    
    /**
     * Refreshes current page
     */
    public static void refreshPage() {
        logger.info("Refreshing page");
        getDriver().navigate().refresh();
    }
}
