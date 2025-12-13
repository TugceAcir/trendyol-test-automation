package pages;

import org.openqa.selenium.JavascriptExecutor;
import constants.Timeouts;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.ElementHelper;
import utils.WaitHelper;

import java.util.List;

/**
 * HomePage - Trendyol.com homepage Page Object
 *
 * RESPONSIBILITIES:
 * - Search functionality
 * - Category navigation
 * - Popup handling (gender, cookies)
 * - Logo, login, cart verification
 *
 * URL: https://www.trendyol.com/
 */
public class HomePage extends BasePage {

    // ============================================================
    // PAGE ELEMENTS - Using @FindBy (PageFactory pattern)
    // ============================================================

    /**
     * Search box - Main search input field
     *
     * HTML: <input data-testid="suggestion" type="text"
     *              placeholder="Aradığınız ürün, kategori veya markayı yazınız">
     */
    @FindBy(css = "input[data-testid='suggestion']")
    private WebElement searchBox;

    /**
     * Search icon - Magnifying glass button
     *
     * HTML: <i data-testid="search-icon" class="ft51BU2r"></i>
     */
    @FindBy(css = "i[data-testid='search-icon']")
    private WebElement searchIcon;

    /**
     * Trendyol logo - Main logo on top left
     */
    @FindBy(css = "a.logo")
    private WebElement trendyolLogo;

    /**
     * Login button - "Giriş Yap"
     */
    @FindBy(xpath = "//p[contains(text(),'Giriş Yap')]")
    private WebElement loginButton;

    /**
     * Cart icon - "Sepetim"
     */
    @FindBy(xpath = "//p[contains(text(),'Sepetim')]")
    private WebElement cartIcon;

    /**
     * Favorites icon - "Favorilerim"
     */
    @FindBy(xpath = "//p[contains(text(),'Favorilerim')]")
    private WebElement favoritesIcon;

    // ============================================================
    // DYNAMIC LOCATORS - For categories (text-based)
    // ============================================================

    /**
     * Get category link by name
     *
     * EXAMPLE: getCategoryLocator("Elektronik")
     *
     * @param categoryName - category name (Kadın, Erkek, Elektronik, etc.)
     * @return By locator for category
     */
    private By getCategoryLocator(String categoryName) {
        return By.xpath("//a[@class='category-header' and contains(text(),'" + categoryName + "')]");
    }

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructor - initializes page and handles popups
     *
     * IMPORTANT: Automatically handles popups after initialization
     *
     * @param driver - WebDriver instance
     */
    public HomePage(WebDriver driver) {
        super(driver);
        logger.info("HomePage initialized");

        // Wait for page to load
        WaitHelper.waitForPageLoad(driver);

        // Handle popups automatically (gender + cookie)
        handlePopups();

        // Verify we're on homepage
        verifyHomePage();
    }

    // ============================================================
    // PAGE VERIFICATION
    // ============================================================

