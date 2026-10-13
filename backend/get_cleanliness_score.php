<?php
require_once 'db.php';

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

$sql = "SELECT COUNT(*) as open_reports FROM reports WHERE status != 'Completed'";
$result = $conn->query($sql);

if ($result) {
    $row = $result->fetch_assoc();
    $openReports = (int)$row['open_reports'];
    $score = max(0, 100 - ($openReports * 2));
    
    echo json_encode(["status" => "success", "score" => $score]);
} else {
    echo json_encode(["status" => "error", "message" => $conn->error]);
}

$conn->close();
?>
