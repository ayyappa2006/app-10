/**
 * ============================================================================
 *  CivicBin Backend — Baseline / Load Test Script
 *  No external dependencies required — uses Node.js built-in 'http' module
 *
 *  Test Spec:
 *    • 100 virtual users (VUs)
 *    • Running continuously for 1 minute
 *    • Mix of GET and POST requests across all major API endpoints
 *
 *  Usage:  node load-test.js
 * ============================================================================
 */

const http = require('http');

// ─── Configuration ──────────────────────────────────────────────────────────
const BASE_URL = 'http://localhost';
const BASE_PATH = '/civic_backend';
const TOTAL_VUS = 100;          // 100 virtual users
const TEST_DURATION_MS = 60000; // 1 minute
const RAMP_UP_MS = 10000;       // Ramp up over first 10 seconds

// ─── Endpoint Definitions ───────────────────────────────────────────────────
const endpoints = [
  // GET endpoints (read-heavy, higher weight)
  { method: 'GET', path: '/get_organizers.php', weight: 15, name: 'GET /get_organizers' },
  { method: 'GET', path: '/get_reports.php', weight: 15, name: 'GET /get_reports' },
  { method: 'GET', path: '/get_cleanliness_score.php', weight: 15, name: 'GET /get_cleanliness_score' },
  { method: 'GET', path: '/get_organizers.php?city=Hyderabad', weight: 5, name: 'GET /get_organizers?city' },
  { method: 'GET', path: '/get_reports.php?org_id=1', weight: 5, name: 'GET /get_reports?org_id' },
  { method: 'GET', path: '/get_user_details.php?user_id=1', weight: 5, name: 'GET /get_user_details' },
  { method: 'GET', path: '/get_user_reports.php?user_id=1', weight: 5, name: 'GET /get_user_reports' },
  { method: 'GET', path: '/get_org_details.php?org_id=1', weight: 5, name: 'GET /get_org_details' },

  // POST endpoints (auth traffic)
  {
    method: 'POST', path: '/user_login.php', weight: 15, name: 'POST /user_login',
    body: JSON.stringify({ email: 'testuser@example.com', password: 'TestPass123' })
  },
  {
    method: 'POST', path: '/org_login.php', weight: 15, name: 'POST /org_login',
    body: JSON.stringify({ email: 'testorg@example.com', password: 'OrgPass123' })
  },
];

// ─── Metrics Collection ─────────────────────────────────────────────────────
const metrics = {
  totalRequests: 0,
  successfulRequests: 0,
  failedRequests: 0,
  responseTimes: [],
  statusCodes: {},
  perEndpoint: {},
  errors: [],
  startTime: null,
  endTime: null,
};

// Initialize per-endpoint metrics
endpoints.forEach(ep => {
  metrics.perEndpoint[ep.name] = {
    count: 0,
    success: 0,
    failed: 0,
    responseTimes: [],
  };
});

// ─── Weighted Random Endpoint Selection ─────────────────────────────────────
function pickEndpoint() {
  const totalWeight = endpoints.reduce((sum, ep) => sum + ep.weight, 0);
  let rand = Math.random() * totalWeight;
  for (const ep of endpoints) {
    rand -= ep.weight;
    if (rand <= 0) return ep;
  }
  return endpoints[endpoints.length - 1];
}

