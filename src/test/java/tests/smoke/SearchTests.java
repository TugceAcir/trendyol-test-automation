package tests.smoke;

import base.BaseTest;
import listeners.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchResultsPage;

/**
 * SearchTests - Search functionality test cases
 *
 * TEST COVERAGE:
 * - Search with valid keywords
 * - Search with Turkish characters
 * - Search with multiple words
 * - Search validation
 * - Product count verification
 *
 * PRIORITY: HIGH (Core e-commerce functionality)
 */
@Listeners(TestListener.class)
public class SearchTests extends BaseTest {

    // ============================================================
    // POSITIVE SEARCH TESTS
    // ============================================================

    /**
     * Test 1: Search with valid single keyword
     *
     * STEPS:
     * 1. Navigate to homepage
     * 2. Search for "laptop"
     * 3. Verify results page loads
     * 4. Verify products displayed
     * 5. Verify product count > 0
     */
    @Test(priority = 1, description = "Search with valid keyword 'laptop'")
    public void testSearchWithValidKeyword() {
        logInfo("Starting test: Search with valid keyword");

        // 1. Navigate to homepage
        HomePage homePage = new HomePage(driver);
        logInfo("Homepage loaded");

        // 2. Search for laptop
        String searchKeyword = "laptop";
        homePage.searchFor(searchKeyword);
        logInfo("Searched for: " + searchKeyword);

        // 3. Verify search results page loaded
        SearchResultsPage searchResults = new SearchResultsPage(driver);
        Assert.assertTrue(searchResults.getCurrentUrl().contains("/sr"),
                "Search results page did not load");
        logPass("Search results page loaded successfully");

        // 4. Verify products displayed
        Assert.assertTrue(searchResults.getVisibleProductCount() > 0,
                "No products displayed on search results");
        logPass("Products displayed on page");

        // 5. Verify product count
        int productCount = searchResults.getProductCount();
        Assert.assertTrue(productCount > 0,
                "Product count is zero");
        logPass("Product count: " + productCount);

        logPass("Test completed: Search with valid keyword");
    }

    /**
     * Test 2: Search with Turkish characters
     *
     * WHY IMPORTANT: Trendyol is Turkish site, must handle Turkish letters
     *
     * CHARACTERS: ç, ğ, ı, ö, ş, ü
     */
    @Test(priority = 2, description = "Search with Turkish characters 'çanta'")
    public void testSearchWithTurkishCharacters() {
        logInfo("Starting test: Search with Turkish characters");

        HomePage homePage = new HomePage(driver);

        // Search with Turkish word containing special characters
        String searchKeyword = "çanta"; // Turkish: bag/purse
        homePage.searchFor(searchKeyword);
        logInfo("Searched for Turkish keyword: " + searchKeyword);

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Verify results
        Assert.assertTrue(searchResults.areProductsDisplayed(),
                "Turkish character search failed - no products");

        int productCount = searchResults.getProductCount();
        Assert.assertTrue(productCount > 0,
                "No products found for Turkish keyword");

        logPass("Turkish character search successful. Products: " + productCount);
    }

    /**
     * Test 3: Search with multiple words
     *
     * EXAMPLE: "apple macbook pro"
     */
    @Test(priority = 3, description = "Search with multiple words 'apple macbook'")
    public void testSearchWithMultipleWords() {
        logInfo("Starting test: Search with multiple words");

        HomePage homePage = new HomePage(driver);

        String searchKeyword = "apple macbook";
        homePage.searchFor(searchKeyword);
        logInfo("Searched for: " + searchKeyword);

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Verify results
        Assert.assertTrue(searchResults.areProductsDisplayed(),
                "Multi-word search failed");

        // Verify search keyword displayed on results page
        String displayedKeyword = searchResults.getSearchKeyword();
        Assert.assertTrue(displayedKeyword.toLowerCase().contains("apple") ||
                        displayedKeyword.toLowerCase().contains("macbook"),
                "Search keyword not reflected in results");

        logPass("Multi-word search successful");
    }

    /**
     * Test 4: Search and verify product count displayed
     *
     * VALIDATES: "X ürün bulundu" message displayed
     */
    @Test(priority = 4, description = "Verify product count message displayed")
    public void testProductCountDisplayed() {
        logInfo("Starting test: Product count display");

        HomePage homePage = new HomePage(driver);
        homePage.searchFor("telefon");

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Get product count from page
        int productCount = searchResults.getProductCount();

        // Verify count is reasonable (Trendyol has thousands of products)
        Assert.assertTrue(productCount > 100,
                "Product count seems too low: " + productCount);

        logInfo("Product count displayed: " + productCount);
        logPass("Product count validation passed");
    }

