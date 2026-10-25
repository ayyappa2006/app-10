# CivicBin Baseline / Load Testing Suite

This testing suite conducts baseline load testing using **Autocannon** to stress-test CivicBin endpoints under expected traffic patterns.

## Target Test Parameters

- **Virtual Users (Concurrency)**: 100 concurrent users at any given time.
- **Duration**: Runs continuously for 60 seconds (1 minute).
- **Target URL**: Defaults to the local web server (`http://localhost:8085`), but can be pointed directly to any staging/hosted frontend or backend API.

## Installation

Run the following in the `/performance-tests` folder:
```bash
npm install
```

## Running the Load Test

To run the load test against the default local web server:
```bash
npm test
```

To run against a custom hosted URL (e.g. your backend API):
```bash
node load-test.js https://your-domain.com/backend/get_organizers.php
```

## Key Performance Metrics (Output Interpretation)

1. **Requests per second (RPS)**:
   - Tracks how many total HTTP requests the system handles in one second.
   - *Example*: `120 req/sec` means the API resolves 120 calls per second.

2. **Response Time (Latency)**:
   - Tracks the delay between sending a request and receiving the response.
   - **Min**: Fastest response time captured.
   - **Average**: Overall mean response time (should stay low, e.g. < 300ms).
   - **Max**: Slowest response time captured (typically database cold-start or image loads).
