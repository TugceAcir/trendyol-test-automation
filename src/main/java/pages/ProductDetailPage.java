package pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ElementHelper;
import utils.WaitHelper;

/**
 * Product Detail Page Object
 *
 * TRENDYOL SPECIFIC:
 * - Products open in NEW TAB when clicked
 * - Price displayed in "discounted" class
 * - Brand in product title with <strong> tag
 */
public class ProductDetailPage extends BasePage {
    private static final Logger logger = LogManager.getLogger(ProductDetailPage.class);

    // ==================== LOCATORS ====================

    /**
     * Product title with brand name
     * Example: "LENOVO IdeaPad Slim 3..."
     */
    private static final By PRODUCT_TITLE = By.cssSelector("h1.product-title");

    /**
     * Brand name (bold part of title)
     */
    private static final By BRAND_NAME = By.cssSelector("h1.product-title strong");

    /**
     * Discounted price
     */
    private static final By DISCOUNTED_PRICE = By.cssSelector(".price-container .discounted");

    /**
     * Add to cart button
     */
    private static final By ADD_TO_CART_BTN = By.cssSelector("button[data-testid='add-to-cart-button']");

    /**
     * Product images
     */
    private static final By PRODUCT_IMAGES = By.cssSelector(".product-details-product-details-container img");

    // ==================== CONSTRUCTOR ====================

    public ProductDetailPage(WebDriver driver) {
        super(driver);
        logger.info("ProductDetailPage initialized");
    }

    // ==================== VERIFICATION METHODS ====================

    /**
     * Verify product detail page loaded
     *
     * @return true if page loaded successfully
     */
    public boolean isProductDetailPageLoaded() {
        try {
            boolean titlePresent = ElementHelper.isElementPresent(driver, PRODUCT_TITLE);
            boolean pricePresent = ElementHelper.isElementPresent(driver, DISCOUNTED_PRICE);

            if (titlePresent && pricePresent) {
                logger.info("Product detail page loaded successfully");
                return true;
            }

            logger.warn("Product detail page not loaded properly");
            return false;

        } catch (Exception e) {
            logger.error("Error verifying product detail page", e);
            return false;
        }
    }

    // ==================== GETTER METHODS ====================

    /**
     * Get product title (brand + name)
     *
     * @return Full product title
     */
    public String getProductTitle() {
        try {
            String title = ElementHelper.safeGetText(driver, driver.findElement(PRODUCT_TITLE));
            logger.info("Product title: " + title);
            return title;

        } catch (Exception e) {
            logger.error("Error getting product title", e);
            return "";
        }
    }

    /**
     * Get brand name only
     *
     * @return Brand name (e.g., "LENOVO")
     */
    public String getBrandName() {
        try {
            String brand = ElementHelper.safeGetText(driver, driver.findElement(BRAND_NAME));
            logger.info("Brand name: " + brand);
            return brand;

        } catch (Exception e) {
            logger.error("Error getting brand name", e);
            return "";
        }
    }

    /**
     * Get discounted price
     *
     * @return Price as string (e.g., "9.499 TL")
     */
    public String getDiscountedPrice() {
        try {
            String price = ElementHelper.safeGetText(driver, driver.findElement(DISCOUNTED_PRICE));
            logger.info("Discounted price: " + price);
            return price;

        } catch (Exception e) {
            logger.error("Error getting price", e);
            return "";
        }
    }

    /**
     * Check if Add to Cart button is displayed
     *
     * @return true if button is visible
     */
    public boolean isAddToCartButtonDisplayed() {
        try {
            boolean displayed = ElementHelper.isElementPresent(driver, ADD_TO_CART_BTN);
            logger.info("Add to cart button displayed: " + displayed);
            return displayed;

        } catch (Exception e) {
            logger.error("Error checking add to cart button", e);
            return false;
        }
    }
}