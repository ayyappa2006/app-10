<?php
require_once 'db.php';

$user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;
$org_id = isset($_GET['org_id']) ? (int)$_GET['org_id'] : 0;
$mark_read = isset($_GET['mark_read']) ? $_GET['mark_read'] : '';
$viewer_type = isset($_GET['viewer_type']) ? trim($_GET['viewer_type']) : '';

if ($user_id === 0 || $org_id === 0) {
    echo json_encode(["status" => "error", "message" => "Missing user_id or org_id"]);
    exit;
}

// Prepared statement to mark messages as read
if ($mark_read === 'true' && !empty($viewer_type)) {
    $sender_to_mark = ($viewer_type === 'user') ? 'org' : 'user';
    $update_stmt = $conn->prepare("UPDATE messages SET is_read = TRUE WHERE user_id = ? AND org_id = ? AND sender_type = ?");
    $update_stmt->bind_param("iis", $user_id, $org_id, $sender_to_mark);
    $update_stmt->execute();
    $update_stmt->close();
}

// Prepared statement to fetch chat messages (SEC-022)
$stmt = $conn->prepare("SELECT id, sender_type, message, created_at, is_read FROM messages WHERE user_id = ? AND org_id = ? ORDER BY created_at ASC");
$stmt->bind_param("ii", $user_id, $org_id);
$stmt->execute();
$result = $stmt->get_result();

$messages = [];
if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $messages[] = [
            "id" => (int)$row['id'],
            "sender_type" => clean_output($row['sender_type']),
            "message" => clean_output($row['message']),
            "created_at" => $row['created_at'],
            "is_read" => (bool)$row['is_read']
        ];
    }
}

echo json_encode(["status" => "success", "data" => $messages]);

$stmt->close();
$conn->close();
?>
