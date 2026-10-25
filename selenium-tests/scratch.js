// Scratch script for debugging individual selectors or webdriver launches
const { Builder } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const config = require('./config');

async function testLaunch() {
  console.log("Testing headless Chrome driver instantiation...");
  const options = new chrome.Options();
  config.chromeOptions.forEach(opt => options.addArguments(opt));
  if (config.headless) {
    options.addArguments('--headless=new');
  }

  let driver;
  try {
    driver = await new Builder()
      .forBrowser('chrome')
      .setChromeOptions(options)
      .build();
    console.log("[PASS] Chrome driver initialized successfully!");
    await driver.get(config.webRoot || "https://www.google.com");
    console.log("[PASS] Navigation connection successful!");
  } catch (err) {
    console.error("[FAIL] Webdriver launch test failed:", err.message);
  } finally {
    if (driver) {
      await driver.quit();
    }
  }
}

if (require.main === module) {
  testLaunch();
}
