<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['report_id']) || !isset($data['status'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$report_id = (int)$data['report_id'];
$status = trim($data['status']);

// Validate status against strict whitelist (SEC-023)
$allowed_statuses = ['Pending', 'In Progress', 'Completed'];
if (!in_array($status, $allowed_statuses)) {
    echo json_encode(["status" => "error", "message" => "Invalid status value"]);
    exit;
}

$org_id = isset($data['org_id']) && (int)$data['org_id'] > 0 ? (int)$data['org_id'] : null;

// Prepared statement to eliminate SQL Injection & unauthorized status attribution (SEC-009)
if ($status === 'Completed' && $org_id !== null) {
    $stmt = $conn->prepare("UPDATE reports SET status = ?, resolved_by_org_id = ? WHERE id = ?");
    $stmt->bind_param("sii", $status, $org_id, $report_id);
} else {
    $stmt = $conn->prepare("UPDATE reports SET status = ?, resolved_by_org_id = NULL WHERE id = ?");
    $stmt->bind_param("si", $status, $report_id);
}

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Status updated successfully"]);
} else {
    error_log("Update status error: " . $stmt->error);
    echo json_encode(["status" => "error", "message" => "Error updating status"]);
}

$stmt->close();
$conn->close();
?>
