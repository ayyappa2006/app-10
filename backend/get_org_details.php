<?php
require_once 'db.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

if (!isset($_GET['org_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing org_id"]);
    exit;
}

$org_id = $conn->real_escape_string($_GET['org_id']);

$sql = "SELECT org_name, email, city FROM organizations WHERE id='$org_id'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        "status" => "success",
        "data" => [
            "org_name" => $row['org_name'],
            "email" => $row['email'],
            "city" => isset($row['city']) ? $row['city'] : ''
        ]
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Organization not found"]);
}

$conn->close();
?>