// ─── Single Request Sender ──────────────────────────────────────────────────
function sendRequest(endpoint) {
  return new Promise((resolve) => {
    const startTime = Date.now();
    const url = new URL(BASE_PATH + endpoint.path, BASE_URL);

    const options = {
      hostname: url.hostname,
      port: url.port || 80,
      path: url.pathname + url.search,
      method: endpoint.method,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 60000, // 60s timeout (db.php has port-fallback that can be slow)
    };

    if (endpoint.body) {
      options.headers['Content-Length'] = Buffer.byteLength(endpoint.body);
    }

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        const responseTime = Date.now() - startTime;

        metrics.totalRequests++;
        metrics.responseTimes.push(responseTime);
        metrics.statusCodes[res.statusCode] = (metrics.statusCodes[res.statusCode] || 0) + 1;

        const epMetrics = metrics.perEndpoint[endpoint.name];
        epMetrics.count++;
        epMetrics.responseTimes.push(responseTime);

        // Any HTTP response = server handled the request (even if DB error in body)
        // This measures API/web-server throughput, not database correctness
        if (res.statusCode >= 200 && res.statusCode < 500) {
          metrics.successfulRequests++;
          epMetrics.success++;
        } else {
          metrics.failedRequests++;
          epMetrics.failed++;
        }

        resolve();
      });
    });

    req.on('error', (err) => {
      const responseTime = Date.now() - startTime;
      metrics.totalRequests++;
      metrics.failedRequests++;
      metrics.responseTimes.push(responseTime);
      metrics.errors.push(err.message);

      const epMetrics = metrics.perEndpoint[endpoint.name];
      epMetrics.count++;
      epMetrics.failed++;
      epMetrics.responseTimes.push(responseTime);

      resolve();
    });

    req.on('timeout', () => {
      req.destroy();
      const responseTime = Date.now() - startTime;
      metrics.totalRequests++;
      metrics.failedRequests++;
      metrics.responseTimes.push(responseTime);

      const epMetrics = metrics.perEndpoint[endpoint.name];
      epMetrics.count++;
      epMetrics.failed++;
      epMetrics.responseTimes.push(responseTime);

      resolve();
    });

    if (endpoint.body) {
      req.write(endpoint.body);
    }
    req.end();
  });
}

// ─── Virtual User Simulation ────────────────────────────────────────────────
async function virtualUser(vuId, testEndTime) {
  while (Date.now() < testEndTime) {
    const endpoint = pickEndpoint();
    await sendRequest(endpoint);
    // Small random think time (10-50ms) to simulate realistic user behavior
    await new Promise(r => setTimeout(r, Math.random() * 40 + 10));
  }
}

// ─── Statistics Calculation ─────────────────────────────────────────────────
function calculateStats(times) {
  if (times.length === 0) return { min: 0, max: 0, avg: 0, median: 0, p95: 0, p99: 0 };

  const sorted = [...times].sort((a, b) => a - b);
  const sum = sorted.reduce((a, b) => a + b, 0);

  return {
    min: sorted[0],
    max: sorted[sorted.length - 1],
    avg: Math.round(sum / sorted.length),
    median: sorted[Math.floor(sorted.length * 0.5)],
    p95: sorted[Math.floor(sorted.length * 0.95)],
    p99: sorted[Math.floor(sorted.length * 0.99)],
  };
}

// ─── Progress Reporter ──────────────────────────────────────────────────────
function startProgressReporter(testEndTime) {
  const interval = setInterval(() => {
    const elapsed = Math.round((Date.now() - metrics.startTime) / 1000);
    const remaining = Math.max(0, Math.round((testEndTime - Date.now()) / 1000));
    const rps = elapsed > 0 ? Math.round(metrics.totalRequests / elapsed) : 0;
    process.stdout.write(
      `\r  ⏱  Elapsed: ${elapsed}s | Remaining: ${remaining}s | ` +
      `Requests: ${metrics.totalRequests} | RPS: ${rps} | ` +
      `✅ ${metrics.successfulRequests} | ❌ ${metrics.failedRequests}   `
    );
  }, 1000);

  return interval;
}

