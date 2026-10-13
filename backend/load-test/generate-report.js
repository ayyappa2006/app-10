/**
 * Generate Excel-compatible CSV report from CivicBin Load Test Results
 * Usage: node generate-report.js
 */

const fs = require('fs');
const path = require('path');

// Read results
const results = JSON.parse(fs.readFileSync(path.join(__dirname, 'results.json'), 'utf8'));

const lines = [];

// ═══════════════════════════════════════════════════════════
// SHEET 1: TEST SUMMARY
// ═══════════════════════════════════════════════════════════
lines.push('CIVICBIN BACKEND - BASELINE / LOAD TEST REPORT');
lines.push(`Generated On:,${results.timestamp || new Date().toISOString()}`);
lines.push('');

// Test Configuration
lines.push('TEST CONFIGURATION');
lines.push('Parameter,Value');
lines.push(`Target URL,${results.config.target}`);
lines.push(`Virtual Users (VUs),${results.config.totalVUs}`);
lines.push(`Test Duration,${results.config.durationMs / 1000} seconds`);
lines.push(`Endpoints Tested,${Object.keys(results.perEndpoint).length}`);
lines.push('');

// Overall Results
lines.push('OVERALL RESULTS');
lines.push('Metric,Value');
lines.push(`Total Requests,${results.summary.totalRequests}`);
lines.push(`Successful Requests,${results.summary.successfulRequests}`);
lines.push(`Failed Requests,${results.summary.failedRequests}`);
lines.push(`Success Rate (%),${((results.summary.successfulRequests / results.summary.totalRequests) * 100).toFixed(2)}%`);
lines.push('');

// Throughput
lines.push('THROUGHPUT');
lines.push('Metric,Value');
lines.push(`Requests Per Second (RPS),${results.summary.rps} req/sec`);
lines.push('');

// Response Times
lines.push('RESPONSE TIMES');
lines.push('Metric,Value (ms)');
lines.push(`Minimum,${results.summary.responseTime.min}`);
lines.push(`Average,${results.summary.responseTime.avg}`);
lines.push(`Median (P50),${results.summary.responseTime.median}`);
lines.push(`P95 (95th Percentile),${results.summary.responseTime.p95}`);
lines.push(`P99 (99th Percentile),${results.summary.responseTime.p99}`);
lines.push(`Maximum,${results.summary.responseTime.max}`);
lines.push('');

// HTTP Status Codes
lines.push('HTTP STATUS CODES');
lines.push('Status Code,Count,Percentage (%)');
Object.entries(results.summary.statusCodes).forEach(([code, count]) => {
  const pct = ((count / results.summary.totalRequests) * 100).toFixed(2);
  lines.push(`${code},${count},${pct}%`);
});
lines.push('');

// ═══════════════════════════════════════════════════════════
// SHEET 2: PER-ENDPOINT BREAKDOWN
// ═══════════════════════════════════════════════════════════
lines.push('PER-ENDPOINT BREAKDOWN');
lines.push('Endpoint,Total Requests,Successful,Failed,Success Rate (%),Min (ms),Avg (ms),Median (ms),P95 (ms),P99 (ms),Max (ms)');

Object.entries(results.perEndpoint).forEach(([name, data]) => {
  const stats = data.stats;
  const successRate = data.count > 0 ? ((data.success / data.count) * 100).toFixed(2) : '0.00';
  lines.push(`${name},${data.count},${data.success},${data.failed},${successRate}%,${stats.min},${stats.avg},${stats.median},${stats.p95},${stats.p99},${stats.max}`);
});
lines.push('');

// ═══════════════════════════════════════════════════════════
// SHEET 3: RESPONSE TIME DISTRIBUTION
// ═══════════════════════════════════════════════════════════
lines.push('RESPONSE TIME DISTRIBUTION');
lines.push('Range (ms),Count,Percentage (%)');

// Collect all response times
const allTimes = [];
Object.values(results.perEndpoint).forEach(ep => {
  allTimes.push(...ep.responseTimes);
});

// Create distribution buckets
const buckets = [
  { label: '0 - 100', min: 0, max: 100 },
  { label: '101 - 250', min: 101, max: 250 },
  { label: '251 - 500', min: 251, max: 500 },
  { label: '501 - 1000', min: 501, max: 1000 },
  { label: '1001 - 2000', min: 1001, max: 2000 },
  { label: '2001 - 3000', min: 2001, max: 3000 },
  { label: '3001 - 5000', min: 3001, max: 5000 },
  { label: '5001+', min: 5001, max: Infinity },
];

