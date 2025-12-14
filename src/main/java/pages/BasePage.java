package pages;

import org.openqa.selenium.JavascriptExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import utils.ElementHelper;
import utils.WaitHelper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * BasePage - Parent class for all Page Objects
 *
 * WHY THIS CLASS EXISTS:
 * - Common functionality shared by all pages
 * - Popup handling (gender selection, cookie banner)
 * - Tab switching (Trendyol opens products in new tab)
 * - Wait utilities wrapper
 *
 * ALL PAGE OBJECTS EXTEND THIS CLASS
 */
public class BasePage {

    protected static final Logger logger = LogManager.getLogger(BasePage.class);
    protected WebDriver driver;

    // Common popup locators
    private static final By GENDER_POPUP = By.cssSelector(".gender-modal-section");
    private static final By GENDER_POPUP_CLOSE = By.cssSelector(".modal-section-close");
    private static final By COOKIE_BANNER = By.id("onetrust-banner-sdk");
    private static final By COOKIE_ACCEPT_BTN = By.id("onetrust-accept-btn-handler");
    private static final By COOKIE_REJECT_BTN = By.id("onetrust-reject-all-handler");

    /**
     * Constructor - initializes PageFactory
     *
     * WHAT IS PageFactory?
     * - Selenium utility that initializes @FindBy elements
     * - Makes page objects cleaner and more maintainable
     *
     * @param driver - WebDriver instance
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        logger.debug("Page initialized: {}", this.getClass().getSimpleName());
    }

    // ============================================================
    // POPUP HANDLING - ULTRA OPTIMIZED!
    // ============================================================

    /**
     * Handle all Trendyol popups
     *
     * ULTRA FAST: Smart waits, no hardWait waste
     * MAX TIME: ~3 seconds (if both popups present)
     * MIN TIME: ~0.2 seconds (if no popups)
     */
    public void handlePopups() {
        logger.info("Handling Trendyol popups");
        closePopupsQuick();
        logger.info("All popups handled");
    }