    /**
     * Test 5: Search with brand name
     *
     * VALIDATES: Brand-specific search works
     */
    @Test(priority = 5, description = "Search with brand name 'Samsung'")
    public void testSearchWithBrandName() {
        logInfo("Starting test: Brand name search");

        HomePage homePage = new HomePage(driver);

        String brandName = "Samsung";
        homePage.searchFor(brandName);
        logInfo("Searched for brand: " + brandName);

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Verify products displayed
        Assert.assertTrue(searchResults.areProductsDisplayed(),
                "No products found for brand: " + brandName);

        // Verify first product name contains brand
        String firstProductName = searchResults.getProductNameByIndex(0);
        Assert.assertTrue(firstProductName.toLowerCase().contains(brandName.toLowerCase()),
                "First product does not contain brand name. Product: " + firstProductName);

        logPass("Brand search successful. First product: " + firstProductName);
    }

    /**
     * Test 6: Search and verify visible product count
     *
     * VALIDATES: Products actually loaded on page (not just count)
     */
    @Test(priority = 6, description = "Verify visible products match expectations")
    public void testVisibleProductCount() {
        logInfo("Starting test: Visible product count");

        HomePage homePage = new HomePage(driver);
        homePage.searchFor("laptop");

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Get visible product count (actual DOM elements)
        int visibleCount = searchResults.getVisibleProductCount();

        // Trendyol typically shows 24 products per page initially
        Assert.assertTrue(visibleCount >= 20 && visibleCount <= 30,
                "Unexpected visible product count: " + visibleCount);

        logInfo("Visible products on page: " + visibleCount);
        logPass("Visible product count validation passed");
    }

    /**
     * Test 7: Search with category-specific keyword
     *
     * VALIDATES: Category keywords return relevant results
     */
    @Test(priority = 7, description = "Search with category keyword 'elektronik'")
    public void testSearchWithCategoryKeyword() {
        logInfo("Starting test: Category keyword search");

        HomePage homePage = new HomePage(driver);

        String categoryKeyword = "elektronik";
        homePage.searchFor(categoryKeyword);
        logInfo("Searched for category: " + categoryKeyword);

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Verify results displayed
        Assert.assertTrue(searchResults.areProductsDisplayed(),
                "No results for category keyword");

        int productCount = searchResults.getProductCount();
        Assert.assertTrue(productCount > 1000,
                "Electronics category should have many products. Found: " + productCount);

        logPass("Category search successful. Products: " + productCount);
    }

    /**
     * Test 8: Search keyword displayed in search box
     *
     * VALIDATES: Search term persists after search
     *
     * NOTE: This test checks if search box still shows the keyword
     * after search results load (some sites clear it)
     */
    @Test(priority = 8, description = "Verify search keyword persists in search box")
    public void testSearchKeywordPersists() {
        logInfo("Starting test: Search keyword persistence");

        HomePage homePage = new HomePage(driver);

        String searchKeyword = "mouse";
        homePage.searchFor(searchKeyword);
        logInfo("Searched for: " + searchKeyword);

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Verify search keyword displayed on results page
        String displayedKeyword = searchResults.getSearchKeyword();

        Assert.assertFalse(displayedKeyword.isEmpty(),
                "Search keyword not displayed on results page");

        Assert.assertTrue(displayedKeyword.toLowerCase().contains(searchKeyword.toLowerCase()),
                "Displayed keyword '" + displayedKeyword + "' does not match searched keyword '" + searchKeyword + "'");

        logPass("Search keyword persists correctly: " + displayedKeyword);
    }

    // ============================================================
    // NEGATIVE SEARCH TESTS
    // ============================================================

    /**
     * Test 9: Search with very long query
     *
     * VALIDATES: System handles edge case gracefully
     */
    @Test(priority = 9, description = "Search with very long query string")
    public void testSearchWithVeryLongQuery() {
        logInfo("Starting test: Very long search query");

        HomePage homePage = new HomePage(driver);

        // Generate long search query
        String longQuery = "laptop apple macbook pro 16 inch 2024 model with retina display";
        homePage.searchFor(longQuery);
        logInfo("Searched for long query (length: " + longQuery.length() + ")");

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // System should handle gracefully - either show results or no results
        // But should not crash
        Assert.assertTrue(searchResults.getCurrentUrl().contains("/sr"),
                "Page did not load after long query");

        if (searchResults.areProductsDisplayed()) {
            logPass("Long query returned results");
        } else if (searchResults.isNoResultsMessageDisplayed()) {
            logPass("Long query showed 'no results' - acceptable");
        } else {
            logPass("Long query handled gracefully");
        }
    }

    /**
     * Test 10: Search with no results expected
     *
     * VALIDATES: "No results" message shown correctly
     */
    @Test(priority = 10, description = "Search with nonsense keyword expecting no results")
    public void testSearchWithNoResults() {
        logInfo("Starting test: Search with no results");

        HomePage homePage = new HomePage(driver);

        // Search for nonsense keyword unlikely to have results
        String nonsenseKeyword = "xyzabc123nonexistent999";
        homePage.searchFor(nonsenseKeyword);
        logInfo("Searched for nonsense keyword: " + nonsenseKeyword);

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Verify no results or very few results
        if (searchResults.isNoResultsMessageDisplayed()) {
            logPass("No results message displayed correctly");
        } else {
            int productCount = searchResults.getProductCount();
            Assert.assertTrue(productCount < 10,
                    "Too many results for nonsense keyword: " + productCount);
            logPass("Very few results returned: " + productCount);
        }
    }
}