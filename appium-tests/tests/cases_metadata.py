screens = [
  {"page": "index.html", "title": "Splash"},
  {"page": "options.html", "title": "Options"},
  {"page": "login.html", "title": "Login"},
  {"page": "signup.html", "title": "Sign Up"},
  {"page": "forgot_password.html", "title": "Forgot Password"},
  {"page": "user_dashboard.html", "title": "Dashboard"},
  {"page": "organizers_list.html", "title": "Organizers"},
  {"page": "upload_photo.html", "title": "Upload Photo"},
  {"page": "report_photo.html", "title": "Review Report"},
  {"page": "user_history.html", "title": "History"},
  {"page": "user_issue_details.html?id=123", "title": "Timeline"},
  {"page": "chat.html?viewer=user&org_id=456&org_name=Hospital&user_id=123", "title": "Chat"},
  {"page": "messages_list.html", "title": "Messages"},
  {"page": "user_profile.html", "title": "Profile"},
  {"page": "user_account_details.html", "title": "Account"},
  {"page": "user_ai.html", "title": "Pending"},
  {"page": "user_reset_password.html", "title": "Reset Password"},
  {"page": "user_help.html", "title": "Help"},
  {"page": "user_privacy.html", "title": "Privacy"},
  {"page": "org_login.html", "title": "Login"},
  {"page": "org_signup.html", "title": "Sign Up"},
  {"page": "org_forgot_password.html", "title": "Forgot Password"},
  {"page": "org_dashboard.html", "title": "Dashboard"},
  {"page": "org_issues.html", "title": "Incoming"},
  {"page": "org_completed.html", "title": "Completed"},
  {"page": "org_issue_details.html?id=123", "title": "Details"},
  {"page": "org_profile.html", "title": "Profile"},
  {"page": "org_settings.html", "title": "Settings"},
  {"page": "org_reset_password.html", "title": "Reset Password"},
  {"page": "org_help.html", "title": "Help"},
  {"page": "org_privacy.html", "title": "Privacy"}
]

categories = [
  "Functional Testing",
  "UI/UX Testing",
  "Compatibility Testing",
  "Performance Testing",
  "Security Testing",
  "API Testing",
  "Database Testing",
  "Accessibility Testing",
  "Platform-Specific Testing",
  "Regression Testing"
]

appium_cases = []
case_count = 1

for screen in screens:
    for index, cat in enumerate(categories):
        case_id = f"TC{str(case_count).zfill(3)}"
        case_count += 1
        screen_name = screen["page"].split(".html")[0].split("?")[0]
        
        # Assign keys
        key = "profileNavVisible"
        if "login" in screen_name:
            key = "loginEmailVisible"
        elif "signup" in screen_name:
            key = "navigateToSignup"
        elif "forgot" in screen_name:
            key = "resetEmailVisible"
        elif "upload" in screen_name:
            key = "uploadScreenOpen"
        elif "history" in screen_name:
            key = "historyNavVisible"
        elif "dashboard" in screen_name:
            key = "dashboardReportCountVisible"
            
        actual_category = cat
        if cat == "Platform-Specific Testing":
            actual_category = "Mobile-Specific Testing"
        elif cat == "Regression Testing":
            actual_category = "End-to-End Testing"
            
        description = ""
        if cat == "Functional Testing":
            description = f"Verify app launch to {screen_name} screen transition on Android"
        elif cat == "UI/UX Testing":
            description = f"Verify text contrast and font styling consistency on {screen_name} screen"
        elif cat == "Compatibility Testing":
            description = f"Verify compatibility of {screen_name} screen layout with small mobile aspect ratios"
        elif cat == "Performance Testing":
            description = f"Measure memory utilization on cold start of {screen_name} mobile screen"
        elif cat == "Security Testing":
            description = f"Verify local encrypted storage for inputs related to {screen_name} screen"
        elif cat == "API Testing":
            description = f"Verify API endpoint connectivity and response times on {screen_name} screen"
        elif cat == "Database Testing":
            description = f"Verify SQLite/indexedDB persistence state when navigating {screen_name} screen"
        elif cat == "Accessibility Testing":
            description = f"Verify talkback announcements and accessible name tag targets for {screen_name} screen"
        elif cat == "Platform-Specific Testing":
            description = f"Verify mobile OS notification hooks and system interaction on {screen_name} screen"
        elif cat == "Regression Testing":
            description = f"Full Flow: E2E regression check covering {screen_name} screen features"
            
        appium_cases.append({
            "id": case_id,
            "description": description,
            "key": key,
            "category": actual_category
        })
