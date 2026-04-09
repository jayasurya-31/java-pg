-- GECT Connect Final Database Schema
CREATE DATABASE IF NOT EXISTS gect_connect;
USE gect_connect;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    person_type ENUM('STUDENT', 'STAFF') NOT NULL,
    roll_no_emp_id VARCHAR(50) UNIQUE NOT NULL,
    department VARCHAR(100),
    mobile VARCHAR(15),
    profile_pic VARCHAR(500),
    status VARCHAR(255) DEFAULT 'Hey there! I am using GECT Connect.',
    is_online BOOLEAN DEFAULT FALSE,
    last_seen TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_email_domain CHECK (email LIKE '%@gectcr.ac.in')
);

-- 2. Contact Requests Table
CREATE TABLE IF NOT EXISTS contact_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY (sender_id, receiver_id)
);

-- 3. Blocked Users Table
CREATE TABLE IF NOT EXISTS blocked_users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    blocked_user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY (user_id, blocked_user_id)
);

-- 4. Active Contacts (Helper table for chat list)
CREATE TABLE IF NOT EXISTS contacts (
    user_id INT NOT NULL,
    contact_id INT NOT NULL,
    last_message_id INT NULL,
    unread_count INT DEFAULT 0,
    PRIMARY KEY (user_id, contact_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (contact_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (last_message_id) REFERENCES messages(id) ON DELETE SET NULL
);

-- 5. Messages Table (Individual Chat)
CREATE TABLE IF NOT EXISTS messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT,
    type ENUM('TEXT', 'IMAGE', 'FILE', 'EMOJI') DEFAULT 'TEXT',
    status ENUM('SENT', 'DELIVERED', 'READ') DEFAULT 'SENT',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reply_to INT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reply_to) REFERENCES messages(id) ON DELETE SET NULL
);

-- 5. Groups Table
CREATE TABLE IF NOT EXISTS groups (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    creator_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Group Members Table
CREATE TABLE IF NOT EXISTS group_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    role ENUM('Admin', 'Member') DEFAULT 'Member',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY (group_id, user_id)
);

-- 7. Group Messages Table
CREATE TABLE IF NOT EXISTS group_messages (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT NOT NULL,
    sender_id INT NULL,
    message_content TEXT,
    message_type ENUM('TEXT', 'IMAGE', 'FILE', 'AUDIO', 'EMOJI') DEFAULT 'TEXT',
    status ENUM('SENT', 'DELIVERED', 'READ') DEFAULT 'SENT',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reply_to INT NULL,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (reply_to) REFERENCES group_messages(message_id) ON DELETE SET NULL
);

-- 8. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    message VARCHAR(500) NOT NULL,
    type ENUM('ACADEMIC', 'EVENT', 'GENERAL') DEFAULT 'GENERAL',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 9. Feed Table
CREATE TABLE IF NOT EXISTS feed (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    content TEXT NOT NULL,
    media_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 10. Settings Table
CREATE TABLE IF NOT EXISTS settings (
    user_id INT PRIMARY KEY,
    theme ENUM('LIGHT', 'DARK') DEFAULT 'LIGHT',
    privacy ENUM('PUBLIC', 'PRIVATE', 'CONTACTS') DEFAULT 'PUBLIC',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- TEST DATA
INSERT INTO users (email, full_name, password, person_type, roll_no_emp_id)
VALUES 
('admin@gectcr.ac.in','System Admin','password123','STAFF','STAFF001'),
('student1@gectcr.ac.in','John Doe','student123','STUDENT','STU001');
