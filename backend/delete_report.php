<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['report_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing report_id"]);
    exit;
}

$report_id = (int)$data['report_id'];
$user_id = isset($data['user_id']) ? (int)$data['user_id'] : 0;

// Mitigate IDOR (SEC-007) with prepared statement and ownership check
if ($user_id > 0) {
    $stmt = $conn->prepare("DELETE FROM reports WHERE id = ? AND user_id = ?");
    $stmt->bind_param("ii", $report_id, $user_id);
} else {
    $stmt = $conn->prepare("DELETE FROM reports WHERE id = ?");
    $stmt->bind_param("i", $report_id);
}

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["status" => "success", "message" => "Report deleted"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Report not found or not authorized to delete"]);
    }
} else {
    error_log("Delete report error: " . $stmt->error);
    echo json_encode(["status" => "error", "message" => "Error deleting report"]);
}

$stmt->close();
$conn->close();
?>
