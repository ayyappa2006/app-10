<?php
require_once 'db.php';

if (!isset($_GET['user_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing user_id parameter"]);
    exit;
}

$user_id = (int)$_GET['user_id'];

$stmt = $conn->prepare("SELECT * FROM reports WHERE user_id = ? ORDER BY created_at DESC");
$stmt->bind_param("i", $user_id);
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
