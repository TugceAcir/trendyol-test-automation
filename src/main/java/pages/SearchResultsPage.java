package pages;

import constants.Timeouts;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ElementHelper;
import utils.TurkishTextHelper;
import utils.WaitHelper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * SearchResultsPage - Trendyol search results page
 *
 * RESPONSIBILITIES:
 * - Product listing and verification
 * - Price filtering (min-max ranges)
 * - Product selection and navigation
 * - Infinite scroll handling (lazy loading)
 * - Brand and product name extraction
 *
 * URL PATTERN: https://www.trendyol.com/sr?q={keyword}
 *
 * TRENDYOL-SPECIFIC BEHAVIORS:
 * - Products open in new tab (requires tab switching)
 * - Lazy loading (24 products initial, more on scroll)
 * - Brand and product name in separate HTML elements
 * - Turkish number formatting (140.000 TL)
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
     * "Did you mean" / No results banner
     * TRENDYOL: Shows "Aradığın ürün bulunamadı. Aşağıdakiler ilgini çekebilir."
     */
    private static final By NO_RESULTS_BANNER = By.cssSelector(".did-you-mean .information-banner");

    /**
     * Sorting dropdown
     * HTML: <button class="select-box">Önerilen Sıralama</button>
     */
    @FindBy(css = "button.select-box")
    private WebElement sortingDropdown;

    // ============================================================
    // DYNAMIC LOCATORS - Robust selectors
    // ============================================================

    /**
     * Product card by index (0-based)
     */
    private By getProductCardByIndex(int index) {
        return By.cssSelector("a.product-card:nth-of-type(" + (index + 1) + ")");
    }

    /**
     * Product brand locator (within product card)
     * TRENDYOL SPECIFIC: Brand is separate from product name
     */
    private static final By PRODUCT_BRAND_LOCATOR = By.cssSelector(".product-brand");

    /**
     * Product name locator (within product card)
     * TRENDYOL SPECIFIC: Name excludes brand
     */
    private static final By PRODUCT_NAME_LOCATOR = By.cssSelector(".product-name");

    /**
     * Product price locator (discounted price)
     */
    private static final By PRODUCT_PRICE_LOCATOR = By.cssSelector(".discounted-price");

    /**
     * No results indicator
     */
    private static final By NO_RESULTS_LOCATOR = By.cssSelector(".empty-result");

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructor - Initializes page and waits for results
     *
     * @param driver - WebDriver instance
     */
    public SearchResultsPage(WebDriver driver) {
        super(driver);
        logger.info("SearchResultsPage initialized");

        // Wait for page to be ready (RULES: smart wait, no hardWait)
        WaitHelper.waitForPageLoad(driver);
        WaitHelper.waitForAjaxToComplete(driver);

        // Wait for products to load
        waitForProductsToLoad();

        // Verify we're on search results page
        verifySearchResultsPage();
    }

    // ============================================================
    // PAGE VERIFICATION
    // ============================================================

    /**
     * Verify search results page loaded successfully
     *
     * CHECKS:
     * - URL contains "/sr" (search results identifier)
     * - Search title is displayed
     * - At least 1 product card is visible
     *
     * RULES: Fail-safe, logs warnings instead of throwing
     */
    public void verifySearchResultsPage() {
        try {
            logger.info("Verifying search results page");

            // Verify URL
            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("/sr")) {
                logger.warn("URL verification failed. Expected '/sr' in URL, got: {}", currentUrl);
            }

            // Verify search title visible
            WaitHelper.waitForElementVisible(driver, searchTitle, Timeouts.ELEMENT_VISIBLE);

            // Verify products loaded
            if (productCards.isEmpty()) {
                logger.warn("No products visible on search results page");
            } else {
                logger.info("Search results page verified: {} products visible", productCards.size());
            }

        } catch (Exception e) {
            logger.error("Search results page verification failed", e);
        }
    }

    /**
     * Wait for products to load (OPTIMIZED)
     *
     * RULES COMPLIANCE:
     * - Uses smart polling (WebDriverWait)
     * - No hardWait/Thread.sleep
     * - Fail-safe with try-catch
     *
     * WHY: Trendyol uses lazy loading, products appear gradually
     */
    private void waitForProductsToLoad() {
        try {
            logger.debug("Waiting for products to load");

            // Smart wait for at least 1 product card (RULES: explicit wait)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Timeouts.SEARCH_RESULTS_LOAD));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.product-card")));

            // Wait for product images to load (check if first product has image src)
            wait.until(driver -> {
                try {
                    WebElement firstProductImage = driver.findElement(By.cssSelector("a.product-card img"));
                    String src = firstProductImage.getAttribute("src");
                    return src != null && !src.isEmpty();
                } catch (Exception e) {
                    return false;
                }
            });

            logger.debug("Products loaded successfully");

        } catch (Exception e) {
            logger.warn("Products not fully loaded within timeout - continuing anyway", e);
        }
    }

    // ============================================================
    // PRODUCT LISTING
    // ============================================================

    /**
     * Get total product count from page
     *
     * PARSES: "67049+ Ürün" → 67049
     * RULES: Fail-safe, returns 0 on error
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

            // Parse number (remove all non-digits)
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
     * RULES: Simple, deterministic, no side effects
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
     * Get search keyword from page title
     *
     * @return search keyword (empty string on error)
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
     * TRENDYOL SPECIFIC:
     * - Shows "Aradığın ürün bulunamadı. Aşağıdakiler ilgini çekebilir."
     * - Still displays recommended products below
     * - This is NOT an empty state, it's a "did you mean" scenario
     *
     * @return true if no results banner displayed
     */
    public boolean isNoResultsMessageDisplayed() {
        try {
            // Check for "did you mean" banner (Aradığın ürün bulunamadı)
            boolean bannerPresent = ElementHelper.isElementPresent(driver, NO_RESULTS_BANNER);

            if (bannerPresent) {
                logger.info("No results banner detected: search returned no matching products");
                return true;
            }

            // Fallback: check for empty state (no products at all)
            By emptyStateLocator = By.cssSelector(".empty-result");
            boolean emptyState = ElementHelper.isElementPresent(driver, emptyStateLocator);

            if (emptyState) {
                logger.info("Empty state detected: absolutely no products");
                return true;
            }

            return false;

        } catch (Exception e) {
            logger.debug("Error checking no results message", e);
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
     * IMPORTANT: Opens in new tab - requires tab switching
     * RULES: Validates input, meaningful error messages
     *
     * @param index - product index
     * @throws IllegalArgumentException if index out of bounds
     */
    public void clickProductByIndex(int index) {
        try {
            logger.info("Clicking product at index: {}", index);

            // Validate index (RULES: no hardcoded assumptions)
            if (index < 0 || index >= productCards.size()) {
                String errorMsg = String.format("Product index %d out of bounds. Available products: %d",
                        index, productCards.size());
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            // Get product element
            WebElement product = productCards.get(index);

            // Scroll to product (may be below fold)
            ElementHelper.scrollToElement(driver, product);

            // Click product
            ElementHelper.safeClick(driver, product);
            logger.info("Product clicked at index: {}", index);

            // Brief pause for new tab to open (OPTIMIZED: microPause instead of hardWait)
            WaitHelper.microPause();

        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation errors
        } catch (Exception e) {
            logger.error("Error clicking product at index: {}", index, e);
            throw new RuntimeException("Failed to click product at index: " + index, e);
        }
    }

    // ============================================================
    // PRODUCT NAME EXTRACTION (TRENDYOL-SPECIFIC)
    // ============================================================

    /**
     * Get product FULL NAME by index (Brand + Product Name)
     *
     * TRENDYOL SPECIFIC:
     * - Brand and name are in separate HTML elements
     * - Brand: <span class="product-brand">Samsung</span>
     * - Name: <span class="product-name">Galaxy A16 4 Gb Ram 128 Gb Gri</span>
     * - Full: "Samsung Galaxy A16 4 Gb Ram 128 Gb Gri"
     *
     * RULES COMPLIANCE:
     * - Single Responsibility: Combines brand + name
     * - Fail-safe: Returns partial info if available
     * - Meaningful logging
     *
     * @param index - product index (0-based)
     * @return full product name (brand + name) or empty string on error
     */
    public String getProductNameByIndex(int index) {
        try {
            logger.debug("Getting product full name at index: {}", index);

            // Validate index
            if (index < 0 || index >= productCards.size()) {
                logger.error("Invalid product index: {}. Available: {}", index, productCards.size());
                return "";
            }

            WebElement product = productCards.get(index);

            // Extract brand name (may not exist for some products)
            String brandName = "";
            try {
                WebElement brandElement = product.findElement(PRODUCT_BRAND_LOCATOR);
                brandName = ElementHelper.safeGetText(driver, brandElement).trim();
            } catch (Exception e) {
                logger.debug("No brand element found at index {} (some products don't have brands)", index);
            }

            // Extract product name (always exists)
            String productName = "";
            try {
                WebElement nameElement = product.findElement(PRODUCT_NAME_LOCATOR);
                productName = ElementHelper.safeGetText(driver, nameElement).trim();
            } catch (Exception e) {
                logger.warn("No product name element found at index {}", index);
            }

            // Combine: "Samsung Galaxy A16..." or just "Galaxy A16..." if no brand
            String fullName = brandName.isEmpty() ? productName : brandName + " " + productName;

            logger.debug("Product full name at index {}: '{}'", index, fullName);
            return fullName;

        } catch (Exception e) {
            logger.error("Error getting product name at index: {}", index, e);
            return "";
        }
    }

    /**
     * Get product BRAND by index
     *
     * USE CASE: When you only need brand for validation
     * EXAMPLE: Verify all products are from "Samsung"
     *
     * @param index - product index (0-based)
     * @return brand name or empty string if no brand/error
     */
    public String getProductBrandByIndex(int index) {
        try {
            logger.debug("Getting product brand at index: {}", index);

            // Validate index
            if (index < 0 || index >= productCards.size()) {
                logger.error("Invalid product index: {}. Available: {}", index, productCards.size());
                return "";
            }

            WebElement product = productCards.get(index);

            try {
                WebElement brandElement = product.findElement(PRODUCT_BRAND_LOCATOR);
                String brand = ElementHelper.safeGetText(driver, brandElement).trim();
                logger.debug("Product brand at index {}: '{}'", index, brand);
                return brand;
            } catch (Exception e) {
                logger.debug("No brand element at index {} (some products don't have explicit brand)", index);
                return "";
            }

        } catch (Exception e) {
            logger.error("Error getting product brand at index: {}", index, e);
            return "";
        }
    }

    // ============================================================
    // PRODUCT PRICE EXTRACTION
    // ============================================================

    /**
     * Get product price by index
     *
     * RETURNS: Raw text like "140.000 TL"
     * RULES: Fail-safe, returns empty string on error
     *
     * @param index - product index
     * @return product price as string
     */
    public String getProductPriceByIndex(int index) {
        try {
            logger.debug("Getting product price at index: {}", index);

            if (index < 0 || index >= productCards.size()) {
                logger.error("Invalid product index: {}. Available: {}", index, productCards.size());
                return "";
            }

            WebElement product = productCards.get(index);
            WebElement priceElement = product.findElement(PRODUCT_PRICE_LOCATOR);

            String price = ElementHelper.safeGetText(driver, priceElement);
            logger.debug("Product price at index {}: '{}'", index, price);
            return price;

        } catch (Exception e) {
            logger.error("Error getting product price at index: {}", index, e);
            return "";
        }
    }

    /**
     * Get product price as double (parsed for calculations)
     *
     * EXAMPLE: "140.000 TL" → 140000.0
     * RULES: Uses utility class (DRY), handles Turkish formatting
     *
     * @param index - product index
     * @return price as double (0.0 on error)
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
     * RULES: Idempotent (safe to call multiple times)
     */
    public void expandPriceFilter() {
        try {
            logger.info("Expanding price filter");

            // Check if already expanded (RULES: no unnecessary actions)
            By containerLocator = By.cssSelector("section[data-aggregationtype='Price'] .aggregation-container");
            WebElement container = driver.findElement(containerLocator);

            String hiddenAttr = container.getAttribute("hidden");
            if (hiddenAttr != null) {
                // Collapsed - click to expand
                ElementHelper.safeClick(driver, priceFilterHeader);

                // Wait for animation (OPTIMIZED: microPause)
                WaitHelper.microPause();
                WaitHelper.microPause(); // 400ms total for animation

                logger.info("Price filter expanded");
            } else {
                logger.debug("Price filter already expanded - no action needed");
            }

        } catch (Exception e) {
            logger.warn("Error expanding price filter - may already be expanded", e);
        }
    }

    /**
     * Apply price filter (min-max range)
     *
     * FLOW:
     * 1. Expand price filter if collapsed
     * 2. Enter min price
     * 3. Enter max price
     * 4. Click search button
     * 5. Wait for results to reload
     *
     * RULES: Clear, sequential steps with logging
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

            // Wait for results to reload (RULES: smart wait, no hardWait)
            WaitHelper.waitForPageLoad(driver);
            WaitHelper.waitForAjaxToComplete(driver);
            waitForProductsToLoad();

            logger.info("Price filter applied successfully");

        } catch (Exception e) {
            logger.error("Error applying price filter: min={}, max={}", minPrice, maxPrice, e);
            throw new RuntimeException("Failed to apply price filter", e);
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
            logger.error("Error applying min price filter: {}", minPrice, e);
            throw new RuntimeException("Failed to apply min price filter", e);
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
            logger.error("Error applying max price filter: {}", maxPrice, e);
            throw new RuntimeException("Failed to apply max price filter", e);
        }
    }

    // ============================================================
    // INFINITE SCROLL (Lazy Loading)
    // ============================================================

    /**
     * Scroll to load more products (infinite scroll)
     *
     * TRENDYOL LAZY LOADING:
     * - Scroll to LAST product (not just bottom)
     * - Products load when last product enters viewport
     *
     * @param scrollCount Number of times to scroll
     */
    public void scrollToLoadMoreProducts(int scrollCount) {
        for (int i = 0; i < scrollCount; i++) {
            try {
                int beforeScrollCount = getVisibleProductCount();
                logger.info("Before scroll " + (i + 1) + ": " + beforeScrollCount + " products");

                // Get all product cards
                java.util.List<org.openqa.selenium.WebElement> products =
                        driver.findElements(By.cssSelector("a.product-card"));

                if (products.isEmpty()) {
                    logger.warn("No products found to scroll to");
                    break;
                }

                // Scroll to LAST product (triggers lazy load)
                org.openqa.selenium.WebElement lastProduct = products.get(products.size() - 1);
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                        lastProduct
                );

                WaitHelper.microPause(); // 200ms for smooth scroll

                // Smart wait for new products (max 5s)
                org.openqa.selenium.support.ui.WebDriverWait wait =
                        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5));

                try {
                    wait.until(driver -> {
                        int currentCount = getVisibleProductCount();
                        return currentCount > beforeScrollCount;
                    });

                    int afterScrollCount = getVisibleProductCount();
                    logger.info("After scroll " + (i + 1) + ": " + afterScrollCount + " products loaded");

                } catch (org.openqa.selenium.TimeoutException e) {
                    logger.debug("No new products loaded (may be at end of results)");
                    break; // Stop scrolling if no more products
                }

            } catch (Exception e) {
                logger.error("Error during scroll " + (i + 1), e);
                break;
            }
        }

        logger.info("Scrolling completed");
    }
    /**
     * Scroll to specific product index
     *
     * USE CASE: Product at index 50, need to scroll to make it visible
     * RULES: Smart approach, scrolls only if needed
     *
     * @param index - product index
     */
    public void scrollToProduct(int index) {
        try {
            logger.info("Scrolling to product at index: {}", index);

            if (index < productCards.size()) {
                // Product already visible - just scroll to it
                WebElement product = productCards.get(index);
                ElementHelper.scrollToElement(driver, product);
            } else {
                // Product not visible yet - scroll to load more
                logger.warn("Product index {} not visible yet. Current: {}. Scrolling to load more.",
                        index, productCards.size());
                scrollToLoadMoreProducts(3);
            }

        } catch (Exception e) {
            logger.error("Error scrolling to product at index: {}", index, e);
        }
    }
    // ==================== NEW LOCATORS (Add to existing locators section) ====================

    /**
     * Sorting dropdown button
     */
    private static final By SORT_DROPDOWN_BTN = By.cssSelector(".web-sort-options button.select-box");

    /**
     * Sorting options list
     */
    private static final By SORT_OPTIONS_LIST = By.cssSelector("ul.select-dropdown");

    /**
     * Individual sort option
     * Options: "Önerilen Sıralama", "En Düşük Fiyat", "En Yüksek Fiyat", "En Yeniler"
     */
    private static final By SORT_OPTION_ITEM = By.cssSelector("ul.select-dropdown li");

    /**
     * Price filter section (collapsed by default)
     */
    private static final By PRICE_FILTER_SECTION = By.cssSelector("section[data-aggregationtype='Price']");

    /**
     * Price filter min input
     */
    private static final By PRICE_MIN_INPUT = By.cssSelector("#price-range-input-min");

    /**
     * Price filter max input
     */
    private static final By PRICE_MAX_INPUT = By.cssSelector("#price-range-input-max");

    /**
     * Price filter apply button
     */
    private static final By PRICE_APPLY_BTN = By.cssSelector("button.price-range-button");

