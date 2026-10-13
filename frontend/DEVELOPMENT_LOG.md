# CivicBin Project Development Log

This document provides a summary of the application build process and a log of the development milestones for the **CivicBin** Android application.

## 1. Project Overview & Architecture
CivicBin is a community-driven application designed to connect citizens (Users) with Organizations to report and resolve local issues.

### Key Components:
- **Frontend**: Native Android (Java) using Material Design components.
- **Backend**: PHP-based REST API (`civic_backend`).
- **Data Exchange**: JSON via `HttpURLConnection`.
- **Core Libraries**:
    - `osmdroid`: OpenStreetMap integration for location-based reporting.
    - `ML Kit (Image Labeling)`: Automated identification of waste/issues in photos.
    - `SharedPreferences`: Local session management.

---

## 2. Build Process Summary

### Phase 1: Foundation & Networking
- Initialized the Android project with a modular Activity-based structure.
- Developed `ApiClient.java` as a centralized networking layer to handle asynchronous GET and POST requests.
- Integrated `JSONObject` and `JSONArray` for efficient data parsing.

### Phase 2: Authentication System
- Built dual authentication flows for **Users** and **Organizations**.
- Implemented Login, Signup, and Password Recovery (`ForgotPasswordActivity`, `ResetPasswordActivity`).
- Handled session persistence using `SharedPreferences`.

### Phase 3: Reporting & AI Integration
- Developed `UploadPhotoActivity` to capture and prepare issue reports.
- Integrated Google ML Kit in `UserAiActivity` to provide real-time image labeling for automated issue categorization.
- Created `ReportPhotoActivity` to finalize details and submit reports to the backend.

### Phase 4: Dashboards & Issue Management
- Created `UserDashboardActivity` for citizens to track their reported issues.
- Created `OrgDashboardActivity` for organizations to manage incoming reports.
- Developed detail views (`UserIssueDetailsActivity`, `OrgIssueDetailsActivity`) to view specific report data and status updates.

### Phase 5: Communication (Real-time Chat)
- Implemented `ChatActivity` featuring:
    - Dynamic message alignment (Sender/Receiver).
    - Polling mechanism (3-second interval) for real-time-like communication.
    - Context-aware UI based on `viewer_type` (User vs. Org).
- Created `OrgChatListActivity` to manage multiple active conversations.

---

## 3. Development Milestones & Prompt History
*Note: This log reflects the logical progression of the project requirements.*

| Date | Milestone | Description |
| :--- | :--- | :--- |
| **Initial** | **Project Setup** | Requirement: Create a robust Android app for civic engagement with a PHP backend. |
| **Step 2** | **Networking Layer** | Prompt: "Build a singleton-like API client to handle POST and GET requests globally." |
| **Step 3** | **Auth Modules** | Prompt: "Create login and signup pages for both users and organizers with validation." |
| **Step 4** | **Mapping & AI** | Prompt: "Integrate OpenStreetMap and ML Kit to label images of garbage or road issues." |
| **Step 5** | **Dashboards** | Prompt: "Design a dashboard to show a list of reported issues with status indicators." |
| **Recent** | **Chat System** | Prompt: "Implement a messaging system between users and organizations." |
| **Current** | **Documentation** | Prompt: "Create a PDF/Log of the prompt history and build process." |

---

## 4. How to Export to PDF
1. Open this file (`DEVELOPMENT_LOG.md`) in Android Studio.
2. If you have the "Markdown" plugin enabled, use the **Export to HTML/PDF** feature if available.
3. Alternatively, copy this content into a Google Doc or Microsoft Word and select **Save as PDF**.

---
*End of Log*