    /**
     * Quick popup closer - OPTIMIZED for speed
     *
     * STRATEGY:
     * 1. Instant check for gender popup (no wait)
     * 2. Smart wait for cookie banner (max 3s)
     * 3. Micro pauses only (200ms)
     */
    private void closePopupsQuick() {
        // Gender popup - INSTANT CHECK (no wait if not present)
        try {
            WebElement genderClose = driver.findElement(GENDER_POPUP_CLOSE);
            if (genderClose.isDisplayed()) {
                genderClose.click();
                logger.info("Gender popup closed");
                microPause();
            }
        } catch (Exception e) {
            // No gender popup - continue immediately
            logger.debug("No gender popup detected");
        }

        // Cookie banner - SMART WAIT (max 3 seconds)
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement cookieBtn = wait.until(ExpectedConditions.elementToBeClickable(COOKIE_ACCEPT_BTN));
            cookieBtn.click();
            logger.info("Cookies accepted");
            microPause();
        } catch (TimeoutException e) {
            // No cookie banner within 3 seconds - continue
            logger.debug("No cookie banner detected");
        } catch (Exception e) {
            logger.warn("Cookie handling exception: {}", e.getMessage());
        }
    }

    /**
     * Micro pause - very short wait (200ms)
     * USE: After quick actions like popup close
     */
    private void microPause() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Close gender selection popup (Kadın/Erkek)
     *
     * DEPRECATED: Use handlePopups() instead
     * Kept for backward compatibility
     */
    @Deprecated
    public void closeGenderPopup() {
        try {
            WebElement closeButton = driver.findElement(GENDER_POPUP_CLOSE);
            if (closeButton.isDisplayed()) {
                closeButton.click();
                microPause();
                logger.info("Gender popup closed");
            }
        } catch (Exception e) {
            logger.debug("No gender popup");
        }
    }

    /**
     * Accept cookie banner
     *
     * DEPRECATED: Use handlePopups() instead
     * Kept for backward compatibility
     */
    @Deprecated
    public void acceptCookies() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement cookieBtn = wait.until(ExpectedConditions.elementToBeClickable(COOKIE_ACCEPT_BTN));
            cookieBtn.click();
            microPause();
            logger.info("Cookies accepted");
        } catch (TimeoutException e) {
            logger.debug("No cookie banner");
        }
    }

    /**
     * Reject cookie banner (alternative)
     *
     * USE CASE: If test requires rejecting cookies
     */
    public void rejectCookies() {
        try {
            logger.debug("Checking for cookie banner");

            if (ElementHelper.isElementPresent(driver, COOKIE_BANNER)) {
                logger.info("Cookie banner detected, rejecting...");
                ElementHelper.safeClick(driver, COOKIE_REJECT_BTN);
                WaitHelper.waitForElementInvisible(driver, COOKIE_BANNER, 5);
                logger.info("Cookies rejected successfully");
            }

        } catch (Exception e) {
            logger.warn("Could not reject cookies: {}", e.getMessage());
        }
    }

    // ============================================================
    // TAB HANDLING - Trendyol opens products in new tab!
    // ============================================================

    /**
     * Switch to new tab
     *
     * USE CASE: When clicking product, Trendyol opens in new tab
     *
     * STRATEGY:
     * 1. Get all window handles
     * 2. Switch to the last one (newest tab)
     *
     * @return true if switched successfully
     */
    public boolean switchToNewTab() {
        try {
            logger.info("Switching to new tab");

            // Get all open tabs
            List<String> tabs = new ArrayList<>(driver.getWindowHandles());

            if (tabs.size() > 1) {
                // Switch to last tab (newest)
                driver.switchTo().window(tabs.get(tabs.size() - 1));
                logger.info("Switched to new tab successfully");

                // Wait for page to load
                WaitHelper.waitForPageLoad(driver);
                return true;
            } else {
                logger.warn("No new tab found");
                return false;
            }

        } catch (Exception e) {
            logger.error("Error switching to new tab", e);
            return false;
        }
    }

    /**
     * Switch to original tab (first tab)
     *
     * USE CASE: After viewing product in new tab, return to search results
     */
    public void switchToOriginalTab() {
        try {
            logger.info("Switching to original tab");

            List<String> tabs = new ArrayList<>(driver.getWindowHandles());

            if (!tabs.isEmpty()) {
                // Switch to first tab
                driver.switchTo().window(tabs.get(0));
                logger.info("Switched to original tab");
                WaitHelper.waitForPageLoad(driver);
            }

        } catch (Exception e) {
            logger.error("Error switching to original tab", e);
        }
    }

    /**
     * Close current tab and switch to original
     *
     * USE CASE: Close product detail tab after viewing
     */
    public void closeCurrentTabAndSwitchToOriginal() {
        try {
            logger.info("Closing current tab and switching back");

            // Close current tab
            driver.close();

            // Switch to remaining tab
            List<String> tabs = new ArrayList<>(driver.getWindowHandles());
            if (!tabs.isEmpty()) {
                driver.switchTo().window(tabs.get(0));
                logger.info("Current tab closed, switched back");
                WaitHelper.waitForPageLoad(driver);
            }

        } catch (Exception e) {
            logger.error("Error closing tab and switching", e);
        }
    }

    /**
     * Get count of open tabs
     *
     * @return number of open tabs
     */
    public int getTabCount() {
        return driver.getWindowHandles().size();
    }
    /**
     * Wait for new tab to open
     *
     * TRENDYOL: Product clicks open new tab
     * STRATEGY: Smart wait (max 5s) until tab count increases
     *
     * @param expectedCount Expected number of tabs after opening
     * @return true if new tab opened
     */
    public boolean waitForNewTab(int expectedCount) {
        try {
            logger.debug("Waiting for new tab (expected count: {})", expectedCount + 1);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(driver -> driver.getWindowHandles().size() > expectedCount);

            logger.info("New tab opened successfully");
            return true;

        } catch (TimeoutException e) {
            logger.warn("Timeout waiting for new tab");
            return false;
        } catch (Exception e) {
            logger.error("Error waiting for new tab", e);
            return false;
        }
    }

    /**
     * Switch to newest tab (not original window)
     *
     * TRENDYOL: After clicking product, switch to product detail tab
     *
     * @param originalWindow Original window handle to skip
     */
    public void switchToNewTab(String originalWindow) {
        try {
            logger.debug("Switching to new tab (excluding original: {})", originalWindow);

            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    logger.info("Switched to new tab successfully");
                    WaitHelper.waitForPageLoad(driver);
                    return;
                }
            }

            logger.warn("No new tab found to switch to");

        } catch (Exception e) {
            logger.error("Error switching to new tab", e);
        }
    }
    // ============================================================
    // COMMON PAGE METHODS
    // ============================================================

    /**
     * Get current page URL
     *
     * @return current URL
     */
    public String getCurrentUrl() {
        String url = driver.getCurrentUrl();
        logger.debug("Current URL: {}", url);
        return url;
    }

    /**
     * Get page title
     *
     * @return page title
     */
    public String getPageTitle() {
        String title = driver.getTitle();
        logger.debug("Page title: {}", title);
        return title;
    }

    /**
     * Refresh page
     */
    public void refreshPage() {
        logger.info("Refreshing page");
        driver.navigate().refresh();
        WaitHelper.waitForPageLoad(driver);
    }

    /**
     * Navigate back
     */
    public void navigateBack() {
        logger.info("Navigating back");
        driver.navigate().back();
        WaitHelper.waitForPageLoad(driver);
    }

    /**
     * Navigate forward
     */
    public void navigateForward() {
        logger.info("Navigating forward");
        driver.navigate().forward();
        WaitHelper.waitForPageLoad(driver);
    }

    /**
     * Scroll to top of page
     */
    public void scrollToTop() {
        ElementHelper.scrollToTop(driver);
    }

    /**
     * Scroll to bottom of page
     */
    public void scrollToBottom() {
        ElementHelper.scrollToBottom(driver);
    }

    /**
     * Check if element is displayed
     *
     * @param element - WebElement to check
     * @return true if displayed
     */
    protected boolean isElementDisplayed(WebElement element) {
        return ElementHelper.isDisplayed(element);
    }

    /**
     * Check if element is enabled
     *
     * @param element - WebElement to check
     * @return true if enabled
     */
    protected boolean isElementEnabled(WebElement element) {
        return ElementHelper.isEnabled(element);
    }

}