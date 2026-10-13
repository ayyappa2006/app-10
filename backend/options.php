<?php
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');

$options = [
    'organization' => [
        ['id' => 'org_team', 'title' => 'Manage Team', 'description' => 'Add or remove members from your organization', 'icon' => 'group'],
        ['id' => 'org_billing', 'title' => 'Billing & Subscriptions', 'description' => 'Manage payment methods and plans', 'icon' => 'payment'],
        ['id' => 'org_settings', 'title' => 'Organization Settings', 'description' => 'Update company details and preferences', 'icon' => 'business']
    ],
    'user' => [
        ['id' => 'user_profile', 'title' => 'Edit Profile', 'description' => 'Update your personal information', 'icon' => 'person'],
        ['id' => 'user_security', 'title' => 'Security', 'description' => 'Change password and 2FA settings', 'icon' => 'security'],
        ['id' => 'user_notifications', 'title' => 'Notifications', 'description' => 'Manage your email and push alerts', 'icon' => 'notifications']
    ]
];

echo json_encode($options);
?>