buckets.forEach(bucket => {
  const count = allTimes.filter(t => t >= bucket.min && t <= bucket.max).length;
  const pct = allTimes.length > 0 ? ((count / allTimes.length) * 100).toFixed(2) : '0.00';
  lines.push(`${bucket.label},${count},${pct}%`);
});
lines.push('');

// ═══════════════════════════════════════════════════════════
// SHEET 4: ENDPOINT COMPARISON CHART DATA
// ═══════════════════════════════════════════════════════════
lines.push('ENDPOINT COMPARISON (For Chart)');
lines.push('Endpoint,Requests,Avg Response Time (ms),P95 Response Time (ms)');

Object.entries(results.perEndpoint).forEach(([name, data]) => {
  lines.push(`${name},${data.count},${data.stats.avg},${data.stats.p95}`);
});
lines.push('');

// ═══════════════════════════════════════════════════════════
// SHEET 5: RAW RESPONSE TIMES PER ENDPOINT (first 50 each)
// ═══════════════════════════════════════════════════════════
lines.push('RAW RESPONSE TIMES (Sample - First 50 per endpoint)');

// Create header row with all endpoint names
const endpointNames = Object.keys(results.perEndpoint);
lines.push(endpointNames.join(','));

// Find max sample size
const sampleSize = 50;
for (let i = 0; i < sampleSize; i++) {
  const row = endpointNames.map(name => {
    const times = results.perEndpoint[name].responseTimes;
    return i < times.length ? times[i] : '';
  });
  lines.push(row.join(','));
}
lines.push('');

// ═══════════════════════════════════════════════════════════
// SHEET 6: VERDICT & RECOMMENDATIONS
// ═══════════════════════════════════════════════════════════
lines.push('VERDICT & RECOMMENDATIONS');
lines.push('Category,Details');

const successRate = (results.summary.successfulRequests / results.summary.totalRequests) * 100;
const avgResponse = results.summary.responseTime.avg;

let verdict, grade;
if (successRate >= 99 && avgResponse < 500) {
  verdict = 'EXCELLENT - API handles baseline load with ease';
  grade = 'A+';
} else if (successRate >= 95 && avgResponse < 1000) {
  verdict = 'GOOD - API handles load well with minor optimizations possible';
  grade = 'A';
} else if (successRate >= 90) {
  verdict = 'FAIR - Some degradation under load. Review slow endpoints';
  grade = 'B';
} else {
  verdict = 'POOR - Significant issues under load. Needs optimization';
  grade = 'C';
}

lines.push(`Overall Verdict,${verdict}`);
lines.push(`Grade,${grade}`);
lines.push(`Success Rate,${successRate.toFixed(2)}%`);
lines.push(`Avg Response Time,${avgResponse}ms`);
lines.push(`Throughput,${results.summary.rps} req/sec`);
lines.push('');
lines.push('RECOMMENDATIONS');
lines.push('Priority,Recommendation');
lines.push('HIGH,"Fix MySQL port configuration in db.php - trying port 3307 first adds ~2s timeout to every request"');
lines.push('MEDIUM,"Add connection pooling or persistent connections to reduce DB overhead"');
lines.push('LOW,"Add response caching for GET endpoints like get_organizers and get_cleanliness_score"');
lines.push('LOW,"Consider adding database indexing on frequently queried columns"');

// Write to CSV
const csvContent = lines.join('\n');
const outputPath = path.join(__dirname, 'CivicBin_Load_Test_Report.csv');
fs.writeFileSync(outputPath, '\uFEFF' + csvContent, 'utf8'); // BOM for Excel UTF-8 support

console.log('');
console.log('╔══════════════════════════════════════════════════════════════╗');
console.log('║  ✅  Excel Report Generated Successfully!                    ║');
console.log('╠══════════════════════════════════════════════════════════════╣');
console.log(`║  📄 File: CivicBin_Load_Test_Report.csv                      ║`);
console.log(`║  📍 Path: ${outputPath}`.padEnd(63) + '║');
console.log('║                                                              ║');
console.log('║  📋 Sections included:                                       ║');
console.log('║     1. Test Configuration                                    ║');
console.log('║     2. Overall Results                                       ║');
console.log('║     3. Response Times (Min/Avg/Median/P95/P99/Max)           ║');
console.log('║     4. HTTP Status Codes                                     ║');
console.log('║     5. Per-Endpoint Breakdown                                ║');
console.log('║     6. Response Time Distribution                            ║');
console.log('║     7. Endpoint Comparison (Chart-ready data)                ║');
console.log('║     8. Raw Response Times (Sample)                           ║');
console.log('║     9. Verdict & Recommendations                             ║');
console.log('║                                                              ║');
console.log('║  💡 Open in Excel: Double-click the .csv file                ║');
console.log('╚══════════════════════════════════════════════════════════════╝');
console.log('');
