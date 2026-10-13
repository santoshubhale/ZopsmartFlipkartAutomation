# Flipkart Selenium Automation (Java, Selenium, TestNG, Maven)

## Prerequisites
- JDK 11+, Maven 3.8+, Google Chrome
- Internet connection (Selenium Manager downloads the chromedriver automatically)

## Run
    mvn clean test

## Config
`src/test/resources/config.properties` -> base URL, search term, brand, number of products to read.

## Output
- Test report: `target/surefire-reports/index.html` (open in browser)
- Extent report: `target/extent-report/ExtentReport.html` (open in browser)
- Screenshots: `target/screenshots/`
- Logs: console output of the run

## Structure
    pages/   HomePage, ResultsPage, ProductPage, CartPage   (page actions only, no assertions)
    utils/   ConfigReader, PriceUtil, ScreenshotUtil, Log   (helpers)
    tests/   BaseTest (browser setup/teardown), FlipkartTest (flow + assertions)

## Flow covered
1. Open Flipkart, close login popup   2. Search "Mobiles", verify results page
3. Filter OnePlus, verify filter + product names   4. Sort Price Low to High
5. Read prices, parse to numbers, verify ascending order
6. Pick the 3rd-highest-priced mobile   7. Open it, capture name/price/variant
8. Add to cart (no order/payment)   9. Open cart, verify product + price
10. Print details and cart result

## Assumptions
- Only the first results page (up to 24 products) is used.
- Third-highest = 3rd highest DISTINCT price. If several products have that price, the first one
  in listing order is chosen (its name is printed).
- Sponsored ("Ad") cards are skipped because they ignore the sort order.
- If a login wall blocks the cart, nothing is bypassed: screenshot is saved and that step is
  reported as SKIPPED.
- Flipkart's page layout changes often; locators use visible text/structure, not fixed positions,
  but may need a small tweak on first run.
