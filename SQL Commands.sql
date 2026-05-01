CREATE TABLE IF NOT EXISTS Users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW()
);

CREATE TABLE IF NOT EXISTS Preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel ENUM('email', 'sms', 'push') NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_hours JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    allowed_messages_priority JSON,
    INDEX (user_id),
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    placeholders JSON,
    template_priority ENUM ('1', '2', '3') NOT NULL DEFAULT '1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW()
);


CREATE TABLE IF NOT EXISTS Notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel ENUM('email', 'sms', 'push') NOT NULL,
    status ENUM('pending', 'sent', 'failed') NOT NULL DEFAULT 'pending',
    message TEXT,
    message_hash char(128), -- SHA512 algo being used 
    priority ENUM ('1', '2', '3') NOT NULL DEFAULT '1',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    UNIQUE(user_id, channel, message_hash),
    INDEX (user_id),
    INDEX (status),
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS delivery_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notification_id BIGINT NOT NULL,
    channel ENUM('email', 'sms', 'push') NOT NULL, -- 'email', 'sms', 'push'
    status VARCHAR(20) NOT NULL, -- 'sent', 'failed', 'retrying'
    error_message TEXT, -- Optional: store error details if failed
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id)
);

INSERT IGNORE INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'OTP Verification',
    'Your OTP is {otp}. Please use this to complete your verification. Do not share this code with anyone.',
    '["otp"]',
    '1'
);
INSERT IGNORE INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'Welcome Greeting',
    'Hello {name}, welcome to Trigear! We are excited to have you onboard.',
    '["name"]',
    '2'
);
INSERT IGNORE INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'Password Reset',
    'Hi {name}, you requested to reset your password. Use the link below to set a new password: {reset_link}',
    '["name", "reset_link"]',
    '1'
);
INSERT IGNORE INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'Account Deactivation Warning',
    'Dear {name}, your account is scheduled for deactivation on {deactivation_date}. Please contact support if this is a mistake.',
    '["name", "deactivation_date"]',
    '1'
);
INSERT IGNORE INTO Templates (name, content, placeholders, template_priority)
VALUES (
    'Birthday Wish',
    'Happy Birthday, {name}! 🎉 Wishing you a fantastic day filled with joy and laughter. Here’s a special treat: {birthday_offer}.',
    '["name", "birthday_offer"]',
    '2'
);

INSERT IGNORE INTO notification_system.templates (name, content, placeholders, template_priority)
VALUES (
    'Trending Nearby',
    'Hi {name}, check out what’s trending in {location}! Don’t miss out on amazing deals and events near you.',
    '["name", "location"]',
    '3'
);

INSERT IGNORE INTO notification_system.users (name, email, phone)
VALUES (
    'Puneett',
    'youremail@gmail.com',
    '+1234567890'
);

INSERT IGNORE INTO preferences (user_id, channel, is_enabled, quiet_hours)
VALUES
(2, 'email', TRUE, '{"quietHoursEnabled": true,"start": "22:00", "end": "06:00"}'),
(2, 'sms', FALSE, '{"quietHoursEnabled": true,"start": "18:00", "end": "09:00"}'),
(2, 'push', TRUE, '{"quietHoursEnabled": false,"start": "00:00", "end": "00:00"}');
