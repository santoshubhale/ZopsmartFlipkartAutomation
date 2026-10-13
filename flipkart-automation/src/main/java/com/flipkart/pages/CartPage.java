package com.flipkart.pages;

import com.flipkart.utils.PriceUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class CartPage extends BasePage {

    private final By cartLink = By.cssSelector("a[href*='viewcart']");
    private final By itemLinks = By.xpath("//a[contains(@href,'/p/')]");
    private final By rupeeTexts = By.xpath("//*[contains(text(),'\u20B9')]");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        if (!driver.getCurrentUrl().contains("viewcart")) {
            if (isVisible(cartLink, 5)) {
                click(cartLink);
            } else {
                driver.get("https://www.flipkart.com/viewcart");
            }
        }
        wait.until(d -> d.getCurrentUrl().contains("viewcart"));
        waitForPageLoad();
    }

    public List<String> getItemNames() {
        try {
            wait.until(d -> !d.findElements(itemLinks).isEmpty());
        } catch (Exception e) {
            return new ArrayList<>();
        }
        List<String> names = new ArrayList<>();
        for (WebElement link : driver.findElements(itemLinks)) {
            if (!link.getText().trim().isEmpty()) {
                names.add(link.getText().trim());
            }
        }
        return names;
    }

    public List<Long> getPrices() {
        List<Long> prices = new ArrayList<>();
        for (WebElement el : driver.findElements(rupeeTexts)) {
            try {
                prices.add(PriceUtil.parse(el.getText()));
            } catch (Exception ignored) {
                // text without a number, ignore
            }
        }
        return prices;
    }
}
