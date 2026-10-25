<?php
require_once 'db.php';

// Check if a file was uploaded (SEC-013)
if (!isset($_FILES['photo']) || $_FILES['photo']['error'] !== UPLOAD_ERR_OK) {
    echo json_encode(["status" => "error", "message" => "No file uploaded or upload error"]);
    exit;
}

// Max upload size 5MB
$maxSize = 5 * 1024 * 1024;
if ($_FILES['photo']['size'] > $maxSize) {
    echo json_encode(["status" => "error", "message" => "File size exceeds 5MB limit"]);
    exit;
}

// Create uploads directory if it doesn't exist
$uploadDir = __DIR__ . '/uploads/';
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

// Security: Prevent script execution in uploads directory by creating .htaccess (SEC-013)
$htaccessPath = $uploadDir . '.htaccess';
if (!file_exists($htaccessPath)) {
    $htaccessContent = "# Disable PHP execution in uploads directory\n<FilesMatch \"\.(php|phtml|php3|php4|php5|php7|phps|pht|phar|inc|cgi|pl|py|sh|bash)$\">\nOrder Deny,Allow\nDeny from all\n</FilesMatch>\n";
    @file_put_contents($htaccessPath, $htaccessContent);
}

// Validate MIME type with strict whitelist
$allowedMimeTypes = [
    'image/jpeg' => 'jpg',
    'image/png'  => 'png',
    'image/webp' => 'webp',
    'image/gif'  => 'gif'
];

$fileTmp = $_FILES['photo']['tmp_name'];
$finfo = finfo_open(FILEINFO_MIME_TYPE);
$fileType = finfo_file($finfo, $fileTmp);
finfo_close($finfo);

if (!array_key_exists($fileType, $allowedMimeTypes)) {
    echo json_encode(["status" => "error", "message" => "Invalid file format. Only JPEG, PNG, WEBP, GIF allowed."]);
    exit;
}

// Determine safe extension strictly from detected MIME type (never trust client filename)
$extension = $allowedMimeTypes[$fileType];

// Generate unguessable, purely random filename (SEC-013)
$filename = 'report_' . time() . '_' . bin2hex(random_bytes(12)) . '.' . $extension;
$targetPath = $uploadDir . $filename;

// Move file to uploads directory
if (!move_uploaded_file($_FILES['photo']['tmp_name'], $targetPath)) {
    echo json_encode(["status" => "error", "message" => "Failed to securely save uploaded file"]);
    exit;
}

// Construct HTTPS asset URL (SEC-025)
$protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
$host = isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] : 'localhost';
$fileUrl = $protocol . '://' . $host . '/civic_backend/uploads/' . $filename;

echo json_encode([
    "status" => "success",
    "message" => "File uploaded successfully",
    "photo_url" => $fileUrl,
    "filename" => $filename
]);
?>
