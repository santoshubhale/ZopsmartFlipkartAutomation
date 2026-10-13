package com.flipkart.pages;

import com.flipkart.utils.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {

    // the close button of the login popup shows the "x" character
    private final By closePopup = By.xpath("//button[normalize-space()='\u2715']");
    private final By searchBox = By.name("q");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open(String url) {
        driver.get(url);
        waitForPageLoad();
        if (isVisible(closePopup, 5)) {
            click(closePopup);
            Log.info("Login popup closed");
        }
    }

    public void search(String term) {
        WebElement box = waitVisible(searchBox);
        box.clear();
        box.sendKeys(term, Keys.ENTER);
        Log.info("Searched for: " + term);
    }
}
