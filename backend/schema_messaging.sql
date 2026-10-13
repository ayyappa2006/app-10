-- Add city to organizations if it doesn't exist
-- We can do this with a stored procedure or simply ALTER TABLE directly.
-- Standard MySQL doesn't have "ADD COLUMN IF NOT EXISTS" for older versions, but we'll try standard ALTER.

ALTER TABLE organizations ADD COLUMN city VARCHAR(255) DEFAULT '';

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    org_id INT NOT NULL,
    sender_type ENUM('user', 'org') NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
);
