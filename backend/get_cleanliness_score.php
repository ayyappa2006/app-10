<?php
require_once 'db.php';

$sql = "SELECT COUNT(*) as open_reports FROM reports WHERE status != 'Completed'";
$result = $conn->query($sql);

if ($result) {
    $row = $result->fetch_assoc();
    $openReports = (int)$row['open_reports'];
    $score = max(0, 100 - ($openReports * 2));
    
    echo json_encode(["status" => "success", "score" => $score]);
} else {
    error_log("Cleanliness score query error: " . $conn->error);
    echo json_encode(["status" => "error", "message" => "Unable to calculate cleanliness score"]);
}

$conn->close();
?>
