<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['report_id']) || !isset($data['city_name']) || !isset($data['address']) || !isset($data['contact_number'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$report_id = (int)$data['report_id'];
$city_name = $conn->real_escape_string($data['city_name']);
$address = $conn->real_escape_string($data['address']);
$contact_number = $conn->real_escape_string($data['contact_number']);

$sql = "UPDATE reports SET city_name='$city_name', address='$address', contact_number='$contact_number' WHERE id=$report_id";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "error", "message" => $conn->error]);
}

$conn->close();
?>