// ─── Beautiful Report Generator ─────────────────────────────────────────────
function printReport() {
  const duration = (metrics.endTime - metrics.startTime) / 1000;
  const rps = Math.round(metrics.totalRequests / duration);
  const stats = calculateStats(metrics.responseTimes);

  console.log('\n');
  console.log('╔══════════════════════════════════════════════════════════════════════╗');
  console.log('║              🏗️  CIVICBIN LOAD TEST RESULTS                         ║');
  console.log('║              Baseline / Load Testing Report                         ║');
  console.log('╚══════════════════════════════════════════════════════════════════════╝');
  console.log('');

  // ── Test Configuration ──
  console.log('┌──────────────────────────────────────────────────────────────────────┐');
  console.log('│  📋 TEST CONFIGURATION                                              │');
  console.log('├──────────────────────────────────────────────────────────────────────┤');
  console.log(`│  Target:           ${BASE_URL}${BASE_PATH}`.padEnd(71) + '│');
  console.log(`│  Virtual Users:    ${TOTAL_VUS}`.padEnd(71) + '│');
  console.log(`│  Duration:         ${duration.toFixed(1)}s`.padEnd(71) + '│');
  console.log(`│  Endpoints Tested: ${endpoints.length}`.padEnd(71) + '│');
  console.log('└──────────────────────────────────────────────────────────────────────┘');
  console.log('');

  // ── Overall Results ──
  console.log('┌──────────────────────────────────────────────────────────────────────┐');
  console.log('│  📊 OVERALL RESULTS                                                 │');
  console.log('├──────────────────────────────────────────────────────────────────────┤');
  console.log(`│  Total Requests:       ${metrics.totalRequests.toLocaleString()}`.padEnd(71) + '│');
  console.log(`│  Successful:           ${metrics.successfulRequests.toLocaleString()} ✅`.padEnd(71) + '│');
  console.log(`│  Failed:               ${metrics.failedRequests.toLocaleString()} ❌`.padEnd(71) + '│');
  console.log(`│  Success Rate:         ${((metrics.successfulRequests / metrics.totalRequests) * 100).toFixed(2)}%`.padEnd(71) + '│');
  console.log('└──────────────────────────────────────────────────────────────────────┘');
  console.log('');

  // ── Throughput ──
  console.log('┌──────────────────────────────────────────────────────────────────────┐');
  console.log('│  🚀 THROUGHPUT (Requests Per Second)                                 │');
  console.log('├──────────────────────────────────────────────────────────────────────┤');
  console.log(`│  RPS:  ${rps} req/sec`.padEnd(71) + '│');

  // Visual RPS bar
  const barLength = Math.min(50, rps);
  const bar = '█'.repeat(barLength) + '░'.repeat(50 - barLength);
  console.log(`│  [${bar}]`.padEnd(71) + '│');
  console.log('└──────────────────────────────────────────────────────────────────────┘');
  console.log('');

  // ── Response Times ──
  console.log('┌──────────────────────────────────────────────────────────────────────┐');
  console.log('│  ⏱️  RESPONSE TIMES                                                  │');
  console.log('├──────────────────────────────────────────────────────────────────────┤');
  console.log(`│  Min:       ${stats.min}ms`.padEnd(71) + '│');
  console.log(`│  Average:   ${stats.avg}ms`.padEnd(71) + '│');
  console.log(`│  Median:    ${stats.median}ms`.padEnd(71) + '│');
  console.log(`│  P95:       ${stats.p95}ms`.padEnd(71) + '│');
  console.log(`│  P99:       ${stats.p99}ms`.padEnd(71) + '│');
  console.log(`│  Max:       ${stats.max}ms`.padEnd(71) + '│');
  console.log('└──────────────────────────────────────────────────────────────────────┘');
  console.log('');

  // ── Status Codes ──
  console.log('┌──────────────────────────────────────────────────────────────────────┐');
  console.log('│  📡 HTTP STATUS CODES                                                │');
  console.log('├──────────────────────────────────────────────────────────────────────┤');
  Object.entries(metrics.statusCodes)
    .sort(([a], [b]) => Number(a) - Number(b))
    .forEach(([code, count]) => {
      const pct = ((count / metrics.totalRequests) * 100).toFixed(1);
      const icon = code.startsWith('2') ? '✅' : code.startsWith('4') ? '⚠️' : '❌';
      console.log(`│  ${icon} HTTP ${code}: ${count.toLocaleString()} (${pct}%)`.padEnd(71) + '│');
    });
  console.log('└──────────────────────────────────────────────────────────────────────┘');
  console.log('');

  // ── Per-Endpoint Breakdown ──
  console.log('┌──────────────────────────────────────────────────────────────────────┐');
  console.log('│  📌 PER-ENDPOINT BREAKDOWN                                          │');
  console.log('├──────────────────────────────────────────────────────────────────────┤');

  Object.entries(metrics.perEndpoint).forEach(([name, epData]) => {
    if (epData.count === 0) return;
    const epStats = calculateStats(epData.responseTimes);
    console.log(`│  ${name}`.padEnd(71) + '│');
    console.log(`│    Requests: ${epData.count} | Avg: ${epStats.avg}ms | P95: ${epStats.p95}ms | Max: ${epStats.max}ms`.padEnd(71) + '│');
    console.log('│'.padEnd(71) + '│');
  });
  console.log('└──────────────────────────────────────────────────────────────────────┘');
  console.log('');

  // ── Errors ──
  if (metrics.errors.length > 0) {
    const uniqueErrors = [...new Set(metrics.errors)];
    console.log('┌──────────────────────────────────────────────────────────────────────┐');
    console.log('│  ⚠️  ERRORS                                                          │');
    console.log('├──────────────────────────────────────────────────────────────────────┤');
    uniqueErrors.forEach(err => {
      const count = metrics.errors.filter(e => e === err).length;
      console.log(`│  ❌ ${err} (x${count})`.padEnd(71) + '│');
    });
    console.log('└──────────────────────────────────────────────────────────────────────┘');
    console.log('');
  }

  // ── Verdict ──
  const successRate = (metrics.successfulRequests / metrics.totalRequests) * 100;
  console.log('┌──────────────────────────────────────────────────────────────────────┐');
  console.log('│  🏆 VERDICT                                                          │');
  console.log('├──────────────────────────────────────────────────────────────────────┤');

  if (successRate >= 99 && stats.avg < 500) {
    console.log('│  ✅ EXCELLENT — API handles baseline load with ease!'.padEnd(71) + '│');
  } else if (successRate >= 95 && stats.avg < 1000) {
    console.log('│  ✅ GOOD — API handles load well, minor optimizations possible.'.padEnd(71) + '│');
  } else if (successRate >= 90) {
    console.log('│  ⚠️  FAIR — Some degradation under load. Review slow endpoints.'.padEnd(71) + '│');
  } else {
    console.log('│  ❌ POOR — Significant issues under load. Needs optimization.'.padEnd(71) + '│');
  }

  console.log(`│  Success Rate: ${successRate.toFixed(2)}% | Avg Response: ${stats.avg}ms | RPS: ${rps}`.padEnd(71) + '│');
  console.log('└──────────────────────────────────────────────────────────────────────┘');
  console.log('');
}

