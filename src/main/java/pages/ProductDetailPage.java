package pages;

import constants.Timeouts;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.ElementHelper;
import utils.TurkishTextHelper;
import utils.WaitHelper;

/**
 * ProductDetailPage - Trendyol product detail page
 *
 * RESPONSIBILITIES:
 * - Product information display
 * - Add to cart functionality
 * - Product images
 * - Size/Color selection (if applicable)
 *
 * URL: https://www.trendyol.com/.../p-{productId}
 *
 * IMPORTANT: This page opens in NEW TAB when clicked from search results!
 * Remember to use switchToNewTab() from BasePage before creating this page.
 */
public class ProductDetailPage extends BasePage {

    // ============================================================
    // PAGE ELEMENTS
    // ============================================================

    /**
     * Product title (name)
     * HTML: <h1 data-testid="product-title">
     */
    @FindBy(css = "h1[data-testid='product-title']")
    private WebElement productTitle;

    /**
     * Product price (discounted price)
     * HTML: <span class="discounted">140.000 TL</span>
     */
    @FindBy(css = ".price-view .discounted")
    private WebElement productPrice;

    /**
     * Product image
     * HTML: <img data-testid="image">
     */
    @FindBy(css = "img[data-testid='image']")
    private WebElement productImage;

    /**
     * Add to cart button - Most important element!
     * HTML: <button data-testid="add-to-cart-button">Sepete Ekle</button>
     */
    @FindBy(css = "button[data-testid='add-to-cart-button']")
    private WebElement addToCartButton;

    /**
     * Product brand name
     * HTML: <strong>Apple</strong> (inside product title)
     */
    @FindBy(css = "h1[data-testid='product-title'] strong")
    private WebElement productBrand;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructor
     *
     * IMPORTANT: Call this AFTER switching to product tab!
     *
     * FLOW:
     * 1. Search results page
     * 2. Click product → new tab opens
     * 3. switchToNewTab()
     * 4. new ProductDetailPage(driver)
     *
     * @param driver - WebDriver instance
     */
    public ProductDetailPage(WebDriver driver) {
        super(driver);
        logger.info("ProductDetailPage initialized");

        // Wait for page to load
        WaitHelper.waitForPageLoad(driver);
        WaitHelper.waitForAjaxToComplete(driver);

        // Wait for product details to load
        waitForProductDetailsToLoad();

        // Verify we're on product detail page
        verifyProductDetailPage();
    }

    // ============================================================
    // PAGE VERIFICATION
    // ============================================================

