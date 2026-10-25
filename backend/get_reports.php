<?php
require_once 'db.php';

$org_id = isset($_GET['org_id']) ? (int)$_GET['org_id'] : 0;

if ($org_id > 0) {
    $stmt = $conn->prepare("SELECT * FROM reports WHERE resolved_by_org_id IS NULL OR resolved_by_org_id = ? ORDER BY created_at DESC");
    $stmt->bind_param("i", $org_id);
} else {
    $stmt = $conn->prepare("SELECT * FROM reports ORDER BY created_at DESC");
}

$stmt->execute();
$result = $stmt->get_result();

$reports = [];
if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $reports[] = [
            "id" => (int)$row['id'],
            "user_id" => (int)$row['user_id'],
            "city_name" => clean_output($row['city_name']),
            "address" => clean_output($row['address']),
            "contact_number" => clean_output($row['contact_number']),
            "category" => clean_output($row['category']),
            "description" => clean_output($row['description']),
            "photo_uri" => clean_output($row['photo_uri']),
            "status" => clean_output($row['status']),
            "resolved_by_org_id" => $row['resolved_by_org_id'] !== null ? (int)$row['resolved_by_org_id'] : null,
            "created_at" => $row['created_at']
        ];
    }
}

echo json_encode(["status" => "success", "data" => $reports]);
$stmt->close();
$conn->close();
?>