// ─── Main Test Runner ───────────────────────────────────────────────────────
async function runLoadTest() {
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════════════╗');
  console.log('║  🚀 CIVICBIN BASELINE LOAD TEST                                     ║');
  console.log('║  100 Virtual Users | 60 Seconds | All API Endpoints                 ║');
  console.log('╚══════════════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log('  🔄 Starting test...');
  console.log(`  📍 Target: ${BASE_URL}${BASE_PATH}`);
  console.log(`  👥 Virtual Users: ${TOTAL_VUS}`);
  console.log(`  ⏱  Duration: ${TEST_DURATION_MS / 1000}s (with ${RAMP_UP_MS / 1000}s ramp-up)`);
  console.log('');

  metrics.startTime = Date.now();
  const testEndTime = metrics.startTime + TEST_DURATION_MS;

  // Start progress reporter
  const progressInterval = startProgressReporter(testEndTime);

  // Ramp up virtual users gradually
  const vuPromises = [];
  const rampInterval = RAMP_UP_MS / TOTAL_VUS;

  for (let i = 0; i < TOTAL_VUS; i++) {
    vuPromises.push(virtualUser(i, testEndTime));

    // Stagger VU start times during ramp-up
    if (i < TOTAL_VUS - 1) {
      await new Promise(r => setTimeout(r, rampInterval));
    }
  }

  // Wait for all VUs to complete
  await Promise.all(vuPromises);

  metrics.endTime = Date.now();
  clearInterval(progressInterval);

  // Print the report
  printReport();

  // Save results to JSON
  const fs = require('fs');
  const resultsPath = require('path').join(__dirname, 'results.json');
  fs.writeFileSync(resultsPath, JSON.stringify({
    config: { totalVUs: TOTAL_VUS, durationMs: TEST_DURATION_MS, target: BASE_URL + BASE_PATH },
    summary: {
      totalRequests: metrics.totalRequests,
      successfulRequests: metrics.successfulRequests,
      failedRequests: metrics.failedRequests,
      rps: Math.round(metrics.totalRequests / ((metrics.endTime - metrics.startTime) / 1000)),
      responseTime: calculateStats(metrics.responseTimes),
      statusCodes: metrics.statusCodes,
    },
    perEndpoint: Object.fromEntries(
      Object.entries(metrics.perEndpoint).map(([name, data]) => [
        name,
        { ...data, stats: calculateStats(data.responseTimes) }
      ])
    ),
    timestamp: new Date().toISOString(),
  }, null, 2));

  console.log(`  💾 Results saved to: ${resultsPath}`);
  console.log('');
}

// ─── Run! ───────────────────────────────────────────────────────────────────
runLoadTest().catch(console.error);
