<?php
require_once 'db.php';

// Only allow POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["status" => "error", "message" => "Method not allowed"]);
    exit;
}

$rawInput = file_get_contents("php://input");
$data = json_decode($rawInput, true);

if (!isset($data['new_password']) || empty(trim($data['new_password']))) {
    echo json_encode(["status" => "error", "message" => "New password is required"]);
    exit;
}

$password_raw = trim($data['new_password']);
if (strlen($password_raw) < 6) {
    echo json_encode(["status" => "error", "message" => "Password must be at least 6 characters long"]);
    exit;
}

$new_password = password_hash($password_raw, PASSWORD_DEFAULT);

// Secure password update with prepared statements (SEC-005)
if (isset($data['org_id']) && !empty(trim($data['org_id']))) {
    $org_id = (int)$data['org_id'];
    
    $checkStmt = $conn->prepare("SELECT id FROM organizations WHERE id = ?");
    $checkStmt->bind_param("i", $org_id);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();
    
    if (!$checkResult || $checkResult->num_rows === 0) {
        $checkStmt->close();
        echo json_encode(["status" => "error", "message" => "Organization not found"]);
        $conn->close();
        exit;
    }
    $checkStmt->close();
    
    $updateStmt = $conn->prepare("UPDATE organizations SET password = ? WHERE id = ?");
    $updateStmt->bind_param("si", $new_password, $org_id);
    
} elseif (isset($data['email']) && !empty(trim($data['email']))) {
    $email = trim($data['email']);
    
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        echo json_encode(["status" => "error", "message" => "Invalid email address"]);
        exit;
    }
    
    $checkStmt = $conn->prepare("SELECT id FROM organizations WHERE email = ?");
    $checkStmt->bind_param("s", $email);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();
    
    if (!$checkResult || $checkResult->num_rows === 0) {
        $checkStmt->close();
        echo json_encode(["status" => "error", "message" => "If the organization exists, password has been reset successfully"]);
        $conn->close();
        exit;
    }
    $checkStmt->close();
    
    $updateStmt = $conn->prepare("UPDATE organizations SET password = ? WHERE email = ?");
    $updateStmt->bind_param("ss", $new_password, $email);
} else {
    echo json_encode(["status" => "error", "message" => "Email address or Organization ID is required"]);
    exit;
}

if ($updateStmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Password updated successfully"]);
} else {
    error_log("Org password reset failed: " . $updateStmt->error);
    echo json_encode(["status" => "error", "message" => "Password update failed. Please try again."]);
}

$updateStmt->close();
$conn->close();
?>
