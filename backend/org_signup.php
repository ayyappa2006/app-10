<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['org_name']) || !isset($data['email']) || !isset($data['password'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$org_name = $conn->real_escape_string($data['org_name']);
$email = $conn->real_escape_string($data['email']);
$password = password_hash($data['password'], PASSWORD_DEFAULT);
$city = isset($data['city']) ? $conn->real_escape_string($data['city']) : '';

// Check if email exists
$check_sql = "SELECT id FROM organizations WHERE email='$email'";
$result = $conn->query($check_sql);

if ($result->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "Email already registered"]);
    exit;
}

$sql = "INSERT INTO organizations (org_name, email, password, city) VALUES ('$org_name', '$email', '$password', '$city')";

if ($conn->query($sql) === TRUE) {
    $org_id = $conn->insert_id;
    echo json_encode([
        "status" => "success", 
        "message" => "Organization registered successfully",
        "org_id" => $org_id,
        "org_name" => $org_name
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Error: " . $conn->error]);
}

$conn->close();
?>
