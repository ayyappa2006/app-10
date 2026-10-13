<?php
require_once 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['report_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing report_id"]);
    exit;
}

$report_id = (int)$data['report_id'];
$sql = "DELETE FROM reports WHERE id=$report_id";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["status" => "success", "message" => "Report deleted"]);
} else {
    echo json_encode(["status" => "error", "message" => "Error deleting report"]);
}
$conn->close();
?>
