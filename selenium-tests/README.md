# CivicBin Selenium Web E2E Test Suite

This directory contains the automated end-to-end (E2E) testing framework for the **CivicBin** web frontend application using **Node.js** and **Selenium Webdriver**.

## Folder Layout

- `config.js`: Central configuration settings for timeouts, browsers, and URLs.
- `reset-db.js`: Database cleaner script simulating clean testing conditions.
- `scratch.js`: Debug tool to check driver startups and page loading.
- `test-runner.js`: Master programmatic launcher executing Mocha suites.
- `tests/`: Directory containing all Selenium test execution scripts.
- `reports/`: Target folder for output test report spreadsheets.
- `screenshots/`: Target folder for capturing UI details during fails.

## Running Tests Locally

1. **Install dependencies**:
   ```bash
   npm install
   ```

2. **Execute E2E suite**:
   ```bash
   npm test
   ```

3. **Check driver startup**:
   ```bash
   node scratch.js
   ```

4. **Verify Excel outputs**:
   Check the `/reports/` folder for generated Excel logs.
