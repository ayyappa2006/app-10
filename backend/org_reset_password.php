<?php
require_once 'db.php';

// Only allow POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["status" => "error", "message" => "Method not allowed"]);
    exit;
}


// Read raw JSON body
$rawInput = file_get_contents("php://input");
$data = json_decode($rawInput, true);

// Validate required fields
if (!isset($data['new_password']) || empty(trim($data['new_password']))) {
    echo json_encode(["status" => "error", "message" => "New password is required"]);
    exit;
}

$new_password = password_hash(trim($data['new_password']), PASSWORD_DEFAULT);

if (isset($data['org_id']) && !empty(trim($data['org_id']))) {
    // Resetting by org_id (logged-in settings page)
    $org_id = $conn->real_escape_string(trim($data['org_id']));
    
    // First check if organization exists
    $checkSql = "SELECT id FROM organizations WHERE id = '$org_id'";
    $checkResult = $conn->query($checkSql);
    if (!$checkResult || $checkResult->num_rows === 0) {
        echo json_encode(["status" => "error", "message" => "Organization not found"]);
        $conn->close();
        exit;
    }
    
    $sql = "UPDATE organizations SET password='$new_password' WHERE id='$org_id'";
} elseif (isset($data['email']) && !empty(trim($data['email']))) {
    // Resetting by email (forgot password page)
    $email = $conn->real_escape_string(trim($data['email']));
    
    // First check if organization exists
    $checkSql = "SELECT id FROM organizations WHERE email = '$email'";
    $checkResult = $conn->query($checkSql);
    if (!$checkResult || $checkResult->num_rows === 0) {
        echo json_encode(["status" => "error", "message" => "No organization account found with that email address"]);
        $conn->close();
        exit;
    }
    
    $sql = "UPDATE organizations SET password='$new_password' WHERE email='$email'";
} else {
    echo json_encode(["status" => "error", "message" => "Email address or Organization ID is required"]);
    exit;
}

if ($conn->query($sql) === TRUE) {
    if ($conn->affected_rows >= 0) {
        // affected_rows can be 0 if the organization enters the exact same password, which is still a success
        echo json_encode(["status" => "success", "message" => "Password updated successfully"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Password update failed. Please try again."]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Database error: " . $conn->error]);
}

$conn->close();
?>
