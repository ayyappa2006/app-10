<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id']) || !isset($data['city_name']) || !isset($data['address']) || !isset($data['contact_number'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$user_id = (int)$data['user_id'];
$city = $conn->real_escape_string($data['city_name']);
$address = $conn->real_escape_string($data['address']);
$contact = $conn->real_escape_string($data['contact_number']);
$category = isset($data['category']) ? $conn->real_escape_string($data['category']) : 'General';
$description = isset($data['description']) ? $conn->real_escape_string($data['description']) : '';
$photo = isset($data['photo_uri']) ? $conn->real_escape_string($data['photo_uri']) : "";

$sql = "INSERT INTO reports (user_id, city_name, address, contact_number, category, description, photo_uri, status) 
        VALUES ($user_id, '$city', '$address', '$contact', '$category', '$description', '$photo', 'Pending')";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["status" => "success", "message" => "Report submitted successfully", "report_id" => $conn->insert_id]);
} else {
    echo json_encode(["status" => "error", "message" => "Error: " . $conn->error]);
}
$conn->close();
?>
