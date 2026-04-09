# GECT Connect (Swing Edition)

A modern, modular Java Swing application for campus connectivity at Government Engineering College, Thrissur (GECT).

## 🚀 Features

- **Authentication**: Secure login and registration with `@gectcr.ac.in` email validation and password hashing.
- **Profile**: Customizable profiles with image upload and status updates.
- **Contacts**: Search for peers, send/accept/reject friend requests, and block users.
- **Chat**: Real-time one-to-one messaging with modern chat bubbles and typing indicators.
- **Groups**: Create and join study groups or clubs.
- **Campus Feed**: Post and view campus-wide updates.
- **Notifications**: Instant alerts for messages, requests, and updates.
- **Modern UI**: WhatsApp-inspired design with Dark Mode support, smooth animations, and toast notifications.

## 🛠️ Tech Stack

- **Frontend**: Java Swing (CardLayout, BoxLayout, GridBagLayout).
- **Backend**: Core Java (OOP, Layered Architecture).
- **Database**: MySQL (JDBC with DAO Pattern).
- **Communication**: Global EventBus (Pub-Sub Pattern).

## 📂 Project Structure

- `core/`: Shared data models (User, Message).
- `shared/`: Global utilities (DB, Session, Password, UI, Animations).
- `teams/`: 7 Feature-specific modules (Auth, Profile, Contacts, Chat, Group, Notifications, Settings).
- `integration/`: Application entry point (`MainFrame`).

## ⚙️ Setup Instructions

1.  **Database Setup**:
    - Import the provided `database_schema.sql` into your MySQL server.
    - Update `URL`, `USER`, and `PASSWORD` in `shared/DBConnection.java`.

2.  **Compilation**:
    ```powershell
    cd gect-connect
    javac -d bin core/*.java shared/*.java integration/*.java teams/team1_auth/*.java teams/team2_profile/*.java teams/team3_contacts/*.java teams/team4_chat/*.java teams/team5_group/*.java teams/team6_notifications/*.java teams/team7_settings/*.java
    ```

3.  **Run**:
    ```powershell
    java -cp bin integration.MainFrame
    ```

## 🎨 UI Guidelines

- **Primary Color**: `#075E54` (WhatsApp Green)
- **Accent Color**: `#25D366`
- **Dark Mode**: Toggled in Settings for eye comfort.
- **Animations**: Shake on error, smooth fade-in transitions.

---
Developed with ❤️ for GECT students.
