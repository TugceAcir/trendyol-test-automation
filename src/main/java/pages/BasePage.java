package pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import utils.ElementHelper;
import utils.WaitHelper;

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
    // POPUP HANDLING - Critical for Trendyol!
    // ============================================================

    /**
     * Handle all Trendyol popups
     *
     * STRATEGY:
     * 1. Wait 2 seconds for gender popup
     * 2. Close gender popup if present
     * 3. Wait 5 seconds for cookie banner
     * 4. Accept cookies if present
     *
     * WHY THIS ORDER:
     * - Gender popup appears immediately
     * - Cookie banner appears ~5 seconds after
     *
     * CALL THIS: After page load in HomePage
     */
    public void handlePopups() {
        logger.info("Handling Trendyol popups");

        // Handle gender selection popup (appears immediately)
        closeGenderPopup();

        // Handle cookie banner (appears after ~5 seconds)
        WaitHelper.hardWait(5000); // Wait for cookie banner
        acceptCookies();

        logger.info("All popups handled");
    }

    /**
     * Close gender selection popup (Kadın/Erkek)
     *
     * POPUP: "Aradığın her şey Trendyol'da!"
     * APPEARS: Immediately on page load
     */
    public void closeGenderPopup() {
        try {
            logger.debug("Checking for gender popup");

            // Wait max 3 seconds for popup
            WaitHelper.hardWait(2000);

            if (ElementHelper.isElementPresent(driver, GENDER_POPUP)) {
                logger.info("Gender popup detected, closing...");
                ElementHelper.safeClick(driver, GENDER_POPUP_CLOSE);

                // Wait for popup to disappear
                WaitHelper.waitForElementInvisible(driver, GENDER_POPUP, 5);
                logger.info("Gender popup closed successfully");
            } else {
                logger.debug("Gender popup not present");
            }

        } catch (Exception e) {
            logger.warn("Could not close gender popup (might not be present): {}", e.getMessage());
        }
    }

    /**
     * Accept cookie banner
     *
     * BANNER: "SANA ÖZEL BİR DENEYİM İÇİN ÇALIŞIYORUZ"
     * APPEARS: ~5 seconds after page load
     */
    public void acceptCookies() {
        try {
            logger.debug("Checking for cookie banner");

            if (ElementHelper.isElementPresent(driver, COOKIE_BANNER)) {
                logger.info("Cookie banner detected, accepting...");

                // Wait for accept button to be clickable
                WaitHelper.waitForElementClickable(driver, COOKIE_ACCEPT_BTN, 5);
                ElementHelper.safeClick(driver, COOKIE_ACCEPT_BTN);

                // Wait for banner to disappear
                WaitHelper.waitForElementInvisible(driver, COOKIE_BANNER, 5);
                logger.info("Cookies accepted successfully");
            } else {
                logger.debug("Cookie banner not present");
            }

        } catch (Exception e) {
            logger.warn("Could not accept cookies (might not be present): {}", e.getMessage());
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