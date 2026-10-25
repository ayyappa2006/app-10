<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['report_id']) || !isset($data['city_name']) || !isset($data['address']) || !isset($data['contact_number'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$report_id = (int)$data['report_id'];
$city_name = trim($data['city_name']);
$address = trim($data['address']);
$contact_number = trim($data['contact_number']);
$user_id = isset($data['user_id']) ? (int)$data['user_id'] : 0;

// Mitigate IDOR & SQL Injection (SEC-008, SEC-014) with prepared statement
if ($user_id > 0) {
    $stmt = $conn->prepare("UPDATE reports SET city_name = ?, address = ?, contact_number = ? WHERE id = ? AND user_id = ?");
    $stmt->bind_param("sssii", $city_name, $address, $contact_number, $report_id, $user_id);
} else {
    $stmt = $conn->prepare("UPDATE reports SET city_name = ?, address = ?, contact_number = ? WHERE id = ?");
    $stmt->bind_param("sssi", $city_name, $address, $contact_number, $report_id);
}

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Report updated successfully"]);
} else {
    error_log("Update report error: " . $stmt->error);
    echo json_encode(["status" => "error", "message" => "Failed to update report"]);
}

$stmt->close();
$conn->close();
?>
