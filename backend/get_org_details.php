<?php
require_once 'db.php';

if (!isset($_GET['org_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing org_id"]);
    exit;
}

$org_id = (int)$_GET['org_id'];

$stmt = $conn->prepare("SELECT org_name, email, city FROM organizations WHERE id = ?");
$stmt->bind_param("i", $org_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result && $result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        "status" => "success",
        "data" => [
            "org_name" => clean_output($row['org_name']),
            "email" => clean_output($row['email']),
            "city" => clean_output(isset($row['city']) ? $row['city'] : '')
        ]
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Organization not found"]);
}

$stmt->close();
$conn->close();
?>
