package com.flipkart.pages;

import com.flipkart.utils.Log;
import com.flipkart.utils.PriceUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultsPage extends BasePage {

    // a product card = div with data-id that contains a link to a product (/p/)
    private final By productCards = By.xpath(
            "//div[@data-id and .//a[contains(@href,'/p/')] and not(ancestor::div[@data-id])]");

    public ResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDisplayed() {
        try {
            wait.until(d -> d.getCurrentUrl().contains("/search") && !d.findElements(productCards).isEmpty());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void applyBrandFilter(String brand) {
        List<String> before = currentIds();
        String lower = brand.toLowerCase();
        // small element (no child elements) whose text contains the brand, outside product cards/header
        By brandText = By.xpath("//*[not(*) and contains(translate(normalize-space(.),"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lower + "')"
                + " and string-length(normalize-space(.)) < " + (brand.length() + 15)
                + " and not(ancestor::div[@data-id])"
                + " and not(ancestor::*[self::header or self::nav])]");

        // 1) brand is already visible in the filter panel
        boolean clicked = clickBrand(brandText, 5);

        // 2) open the BRAND section / "MORE" / brand search box, then try again
        if (!clicked) {
            By header = By.xpath("//div[normalize-space(text())='BRAND' or normalize-space(text())='Brand']");
            if (isVisible(header, 3)) {
                click(header);
            }
            By more = By.xpath("//*[not(*) and contains(normalize-space(text()),'MORE')]");
            if (isVisible(more, 2)) {
                click(more);
            }
            By brandSearch = By.xpath("//input[contains(translate(@placeholder,'BRAND','brand'),'brand')]");
            if (isVisible(brandSearch, 2)) {
                driver.findElement(brandSearch).sendKeys(brand);
            }
            clicked = clickBrand(brandText, 5);
        }

        // 3) fallback: apply the same brand facet through the URL
        if (!clicked) {
            Log.warn("Brand option not found in filter panel - applying the brand facet through the URL instead");
            String url = driver.getCurrentUrl();
            driver.get(url + (url.contains("?") ? "&" : "?") + "p%5B%5D=facets.brand%255B%255D%3D" + brand);
        }

        wait.until(d -> isBrandApplied(brand));
        waitForPageLoad();
//        wait.until(d -> !d.findElements(productCards).isEmpty());
        waitForListToChange(before);
        Log.info("Brand filter applied: " + brand + (clicked ? " (via filter panel)" : " (via URL)"));
    }

    private boolean clickBrand(By locator, int seconds) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(seconds))
                    .until(d -> !d.findElements(locator).isEmpty());
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
        WebElement target = driver.findElements(locator).get(0);
        for (WebElement e : driver.findElements(locator)) {
            if (e.isDisplayed()) {
                target = e;
                break;
            }
        }
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", target);
        return true;
    }

    private boolean isPresent(By locator, int seconds) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(seconds))
                    .until(d -> !d.findElements(locator).isEmpty());
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public boolean isBrandApplied(String brand) {
        return driver.getCurrentUrl().toLowerCase().contains(brand.toLowerCase());
    }

    public void sortLowToHigh() {
        List<String> before = currentIds();
        click(By.xpath("//div[contains(text(),'Price') and contains(text(),'Low to High')]"));
        wait.until(d -> d.getCurrentUrl().contains("sort=price_asc"));
        waitForPageLoad();
        waitForListToChange(before);
        Log.info("Sorted by Price - Low to High");
    }

    /** Reads name and price of the first 'count' products (sponsored cards are skipped). */
    public List<Product> getProducts(int count) {
        return getProducts(count, null);
    }

    /** Reads products while excluding sponsored cards and, when supplied, other brands. */
    public List<Product> getProducts(int count, String requiredBrand) {
        wait.until(d -> !d.findElements(productCards).isEmpty());
        List<Product> products = new ArrayList<>();
        int total = driver.findElements(productCards).size();
        for (int i = 0; i < total && products.size() < count; i++) {
            try {
                WebElement card = driver.findElements(productCards).get(i);
                if (!card.findElements(By.xpath(".//*[normalize-space(text())='Ad']")).isEmpty()) {
                    continue; // sponsored card, does not follow the sort order
                }
                List<WebElement> priceEls = card.findElements(
                        By.xpath(".//*[starts-with(normalize-space(text()),'\u20B9')]"));
                if (priceEls.isEmpty()) {
                    continue;
                }
                long price = PriceUtil.parse(priceEls.get(0).getText());
                String name = readName(card);
                if (requiredBrand != null && !matchesBrand(name, requiredBrand)) {
                    Log.warn("Ignoring non-" + requiredBrand + " result: " + name);
                    continue;
                }
                products.add(new Product(card.getAttribute("data-id"), name, price));
            } catch (StaleElementReferenceException e) {
                Log.warn("Card " + i + " changed while reading, skipped");
            }
        }
        return products;
    }

    private boolean matchesBrand(String productName, String brand) {
        return normalize(productName).contains(normalize(brand));
    }

    private String normalize(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String readName(WebElement card) {
        for (WebElement a : card.findElements(By.cssSelector("a[title]"))) {
            if (!a.getAttribute("title").trim().isEmpty()) {
                return a.getAttribute("title").trim();
            }
        }
        for (WebElement img : card.findElements(By.cssSelector("img[alt]"))) {
            if (img.getAttribute("alt").trim().length() > 3) {
                return img.getAttribute("alt").trim();
            }
        }
        return card.getText().split("\\R")[0].trim();
    }

    public void openProduct(Product product) {
        Set<String> oldTabs = new HashSet<>(driver.getWindowHandles());
        WebElement link = wait.until(d -> d.findElement(
                By.xpath("//div[@data-id='" + product.id + "']//a[contains(@href,'/p/')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
        link.click();
        // Flipkart usually opens the product in a new tab
        try {
            wait.until(d -> d.getWindowHandles().size() > oldTabs.size());
            for (String handle : driver.getWindowHandles()) {
                if (!oldTabs.contains(handle)) {
                    driver.switchTo().window(handle);
                }
            }
        } catch (Exception e) {
            Log.info("Product opened in the same tab");
        }
    }
    private List<String> currentIds() {
        List<String> ids = new ArrayList<>();
        for (WebElement card : driver.findElements(productCards)) {
            try {
                ids.add(card.getAttribute("data-id"));
            } catch (StaleElementReferenceException ignored) {
                // list is being redrawn, ignore this card
            }
        }
        return ids;
    }

    /** Waits until the product list differs from 'before' and then stops changing. */
    private void waitForListToChange(List<String> before) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
                List<String> now = currentIds();
                return !now.isEmpty() && !now.equals(before);
            });
        } catch (TimeoutException e) {
            Log.warn("Product list did not change after the action");
        }
        // stable = same list seen in two checks in a row
        AtomicReference<List<String>> last = new AtomicReference<>();
        wait.until(d -> {
            List<String> now = currentIds();
            boolean stable = !now.isEmpty() && now.equals(last.get());
            last.set(now);
            return stable;
        });
    }
}
