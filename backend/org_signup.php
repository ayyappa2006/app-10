<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['org_name']) || !isset($data['email']) || !isset($data['password'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$org_name = trim($data['org_name']);
$email = trim($data['email']);
$password_raw = $data['password'];
$city = isset($data['city']) ? trim($data['city']) : '';

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(["status" => "error", "message" => "Invalid email format"]);
    exit;
}

if (strlen($password_raw) < 6) {
    echo json_encode(["status" => "error", "message" => "Password must be at least 6 characters long"]);
    exit;
}

$password = password_hash($password_raw, PASSWORD_DEFAULT);

// Prepared statement to check existing email (SEC-002)
$check_stmt = $conn->prepare("SELECT id FROM organizations WHERE email = ?");
$check_stmt->bind_param("s", $email);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result && $check_result->num_rows > 0) {
    $check_stmt->close();
    echo json_encode(["status" => "error", "message" => "Email already registered"]);
    exit;
}
$check_stmt->close();

// Prepared statement for insert (SEC-002)
$insert_stmt = $conn->prepare("INSERT INTO organizations (org_name, email, password, city) VALUES (?, ?, ?, ?)");
$insert_stmt->bind_param("ssss", $org_name, $email, $password, $city);

if ($insert_stmt->execute()) {
    $org_id = $insert_stmt->insert_id;
    $auth_token = bin2hex(random_bytes(32));
    echo json_encode([
        "status" => "success", 
        "message" => "Organization registered successfully",
        "org_id" => $org_id,
        "org_name" => clean_output($org_name),
        "auth_token" => $auth_token
    ]);
} else {
    error_log("Org signup error: " . $insert_stmt->error);
    echo json_encode(["status" => "error", "message" => "Unable to complete organization registration"]);
}

$insert_stmt->close();
$conn->close();
?>
