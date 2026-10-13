<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id']) || !isset($data['org_id']) || !isset($data['sender_type']) || !isset($data['message'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$user_id = (int)$data['user_id'];
$org_id = (int)$data['org_id'];
$sender_type = $conn->real_escape_string($data['sender_type']);
$message = $conn->real_escape_string($data['message']);

$sql = "INSERT INTO messages (user_id, org_id, sender_type, message) VALUES ($user_id, $org_id, '$sender_type', '$message')";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["status" => "success", "message" => "Message sent"]);
} else {
    echo json_encode(["status" => "error", "message" => "Error: " . $conn->error]);
}
$conn->close();
?>
