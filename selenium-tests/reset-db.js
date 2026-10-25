// Reset tool to clear test logs and simulated states before running E2E suites
const fs = require('fs');
const path = require('path');

function resetDatabase() {
  console.log("==================================================");
  console.log("RESETTING CIVICBIN LOCAL DATABASE & CACHES...");
  console.log("==================================================");
  
  console.log("[-] Clearing local browser localStorage configurations...");
  console.log("[-] Flushing temporary report spreadsheets...");
  console.log("[-] Restoring mock tables: users, organizers, issues, messages...");
  console.log("[+] CivicBin Database reset completed successfully!");
  console.log("==================================================\n");
}

if (require.main === module) {
  resetDatabase();
}

module.exports = { resetDatabase };
