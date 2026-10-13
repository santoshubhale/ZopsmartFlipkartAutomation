package com.flipkart.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductPage extends BasePage {

    private final By title = By.tagName("h1");
    // first rupee amount that appears after the product title
    private final By price = By.xpath("(//h1/following::*[starts-with(normalize-space(text()),'\u20B9')])[1]");
    private final By addToCart = By.xpath(
            "//*[not(*) and contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                    + "'abcdefghijklmnopqrstuvwxyz'),'add to cart')]");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    /** Reads text with retry: ignores stale elements while the page is re-rendering. */
    private String readText(By locator) {
        return wait.until(d -> {
            String text = d.findElement(locator).getText().trim();
            return text.isEmpty() ? null : text;
        });
    }

    public String getName() {
        waitForPageLoad();
        return readText(title);
    }

    public String getPriceText() {
        return readText(price);
    }

    /** Variant info such as "Black, 128 GB" taken from the title, plus RAM if shown on the page. */
    public String getVariant() {
        StringBuilder sb = new StringBuilder();
        Matcher m = Pattern.compile("\\(([^)]*)\\)").matcher(getName());
        if (m.find()) {
            sb.append(m.group(1));
        }
        String body = driver.findElement(By.tagName("body")).getText();
        Matcher ram = Pattern.compile("(\\d+\\s*GB)\\s*RAM", Pattern.CASE_INSENSITIVE).matcher(body);
        if (ram.find()) {
            sb.append(sb.length() > 0 ? " | " : "").append("RAM ").append(ram.group(1));
        }
        return sb.length() == 0 ? "not visible" : sb.toString();
    }

    public boolean isAddToCartAvailable() {
        return isVisible(addToCart, 10);
    }

    public void addToCart() {
        click(addToCart);
    }

    /** Texts of the buttons currently visible on the page, for diagnosis. */
    public List<String> getVisibleButtons() {
        List<String> texts = new ArrayList<>();
        for (WebElement b : driver.findElements(By.tagName("button"))) {
            try {
                String t = b.getText().trim();
                if (!t.isEmpty() && b.isDisplayed()) {
                    texts.add(t);
                }
            } catch (Exception ignored) {
                // element changed while reading, ignore
            }
        }
        return texts;
    }
}