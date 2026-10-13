<?php
require_once 'db.php';

$org_id = isset($_GET['org_id']) ? (int)$_GET['org_id'] : 0;

if ($org_id === 0) {
    echo json_encode(["status" => "error", "message" => "Missing org_id"]);
    exit;
}

// Get the latest message for each user chatting with this organization
$sql = "
    SELECT 
        u.id AS user_id, 
        u.name AS user_name, 
        m.message AS last_message, 
        m.created_at AS last_message_time,
        (SELECT COUNT(*) FROM messages WHERE org_id = $org_id AND user_id = u.id AND sender_type = 'user' AND is_read = FALSE) AS unread_count
    FROM users u
    JOIN messages m ON u.id = m.user_id
    WHERE m.org_id = $org_id 
      AND m.id = (
          SELECT MAX(id) FROM messages WHERE user_id = u.id AND org_id = $org_id
      )
    ORDER BY m.created_at DESC
";

$result = $conn->query($sql);
$chats = [];

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $chats[] = $row;
    }
}

echo json_encode(["status" => "success", "data" => $chats]);
$conn->close();
?>
