// Configuration parameters for CivicBin E2E Selenium Web Tests
module.exports = {
  PORT: 8085,
  webRoot: "http://localhost:8085",
  headless: true,
  timeouts: {
    implicit: 5000,
    elementWait: 15000
  },
  chromeOptions: [
    '--disable-infobars',
    '--disable-notifications',
    '--start-maximized',
    '--no-sandbox',
    '--disable-dev-shm-usage',
    '--allow-file-access-from-files',
    '--disable-web-security'
  ]
};
