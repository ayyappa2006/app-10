module.exports = {
  // Default URL to hit if not overridden via command arguments
  defaultUrl: "http://localhost:8085",
  
  // Baseline / Load testing criteria
  connections: 100, // 100 concurrent virtual users
  duration: 60,      // Run continuously for 60 seconds (1 minute)
  
  // Headers to pass if necessary (e.g. simulating user JSON requests)
  headers: {
    "accept": "*/*"
  }
};
