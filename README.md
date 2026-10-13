# CivicBin (app-final)

CivicBin is a full-stack civic reporting and issue resolution platform consisting of an Android mobile app, PHP REST backend, and responsive web portal for citizens and civic organizers.

---

## 📁 Repository Structure

```
app-final/
├── frontend/             # Native Android Application & Automated Test Suites
│   ├── app/              # Android app source code (Java/Kotlin, layouts, assets)
│   ├── appium_tests/     # Appium automated mobile test suite
│   ├── selenium_tests/   # Selenium automated web & integration test suite
│   ├── test_plans/       # Test strategy documents & test cases
│   └── reports/          # Test execution reports
│
├── backend/              # PHP REST API & Database Schemas
│   ├── *.php             # Authentication, issue submission, updates & messaging endpoints
│   ├── *.sql             # MySQL schema definitions & sample database dump
│   └── load-test/        # Artillery / Node.js load testing scripts & reports
│
└── web/                  # CivicBin Web Frontend
    ├── *.html            # Citizen and Organizer dashboards, authentication, issue tracking & AI chat
    ├── style.css         # Styling system & responsive layout design
    └── options.js        # Dynamic web interactions & options management
```

---

## 🚀 Getting Started

### 1. Backend Setup
- Deploy the PHP files in `backend/` to your web server (e.g., Apache/Nginx with PHP 8.x).
- Import `backend/civicbin_dump.sql` (or `schema.sql`, `schema_reports.sql`, `schema_messaging.sql`) into your MySQL database.
- Configure database credentials in `backend/db.php`.

### 2. Android App (`frontend`)
- Open the `frontend/` directory in Android Studio.
- Sync Gradle dependencies and configure API endpoint URLs in the app configuration.
- Build and run on an Android device or emulator.

### 3. Web Portal (`web`)
- Open `web/index.html` or serve the `web/` directory via any static file server or web host.

---

## 🧪 Testing & Quality Assurance
- **Mobile Automation**: Appium test suite available under `frontend/appium_tests/`.
- **E2E & Web Automation**: Selenium test suite under `frontend/selenium_tests/`.
- **Load Testing**: Load test scripts and results under `backend/load-test/`.