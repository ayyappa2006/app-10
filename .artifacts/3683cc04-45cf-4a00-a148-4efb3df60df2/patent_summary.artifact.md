# CivicBin: Comprehensive Technical & Architectural Summary

## 1. Project Overview
**CivicBin** is an intelligent, multi-platform civic engagement and municipal issue-resolution ecosystem. It bridges the gap between citizens (users) and local municipal authorities (organizations) by streamlining reporting, automated validation, geospatial mapping, real-time communication, and proximity-based issue management.

---

## 2. System Architecture & Tech Stack
- **Mobile Frontend**: Native Android application developed in **Java** using modern Material Design guidelines, Android Views, and `osmdroid` for map rendering.
- **Web Frontend**: Responsive web portal built with **HTML5**, **CSS3**, and **Vanilla JavaScript** utilizing the Fetch API and Leaflet maps.
- **Backend**: RESTful API built with **PHP** (`mysqli`/`PDO`) running on an Apache/XAMPP server.
- **Database**: **MySQL** relational database storing user profiles, organization accounts, issue reports, status updates, and messages.

---

## 3. Core Modules & Key Features Developed

### A. Project Structure & Module Resolution
- Reconfigured Gradle multi-project resolution (`settings.gradle.kts`) to map the root module `:app` directly to the production application (`frontend/app`), resolving initial template discrepancies and ensuring direct deployment from Android Studio.

### B. Dual Authentication & Session Management
- Implemented isolated authentication pipelines for **Citizens (Users)** and **Municipal Organizations**.
- Handled secure sign-up, login, password recovery, and session persistence across both Android (`SharedPreferences`) and Web (`localStorage`).
- Configured clean logout routing that returns users to the primary role-selection portal (`options.html`).

### C. AI-Powered Image Validation (Google ML Kit)
- Integrated Google ML Kit Image Labeling into the reporting pipeline (`UserAiActivity` / `ReportPhotoActivity`).
- Automatically scans user-uploaded photos for confidence thresholds and keywords (`waste`, `garbage`, `trash`, `litter`, `debris`, etc.).
- Rejects invalid uploads (e.g., selfies, portraits, humans, QR/barcodes) in real time before submission, ensuring report integrity.

### D. Automated Live Location & Geolocation
- **Mobile (Android)**: Utilized `LocationManager` and `Geocoder` to automatically retrieve GPS/Network coordinates and reverse-geocode them into precise city names and addresses. Added on-demand **📍 Live Location** buttons.
- **Web Portal**: Integrated HTML5 Geolocation API with OpenStreetMap Nominatim reverse geocoding to auto-detect user and organization locations.

### E. Proximity-Based "Nearby Issues" for Organizations
- Enabled municipal organizations to automatically detect their live location upon opening the issue management interface (`OrgIssuesActivity`).
- Dynamically filters and displays **nearby issues** matching the organization's current geographic sector/city.

### F. Network Resilience & Timeout Management
- Configured explicit connection and read timeouts (5 seconds) in `ApiClient.java` for all HTTP operations (`GET`, `POST`, multipart photo uploads).
- Prevented network hangs and UI freezing on unreachable backends, ensuring fail-fast error toasts and instant button state recovery.

### G. Action Safeguards (Delete Confirmation Dialogs)
- Implemented Android `AlertDialog` confirmation prompts (*"Are you sure you want to delete this issue?"*) across all organizer dashboards and detail views (`OrgDashboardActivity`, `OrgCompletedActivity`, `OrgIssueDetailsActivity`) to prevent accidental deletions.

### H. Optimized Map Rendering
- Replaced blocked OpenStreetMap tile servers and API-key-restricted CARTO tiles with **ESRI World Street Map** tiles (`https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}`).
- Removed default copyright watermark controls (`attributionControl: false`) for a clean, professional user interface.