    /**
     * Verify we're on Trendyol homepage
     *
     * CHECKS:
     * - URL contains "trendyol.com"
     * - Logo is displayed
     * - Search box is displayed
     */
    public void verifyHomePage() {
        try {
            logger.info("Verifying homepage loaded");

            // Check URL
            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("trendyol.com")) {
                logger.warn("Not on Trendyol homepage. Current URL: {}", currentUrl);
            }

            // Check logo
            WaitHelper.waitForElementVisible(driver, trendyolLogo, Timeouts.ELEMENT_VISIBLE);

            // Check search box
            WaitHelper.waitForElementVisible(driver, searchBox, Timeouts.ELEMENT_VISIBLE);

            logger.info("Homepage verified successfully");

        } catch (Exception e) {
            logger.error("Homepage verification failed", e);
        }
    }

    /**
     * Check if homepage loaded successfully
     *
     * @return true if logo displayed
     */
    public boolean isHomePageLoaded() {
        try {
            return isElementDisplayed(trendyolLogo);
        } catch (Exception e) {
            logger.error("Error checking if homepage loaded", e);
            return false;
        }
    }

    /**
     * Check if logo is displayed
     *
     * @return true if logo visible
     */
    public boolean isLogoDisplayed() {
        return isElementDisplayed(trendyolLogo);
    }

    // ============================================================
    // SEARCH FUNCTIONALITY
    // ============================================================

    /**
     * Search for a product/keyword
     *
     * FLOW:
     * 1. Wait for search box to be visible
     * 2. Clear existing text
     * 3. Type search keyword
     * 4. Click search icon
     * 5. Wait for results page to load
     *
     * EXAMPLE: searchFor("laptop")
     *
     * @param keyword - search term
     */
    public void searchFor(String keyword) {
        try {
            logger.info("Searching for: '{}'", keyword);

            // Wait for search box - TIMEOUT AZALT
            WaitHelper.waitForElementVisible(driver, searchBox, 3); // 10 → 3

            // Clear and type - NO EXTRA WAIT
            searchBox.clear();
            searchBox.sendKeys(keyword);
            logger.info("Keyword entered: '{}'", keyword);

            // Click search icon - JAVASCRIPT CLICK!
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchIcon);
            logger.info("Search icon clicked");

            // Wait for results page
            WaitHelper.waitForPageLoad(driver);

            logger.info("Search completed for: '{}'", keyword);

        } catch (Exception e) {
            logger.error("Error during search for: '{}'", keyword, e);
            throw e;
        }
    }
    /**
     * Type in search box (without submitting)
     *
     * USE CASE: Check search suggestions
     *
     * @param keyword - search term
     */
    public void typeInSearchBox(String keyword) {
        try {
            logger.info("Typing in search box: '{}'", keyword);
            WaitHelper.waitForElementVisible(driver, searchBox, Timeouts.ELEMENT_VISIBLE);
            ElementHelper.safeSendKeys(driver, searchBox, keyword);
        } catch (Exception e) {
            logger.error("Error typing in search box", e);
            throw e;
        }
    }

    /**
     * Click search icon/button
     */
    public void clickSearchIcon() {
        try {
            logger.info("Clicking search icon");
            ElementHelper.safeClick(driver, searchIcon);
            WaitHelper.waitForPageLoad(driver);
        } catch (Exception e) {
            logger.error("Error clicking search icon", e);
            throw e;
        }
    }

    /**
     * Get search box placeholder text
     *
     * EXPECTED: "Aradığınız ürün, kategori veya markayı yazınız"
     *
     * @return placeholder text
     */
    public String getSearchBoxPlaceholder() {
        try {
            String placeholder = ElementHelper.getAttribute(driver, searchBox, "placeholder");
            logger.debug("Search box placeholder: '{}'", placeholder);
            return placeholder;
        } catch (Exception e) {
            logger.error("Error getting search box placeholder", e);
            return "";
        }
    }

    /**
     * Check if search box is displayed
     *
     * @return true if visible
     */
    public boolean isSearchBoxDisplayed() {
        return isElementDisplayed(searchBox);
    }

    // ============================================================
    // CATEGORY NAVIGATION
    // ============================================================

    /**
     * Click on category by name
     *
     * CATEGORIES:
     * - "Kadın"
     * - "Erkek"
     * - "Anne & Çocuk"
     * - "Ev & Yaşam"
     * - "Süpermarket"
     * - "Kozmetik"
     * - "Ayakkabı & Çanta"
     * - "Elektronik"
     * - "Çok Satanlar"
     * - "Flaş Ürünler"
     *
     * EXAMPLE: clickCategory("Elektronik")
     *
     * @param categoryName - category name
     */
    public void clickCategory(String categoryName) {
        try {
            logger.info("Clicking category: '{}'", categoryName);

            By categoryLocator = getCategoryLocator(categoryName);

            // Wait for category to be clickable
            WaitHelper.waitForElementClickable(driver, categoryLocator, Timeouts.ELEMENT_CLICKABLE);

            // Click category
            ElementHelper.safeClick(driver, categoryLocator);
            logger.info("Category '{}' clicked", categoryName);

            // Wait for category page to load
            WaitHelper.waitForPageLoad(driver);
            WaitHelper.waitForAjaxToComplete(driver);

        } catch (Exception e) {
            logger.error("Error clicking category: '{}'", categoryName, e);
            throw e;
        }
    }

    /**
     * Check if category exists
     *
     * @param categoryName - category name
     * @return true if category link exists
     */
    public boolean isCategoryDisplayed(String categoryName) {
        try {
            By categoryLocator = getCategoryLocator(categoryName);
            return ElementHelper.isElementPresent(driver, categoryLocator);
        } catch (Exception e) {
            logger.error("Error checking category: '{}'", categoryName, e);
            return false;
        }
    }

    /**
     * Get all category names
     *
     * @return list of category names
     */
    public List<String> getAllCategoryNames() {
        try {
            logger.info("Getting all category names");

            By allCategoriesLocator = By.cssSelector("a.category-header");
            List<WebElement> categories = driver.findElements(allCategoriesLocator);

            List<String> categoryNames = new java.util.ArrayList<>();
            for (WebElement category : categories) {
                String name = ElementHelper.safeGetText(driver, category);
                if (!name.isEmpty()) {
                    categoryNames.add(name);
                }
            }

            logger.info("Found {} categories", categoryNames.size());
            return categoryNames;

        } catch (Exception e) {
            logger.error("Error getting category names", e);
            return new java.util.ArrayList<>();
        }
    }

    // ============================================================
    // HEADER ELEMENTS (Login, Cart, Favorites)
    // ============================================================

    /**
     * Click login button
     */
    public void clickLogin() {
        try {
            logger.info("Clicking login button");
            ElementHelper.safeClick(driver, loginButton);
            WaitHelper.waitForPageLoad(driver);
        } catch (Exception e) {
            logger.error("Error clicking login button", e);
            throw e;
        }
    }

    /**
     * Click cart icon
     */
    public void clickCart() {
        try {
            logger.info("Clicking cart icon");
            ElementHelper.safeClick(driver, cartIcon);
            WaitHelper.waitForPageLoad(driver);
        } catch (Exception e) {
            logger.error("Error clicking cart icon", e);
            throw e;
        }
    }

    /**
     * Click favorites icon
     */
    public void clickFavorites() {
        try {
            logger.info("Clicking favorites icon");
            ElementHelper.safeClick(driver, favoritesIcon);
            WaitHelper.waitForPageLoad(driver);
        } catch (Exception e) {
            logger.error("Error clicking favorites icon", e);
            throw e;
        }
    }

    /**
     * Click Trendyol logo (go to homepage)
     */
    public void clickLogo() {
        try {
            logger.info("Clicking Trendyol logo");
            ElementHelper.safeClick(driver, trendyolLogo);
            WaitHelper.waitForPageLoad(driver);
        } catch (Exception e) {
            logger.error("Error clicking logo", e);
            throw e;
        }
    }

    /**
     * Check if login button is displayed
     *
     * @return true if visible (user not logged in)
     */
    public boolean isLoginButtonDisplayed() {
        return isElementDisplayed(loginButton);
    }

    /**
     * Check if cart icon is displayed
     *
     * @return true if visible
     */
    public boolean isCartIconDisplayed() {
        return isElementDisplayed(cartIcon);
    }

    /**
     * Check if favorites icon is displayed
     *
     * @return true if visible
     */
    public boolean isFavoritesIconDisplayed() {
        return isElementDisplayed(favoritesIcon);
    }
}