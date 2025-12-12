package pages;

import constants.Timeouts;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.ElementHelper;
import utils.TurkishTextHelper;
import utils.WaitHelper;

import java.util.List;

/**
 * CartPage - Trendyol shopping cart page
 *
 * RESPONSIBILITIES:
 * - View cart items
 * - Update quantity (increase/decrease)
 * - Remove items from cart
 * - View cart total
 * - Proceed to checkout
 * - Handle empty cart state
 *
 * URL: https://www.trendyol.com/sepet or /sepetim
 */
public class CartPage extends BasePage {

    // ============================================================
    // PAGE ELEMENTS
    // ============================================================

    /**
     * All cart items (products in cart)
     */
    @FindBy(css = ".merchant-item-container")
    private List<WebElement> cartItems;

    /**
     * Product name in cart
     * NOTE: First element is brand (bold), rest is product name
     */
    @FindBy(css = ".product-name")
    private List<WebElement> productNames;

    /**
     * Product brand in cart
     */
    @FindBy(css = ".product-brand-name")
    private List<WebElement> productBrands;

    /**
     * Product price in cart
     */
    @FindBy(css = ".basket-product-price-text")
    private List<WebElement> productPrices;

    /**
     * Quantity selector input
     * HTML: <input data-testid="quantity-selector" value="1">
     */
    @FindBy(css = "input[data-testid='quantity-selector']")
    private List<WebElement> quantitySelectors;

    /**
     * Quantity increase button (+)
     * HTML: <button data-testid="quantity-button-increment">
     */
    @FindBy(css = "button[data-testid='quantity-button-increment']")
    private List<WebElement> increaseButtons;

    /**
     * Quantity decrease button (-)
     * HTML: <button data-testid="quantity-button-decrement">
     */
    @FindBy(css = "button[data-testid='quantity-button-decrement']")
    private List<WebElement> decreaseButtons;

    /**
     * Remove item button (Sil)
     */
    @FindBy(css = ".remove-item-container")
    private List<WebElement> removeButtons;

    /**
     * Cart total price
     * HTML: <div class="order-total"><p class="price">140.044,99 TL</p>
     */
    @FindBy(css = ".order-total .price")
    private WebElement cartTotalPrice;

    /**
     * Subtotal (Ara Toplam)
     */
    @FindBy(css = "[data-testid='basket-summary-subtotal-value']")
    private WebElement subtotalPrice;

    /**
     * Cargo price (Kargo Tutarı)
     */
    @FindBy(css = "[data-testid='basket-summary-cargo-value']")
    private WebElement cargoPrice;

    /**
     * Checkout button (Sepeti Onayla)
     * HTML: <button data-testid="checkout-button">
     */
    @FindBy(css = "button[data-testid='checkout-button']")
    private WebElement checkoutButton;

    /**
     * Empty cart message
     * HTML: <p>Sepetinde ürün bulunmamaktadır.</p>
     */
    @FindBy(css = ".empty-basket-container p")
    private WebElement emptyCartMessage;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Constructor
     *
     * @param driver - WebDriver instance
     */
    public CartPage(WebDriver driver) {
        super(driver);
        logger.info("CartPage initialized");

        // Wait for page to load
        WaitHelper.waitForPageLoad(driver);
        WaitHelper.waitForAjaxToComplete(driver);

        // Small wait for cart items to load
        WaitHelper.hardWait(1000);

        // Verify we're on cart page
        verifyCartPage();
    }

    // ============================================================
    // PAGE VERIFICATION
    // ============================================================

    /**
     * Verify cart page loaded
     *
     * CHECKS:
     * - URL contains "/sepet"
     * - Either cart items OR empty cart message displayed
     */
    public void verifyCartPage() {
        try {
            logger.info("Verifying cart page");

            // Check URL
            String currentUrl = getCurrentUrl();
            if (!currentUrl.contains("/sepet")) {
                logger.warn("Not on cart page. URL: {}", currentUrl);
            }

            // Check if cart loaded (either items or empty message)
            if (!cartItems.isEmpty()) {
                logger.info("Cart page verified: {} items in cart", cartItems.size());
            } else if (isElementDisplayed(emptyCartMessage)) {
                logger.info("Cart page verified: Cart is empty");
            } else {
                logger.warn("Cart page state unclear");
            }

        } catch (Exception e) {
            logger.error("Cart page verification failed", e);
        }
    }

