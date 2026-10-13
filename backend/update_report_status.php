<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['report_id']) || !isset($data['status'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$report_id = (int)$data['report_id'];
$status = $conn->real_escape_string($data['status']);
$org_id = isset($data['org_id']) ? (int)$data['org_id'] : 'NULL';

if ($status === 'Completed') {
    $sql = "UPDATE reports SET status='$status', resolved_by_org_id=$org_id WHERE id=$report_id";
} else {
    $sql = "UPDATE reports SET status='$status', resolved_by_org_id=NULL WHERE id=$report_id";
}

if ($conn->query($sql) === TRUE) {
    echo json_encode(["status" => "success", "message" => "Status updated"]);
} else {
    echo json_encode(["status" => "error", "message" => "Error updating status"]);
}
$conn->close();
?>
