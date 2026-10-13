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

if (isset($data['user_id']) && !empty(trim($data['user_id']))) {
    // Resetting by user_id (logged-in settings page)
    $user_id = $conn->real_escape_string(trim($data['user_id']));
    
    // First check if user exists
    $checkSql = "SELECT id FROM users WHERE id = '$user_id'";
    $checkResult = $conn->query($checkSql);
    if (!$checkResult || $checkResult->num_rows === 0) {
        echo json_encode(["status" => "error", "message" => "User not found"]);
        $conn->close();
        exit;
    }
    
    $sql = "UPDATE users SET password='$new_password' WHERE id='$user_id'";
} elseif (isset($data['email']) && !empty(trim($data['email']))) {
    // Resetting by email (forgot password page)
    $email = $conn->real_escape_string(trim($data['email']));
    
    // First check if user exists
    $checkSql = "SELECT id FROM users WHERE email = '$email'";
    $checkResult = $conn->query($checkSql);
    if (!$checkResult || $checkResult->num_rows === 0) {
        echo json_encode(["status" => "error", "message" => "No account found with that email address"]);
        $conn->close();
        exit;
    }
    
    $sql = "UPDATE users SET password='$new_password' WHERE email='$email'";
} else {
    echo json_encode(["status" => "error", "message" => "Email address or User ID is required"]);
    exit;
}

if ($conn->query($sql) === TRUE) {
    if ($conn->affected_rows >= 0) {
        // affected_rows can be 0 if the user enters the exact same password, which is still a database query success
        echo json_encode(["status" => "success", "message" => "Password updated successfully"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Password update failed. Please try again."]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Database error: " . $conn->error]);
}

$conn->close();
?>
