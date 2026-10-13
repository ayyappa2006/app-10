<?php
require_once 'db.php';

$org_id = isset($_GET['org_id']) ? (int)$_GET['org_id'] : 0;

if ($org_id > 0) {
    $sql = "SELECT * FROM reports WHERE resolved_by_org_id IS NULL OR resolved_by_org_id = $org_id ORDER BY created_at DESC";
} else {
    $sql = "SELECT * FROM reports ORDER BY created_at DESC";
}
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