    /**
     * Verify product detail page loaded
     *
     * CHECKS:
     * - URL contains "/p-"
     * - Product title displayed
     * - Add to cart button displayed
     */
    public void verifyProductDetailPage() {
        try {
            logger.info("Verifying product detail page");

            // Check URL
            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("/p-")) {
                logger.warn("Not on product detail page. URL: {}", currentUrl);
            }

            // Check product title
            WaitHelper.waitForElementVisible(driver, productTitle, Timeouts.ELEMENT_VISIBLE);

            // Check add to cart button
            WaitHelper.waitForElementVisible(driver, addToCartButton, Timeouts.ELEMENT_VISIBLE);

            logger.info("Product detail page verified successfully");

        } catch (Exception e) {
            logger.error("Product detail page verification failed", e);
        }
    }

    /**
     * Wait for product details to load
     *
     * WHY: Product images, prices load dynamically
     */
    private void waitForProductDetailsToLoad() {
        try {
            logger.debug("Waiting for product details to load");

            // Wait for product title
            WaitHelper.waitForElementVisible(driver, productTitle, Timeouts.ELEMENT_VISIBLE);

            // Wait for price
            WaitHelper.waitForElementVisible(driver, productPrice, Timeouts.ELEMENT_VISIBLE);

            // Wait for image
            WaitHelper.waitForElementVisible(driver, productImage, Timeouts.PRODUCT_IMAGE_LOAD);

            logger.debug("Product details loaded successfully");

        } catch (Exception e) {
            logger.warn("Product details not fully loaded within timeout", e);
        }
    }

    /**
     * Check if product detail page loaded
     *
     * @return true if product title displayed
     */
    public boolean isProductDetailPageLoaded() {
        return isElementDisplayed(productTitle);
    }

    // ============================================================
    // PRODUCT INFORMATION
    // ============================================================

    /**
     * Get product title (full name)
     *
     * EXAMPLE: "Apple 16" MacBook Pro: Apple M4 Pro chip..."
     *
     * @return product title
     */
    public String getProductTitle() {
        try {
            logger.info("Getting product title");
            String title = ElementHelper.safeGetText(driver, productTitle);
            logger.info("Product title: '{}'", title);
            return title;
        } catch (Exception e) {
            logger.error("Error getting product title", e);
            return "";
        }
    }

    /**
     * Get product brand name
     *
     * EXAMPLE: "Apple"
     *
     * @return brand name
     */
    public String getProductBrand() {
        try {
            logger.debug("Getting product brand");
            String brand = ElementHelper.safeGetText(driver, productBrand);
            logger.debug("Product brand: '{}'", brand);
            return brand;
        } catch (Exception e) {
            logger.error("Error getting product brand", e);
            return "";
        }
    }

    /**
     * Get product price (as text)
     *
     * RETURNS: "140.000 TL"
     *
     * @return price as string
     */
    public String getProductPrice() {
        try {
            logger.info("Getting product price");
            String price = ElementHelper.safeGetText(driver, productPrice);
            logger.info("Product price: '{}'", price);
            return price;
        } catch (Exception e) {
            logger.error("Error getting product price", e);
            return "";
        }
    }

    /**
     * Get product price as double (parsed)
     *
     * EXAMPLE: "140.000 TL" → 140000.0
     *
     * USES: TurkishTextHelper to parse Turkish number format
     *
     * @return price as double
     */
    public double getProductPriceAsDouble() {
        String priceText = getProductPrice();
        double price = TurkishTextHelper.parseTurkishNumber(priceText);
        logger.debug("Product price as double: {}", price);
        return price;
    }

    /**
     * Check if product image is displayed
     *
     * @return true if image visible
     */
    public boolean isProductImageDisplayed() {
        return isElementDisplayed(productImage);
    }

    /**
     * Get product image URL
     *
     * @return image URL
     */
    public String getProductImageUrl() {
        try {
            String imageUrl = ElementHelper.getAttribute(driver, productImage, "src");
            logger.debug("Product image URL: {}", imageUrl);
            return imageUrl;
        } catch (Exception e) {
            logger.error("Error getting product image URL", e);
            return "";
        }
    }

    // ============================================================
    // ADD TO CART FUNCTIONALITY
    // ============================================================

    /**
     * Add product to cart
     *
     * FLOW:
     * 1. Wait for button to be clickable
     * 2. Click "Sepete Ekle" button
     * 3. Wait for cart update
     *
     * NOTE: After this, button text changes to "Sepete Eklendi"
     */
    public void addToCart() {
        try {
            logger.info("Adding product to cart");

            // Wait for button to be clickable
            WaitHelper.waitForElementClickable(driver, addToCartButton, Timeouts.ELEMENT_CLICKABLE);

            // Scroll to button (might be below fold)
            ElementHelper.scrollToElement(driver, addToCartButton);

            // Click add to cart
            ElementHelper.safeClick(driver, addToCartButton);
            logger.info("'Sepete Ekle' button clicked");

            // Wait for cart update
            WaitHelper.waitForAjaxToComplete(driver);
            WaitHelper.hardWait(1000); // Wait for button state change

            logger.info("Product added to cart successfully");

        } catch (Exception e) {
            logger.error("Error adding product to cart", e);
            throw e;
        }
    }

    /**
     * Check if "Add to Cart" button is enabled
     *
     * USE CASE: Check if product is in stock
     *
     * @return true if button enabled (product available)
     */
    public boolean isAddToCartButtonEnabled() {
        try {
            boolean enabled = isElementEnabled(addToCartButton);
            logger.debug("Add to cart button enabled: {}", enabled);
            return enabled;
        } catch (Exception e) {
            logger.error("Error checking add to cart button state", e);
            return false;
        }
    }

    /**
     * Check if "Add to Cart" button is displayed
     *
     * @return true if button visible
     */
    public boolean isAddToCartButtonDisplayed() {
        return isElementDisplayed(addToCartButton);
    }

    /**
     * Get add to cart button text
     *
     * STATES:
     * - "Sepete Ekle" (initial)
     * - "Sepete Eklendi" (after adding)
     * - "Tükendi" (out of stock)
     *
     * @return button text
     */
    public String getAddToCartButtonText() {
        try {
            String buttonText = ElementHelper.safeGetText(driver, addToCartButton);
            logger.debug("Add to cart button text: '{}'", buttonText);
            return buttonText;
        } catch (Exception e) {
            logger.error("Error getting add to cart button text", e);
            return "";
        }
    }

    /**
     * Check if product was successfully added to cart
     *
     * CHECKS: Button text changed to "Sepete Eklendi"
     *
     * @return true if product in cart
     */
    public boolean isProductAddedToCart() {
        try {
            String buttonText = getAddToCartButtonText();
            boolean added = buttonText.contains("Sepete Eklendi");
            logger.debug("Product added to cart: {}", added);
            return added;
        } catch (Exception e) {
            logger.error("Error checking if product added to cart", e);
            return false;
        }
    }

    /**
     * Check if product is out of stock
     *
     * CHECKS: Button text contains "Tükendi" or button disabled
     *
     * @return true if out of stock
     */
    public boolean isProductOutOfStock() {
        try {
            String buttonText = getAddToCartButtonText();
            boolean outOfStock = buttonText.contains("Tükendi") || !isAddToCartButtonEnabled();
            logger.debug("Product out of stock: {}", outOfStock);
            return outOfStock;
        } catch (Exception e) {
            logger.error("Error checking if product out of stock", e);
            return false;
        }
    }

    // ============================================================
    // NAVIGATION
    // ============================================================

    /**
     * Go to cart page
     *
     * USE CASE: After adding to cart, verify in cart
     *
     * NOTE: Uses header cart icon from HomePage (inherited)
     */
    public void goToCart() {
        try {
            logger.info("Navigating to cart page");

            // Click cart icon (inherited from BasePage/HomePage methods)
            // Navigate to cart URL directly
            driver.get("https://www.trendyol.com/sepet");

            WaitHelper.waitForPageLoad(driver);
            logger.info("Navigated to cart page");

        } catch (Exception e) {
            logger.error("Error navigating to cart", e);
            throw e;
        }
    }

    /**
     * Close this tab and return to search results
     *
     * USE CASE: View multiple products from search results
     *
     * FLOW:
     * 1. Close current product tab
     * 2. Switch back to search results tab
     */
    public void closeAndReturnToSearchResults() {
        try {
            logger.info("Closing product tab and returning to search results");
            closeCurrentTabAndSwitchToOriginal();
            logger.info("Returned to search results");
        } catch (Exception e) {
            logger.error("Error closing tab and returning", e);
            throw e;
        }
    }
}