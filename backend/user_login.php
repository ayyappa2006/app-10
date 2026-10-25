<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['email']) || !isset($data['password'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$email = trim($data['email']);
$password = $data['password'];

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(["status" => "error", "message" => "Invalid email format"]);
    exit;
}

// Prepared statement to eliminate SQL Injection (SEC-001)
$stmt = $conn->prepare("SELECT id, name, password FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result && $result->num_rows > 0) {
    $row = $result->fetch_assoc();
    if (password_verify($password, $row['password'])) {
        // Generate secure random session token (SEC-006)
        $session_token = bin2hex(random_bytes(32));
        
        echo json_encode([
            "status" => "success",
            "message" => "Login successful",
            "user_id" => (int)$row['id'],
            "name" => clean_output($row['name']),
            "auth_token" => $session_token
        ]);
    } else {
        // Generic error message to prevent account enumeration (SEC-024)
        echo json_encode(["status" => "error", "message" => "Invalid email or password"]);
    }
} else {
    // Generic error message to prevent account enumeration (SEC-024)
    echo json_encode(["status" => "error", "message" => "Invalid email or password"]);
}

$stmt->close();
$conn->close();
?>
