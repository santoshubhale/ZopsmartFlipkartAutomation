package com.flipkart.tests;

import com.flipkart.pages.CartPage;
import com.flipkart.pages.HomePage;
import com.flipkart.pages.Product;
import com.flipkart.pages.ProductPage;
import com.flipkart.pages.ResultsPage;
import com.flipkart.utils.ConfigReader;
import com.flipkart.utils.Log;
import com.flipkart.utils.PriceUtil;
import com.flipkart.utils.ScreenshotUtil;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class FlipkartTest extends BaseTest {

    @Test
    public void searchFilterSortAndAddToCart() {
        String baseUrl = ConfigReader.get("base.url");
        String searchTerm = ConfigReader.get("search.term");
        String brand = ConfigReader.get("brand");
        int count = Integer.parseInt(ConfigReader.get("product.count"));

        test.info("Opening Flipkart home page");
        HomePage home = new HomePage(driver);
        home.open(baseUrl);
        ScreenshotUtil.takeAndAttach(driver, test, "01_home", "Home page");

        test.info("Searching for Mobiles");
        home.search(searchTerm);
        ResultsPage results = new ResultsPage(driver);
        Assert.assertTrue(results.isDisplayed(), "Search results page is not displayed");
        ScreenshotUtil.takeAndAttach(driver, test, "02_search_results", "Search results page");

        test.info("Applying brand filter: " + brand);
        results.applyBrandFilter(brand);
        Assert.assertTrue(results.isBrandApplied(brand), "Brand filter not applied: " + brand);
        List<Product> brandProducts = results.getProducts(count, brand);
        Assert.assertFalse(brandProducts.isEmpty(), "No " + brand + " products found after filtering");
        for (Product p : brandProducts) {
            Assert.assertTrue(brandMatches(p.name, brand),
                    "Not a " + brand + " mobile: " + p.name);
        }
        ScreenshotUtil.takeAndAttach(driver, test, "03_brand_filter", "Brand filtered results");

        test.info("Sorting products by Price Low to High");
        results.sortLowToHigh();
        ScreenshotUtil.takeAndAttach(driver, test, "04_sorted", "Sorted results");

        test.info("Validating numeric price order");
        List<Product> products = results.getProducts(count, brand);
        List<Long> prices = new ArrayList<>();
        for (Product p : products) {
            prices.add(p.price);
            Log.info("Listing: " + p);
        }
        Assert.assertTrue(prices.size() >= 3, "Need at least 3 products, found " + prices.size());
        Assert.assertTrue(PriceUtil.isAscending(prices), "Prices are not in ascending order: " + prices);
        Log.info("Price order verified (ascending): " + prices);

        test.info("Selecting third-highest-priced mobile");
        Product selected = PriceUtil.thirdHighest(products);
        Log.info("Products considered: " + products.size()
                + " | Rule: 3rd highest distinct price, tie -> first in listing order");
        Log.info("Selected from listing: " + selected);

        test.info("Opening selected product page");
        results.openProduct(selected);
        ProductPage productPage = new ProductPage(driver);
        String name = productPage.getName();
        String priceText = productPage.getPriceText();
        String variant = productPage.getVariant();
        long pagePrice = PriceUtil.parse(priceText);
        Log.info("Product page URL: " + driver.getCurrentUrl());
        Log.info("Product page name: " + name + " | price text: " + priceText);
        ScreenshotUtil.takeAndAttach(driver, test, "05_product_page", "Product page");
        Assert.assertTrue(brandMatches(name, brand), "Unexpected product: " + name);
        if (pagePrice == selected.price) {
            Log.info("Listing price matches product page price: " + pagePrice);
        } else {
            Log.warn("Listing price " + selected.price + " differs from product page price " + pagePrice);
        }

        test.info("Adding selected product to cart");
        if (!productPage.isAddToCartAvailable()) {
            String blocker = "BLOCKED - Add to cart button not available. Buttons seen: " + productPage.getVisibleButtons();
            Log.warn(blocker);
            printSummary(name, priceText, variant, blocker);
            throw new SkipException("Website blocker: " + blocker);
        }
        productPage.addToCart();
        ScreenshotUtil.takeAndAttach(driver, test, "06_after_add_to_cart", "After add to cart");

        String cartResult;
        if (productPage.isLoginPromptShown()) {
            cartResult = "BLOCKED - login required, not bypassed";
            printSummary(name, priceText, variant, cartResult);
            throw new SkipException("Website blocker: " + cartResult);
        }

        test.info("Opening cart and validating selected item");
        CartPage cart = new CartPage(driver);
        cart.open();
        ScreenshotUtil.takeAndAttach(driver, test, "07_cart", "Cart page");

        List<String> cartItems = cart.getItemNames();
        String core = name.replaceAll("\\([^)]*\\)", "").trim().toLowerCase();
        boolean inCart = false;
        for (String item : cartItems) {
            if (item.toLowerCase().contains(core) || core.contains(item.toLowerCase())) {
                inCart = true;
            }
        }
        boolean priceMatches = cart.getPrices().contains(pagePrice);
        cartResult = (inCart && priceMatches) ? "PASS - product in cart, price matches"
                : "FAIL - inCart=" + inCart + ", priceMatches=" + priceMatches;

        printSummary(name, priceText, variant, cartResult);
        Assert.assertTrue(inCart, "Selected mobile not found in cart. Items: " + cartItems);
        Assert.assertTrue(priceMatches, "Cart price does not match " + pagePrice);
    }

    private boolean brandMatches(String productName, String brand) {
        String normalizedProduct = normalize(productName);
        String normalizedBrand = normalize(brand);
        return normalizedProduct.contains(normalizedBrand);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase().replaceAll("[^a-z0-9]", "").trim();
    }

    private void printSummary(String name, String price, String variant, String cartResult) {
        Log.info("========== SELECTED MOBILE ==========");
        Log.info("Name   : " + name);
        Log.info("Price  : " + price);
        Log.info("Variant: " + variant);
        Log.info("Cart   : " + cartResult);
        Log.info("=====================================");
    }
}
