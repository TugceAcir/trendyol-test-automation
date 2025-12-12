package constants;

/**
 * Central repository for all timeout values
 * Provides consistent timing across the framework
 */
public class Timeouts {
    
    // ============================================================
    // WAIT TIMEOUTS (in seconds)
    // ============================================================
    
    /**
     * Implicit wait - applied globally to element location
     * Used as a baseline for findElement operations
     */
    public static final int IMPLICIT_WAIT = 10;
    
    /**
     * Explicit wait - maximum time for specific conditions
     * Used with WebDriverWait for dynamic waits
     */
    public static final int EXPLICIT_WAIT = 20;
    
    /**
     * Short wait - for quick operations
     * Example: Button click, simple text verification
     */
    public static final int SHORT_WAIT = 5;
    
    /**
     * Medium wait - for standard operations
     * Example: Form submission, simple page transitions
     */
    public static final int MEDIUM_WAIT = 15;
    
    /**
     * Long wait - for slow operations
     * Example: Report generation, complex data loading
     */
    public static final int LONG_WAIT = 30;
    
    /**
     * Extra long wait - for very slow operations
     * Example: File upload, payment processing
     */
    public static final int EXTRA_LONG_WAIT = 60;
    
    // ============================================================
    // PAGE LOAD TIMEOUTS
    // ============================================================
    
    /**
     * Standard page load timeout
     */
    public static final int PAGE_LOAD_TIMEOUT = 30;
    
    /**
     * Ajax request timeout
     */
    public static final int AJAX_TIMEOUT = 20;
    
    /**
     * JavaScript execution timeout
     */
    public static final int SCRIPT_TIMEOUT = 30;
    
    // ============================================================
    // ELEMENT-SPECIFIC TIMEOUTS
    // ============================================================
    
    /**
     * Wait for element to be visible
     */
    public static final int ELEMENT_VISIBLE = 15;
    
    /**
     * Wait for element to be clickable
     */
    public static final int ELEMENT_CLICKABLE = 15;
    
    /**
     * Wait for element to disappear
     */
    public static final int ELEMENT_INVISIBLE = 10;
    
    /**
     * Wait for stale element resolution
     */
    public static final int STALE_ELEMENT = 10;
    
    // ============================================================
    // TRENDYOL-SPECIFIC TIMEOUTS
    // ============================================================
    
    /**
     * Product image loading timeout
     * Trendyol uses lazy loading for images
     */
    public static final int PRODUCT_IMAGE_LOAD = 10;
    
    /**
     * Search results loading timeout
     */
    public static final int SEARCH_RESULTS_LOAD = 15;
    
    /**
     * Cart update timeout
     */
    public static final int CART_UPDATE = 10;
    
    /**
     * Filter application timeout
     * Dynamic filtering without page reload
     */
    public static final int FILTER_APPLICATION = 15;
    
    /**
     * Checkout page load timeout
     */
    public static final int CHECKOUT_LOAD = 20;
    
    /**
     * Modal/popup appearance timeout
     */
    public static final int MODAL_APPEAR = 5;
    
    /**
     * Promotional overlay timeout
     * Trendyol often shows popups on page load
     */
    public static final int PROMO_OVERLAY = 8;
    
    // ============================================================
    // RETRY CONFIGURATION
    // ============================================================
    
    /**
     * Polling interval for explicit waits (in milliseconds)
     */
    public static final int POLLING_INTERVAL = 500;
    
    /**
     * Number of retry attempts for flaky operations
     */
    public static final int RETRY_ATTEMPTS = 3;
    
    /**
     * Delay between retries (in milliseconds)
     */
    public static final int RETRY_DELAY = 1000;
}
