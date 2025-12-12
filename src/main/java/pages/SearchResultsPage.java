package pages;

import constants.Timeouts;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.ElementHelper;
import utils.TurkishTextHelper;
import utils.WaitHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchResultsPage - Trendyol search results page
 *
 * RESPONSIBILITIES:
 * - Product listing
 * - Price filtering (min-max)
 * - Sorting
 * - Product selection
 * - Infinite scroll handling (lazy loading)
 *
 * URL: https://www.trendyol.com/sr?q=...
 */
public class SearchResultsPage extends BasePage {

    // ============================================================
    // PAGE ELEMENTS
    // ============================================================

    /**
     * Search title - Shows search keyword
     * HTML: <h1 class="title" data-testid="title">Laptop</h1>
     */
    @FindBy(css = "h1[data-testid='title']")
    private WebElement searchTitle;

    /**
     * Result count - Shows total products found
     * HTML: <span data-testid="result-count-info">67049+ Ürün</span>
     */
    @FindBy(css = "span[data-testid='result-count-info']")
    private WebElement resultCountInfo;

    /**
     * All product cards
     * HTML: <a class="product-card" data-testid="product-card">
     */
    @FindBy(css = "a.product-card")
    private List<WebElement> productCards;

    /**
     * Price filter - Minimum input
     * HTML: <input data-testid="price-range-input-min" placeholder="En Az">
     */
    @FindBy(css = "input[data-testid='price-range-input-min']")
    private WebElement priceMinInput;

    /**
     * Price filter - Maximum input
     * HTML: <input data-testid="price-range-input-max" placeholder="En Çok">
     */
    @FindBy(css = "input[data-testid='price-range-input-max']")
    private WebElement priceMaxInput;

    /**
     * Price filter - Search button
     * HTML: <button data-testid="price-range-button">
     */
    @FindBy(css = "button[data-testid='price-range-button']")
    private WebElement priceSearchButton;

    /**
     * Price filter header - To expand/collapse
     * HTML: <section data-aggregationtype="Price">
     */
    @FindBy(css = "section[data-aggregationtype='Price'] button.expand-collapse-button")
    private WebElement priceFilterHeader;

    /**
     * Sorting dropdown
     * HTML: <button class="select-box">Önerilen Sıralama</button>
     */
    @FindBy(css = "button.select-box")
    private WebElement sortingDropdown;

    // ============================================================
    // DYNAMIC LOCATORS
    // ============================================================

    /**
     * Product card by index
     * @param index - product index (0-based)
     */
    private By getProductCardByIndex(int index) {
        return By.cssSelector("a.product-card:nth-of-type(" + (index + 1) + ")");
    }

    /**
     * Product name locator
     */
    private By productNameLocator = By.cssSelector(".product-name");

    /**
     * Product price locator
     */
    private By productPriceLocator = By.cssSelector(".discounted-price");

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructor
     *
     * @param driver - WebDriver instance
     */
    public SearchResultsPage(WebDriver driver) {
        super(driver);
        logger.info("SearchResultsPage initialized");

        // Wait for search results to load
        WaitHelper.waitForPageLoad(driver);
        WaitHelper.waitForAjaxToComplete(driver);

        // Wait for products to appear
        waitForProductsToLoad();

        // Verify we're on search results page
        verifySearchResultsPage();
    }

    // ============================================================
    // PAGE VERIFICATION
    // ============================================================

