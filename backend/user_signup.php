<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['name']) || !isset($data['email']) || !isset($data['password'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$name = trim($data['name']);
$email = trim($data['email']);
$password_raw = $data['password'];

// Input validation: email format (SEC-018)
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(["status" => "error", "message" => "Invalid email format"]);
    exit;
}

// Input validation: password strength policy (SEC-017)
if (strlen($password_raw) < 6) {
    echo json_encode(["status" => "error", "message" => "Password must be at least 6 characters long"]);
    exit;
}

$password = password_hash($password_raw, PASSWORD_DEFAULT);

// Check if email already registered using prepared statement (SEC-002)
$check_stmt = $conn->prepare("SELECT id FROM users WHERE email = ?");
$check_stmt->bind_param("s", $email);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result && $check_result->num_rows > 0) {
    $check_stmt->close();
    echo json_encode(["status" => "error", "message" => "Email already registered"]);
    exit;
}
$check_stmt->close();

// Insert user using prepared statement (SEC-002)
$insert_stmt = $conn->prepare("INSERT INTO users (name, email, password) VALUES (?, ?, ?)");
$insert_stmt->bind_param("sss", $name, $email, $password);

if ($insert_stmt->execute()) {
    $user_id = $insert_stmt->insert_id;
    $auth_token = bin2hex(random_bytes(32));
    echo json_encode([
        "status" => "success", 
        "message" => "User registered successfully",
        "user_id" => $user_id,
        "name" => clean_output($name),
        "auth_token" => $auth_token
    ]);
} else {
    // Generic error - never leak $conn->error (SEC-014)
    error_log("Signup error: " . $insert_stmt->error);
    echo json_encode(["status" => "error", "message" => "Unable to complete registration. Please try again."]);
}

$insert_stmt->close();
$conn->close();
?>
