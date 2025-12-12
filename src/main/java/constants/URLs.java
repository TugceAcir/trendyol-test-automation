package constants;

/**
 * Central repository for all application URLs
 * Makes URL management clean and maintainable
 */
public class URLs {
    
    // Base URLs
    public static final String BASE_URL = "https://www.trendyol.com/";
    
    // Page-specific URLs
    public static final String LOGIN_PAGE = BASE_URL + "giris";
    public static final String REGISTER_PAGE = BASE_URL + "uye-ol";
    public static final String CART_PAGE = BASE_URL + "sepet";
    public static final String MY_ACCOUNT = BASE_URL + "Hesabim";
    public static final String MY_ORDERS = BASE_URL + "Hesabim/Siparislerim";
    public static final String MY_FAVORITES = BASE_URL + "Hesabim/Favoriler";
    public static final String MY_COUPONS = BASE_URL + "Hesabim/IndirimKuponlari";
    
    // Category URLs (examples - can be expanded)
    public static final String CATEGORY_ELECTRONICS = BASE_URL + "butik/liste/5/elektronik";
    public static final String CATEGORY_FASHION_WOMEN = BASE_URL + "butik/liste/1/kadin";
    public static final String CATEGORY_FASHION_MEN = BASE_URL + "butik/liste/2/erkek";
    public static final String CATEGORY_HOME = BASE_URL + "butik/liste/12/ev--mobilya";
    
    // Help & Support
    public static final String HELP_CENTER = BASE_URL + "yardim";
    public static final String ABOUT_US = BASE_URL + "s/meet-us#whoweare";
    
    // Trendyol Plus
    public static final String TRENDYOL_PLUS = BASE_URL + "trendyolplus";
    
    // Search URL pattern
    public static final String SEARCH_URL_PATTERN = BASE_URL + "sr?q=";
    
    /**
     * Constructs a search URL with given keyword
     * @param keyword - search term
     * @return full search URL
     */
    public static String getSearchUrl(String keyword) {
        return SEARCH_URL_PATTERN + keyword.replace(" ", "%20");
    }
    
    /**
     * Constructs a product detail URL
     * @param productSlug - product URL slug
     * @return full product URL
     */
    public static String getProductUrl(String productSlug) {
        return BASE_URL + productSlug;
    }
}
