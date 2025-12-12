package utils;

import constants.Timeouts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

/**
 * ElementHelper - Safe element interaction utilities with retry mechanism
 * 
 * WHY THIS CLASS EXISTS:
 * - Raw Selenium methods can fail (StaleElementException, ElementNotInteractableException)
 * - Trendyol has overlays, popups, dynamic content that can block clicks
 * - We need retry logic and smart error handling
 * 
 * WHAT IT PROVIDES:
 * - Safe click (with retry)
 * - Safe send keys (with clear + retry)
 * - Safe getText (handles empty elements)
 * - Scroll to element (for elements below fold)
 * - JavaScript click (when normal click fails)
 * - Hover actions
 */
public class ElementHelper {
    
    private static final Logger logger = LogManager.getLogger(ElementHelper.class);
    
    /**
     * Safe click with retry mechanism
     * 
     * WHY NEEDED: Element might be:
     * - Covered by overlay/popup
     * - Stale (DOM updated)
     * - Not yet clickable
     * 
     * STRATEGY:
     * 1. Wait for element to be clickable
     * 2. Try click
     * 3. If fails, retry up to 3 times
     * 4. If still fails, try JavaScript click
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to click
     */
    public static void safeClick(WebDriver driver, WebElement element) {
        int attempts = 0;
        while (attempts < Timeouts.RETRY_ATTEMPTS) {
            try {
                logger.debug("Attempting safe click (attempt {})", attempts + 1);
                
                // Wait for element to be clickable
                WaitHelper.waitForElementClickable(driver, element, Timeouts.ELEMENT_CLICKABLE);
                
                // Scroll to element (in case it's below fold)
                scrollToElement(driver, element);
                
                // Click
                element.click();
                
                logger.debug("Click successful");
                return;
                
            } catch (ElementClickInterceptedException e) {
                logger.warn("Click intercepted by overlay (attempt {})", attempts + 1);
                attempts++;
                
                if (attempts >= Timeouts.RETRY_ATTEMPTS) {
                    logger.warn("Click still intercepted, trying JavaScript click");
                    clickViaJavaScript(driver, element);
                    return;
                }
                
                WaitHelper.hardWait(Timeouts.RETRY_DELAY);
                
            } catch (StaleElementReferenceException e) {
                logger.warn("Stale element (attempt {})", attempts + 1);
                attempts++;
                
                if (attempts >= Timeouts.RETRY_ATTEMPTS) {
                    logger.error("Element still stale after {} attempts", Timeouts.RETRY_ATTEMPTS);
                    throw e;
                }
                
                WaitHelper.hardWait(Timeouts.RETRY_DELAY);
                
            } catch (Exception e) {
                logger.error("Unexpected error during click", e);
                throw e;
            }
        }
    }
    
    /**
     * Click using By locator (finds element first, then clicks)
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     */
    public static void safeClick(WebDriver driver, By locator) {
        WebElement element = WaitHelper.waitForElementClickable(driver, locator);
        safeClick(driver, element);
    }
    
    /**
     * Click via JavaScript
     * 
     * USE CASE: When normal click fails due to overlay/popup
     * NOTE: Bypasses visibility checks - use as last resort!
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to click
     */
    public static void clickViaJavaScript(WebDriver driver, WebElement element) {
        try {
            logger.debug("Clicking via JavaScript");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", element);
            logger.debug("JavaScript click successful");
        } catch (Exception e) {
            logger.error("JavaScript click failed", e);
            throw e;
        }
    }
    
    /**
     * Safe send keys with clear + retry
     * 
     * WHY NEEDED:
     * - Field might not be clear
     * - Field might be disabled/readonly
     * - StaleElementException can occur
     * 
     * STRATEGY:
     * 1. Wait for element to be visible
     * 2. Clear existing text
     * 3. Send keys
     * 4. Verify text entered (optional)
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to type into
     * @param text - text to enter
     */
    public static void safeSendKeys(WebDriver driver, WebElement element, String text) {
        int attempts = 0;
        while (attempts < Timeouts.RETRY_ATTEMPTS) {
            try {
                logger.debug("Attempting safe send keys: '{}' (attempt {})", text, attempts + 1);
                
                // Wait for element to be visible
                WaitHelper.waitForElementVisible(driver, element, Timeouts.ELEMENT_VISIBLE);
                
                // Clear existing text
                element.clear();
                
                // Send keys
                element.sendKeys(text);
                
                logger.debug("Send keys successful");
                return;
                
            } catch (StaleElementReferenceException e) {
                logger.warn("Stale element during send keys (attempt {})", attempts + 1);
                attempts++;
                
                if (attempts >= Timeouts.RETRY_ATTEMPTS) {
                    logger.error("Element still stale after {} attempts", Timeouts.RETRY_ATTEMPTS);
                    throw e;
                }
                
                WaitHelper.hardWait(Timeouts.RETRY_DELAY);
                
            } catch (Exception e) {
                logger.error("Unexpected error during send keys", e);
                throw e;
            }
        }
    }
    
