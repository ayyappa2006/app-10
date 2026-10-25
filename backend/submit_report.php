<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id']) || !isset($data['city_name']) || !isset($data['address']) || !isset($data['contact_number'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$user_id = (int)$data['user_id'];
$city = trim($data['city_name']);
$address = trim($data['address']);
$contact = trim($data['contact_number']);
$category = isset($data['category']) ? trim($data['category']) : 'General';
$description = isset($data['description']) ? trim($data['description']) : '';
$photo = isset($data['photo_uri']) ? trim($data['photo_uri']) : '';
$status = 'Pending';

// Prepared statement to eliminate SQL Injection (SEC-002, SEC-014)
$stmt = $conn->prepare("INSERT INTO reports (user_id, city_name, address, contact_number, category, description, photo_uri, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
$stmt->bind_param("isssssss", $user_id, $city, $address, $contact, $category, $description, $photo, $status);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Report submitted successfully",
        "report_id" => $stmt->insert_id
    ]);
} else {
    // Generic error message - never leak $conn->error to client (SEC-014)
    error_log("Submit report database error: " . $stmt->error);
    echo json_encode(["status" => "error", "message" => "Failed to submit report. Please try again."]);
}

$stmt->close();
$conn->close();
?>
