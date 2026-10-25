<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id']) || !isset($data['org_id']) || !isset($data['sender_type']) || !isset($data['message'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$user_id = (int)$data['user_id'];
$org_id = (int)$data['org_id'];
$sender_type = trim($data['sender_type']);
$message = trim($data['message']);

if (!in_array($sender_type, ['user', 'org'])) {
    echo json_encode(["status" => "error", "message" => "Invalid sender type"]);
    exit;
}

$stmt = $conn->prepare("INSERT INTO messages (user_id, org_id, sender_type, message) VALUES (?, ?, ?, ?)");
$stmt->bind_param("iiss", $user_id, $org_id, $sender_type, $message);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Message sent", "message_id" => $stmt->insert_id]);
} else {
    error_log("Send message error: " . $stmt->error);
    echo json_encode(["status" => "error", "message" => "Failed to deliver message"]);
}

$stmt->close();
$conn->close();
?>