    /**
     * Send keys using By locator
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @param text - text to enter
     */
    public static void safeSendKeys(WebDriver driver, By locator, String text) {
        WebElement element = WaitHelper.waitForElementVisible(driver, locator);
        safeSendKeys(driver, element, text);
    }
    
    /**
     * Safe getText with null/empty handling
     * 
     * WHY NEEDED:
     * - Element might not have text
     * - Element might be invisible but present
     * - getText() can return null
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to get text from
     * @return text content (empty string if no text)
     */
    public static String safeGetText(WebDriver driver, WebElement element) {
        try {
            logger.debug("Getting text from element");
            
            // Wait for element to be visible
            WaitHelper.waitForElementVisible(driver, element, Timeouts.ELEMENT_VISIBLE);
            
            String text = element.getText();
            
            // Handle null or empty
            if (text == null || text.trim().isEmpty()) {
                logger.debug("Element has no visible text, trying getAttribute('textContent')");
                text = element.getAttribute("textContent");
            }
            
            logger.debug("Retrieved text: '{}'", text);
            return text != null ? text.trim() : "";
            
        } catch (Exception e) {
            logger.error("Error getting text from element", e);
            return "";
        }
    }
    
    /**
     * Get text using By locator
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @return text content
     */
    public static String safeGetText(WebDriver driver, By locator) {
        WebElement element = WaitHelper.waitForElementVisible(driver, locator);
        return safeGetText(driver, element);
    }
    
    /**
     * Get attribute value
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement
     * @param attributeName - attribute name
     * @return attribute value
     */
    public static String getAttribute(WebDriver driver, WebElement element, String attributeName) {
        try {
            logger.debug("Getting attribute '{}' from element", attributeName);
            WaitHelper.waitForElementVisible(driver, element, Timeouts.ELEMENT_VISIBLE);
            String value = element.getAttribute(attributeName);
            logger.debug("Attribute '{}' value: '{}'", attributeName, value);
            return value != null ? value : "";
        } catch (Exception e) {
            logger.error("Error getting attribute '{}'", attributeName, e);
            return "";
        }
    }
    
