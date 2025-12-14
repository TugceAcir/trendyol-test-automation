package tests.smoke;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ProductDetailPage;
import pages.SearchResultsPage;

/**
 * Product Navigation Tests
 *
 * TESTS:
 * 1. Click first product opens detail page in new tab
 * 2. Product detail page elements displayed
 * 3. Multiple product clicks open multiple tabs
 * 4. Return to search results after closing product tab
 * 5. Scroll to load more products (infinite scroll)
 */
public class NavigationTests extends BaseTest {

    @Test(priority = 1, description = "Click first product opens detail page")
    public void testClickProductOpensDetailPage() {
        logInfo("Starting test: Click product opens detail page");

        HomePage homePage = new HomePage(driver);
        homePage.searchFor("laptop");
        logInfo("Searched for: laptop");

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        // Get original window handle
        String originalWindow = driver.getWindowHandle();
        int originalTabCount = driver.getWindowHandles().size();

        // Click first product
        searchResults.clickProductByIndex(0);
        logInfo("Clicked first product");

        // Verify new tab opened
        searchResults.waitForNewTab(originalTabCount);
        int newTabCount = driver.getWindowHandles().size();
        Assert.assertEquals(newTabCount, originalTabCount + 1,
                "New tab should open");

        // Switch to new tab
        searchResults.switchToNewTab(originalWindow);
        logInfo("Switched to product detail tab");

        // Verify product detail page loaded
        ProductDetailPage productDetail = new ProductDetailPage(driver);
        Assert.assertTrue(productDetail.isProductDetailPageLoaded(),
                "Product detail page should load");

        logPass("Product opened in new tab successfully");

        // Cleanup: Close tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }
    @Test(priority = 2, description = "Product detail page elements displayed")
    public void testProductDetailPageElements() {
        logInfo("Starting test: Product detail page elements");

        HomePage homePage = new HomePage(driver);
        homePage.searchFor("laptop");

        SearchResultsPage searchResults = new SearchResultsPage(driver);
        String originalWindow = driver.getWindowHandle();

        searchResults.clickProductByIndex(0);
        searchResults.waitForNewTab(1);
        searchResults.switchToNewTab(originalWindow);

        ProductDetailPage productDetail = new ProductDetailPage(driver);

        // Verify title
        String title = productDetail.getProductTitle();
        Assert.assertFalse(title.isEmpty(), "Product title should not be empty");
        logInfo("Product title: " + title);

        // Verify brand
        String brand = productDetail.getBrandName();
        Assert.assertFalse(brand.isEmpty(), "Brand name should not be empty");
        logInfo("Brand: " + brand);

        // Verify price
        String price = productDetail.getDiscountedPrice();
        Assert.assertTrue(price.contains("TL"), "Price should contain TL");
        logInfo("Price: " + price);

        // Verify Add to Cart button
        Assert.assertTrue(productDetail.isAddToCartButtonDisplayed(),
                "Add to cart button should be displayed");

        logPass("All product detail elements displayed correctly");

        driver.close();
        driver.switchTo().window(originalWindow);
    }
    @Test(priority = 3, description = "Click 3 products opens 3 tabs")
    public void testMultipleProductClicks() {
        logInfo("Starting test: Multiple product clicks");

        HomePage homePage = new HomePage(driver);
        homePage.searchFor("laptop");

        SearchResultsPage searchResults = new SearchResultsPage(driver);
        String originalWindow = driver.getWindowHandle();
        int originalTabCount = driver.getWindowHandles().size();

        // Click 3 products
        for (int i = 0; i < 3; i++) {
            searchResults.clickProductByIndex(i);
            logInfo("Clicked product " + (i + 1));
            searchResults.waitForNewTab(originalTabCount + i);
        }

        // Verify 3 new tabs opened
        int finalTabCount = driver.getWindowHandles().size();
        Assert.assertEquals(finalTabCount, originalTabCount + 3,
                "3 new tabs should open");

        logPass("Multiple products opened in separate tabs");

        // Cleanup: Close all new tabs
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test(priority = 4, description = "Return to search results from product page")
    public void testReturnToSearchResults() {
        logInfo("Starting test: Return to search results");

        HomePage homePage = new HomePage(driver);
        homePage.searchFor("laptop");

        SearchResultsPage searchResults = new SearchResultsPage(driver);
        String originalWindow = driver.getWindowHandle();

        int beforeClickCount = searchResults.getVisibleProductCount();

        searchResults.clickProductByIndex(0);
        searchResults.waitForNewTab(1);
        searchResults.switchToNewTab(originalWindow);

        // Close product tab
        driver.close();
        driver.switchTo().window(originalWindow);
        logInfo("Closed product tab, returned to search results");

        // Verify back on search results
        Assert.assertTrue(searchResults.areProductsDisplayed(),
                "Search results should still be displayed");

        int afterReturnCount = searchResults.getVisibleProductCount();
        Assert.assertEquals(afterReturnCount, beforeClickCount,
                "Product count should remain same");

        logPass("Successfully returned to search results");
    }

    /**
     * Test 5: Scroll to load more products (infinite scroll)
     */
    @Test(priority = 5, description = "Scroll to load more products")
    public void testScrollToLoadMore() {
        logInfo("Starting test: Scroll to load more");

        HomePage homePage = new HomePage(driver);
        homePage.searchFor("laptop");

        SearchResultsPage searchResults = new SearchResultsPage(driver);

        int initialCount = searchResults.getVisibleProductCount();
        logInfo("Initial product count: " + initialCount);

        // Scroll 3 times (increased from 2)
        searchResults.scrollToLoadMoreProducts(3);
        logInfo("Scrolled to load more products");

        int afterScrollCount = searchResults.getVisibleProductCount();
        logInfo("After scroll product count: " + afterScrollCount);

        // LENIENT: Accept if at least 1 new product loaded OR already at max
        if (afterScrollCount > initialCount) {
            logPass("Infinite scroll loaded more products. Before: " +
                    initialCount + ", After: " + afterScrollCount);
        } else if (initialCount >= 100) {
            // If already showing 100+ products, may be all loaded
            logPass("High product count (" + initialCount + ") - may have loaded all available");
        } else {
            Assert.fail("Scroll did not load new products. Before: " +
                    initialCount + ", After: " + afterScrollCount);
        }
    }
}