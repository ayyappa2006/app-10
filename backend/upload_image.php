<?php
require_once 'db.php';


// Check if a file was uploaded
if (!isset($_FILES['photo']) || $_FILES['photo']['error'] !== UPLOAD_ERR_OK) {
    echo json_encode(["status" => "error", "message" => "No file uploaded or upload error: " . (isset($_FILES['photo']) ? $_FILES['photo']['error'] : 'no file')]);
    exit;
}

// Create uploads directory if it doesn't exist
$uploadDir = __DIR__ . '/uploads/';
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

// Validate file type
$allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/heic'];
$fileType = mime_content_type($_FILES['photo']['tmp_name']);
if (!in_array($fileType, $allowedTypes)) {
    echo json_encode(["status" => "error", "message" => "Invalid file type: $fileType"]);
    exit;
}

// Generate unique filename
$extension = pathinfo($_FILES['photo']['name'], PATHINFO_EXTENSION);
if (empty($extension)) {
    $extension = 'jpg'; // Default extension
}
$filename = 'report_' . time() . '_' . bin2hex(random_bytes(8)) . '.' . $extension;
$targetPath = $uploadDir . $filename;

// Move file to uploads directory
if (!move_uploaded_file($_FILES['photo']['tmp_name'], $targetPath)) {
    echo json_encode(["status" => "error", "message" => "Failed to save uploaded file"]);
    exit;
}

// Return the URL to access this file
$protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
$host = $_SERVER['HTTP_HOST'];
$fileUrl = $protocol . '://' . $host . '/civic_backend/uploads/' . $filename;

echo json_encode([
    "status" => "success",
    "message" => "File uploaded successfully",
    "photo_url" => $fileUrl,
    "filename" => $filename
]);
?>