    /**
     * Check if element is displayed
     * 
     * @param element - WebElement
     * @return true if displayed, false otherwise
     */
    public static boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            logger.debug("Element not displayed");
            return false;
        }
    }
    
    /**
     * Check if element is enabled
     * 
     * @param element - WebElement
     * @return true if enabled, false otherwise
     */
    public static boolean isEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            logger.debug("Element not enabled");
            return false;
        }
    }
    
    /**
     * Check if element is selected (checkbox, radio button)
     * 
     * @param element - WebElement
     * @return true if selected, false otherwise
     */
    public static boolean isSelected(WebElement element) {
        try {
            return element.isSelected();
        } catch (Exception e) {
            logger.debug("Element not selected");
            return false;
        }
    }
    
    /**
     * Scroll to element
     * 
     * WHY NEEDED: Element might be below fold, not visible in viewport
     * USE CASE: Footer elements, long product lists
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to scroll to
     */
    public static void scrollToElement(WebDriver driver, WebElement element) {
        try {
            logger.debug("Scrolling to element");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            
            // Wait a bit for smooth scroll animation
            WaitHelper.hardWait(500);
            
            logger.debug("Scrolled to element");
        } catch (Exception e) {
            logger.warn("Error scrolling to element", e);
        }
    }
    
    /**
     * Scroll to element using By locator
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     */
    public static void scrollToElement(WebDriver driver, By locator) {
        WebElement element = driver.findElement(locator);
        scrollToElement(driver, element);
    }
    
    /**
     * Scroll to bottom of page
     * 
     * USE CASE: Infinite scroll pages (product listings)
     * 
     * @param driver - WebDriver instance
     */
    public static void scrollToBottom(WebDriver driver) {
        try {
            logger.debug("Scrolling to bottom of page");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            
            // Wait for lazy-loaded content
            WaitHelper.hardWait(1000);
            
            logger.debug("Scrolled to bottom");
        } catch (Exception e) {
            logger.warn("Error scrolling to bottom", e);
        }
    }
    
    /**
     * Scroll to top of page
     * 
     * @param driver - WebDriver instance
     */
    public static void scrollToTop(WebDriver driver) {
        try {
            logger.debug("Scrolling to top of page");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, 0);");
            logger.debug("Scrolled to top");
        } catch (Exception e) {
            logger.warn("Error scrolling to top", e);
        }
    }
    
    /**
     * Hover over element
     * 
     * USE CASE: Mega menus, tooltips, dropdown menus
     * EXAMPLE: Trendyol category mega menu appears on hover
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to hover over
     */
    public static void hoverOverElement(WebDriver driver, WebElement element) {
        try {
            logger.debug("Hovering over element");
            Actions actions = new Actions(driver);
            actions.moveToElement(element).perform();
            
            // Wait for hover effect
            WaitHelper.hardWait(500);
            
            logger.debug("Hover successful");
        } catch (Exception e) {
            logger.error("Error hovering over element", e);
            throw e;
        }
    }
    
    /**
     * Hover using By locator
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     */
    public static void hoverOverElement(WebDriver driver, By locator) {
        WebElement element = WaitHelper.waitForElementVisible(driver, locator);
        hoverOverElement(driver, element);
    }
    
    /**
     * Double click on element
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to double click
     */
    public static void doubleClick(WebDriver driver, WebElement element) {
        try {
            logger.debug("Double clicking element");
            Actions actions = new Actions(driver);
            actions.doubleClick(element).perform();
            logger.debug("Double click successful");
        } catch (Exception e) {
            logger.error("Error double clicking element", e);
            throw e;
        }
    }
    
    /**
     * Right click (context menu) on element
     * 
     * @param driver - WebDriver instance
     * @param element - WebElement to right click
     */
    public static void rightClick(WebDriver driver, WebElement element) {
        try {
            logger.debug("Right clicking element");
            Actions actions = new Actions(driver);
            actions.contextClick(element).perform();
            logger.debug("Right click successful");
        } catch (Exception e) {
            logger.error("Error right clicking element", e);
            throw e;
        }
    }
    
    /**
     * Select dropdown option by visible text
     * 
     * @param element - Select element
     * @param visibleText - text to select
     */
    public static void selectByVisibleText(WebElement element, String visibleText) {
        try {
            logger.debug("Selecting option by visible text: '{}'", visibleText);
            Select select = new Select(element);
            select.selectByVisibleText(visibleText);
            logger.debug("Option selected");
        } catch (Exception e) {
            logger.error("Error selecting option by visible text '{}'", visibleText, e);
            throw e;
        }
    }
    
    /**
     * Select dropdown option by value
     * 
     * @param element - Select element
     * @param value - value attribute to select
     */
    public static void selectByValue(WebElement element, String value) {
        try {
            logger.debug("Selecting option by value: '{}'", value);
            Select select = new Select(element);
            select.selectByValue(value);
            logger.debug("Option selected");
        } catch (Exception e) {
            logger.error("Error selecting option by value '{}'", value, e);
            throw e;
        }
    }
    
    /**
     * Select dropdown option by index
     * 
     * @param element - Select element
     * @param index - index to select
     */
    public static void selectByIndex(WebElement element, int index) {
        try {
            logger.debug("Selecting option by index: {}", index);
            Select select = new Select(element);
            select.selectByIndex(index);
            logger.debug("Option selected");
        } catch (Exception e) {
            logger.error("Error selecting option by index {}", index, e);
            throw e;
        }
    }
    
    /**
     * Get count of elements matching locator
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @return count of elements
     */
    public static int getElementCount(WebDriver driver, By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            int count = elements.size();
            logger.debug("Found {} elements matching locator {}", count, locator);
            return count;
        } catch (Exception e) {
            logger.error("Error counting elements", e);
            return 0;
        }
    }
    
    /**
     * Check if element exists in DOM
     * 
     * @param driver - WebDriver instance
     * @param locator - By locator
     * @return true if exists, false otherwise
     */
    public static boolean isElementPresent(WebDriver driver, By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
