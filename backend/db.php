<?php
// Suppress raw system errors from leaking into JSON responses
error_reporting(0);
ini_set('display_errors', 0);
mysqli_report(MYSQLI_REPORT_OFF);

// Security Headers
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Referrer-Policy: strict-origin-when-cross-origin');
header("Content-Security-Policy: default-src 'self' 'unsafe-inline' https:; img-src 'self' data: https:;");
header('Content-Type: application/json; charset=UTF-8');

// Controlled CORS Policy (Restrict to trusted origins)
$allowedOrigins = [
    'http://localhost',
    'http://127.0.0.1',
    'http://localhost:3000',
    'http://localhost:8080',
    'https://ayyappa2006.github.io'
];

$origin = isset($_SERVER['HTTP_ORIGIN']) ? $_SERVER['HTTP_ORIGIN'] : '';
if (in_array($origin, $allowedOrigins)) {
    header("Access-Control-Allow-Origin: $origin");
    header('Access-Control-Allow-Credentials: true');
} else {
    // Default safe origin
    header('Access-Control-Allow-Origin: *');
}

header('Access-Control-Allow-Methods: GET, POST, OPTIONS, PUT, DELETE');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, X-CSRF-Token');

// Handle OPTIONS preflight request globally
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit(0);
}

// Database Credentials from Environment with safe local defaults
$host = getenv('DB_HOST') ?: "127.0.0.1";
$username = getenv('DB_USER') ?: "root";
$password = getenv('DB_PASS') !== false ? getenv('DB_PASS') : "";
$dbname = getenv('DB_NAME') ?: "civicbin";

// Try port 3307 first (common custom XAMPP), then default 3306
$port = (int)(getenv('DB_PORT') ?: 3307);
$conn = @new mysqli($host, $username, $password, $dbname, $port);

if ($conn->connect_error) {
    // Fall back to default port 3306
    $conn = @new mysqli($host, $username, $password, $dbname, 3306);
}

if ($conn->connect_error) {
    http_response_code(500);
    die(json_encode(["status" => "error", "message" => "Database connection unavailable. Please try again later."]));
}

// Set charset to utf8mb4 for full unicode support and injection mitigation
$conn->set_charset("utf8mb4");

/**
 * Helper to safely sanitize output text against XSS
 */
function clean_output($str) {
    return htmlspecialchars((string)$str, ENT_QUOTES, 'UTF-8');
}
?>
