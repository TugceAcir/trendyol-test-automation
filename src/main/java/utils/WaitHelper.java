package utils;

import constants.Timeouts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * WaitHelper - Smart explicit wait strategies
 * 
 * WHY THIS CLASS EXISTS:
 * - Trendyol.com uses heavy JavaScript and AJAX
 * - Elements load dynamically (lazy loading, infinite scroll)
 * - Thread.sleep() is bad practice (wastes time, not reliable)
 * - We need intelligent waits that check conditions repeatedly
 * 
 * WHAT IT PROVIDES:
 * - Wait for element to be visible
 * - Wait for element to be clickable
 * - Wait for element to disappear
 * - Wait for page to be ready
 * - Custom wait conditions
 */
public class WaitHelper {
    
    private static final Logger logger = LogManager.getLogger(WaitHelper.class);
    
    /**
     * Wait for element to be visible
     * 
     * USE CASE: Element exists in DOM but not yet visible (display:none, opacity:0)
     * EXAMPLE: Product images loading, modal popups appearing
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to wait for
     * @param timeoutInSeconds - max wait time
     * @return WebElement if visible, throws TimeoutException otherwise
     */
    public static WebElement waitForElementVisible(WebDriver driver, WebElement element, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for element to be visible (timeout: {}s)", timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            WebElement visibleElement = wait.until(ExpectedConditions.visibilityOf(element));
            logger.debug("Element is now visible");
            return visibleElement;
        } catch (TimeoutException e) {
            logger.error("Element not visible within {} seconds", timeoutInSeconds);
            throw e;
        }
    }
    
    /**
     * Wait for element located by By locator to be visible
     * 
     * USE CASE: When you have a locator, not a WebElement yet
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @param timeoutInSeconds - max wait time
     * @return WebElement if visible
     */
    public static WebElement waitForElementVisible(WebDriver driver, By locator, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for element {} to be visible (timeout: {}s)", locator, timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            logger.debug("Element {} is now visible", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element {} not visible within {} seconds", locator, timeoutInSeconds);
            throw e;
        }
    }
    
    /**
     * Wait for element to be clickable
     * 
     * USE CASE: Element is visible but not yet clickable (disabled, covered by overlay)
     * EXAMPLE: Button disabled until form validation passes
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to wait for
     * @param timeoutInSeconds - max wait time
     * @return WebElement if clickable
     */
    public static WebElement waitForElementClickable(WebDriver driver, WebElement element, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for element to be clickable (timeout: {}s)", timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            WebElement clickableElement = wait.until(ExpectedConditions.elementToBeClickable(element));
            logger.debug("Element is now clickable");
            return clickableElement;
        } catch (TimeoutException e) {
            logger.error("Element not clickable within {} seconds", timeoutInSeconds);
            throw e;
        }
    }
    
    /**
     * Wait for element located by By locator to be clickable
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @param timeoutInSeconds - max wait time
     * @return WebElement if clickable
     */
    public static WebElement waitForElementClickable(WebDriver driver, By locator, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for element {} to be clickable (timeout: {}s)", locator, timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            logger.debug("Element {} is now clickable", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element {} not clickable within {} seconds", locator, timeoutInSeconds);
            throw e;
        }
    }
    
    /**
     * Wait for element to be invisible (disappear)
     * 
     * USE CASE: Loading spinners, progress bars, modal overlays that need to disappear
     * EXAMPLE: Wait for "Loading..." overlay to disappear before interacting
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @param timeoutInSeconds - max wait time
     * @return true if invisible, false otherwise
     */
    public static boolean waitForElementInvisible(WebDriver driver, By locator, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for element {} to be invisible (timeout: {}s)", locator, timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            boolean isInvisible = wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            logger.debug("Element {} is now invisible", locator);
            return isInvisible;
        } catch (TimeoutException e) {
            logger.warn("Element {} still visible after {} seconds", locator, timeoutInSeconds);
            return false;
        }
    }
    
    /**
     * Wait for element to be present in DOM
     * 
     * USE CASE: Element not yet in DOM (AJAX loading)
     * NOTE: Element may be present but not visible!
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @param timeoutInSeconds - max wait time
     * @return WebElement if present
     */
    public static WebElement waitForElementPresent(WebDriver driver, By locator, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for element {} to be present in DOM (timeout: {}s)", locator, timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            logger.debug("Element {} is now present in DOM", locator);
            return element;
        } catch (TimeoutException e) {
            logger.error("Element {} not present in DOM within {} seconds", locator, timeoutInSeconds);
            throw e;
        }
    }
    
    /**
     * Wait for all elements to be visible
     * 
     * USE CASE: Product list loading, search results appearing
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @param timeoutInSeconds - max wait time
     * @return List of WebElements
     */
    public static List<WebElement> waitForAllElementsVisible(WebDriver driver, By locator, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for all elements {} to be visible (timeout: {}s)", locator, timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            List<WebElement> elements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
            logger.debug("All elements {} are now visible (count: {})", locator, elements.size());
            return elements;
        } catch (TimeoutException e) {
            logger.error("Elements {} not all visible within {} seconds", locator, timeoutInSeconds);
            throw e;
        }
    }
    
    /**
     * Wait for text to be present in element
     * 
     * USE CASE: Waiting for dynamic text update (cart count, price update)
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @param text - expected text
     * @param timeoutInSeconds - max wait time
     * @return true if text present
     */
    public static boolean waitForTextToBePresentInElement(WebDriver driver, By locator, String text, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for text '{}' in element {} (timeout: {}s)", text, locator, timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            boolean textPresent = wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
            logger.debug("Text '{}' is now present in element {}", text, locator);
            return textPresent;
        } catch (TimeoutException e) {
            logger.warn("Text '{}' not present in element {} after {} seconds", text, locator, timeoutInSeconds);
            return false;
        }
    }
    
    /**
     * Wait for page to be fully loaded (document.readyState = complete)
     * 
     * USE CASE: After navigation, before interacting with page
     * IMPORTANT: Trendyol uses heavy JavaScript, so this is crucial
     * 
     * @param driver - WebDriver instance
     * @param timeoutInSeconds - max wait time
     */
    public static void waitForPageLoad(WebDriver driver, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for page to load (timeout: {}s)", timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
            logger.debug("Page is fully loaded");
        } catch (TimeoutException e) {
            logger.warn("Page not fully loaded within {} seconds", timeoutInSeconds);
        }
    }
    
    /**
     * Wait for AJAX/jQuery calls to complete
     * 
     * USE CASE: Trendyol uses AJAX extensively for dynamic content
     * EXAMPLE: Filters applied via AJAX, cart updates
     * 
     * @param driver - WebDriver instance
     * @param timeoutInSeconds - max wait time
     */
    public static void waitForAjaxToComplete(WebDriver driver, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for AJAX to complete (timeout: {}s)", timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(webDriver -> {
                JavascriptExecutor js = (JavascriptExecutor) webDriver;
                // Check if jQuery is defined and active
                return (Boolean) js.executeScript("return (typeof jQuery != 'undefined') ? jQuery.active == 0 : true");
            });
            logger.debug("AJAX calls completed");
        } catch (TimeoutException e) {
            logger.warn("AJAX still active after {} seconds", timeoutInSeconds);
        } catch (Exception e) {
            logger.debug("jQuery not present on page, skipping AJAX wait");
        }
    }
    
    /**
     * FluentWait - Advanced wait with custom polling and exceptions to ignore
     * 
     * USE CASE: When you need fine control over wait behavior
     * EXAMPLE: Waiting for specific condition with custom polling interval
     * 
     * @param driver - WebDriver instance
     * @param timeoutInSeconds - max wait time
     * @param pollingInMillis - how often to check condition
     * @return FluentWait instance
     */
    public static Wait<WebDriver> getFluentWait(WebDriver driver, int timeoutInSeconds, int pollingInMillis) {
        logger.debug("Creating FluentWait (timeout: {}s, polling: {}ms)", timeoutInSeconds, pollingInMillis);
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutInSeconds))
                .pollingEvery(Duration.ofMillis(pollingInMillis))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }
    
    /**
     * Custom wait condition
     * 
     * USE CASE: When built-in conditions aren't enough
     * EXAMPLE: Wait for element attribute to change, wait for URL to contain text
     * 
     * @param driver - WebDriver instance
     * @param condition - custom condition function
     * @param timeoutInSeconds - max wait time
     * @return result of condition function
     */
    public static <T> T waitForCustomCondition(WebDriver driver, Function<WebDriver, T> condition, int timeoutInSeconds) {
        try {
            logger.debug("Waiting for custom condition (timeout: {}s)", timeoutInSeconds);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            T result = wait.until(condition);
            logger.debug("Custom condition met");
            return result;
        } catch (TimeoutException e) {
            logger.error("Custom condition not met within {} seconds", timeoutInSeconds);
            throw e;
        }
    }
    
    /**
     * Hard wait (Thread.sleep) - USE SPARINGLY!
     * 
     * WARNING: This is generally BAD practice!
     * Only use when absolutely necessary (e.g., animations, deliberate delays)
     * 
     * @param milliseconds - time to sleep
     */
    public static void hardWait(int milliseconds) {
        try {
            logger.warn("Using hard wait (Thread.sleep) for {}ms - avoid if possible!", milliseconds);
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted during hard wait", e);
            Thread.currentThread().interrupt();
        }
    }
    
    // ============================================================
    // CONVENIENCE METHODS - Use default timeouts from constants
    // ============================================================
    
    public static WebElement waitForElementVisible(WebDriver driver, WebElement element) {
        return waitForElementVisible(driver, element, Timeouts.ELEMENT_VISIBLE);
    }
    
    public static WebElement waitForElementVisible(WebDriver driver, By locator) {
        return waitForElementVisible(driver, locator, Timeouts.ELEMENT_VISIBLE);
    }
    
    public static WebElement waitForElementClickable(WebDriver driver, WebElement element) {
        return waitForElementClickable(driver, element, Timeouts.ELEMENT_CLICKABLE);
    }
    
    public static WebElement waitForElementClickable(WebDriver driver, By locator) {
        return waitForElementClickable(driver, locator, Timeouts.ELEMENT_CLICKABLE);
    }
    
    public static boolean waitForElementInvisible(WebDriver driver, By locator) {
        return waitForElementInvisible(driver, locator, Timeouts.ELEMENT_INVISIBLE);
    }
    
    public static void waitForPageLoad(WebDriver driver) {
        waitForPageLoad(driver, Timeouts.PAGE_LOAD_TIMEOUT);
    }
    
    public static void waitForAjaxToComplete(WebDriver driver) {
        waitForAjaxToComplete(driver, Timeouts.AJAX_TIMEOUT);
    }
}
