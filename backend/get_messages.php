<?php
require_once 'db.php';

$user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;
$org_id = isset($_GET['org_id']) ? (int)$_GET['org_id'] : 0;
$mark_read = isset($_GET['mark_read']) ? $_GET['mark_read'] : '';
$viewer_type = isset($_GET['viewer_type']) ? $conn->real_escape_string($_GET['viewer_type']) : '';

if ($user_id === 0 || $org_id === 0) {
    echo json_encode(["status" => "error", "message" => "Missing user_id or org_id"]);
    exit;
}

if ($mark_read === 'true' && !empty($viewer_type)) {
    // If user is viewing, mark messages from org as read. If org is viewing, mark messages from user as read.
    $sender_to_mark = ($viewer_type === 'user') ? 'org' : 'user';
    $update_sql = "UPDATE messages SET is_read = TRUE WHERE user_id = $user_id AND org_id = $org_id AND sender_type = '$sender_to_mark'";
    $conn->query($update_sql);
}

$sql = "SELECT id, sender_type, message, created_at, is_read FROM messages WHERE user_id = $user_id AND org_id = $org_id ORDER BY created_at ASC";
$result = $conn->query($sql);
$messages = [];

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $messages[] = $row;
    }
}

echo json_encode(["status" => "success", "data" => $messages]);
$conn->close();
?>