    /**
     * Check if cart page loaded
     *
     * @return true if cart elements visible
     */
    public boolean isCartPageLoaded() {
        try {
            String url = getCurrentUrl();
            return url.contains("/sepet");
        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================
    // CART STATE
    // ============================================================

    /**
     * Check if cart is empty
     *
     * @return true if no items in cart
     */
    public boolean isCartEmpty() {
        try {
            boolean empty = cartItems.isEmpty() || isElementDisplayed(emptyCartMessage);
            logger.debug("Cart is empty: {}", empty);
            return empty;
        } catch (Exception e) {
            logger.error("Error checking if cart is empty", e);
            return true;
        }
    }

    /**
     * Get number of items in cart
     *
     * @return item count
     */
    public int getItemCount() {
        try {
            int count = cartItems.size();
            logger.info("Cart item count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error getting cart item count", e);
            return 0;
        }
    }

    /**
     * Get empty cart message
     *
     * EXPECTED: "Sepetinde ürün bulunmamaktadır."
     *
     * @return empty cart message or empty string
     */
    public String getEmptyCartMessage() {
        try {
            if (isElementDisplayed(emptyCartMessage)) {
                String message = ElementHelper.safeGetText(driver, emptyCartMessage);
                logger.debug("Empty cart message: '{}'", message);
                return message;
            }
            return "";
        } catch (Exception e) {
            logger.error("Error getting empty cart message", e);
            return "";
        }
    }

    // ============================================================
    // PRODUCT INFORMATION
    // ============================================================

    /**
     * Get product name by index
     *
     * NOTE: Excludes brand name (first bold element)
     *
     * @param index - product index (0-based)
     * @return product name
     */
    public String getProductNameByIndex(int index) {
        try {
            logger.debug("Getting product name at index: {}", index);

            if (index < 0 || index >= productNames.size()) {
                logger.error("Invalid product index: {}", index);
                return "";
            }

            String name = ElementHelper.safeGetText(driver, productNames.get(index));
            logger.debug("Product name at index {}: '{}'", index, name);
            return name;

        } catch (Exception e) {
            logger.error("Error getting product name at index: {}", index, e);
            return "";
        }
    }

    /**
     * Get product brand by index
     *
     * @param index - product index
     * @return brand name (e.g., "Apple")
     */
    public String getProductBrandByIndex(int index) {
        try {
            logger.debug("Getting product brand at index: {}", index);

            if (index < 0 || index >= productBrands.size()) {
                logger.error("Invalid product index: {}", index);
                return "";
            }

            String brand = ElementHelper.safeGetText(driver, productBrands.get(index));
            logger.debug("Product brand at index {}: '{}'", index, brand);
            return brand;

        } catch (Exception e) {
            logger.error("Error getting product brand at index: {}", index, e);
            return "";
        }
    }

    /**
     * Get product price by index
     *
     * RETURNS: "140.000 TL"
     *
     * @param index - product index
     * @return price as string
     */
    public String getProductPriceByIndex(int index) {
        try {
            logger.debug("Getting product price at index: {}", index);

            if (index < 0 || index >= productPrices.size()) {
                logger.error("Invalid product index: {}", index);
                return "";
            }

            String price = ElementHelper.safeGetText(driver, productPrices.get(index));
            logger.debug("Product price at index {}: '{}'", index, price);
            return price;

        } catch (Exception e) {
            logger.error("Error getting product price at index: {}", index, e);
            return "";
        }
    }

    /**
     * Get product price as double by index
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

    /**
     * Get product quantity by index
     *
     * @param index - product index
     * @return quantity
     */
    public int getProductQuantityByIndex(int index) {
        try {
            logger.debug("Getting product quantity at index: {}", index);

            if (index < 0 || index >= quantitySelectors.size()) {
                logger.error("Invalid product index: {}", index);
                return 0;
            }

            String quantityText = ElementHelper.getAttribute(driver, quantitySelectors.get(index), "value");
            int quantity = Integer.parseInt(quantityText);
            logger.debug("Product quantity at index {}: {}", index, quantity);
            return quantity;

        } catch (Exception e) {
            logger.error("Error getting product quantity at index: {}", index, e);
            return 0;
        }
    }

    // ============================================================
    // QUANTITY MANAGEMENT
    // ============================================================

    /**
     * Increase product quantity by index
     *
     * CLICKS: + button
     *
     * @param index - product index
     */
    public void increaseQuantityByIndex(int index) {
        try {
            logger.info("Increasing quantity for product at index: {}", index);

            if (index < 0 || index >= increaseButtons.size()) {
                logger.error("Invalid product index: {}", index);
                throw new IllegalArgumentException("Product index out of bounds: " + index);
            }

            int currentQuantity = getProductQuantityByIndex(index);
            logger.debug("Current quantity: {}", currentQuantity);

            // Click increase button
            ElementHelper.safeClick(driver, increaseButtons.get(index));

            // Wait for cart update
            WaitHelper.waitForAjaxToComplete(driver);
            WaitHelper.hardWait(1000);

            int newQuantity = getProductQuantityByIndex(index);
            logger.info("Quantity increased from {} to {}", currentQuantity, newQuantity);

        } catch (Exception e) {
            logger.error("Error increasing quantity at index: {}", index, e);
            throw e;
        }
    }

    /**
     * Decrease product quantity by index
     *
     * CLICKS: - button
     *
     * NOTE: If quantity = 1, this won't work (button disabled)
     *
     * @param index - product index
     */
    public void decreaseQuantityByIndex(int index) {
        try {
            logger.info("Decreasing quantity for product at index: {}", index);

            if (index < 0 || index >= decreaseButtons.size()) {
                logger.error("Invalid product index: {}", index);
                throw new IllegalArgumentException("Product index out of bounds: " + index);
            }

            int currentQuantity = getProductQuantityByIndex(index);

            if (currentQuantity <= 1) {
                logger.warn("Cannot decrease quantity below 1. Current: {}", currentQuantity);
                return;
            }

            logger.debug("Current quantity: {}", currentQuantity);

            // Click decrease button
            ElementHelper.safeClick(driver, decreaseButtons.get(index));

            // Wait for cart update
            WaitHelper.waitForAjaxToComplete(driver);
            WaitHelper.hardWait(1000);

            int newQuantity = getProductQuantityByIndex(index);
            logger.info("Quantity decreased from {} to {}", currentQuantity, newQuantity);

        } catch (Exception e) {
            logger.error("Error decreasing quantity at index: {}", index, e);
            throw e;
        }
    }

    /**
     * Set product quantity to specific value
     *
     * STRATEGY: Click + or - multiple times to reach target
     *
     * @param index - product index
     * @param targetQuantity - desired quantity
     */
    public void setQuantityByIndex(int index, int targetQuantity) {
        try {
            logger.info("Setting quantity to {} for product at index: {}", targetQuantity, index);

            int currentQuantity = getProductQuantityByIndex(index);

            if (currentQuantity == targetQuantity) {
                logger.info("Quantity already at target: {}", targetQuantity);
                return;
            }

            if (currentQuantity < targetQuantity) {
                // Need to increase
                int clicksNeeded = targetQuantity - currentQuantity;
                for (int i = 0; i < clicksNeeded; i++) {
                    increaseQuantityByIndex(index);
                }
            } else {
                // Need to decrease
                int clicksNeeded = currentQuantity - targetQuantity;
                for (int i = 0; i < clicksNeeded; i++) {
                    decreaseQuantityByIndex(index);
                }
            }

            logger.info("Quantity set to: {}", targetQuantity);

        } catch (Exception e) {
            logger.error("Error setting quantity", e);
            throw e;
        }
    }

    // ============================================================
    // REMOVE ITEMS
    // ============================================================

    /**
     * Remove product from cart by index
     *
     * CLICKS: Sil (trash icon)
     *
     * @param index - product index
     */
    public void removeProductByIndex(int index) {
        try {
            logger.info("Removing product from cart at index: {}", index);

            if (index < 0 || index >= removeButtons.size()) {
                logger.error("Invalid product index: {}", index);
                throw new IllegalArgumentException("Product index out of bounds: " + index);
            }

            // Click remove button
            ElementHelper.safeClick(driver, removeButtons.get(index));

            // Wait for cart update
            WaitHelper.waitForAjaxToComplete(driver);
            WaitHelper.hardWait(1500); // Wait for removal animation

            logger.info("Product removed from cart at index: {}", index);

        } catch (Exception e) {
            logger.error("Error removing product at index: {}", index, e);
            throw e;
        }
    }

    /**
     * Remove first product from cart
     */
    public void removeFirstProduct() {
        removeProductByIndex(0);
    }

    /**
     * Remove all products from cart
     *
     * NOTE: Removes one by one
     */
    public void removeAllProducts() {
        try {
            logger.info("Removing all products from cart");

            int itemCount = getItemCount();
            logger.debug("Total items to remove: {}", itemCount);

            // Always remove index 0 (as items shift after removal)
            for (int i = 0; i < itemCount; i++) {
                removeProductByIndex(0);
                WaitHelper.hardWait(1000);
            }

            logger.info("All products removed from cart");

        } catch (Exception e) {
            logger.error("Error removing all products", e);
            throw e;
        }
    }

    // ============================================================
    // CART TOTALS
    // ============================================================

    /**
     * Get subtotal (Ara Toplam)
     *
     * RETURNS: "140.000 TL"
     *
     * @return subtotal as string
     */
    public String getSubtotal() {
        try {
            logger.debug("Getting cart subtotal");
            String subtotal = ElementHelper.safeGetText(driver, subtotalPrice);
            logger.info("Cart subtotal: '{}'", subtotal);
            return subtotal;
        } catch (Exception e) {
            logger.error("Error getting subtotal", e);
            return "";
        }
    }

    /**
     * Get subtotal as double
     *
     * @return subtotal as double
     */
    public double getSubtotalAsDouble() {
        String subtotalText = getSubtotal();
        return TurkishTextHelper.parseTurkishNumber(subtotalText);
    }

    /**
     * Get cargo price (Kargo Tutarı)
     *
     * RETURNS: "44,99 TL"
     *
     * @return cargo price as string
     */
    public String getCargoPrice() {
        try {
            logger.debug("Getting cargo price");
            String cargo = ElementHelper.safeGetText(driver, cargoPrice);
            logger.info("Cargo price: '{}'", cargo);
            return cargo;
        } catch (Exception e) {
            logger.error("Error getting cargo price", e);
            return "";
        }
    }

    /**
     * Get cargo price as double
     *
     * @return cargo price as double
     */
    public double getCargoPriceAsDouble() {
        String cargoText = getCargoPrice();
        return TurkishTextHelper.parseTurkishNumber(cargoText);
    }

    /**
     * Get cart total (Toplam)
     *
     * RETURNS: "140.044,99 TL"
     *
     * @return total price as string
     */
    public String getCartTotal() {
        try {
            logger.info("Getting cart total");
            String total = ElementHelper.safeGetText(driver, cartTotalPrice);
            logger.info("Cart total: '{}'", total);
            return total;
        } catch (Exception e) {
            logger.error("Error getting cart total", e);
            return "";
        }
    }

    /**
     * Get cart total as double
     *
     * EXAMPLE: "140.044,99 TL" → 140044.99
     *
     * @return total as double
     */
    public double getCartTotalAsDouble() {
        String totalText = getCartTotal();
        return TurkishTextHelper.parseTurkishNumber(totalText);
    }

    // ============================================================
    // CHECKOUT
    // ============================================================

    /**
     * Proceed to checkout
     *
     * CLICKS: "Sepeti Onayla" button
     *
     * NOTE: This leads to login/address/payment flow
     * Our framework stops here (no real purchase)
     */
    public void proceedToCheckout() {
        try {
            logger.info("Proceeding to checkout");

            // Wait for button to be clickable
            WaitHelper.waitForElementClickable(driver, checkoutButton, Timeouts.ELEMENT_CLICKABLE);

            // Click checkout
            ElementHelper.safeClick(driver, checkoutButton);
            logger.info("Checkout button clicked");

            // Wait for next page
            WaitHelper.waitForPageLoad(driver);

            logger.info("Navigated to checkout flow");

        } catch (Exception e) {
            logger.error("Error proceeding to checkout", e);
            throw e;
        }
    }

    /**
     * Check if checkout button is enabled
     *
     * @return true if button clickable
     */
    public boolean isCheckoutButtonEnabled() {
        return isElementEnabled(checkoutButton);
    }

    /**
     * Check if checkout button is displayed
     *
     * @return true if button visible
     */
    public boolean isCheckoutButtonDisplayed() {
        return isElementDisplayed(checkoutButton);
    }
}