// ==================== NEW METHODS (Add to class) ====================

    /**
     * Select sorting option
     *
     * TRENDYOL OPTIONS:
     * - "Önerilen Sıralama" (Recommended)
     * - "En Düşük Fiyat" (Lowest Price)
     * - "En Yüksek Fiyat" (Highest Price)
     * - "En Yeniler" (Newest)
     * - "En Çok Satan" (Best Selling)
     *
     * @param sortOption Turkish sort option text
     */
    public void selectSortOption(String sortOption) {
        try {
            // Click dropdown to open
            ElementHelper.safeClick(driver, driver.findElement(SORT_DROPDOWN_BTN));
            WaitHelper.microPause();

            logger.info("Selecting sort option: " + sortOption);

            // Find and click option
            java.util.List<org.openqa.selenium.WebElement> options =
                    driver.findElements(SORT_OPTION_ITEM);

            for (org.openqa.selenium.WebElement option : options) {
                String optionText = ElementHelper.safeGetText(driver, option);
                if (optionText.contains(sortOption)) {
                    option.click();
                    logger.info("Sort option selected: " + sortOption);

                    // Wait for results to reload
                    waitForProductsToLoad();
                    return;
                }
            }

            logger.warn("Sort option not found: " + sortOption);

        } catch (Exception e) {
            logger.error("Error selecting sort option: " + sortOption, e);
        }
    }

    /**
     * Apply price filter with min and max
     *
     * @param minPrice Minimum price (e.g., 5000)
     * @param maxPrice Maximum price (e.g., 10000)
     */
    public void applyPriceFilter(String minPrice, String maxPrice) {
        try {
            // Expand price filter section if collapsed
            expandFilterSection(PRICE_FILTER_SECTION);

            logger.info("Applying price filter: " + minPrice + " - " + maxPrice);

            // Enter min price
            org.openqa.selenium.WebElement minInput = driver.findElement(PRICE_MIN_INPUT);
            minInput.clear();
            minInput.sendKeys(minPrice);
            WaitHelper.microPause();

            // Enter max price
            org.openqa.selenium.WebElement maxInput = driver.findElement(PRICE_MAX_INPUT);
            maxInput.clear();
            maxInput.sendKeys(maxPrice);
            WaitHelper.microPause();

            // Click apply button
            ElementHelper.safeClick(driver, driver.findElement(PRICE_APPLY_BTN));

            logger.info("Price filter applied successfully");

            // Wait for filtered results
            waitForProductsToLoad();

        } catch (Exception e) {
            logger.error("Error applying price filter", e);
        }
    }

    /**
     * Apply minimum price filter only
     *
     * @param minPrice Minimum price
     */
    public void applyMinPriceFilter(String minPrice) {
        applyPriceFilter(minPrice, "");
    }

    /**
     * Apply maximum price filter only
     *
     * @param maxPrice Maximum price
     */
    public void applyMaxPriceFilter(String maxPrice) {
        applyPriceFilter("", maxPrice);
    }

    /**
     * Expand filter section if collapsed
     *
     * @param sectionLocator Section locator
     */
    private void expandFilterSection(By sectionLocator) {
        try {
            org.openqa.selenium.WebElement section = driver.findElement(sectionLocator);

            // Check if section is collapsed
            if (section.getAttribute("class").contains("collapsed")) {
                // Click expand button
                org.openqa.selenium.WebElement expandBtn =
                        section.findElement(By.cssSelector("button.expand-collapse-button"));
                expandBtn.click();
                WaitHelper.microPause();
                logger.debug("Filter section expanded");
            }

        } catch (Exception e) {
            logger.debug("Section already expanded or error expanding", e);
        }
    }

    /**
     * Get first product price (for sorting validation)
     *
     * @return Price as double (parsed from "9.499 TL" format)
     */
    public double getFirstProductPrice() {
        try {
            String priceText = getProductPriceByIndex(0);

            // Parse "9.499 TL" → 9499.0
            String numericPrice = priceText
                    .replace(" TL", "")
                    .replace(".", "")
                    .replace(",", ".");

            return Double.parseDouble(numericPrice);

        } catch (Exception e) {
            logger.error("Error getting first product price", e);
            return 0.0;
        }
    }

    /**
     * Get last visible product price
     *
     * @return Price as double
     */
    public double getLastVisibleProductPrice() {
        try {
            int lastIndex = getVisibleProductCount() - 1;
            String priceText = getProductPriceByIndex(lastIndex);

            String numericPrice = priceText
                    .replace(" TL", "")
                    .replace(".", "")
                    .replace(",", ".");

            return Double.parseDouble(numericPrice);

        } catch (Exception e) {
            logger.error("Error getting last product price", e);
            return 0.0;
        }
    }


}