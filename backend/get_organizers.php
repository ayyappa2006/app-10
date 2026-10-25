<?php
require_once 'db.php';

$city = isset($_GET['city']) ? trim($_GET['city']) : '';

// Mitigate UNION SQLi and LIKE Wildcard Injection (SEC-003, SEC-020)
if (!empty($city)) {
    // Escape LIKE wildcards to prevent directory/wildcard leaks
    $escaped_city = addcslashes($city, '%_');
    $like_param = '%' . $escaped_city . '%';
    
    $stmt = $conn->prepare("SELECT id, org_name, email, city FROM organizations WHERE city LIKE ?");
    $stmt->bind_param("s", $like_param);
    $stmt->execute();
    $result = $stmt->get_result();
} else {
    $stmt = $conn->prepare("SELECT id, org_name, email, city FROM organizations");
    $stmt->execute();
    $result = $stmt->get_result();
}

$organizers = [];
if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $organizers[] = [
            "id" => (int)$row['id'],
            "org_name" => clean_output($row['org_name']),
            "email" => clean_output($row['email']),
            "city" => clean_output($row['city'])
        ];
    }
}

echo json_encode(["status" => "success", "data" => $organizers]);

$stmt->close();
$conn->close();
?>
