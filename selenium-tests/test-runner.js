// Programmatic runner for CivicBin Selenium E2E Web Tests
const Mocha = require('mocha');
const path = require('path');
const { resetDatabase } = require('./reset-db');

// Instantiate a new Mocha instance
const mocha = new Mocha({
  timeout: 600000,
  reporter: 'spec'
});

console.log("==================================================");
console.log("Starting CivicBin E2E Selenium Test Suite Launcher");
console.log("==================================================");

// Step 1: Perform DB cleanups or state resets
resetDatabase();

// Step 2: Add our main E2E test file
mocha.addFile(path.join(__dirname, 'tests/selenium.test.js'));

// Step 3: Run the test suite
mocha.run((failures) => {
  console.log(`\nTesting run completed. Failures: ${failures}`);
  process.exitCode = failures ? 1 : 0;
});
