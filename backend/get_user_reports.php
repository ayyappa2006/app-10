<?php
require_once 'db.php';

if (!isset($_GET['user_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing user_id parameter"]);
    exit;
}

$user_id = (int)$_GET['user_id'];
$sql = "SELECT * FROM reports WHERE user_id=$user_id ORDER BY created_at DESC";
$result = $conn->query($sql);

$reports = [];
if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $reports[] = $row;
    }
}

echo json_encode(["status" => "success", "data" => $reports]);
$conn->close();
?>
