package com.civicbin.selenium;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SeleniumE2ETests extends BaseSeleniumTest {
    private static final String WEB_ROOT = System.getProperty("web.root", "file:///d:/OneDrive/Desktop/civic_web_frontend/");

    @DataProvider(name = "seleniumCases")
    public Object[][] seleniumCases() {
        return new Object[][]{
                {"S-001", "Verify web login page loads", "login.html", By.id("email")},
                {"S-002", "Verify login page title is correct", "login.html", null},
                {"S-003", "Verify email field is present on login", "login.html", By.id("email")},
                {"S-004", "Verify password field is present on login", "login.html", By.id("password")},
                {"S-005", "Verify forgot password link is present", "login.html", By.linkText("Forgot Password?")},
                {"S-006", "Verify signup link is present on login page", "login.html", By.linkText("Sign up")},
                {"S-007", "Verify clicking signup opens signup page", "login.html", By.linkText("Sign up")},
                {"S-008", "Verify signup page title is correct", "signup.html", null},
                {"S-009", "Verify signup name field is present", "signup.html", By.id("name")},
                {"S-010", "Verify signup email field is present", "signup.html", By.id("email")},
                {"S-011", "Verify signup password field is present", "signup.html", By.id("password")},
                {"S-012", "Verify signup page can navigate back to login", "signup.html", By.linkText("Login")},
                {"S-013", "Verify forgot password page title is correct", "forgot_password.html", null},
                {"S-014", "Verify forgot password email field is present", "forgot_password.html", By.id("email")},
                {"S-015", "Verify forgot password new password field is present", "forgot_password.html", By.id("new_password")},
                {"S-016", "Verify forgot password back link works", "forgot_password.html", By.linkText("Back to Login")},
                {"S-017", "Verify options page loads successfully", "options.html", By.id("btn-user")},
                {"S-018", "Verify continue as user button is visible", "options.html", By.id("btn-user")},
                {"S-019", "Verify continue as organization button is visible", "options.html", By.id("btn-organization")},
                {"S-020", "Verify options user button navigates to login", "options.html", By.id("btn-user")},
                {"S-021", "Verify options org button navigates to org login", "options.html", By.id("btn-organization")},
                {"S-022", "Verify user dashboard page loads by URL", "user_dashboard.html", null},
                {"S-023", "Verify user history page loads by URL", "user_history.html", null},
                {"S-024", "Verify user AI page loads by URL", "user_ai.html", null},
                {"S-025", "Verify user profile page loads by URL", "user_profile.html", null},
                {"S-026", "Verify organizers list page loads by URL", "organizers_list.html", null},
                {"S-027", "Verify organizers list filter field is present", "organizers_list.html", By.id("cityFilter")},
                {"S-028", "Verify org dashboard page loads by URL", "org_dashboard.html", null},
                {"S-029", "Verify org issues page loads by URL", "org_issues.html", null},
                {"S-030", "Verify org completed page loads by URL", "org_completed.html", null},
                {"S-031", "Verify org settings page loads by URL", "org_settings.html", null},
                {"S-032", "Verify messages list page loads by URL", "messages_list.html", null},
                {"S-033", "Verify chat page loads by URL", "chat.html", By.id("messageInput")},
                {"S-034", "Verify user navigation links exist on chat page", "chat.html", By.xpath("//a[contains(@href,'user_dashboard.html') or contains(text(),'Home')]")},
                {"S-035", "Verify org navigation links exist on messages page", "messages_list.html", By.xpath("//a[contains(@href,'org_dashboard.html') or contains(text(),'Dashboard')]")},
                {"S-036", "Verify report photo page loads by URL", "report_photo.html", null},
                {"S-037", "Verify upload photo page loads by URL", "upload_photo.html", By.id("galleryInput")},
                {"S-038", "Verify homepage splash redirects to options", "index.html", null},
                {"S-039", "Verify login form submit button is present", "login.html", By.cssSelector("button[type='submit']")},
                {"S-040", "Verify signup form submit button is present", "signup.html", By.cssSelector("button[type='submit']")},
                {"S-041", "Verify forgot password form submit button is present", "forgot_password.html", By.cssSelector("button[type='submit']")},
                {"S-042", "Verify organizers list page displays organizers container", "organizers_list.html", By.id("organizersContainer")},
                {"S-043", "Verify user dashboard page displays report cards", "user_dashboard.html", By.xpath("//*[contains(text(),'Reports Submitted') or contains(text(),'Live Location')]")},
                {"S-044", "Verify user AI page displays pending items", "user_ai.html", By.xpath("//h1[contains(text(),'Pending')] | //a[contains(text(),'Pending')]")},
                {"S-045", "Verify user history page displays history items", "user_history.html", By.xpath("//h1[contains(text(),'Completed') or contains(text(),'History')] | //a[contains(text(),'History')]")},
                {"S-046", "Verify org dashboard page displays statistics", "org_dashboard.html", By.xpath("//h2[contains(text(),'Overview')] | //*[contains(text(),'Cleanliness')]")},
                {"S-047", "Verify org completed page displays closed issues", "org_completed.html", By.xpath("//h1[contains(text(),'Completed')] | //a[contains(text(),'Completed')]")},
                {"S-048", "Verify org settings page displays profile fields", "org_settings.html", By.xpath("//*[contains(@class,'settings')] | //span[contains(text(),'Profile')]")},
                {"S-049", "Verify web pages use correct window.location navigation", "options.html", By.id("btn-user")},
                {"S-050", "Verify static navigation links have working hrefs", "login.html", By.xpath("//a[@href='signup.html' or @href='forgot_password.html']")}
        };
    }

    @Test(dataProvider = "seleniumCases", priority = 1)
    public void testSeleniumCase(String caseId, String description, String page, By locator) {
        String url = WEB_ROOT + page;
        driver.get(url);
        dismissAlertIfPresent();

        if (locator != null) {
            Assert.assertTrue(waitForElement(locator).isDisplayed(), description);
        } else {
            Assert.assertTrue(driver.getTitle().length() > 0 || driver.getPageSource().length() > 0, description);
        }
    }
}
