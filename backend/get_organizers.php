<?php
require_once 'db.php';

$city = isset($_GET['city']) ? $conn->real_escape_string($_GET['city']) : '';

$sql = "SELECT id, org_name, email, city FROM organizations";
if (!empty($city)) {
    $sql .= " WHERE city LIKE '%$city%'";
}

$result = $conn->query($sql);
$organizers = [];

if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $organizers[] = $row;
    }
}

echo json_encode(["status" => "success", "data" => $organizers]);
$conn->close();
?>