    /**
     * Verify search results page loaded
     *
     * CHECKS:
     * - URL contains "/sr"
     * - Search title displayed
     * - At least 1 product displayed
     */
    public void verifySearchResultsPage() {
        try {
            logger.info("Verifying search results page");

            // Check URL
            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("/sr")) {
                logger.warn("Not on search results page. URL: {}", currentUrl);
            }

            // Check search title
            WaitHelper.waitForElementVisible(driver, searchTitle, Timeouts.ELEMENT_VISIBLE);

            // Check products
            if (productCards.isEmpty()) {
                logger.warn("No products found on page");
            } else {
                logger.info("Search results page verified: {} products visible", productCards.size());
            }

        } catch (Exception e) {
            logger.error("Search results page verification failed", e);
        }
    }

    /**
     * Wait for products to load
     *
     * WHY: Trendyol uses lazy loading, products load gradually
     */
    private void waitForProductsToLoad() {
        try {
            logger.debug("Waiting for products to load");

            // Wait for at least 1 product card
            WaitHelper.waitForElementVisible(driver, By.cssSelector("a.product-card"), Timeouts.SEARCH_RESULTS_LOAD);

            // Small delay for images to load
            WaitHelper.hardWait(1000);

            logger.debug("Products loaded successfully");

        } catch (Exception e) {
            logger.warn("Products not loaded within timeout", e);
        }
    }

    // ============================================================
    // PRODUCT LISTING
    // ============================================================

    /**
     * Get total product count from page
     *
     * PARSES: "67049+ Ürün" → 67049
     *
     * @return product count (0 if unable to parse)
     */
    public int getProductCount() {
        try {
            logger.info("Getting product count");

            // Wait for count element
            WaitHelper.waitForElementVisible(driver, resultCountInfo, Timeouts.ELEMENT_VISIBLE);

            // Get text: "67049+ Ürün"
            String countText = ElementHelper.safeGetText(driver, resultCountInfo);
            logger.debug("Product count text: '{}'", countText);

            // Parse number (remove non-digits except +)
            String numberOnly = countText.replaceAll("[^0-9]", "");

            if (!numberOnly.isEmpty()) {
                int count = Integer.parseInt(numberOnly);
                logger.info("Product count: {}", count);
                return count;
            }

            logger.warn("Unable to parse product count from: '{}'", countText);
            return 0;

        } catch (Exception e) {
            logger.error("Error getting product count", e);
            return 0;
        }
    }

    /**
     * Get number of product cards currently visible on page
     *
     * NOTE: Due to lazy loading, this may be less than total count
     *
     * @return number of visible products
     */
    public int getVisibleProductCount() {
        try {
            int count = productCards.size();
            logger.info("Visible product count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error getting visible product count", e);
            return 0;
        }
    }

    /**
     * Get search keyword from title
     *
     * @return search keyword
     */
    public String getSearchKeyword() {
        try {
            String keyword = ElementHelper.safeGetText(driver, searchTitle);
            logger.debug("Search keyword: '{}'", keyword);
            return keyword;
        } catch (Exception e) {
            logger.error("Error getting search keyword", e);
            return "";
        }
    }

    /**
     * Check if products are displayed
     *
     * @return true if at least 1 product visible
     */
    public boolean areProductsDisplayed() {
        return !productCards.isEmpty();
    }

    /**
     * Check if "no results" message displayed
     *
     * @return true if no results found
     */
    public boolean isNoResultsMessageDisplayed() {
        try {
            // Trendyol shows empty state when no results
            By noResultsLocator = By.cssSelector(".empty-result");
            return ElementHelper.isElementPresent(driver, noResultsLocator);
        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================
    // PRODUCT SELECTION
    // ============================================================

    /**
     * Click first product
     *
     * IMPORTANT: Trendyol opens product in NEW TAB!
     * After calling this, use switchToNewTab() from BasePage
     */
    public void clickFirstProduct() {
        clickProductByIndex(0);
    }

    /**
     * Click product by index
     *
     * INDEX: 0-based (0 = first product, 1 = second, etc.)
     *
     * IMPORTANT: Opens in new tab!
     *
     * @param index - product index
     */
    public void clickProductByIndex(int index) {
        try {
            logger.info("Clicking product at index: {}", index);

            // Check if index valid
            if (index < 0 || index >= productCards.size()) {
                logger.error("Invalid product index: {}. Available: {}", index, productCards.size());
                throw new IllegalArgumentException("Product index out of bounds: " + index);
            }

            // Get product element
            WebElement product = productCards.get(index);

            // Scroll to product (may be below fold)
            ElementHelper.scrollToElement(driver, product);

            // Click product
            ElementHelper.safeClick(driver, product);
            logger.info("Product clicked at index: {}", index);

            // Wait a bit for new tab to open
            WaitHelper.hardWait(1000);

        } catch (Exception e) {
            logger.error("Error clicking product at index: {}", index, e);
            throw e;
        }
    }

    /**
     * Get product name by index
     *
     * @param index - product index
     * @return product name
     */
    public String getProductNameByIndex(int index) {
        try {
            logger.debug("Getting product name at index: {}", index);

            if (index < 0 || index >= productCards.size()) {
                logger.error("Invalid product index: {}", index);
                return "";
            }

            WebElement product = productCards.get(index);
            WebElement nameElement = product.findElement(productNameLocator);

            String name = ElementHelper.safeGetText(driver, nameElement);
            logger.debug("Product name at index {}: '{}'", index, name);
            return name;

        } catch (Exception e) {
            logger.error("Error getting product name at index: {}", index, e);
            return "";
        }
    }

    /**
     * Get product price by index
     *
     * RETURNS: Raw text like "140.000 TL"
     *
     * @param index - product index
     * @return product price as string
     */
    public String getProductPriceByIndex(int index) {
        try {
            logger.debug("Getting product price at index: {}", index);

            if (index < 0 || index >= productCards.size()) {
                logger.error("Invalid product index: {}", index);
                return "";
            }

            WebElement product = productCards.get(index);
            WebElement priceElement = product.findElement(productPriceLocator);

            String price = ElementHelper.safeGetText(driver, priceElement);
            logger.debug("Product price at index {}: '{}'", index, price);
            return price;

        } catch (Exception e) {
            logger.error("Error getting product price at index: {}", index, e);
            return "";
        }
    }

    /**
     * Get product price as double (parsed)
     *
     * EXAMPLE: "140.000 TL" → 140000.0
     *
     * @param index - product index
     * @return price as double
     */
    public double getProductPriceAsDoubleByIndex(int index) {
        String priceText = getProductPriceByIndex(index);
        return TurkishTextHelper.parseTurkishNumber(priceText);
    }

    // ============================================================
    // PRICE FILTERING
    // ============================================================

    /**
     * Expand price filter section
     *
     * WHY: Price filter is collapsed by default
     */
    public void expandPriceFilter() {
        try {
            logger.info("Expanding price filter");

            // Check if already expanded
            By containerLocator = By.cssSelector("section[data-aggregationtype='Price'] .aggregation-container");
            WebElement container = driver.findElement(containerLocator);

            // Check if hidden attribute exists
            String hiddenAttr = container.getAttribute("hidden");
            if (hiddenAttr != null) {
                // Collapsed, click to expand
                ElementHelper.safeClick(driver, priceFilterHeader);
                WaitHelper.hardWait(500); // Wait for animation
                logger.info("Price filter expanded");
            } else {
                logger.debug("Price filter already expanded");
            }

        } catch (Exception e) {
            logger.warn("Error expanding price filter", e);
        }
    }

    /**
     * Apply price filter (min-max)
     *
     * FLOW:
     * 1. Expand price filter
     * 2. Enter min price
     * 3. Enter max price
     * 4. Click search button
     * 5. Wait for results to reload
     *
     * @param minPrice - minimum price
     * @param maxPrice - maximum price
     */
    public void applyPriceFilter(int minPrice, int maxPrice) {
        try {
            logger.info("Applying price filter: {} TL - {} TL", minPrice, maxPrice);

            // Expand filter
            expandPriceFilter();

            // Wait for inputs to be visible
            WaitHelper.waitForElementVisible(driver, priceMinInput, Timeouts.ELEMENT_VISIBLE);

            // Enter min price
            ElementHelper.safeSendKeys(driver, priceMinInput, String.valueOf(minPrice));
            logger.debug("Min price entered: {}", minPrice);

            // Enter max price
            ElementHelper.safeSendKeys(driver, priceMaxInput, String.valueOf(maxPrice));
            logger.debug("Max price entered: {}", maxPrice);

            // Click search button
            ElementHelper.safeClick(driver, priceSearchButton);
            logger.info("Price filter search button clicked");

            // Wait for results to reload
            WaitHelper.waitForPageLoad(driver);
            WaitHelper.waitForAjaxToComplete(driver);
            waitForProductsToLoad();

            logger.info("Price filter applied successfully");

        } catch (Exception e) {
            logger.error("Error applying price filter", e);
            throw e;
        }
    }

    /**
     * Apply minimum price only
     *
     * @param minPrice - minimum price
     */
    public void applyMinPriceFilter(int minPrice) {
        try {
            logger.info("Applying min price filter: {} TL", minPrice);

            expandPriceFilter();
            WaitHelper.waitForElementVisible(driver, priceMinInput, Timeouts.ELEMENT_VISIBLE);

            ElementHelper.safeSendKeys(driver, priceMinInput, String.valueOf(minPrice));
            ElementHelper.safeClick(driver, priceSearchButton);

            WaitHelper.waitForPageLoad(driver);
            WaitHelper.waitForAjaxToComplete(driver);
            waitForProductsToLoad();

            logger.info("Min price filter applied");

        } catch (Exception e) {
            logger.error("Error applying min price filter", e);
            throw e;
        }
    }

    /**
     * Apply maximum price only
     *
     * @param maxPrice - maximum price
     */
    public void applyMaxPriceFilter(int maxPrice) {
        try {
            logger.info("Applying max price filter: {} TL", maxPrice);

            expandPriceFilter();
            WaitHelper.waitForElementVisible(driver, priceMaxInput, Timeouts.ELEMENT_VISIBLE);

            ElementHelper.safeSendKeys(driver, priceMaxInput, String.valueOf(maxPrice));
            ElementHelper.safeClick(driver, priceSearchButton);

            WaitHelper.waitForPageLoad(driver);
            WaitHelper.waitForAjaxToComplete(driver);
            waitForProductsToLoad();

            logger.info("Max price filter applied");

        } catch (Exception e) {
            logger.error("Error applying max price filter", e);
            throw e;
        }
    }

    // ============================================================
    // INFINITE SCROLL (Lazy Loading)
    // ============================================================

    /**
     * Scroll down to load more products
     *
     * WHY: Trendyol uses infinite scroll / lazy loading
     * Products below fold don't load until you scroll
     *
     * @param scrollCount - how many times to scroll down
     */
    public void scrollToLoadMoreProducts(int scrollCount) {
        try {
            logger.info("Scrolling to load more products (scroll count: {})", scrollCount);

            for (int i = 0; i < scrollCount; i++) {
                // Scroll to bottom
                ElementHelper.scrollToBottom(driver);

                // Wait for products to load
                WaitHelper.hardWait(2000);

                logger.debug("Scroll iteration: {}/{}", i + 1, scrollCount);
            }

            int visibleCount = getVisibleProductCount();
            logger.info("After scrolling, visible products: {}", visibleCount);

        } catch (Exception e) {
            logger.error("Error scrolling to load products", e);
        }
    }

    /**
     * Scroll to specific product index
     *
     * USE CASE: Product at index 50, need to scroll to see it
     *
     * @param index - product index
     */
    public void scrollToProduct(int index) {
        try {
            logger.info("Scrolling to product at index: {}", index);

            if (index < productCards.size()) {
                WebElement product = productCards.get(index);
                ElementHelper.scrollToElement(driver, product);
            } else {
                logger.warn("Product index {} not visible yet, scrolling down", index);
                scrollToLoadMoreProducts(3);
            }

        } catch (Exception e) {
            logger.error("Error scrolling to product", e);
        }
    }
}