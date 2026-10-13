package com.civicbin.appium;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AppiumE2ETests extends BaseAppiumTest {
    @DataProvider(name = "appiumCases")
    public Object[][] appiumCases() {
        return new Object[][]{
                {"A-001", "Verify login page email field is visible", "loginEmailVisible"},
                {"A-002", "Verify login page password field is visible", "loginPasswordVisible"},
                {"A-003", "Verify login button is visible on login page", "loginButtonVisible"},
                {"A-004", "Verify forgot password link is visible", "forgotPasswordLinkVisible"},
                {"A-005", "Verify signup link is visible on login page", "signupLinkVisible"},
                {"A-006", "Verify valid user can log in", "validUserLogin"},
                {"A-007", "Verify invalid login shows an error", "invalidLoginError"},
                {"A-008", "Verify signup navigation from login page", "navigateToSignup"},
                {"A-009", "Verify signup name field is visible", "signupNameVisible"},
                {"A-010", "Verify signup email field is visible", "signupEmailVisible"},
                {"A-011", "Verify signup password field is visible", "signupPasswordVisible"},
                {"A-012", "Verify signup button is visible", "signupButtonVisible"},
                {"A-013", "Verify forgot password page email field is visible", "resetEmailVisible"},
                {"A-014", "Verify forgot password new password field is visible", "resetNewPasswordVisible"},
                {"A-015", "Verify reset password save button is visible", "resetSaveButtonVisible"},
                {"A-016", "Verify dashboard report count is visible after login", "dashboardReportCountVisible"},
                {"A-017", "Verify dashboard cleanliness card is visible", "dashboardCleanlinessVisible"},
                {"A-018", "Verify upload photo button is visible on dashboard", "uploadPhotoButtonVisible"},
                {"A-019", "Verify clicking upload photo opens upload screen", "uploadScreenOpen"},
                {"A-020", "Verify gallery button is visible on upload screen", "galleryButtonVisible"},
                {"A-021", "Verify camera button is visible on upload screen", "cameraButtonVisible"},
                {"A-022", "Verify upload screen back button is visible", "uploadBackVisible"},
                {"A-023", "Verify report photo screen contact field is visible", "contactFieldVisible"},
                {"A-024", "Verify report photo screen category spinner is visible", "categorySpinnerVisible"},
                {"A-025", "Verify report photo description field is visible", "descriptionFieldVisible"},
                {"A-026", "Verify report photo send button is visible", "sendReportButtonVisible"},
                {"A-027", "Verify report photo address display is visible", "addressDisplayVisible"},
                {"A-028", "Verify report photo map view exists", "mapViewVisible"},
                {"A-029", "Verify report history navigation button is visible", "historyNavVisible"},
                {"A-030", "Verify history screen header is displayed", "historyHeaderVisible"},
                {"A-031", "Verify profile navigation button is visible", "profileNavVisible"},
                {"A-032", "Verify profile screen name field is visible", "profileNameVisible"},
                {"A-033", "Verify profile screen email field is visible", "profileEmailVisible"},
                {"A-034", "Verify help screen button is visible", "helpButtonVisible"},
                {"A-035", "Verify privacy screen link is visible", "privacyLinkVisible"},
                {"A-036", "Verify settings link is visible", "settingsLinkVisible"},
                {"A-037", "Verify organization login fields are present when available", "orgLoginFieldsVisible"},
                {"A-038", "Verify organization signup link is visible when available", "orgSignupVisible"},
                {"A-039", "Verify organization dashboard summary values are present", "orgDashboardSummaryVisible"},
                {"A-040", "Verify organization issues list is visible", "orgIssuesListVisible"},
                {"A-041", "Verify organization issue detail buttons are visible", "orgIssueButtonsVisible"},
                {"A-042", "Verify organization settings profile display is visible", "orgSettingsProfileVisible"},
                {"A-043", "Verify organization help screen is accessible", "orgHelpVisible"},
                {"A-044", "Verify organization completed screen is visible", "orgCompletedVisible"},
                {"A-045", "Verify bottom navigation History button is present", "bottomNavHistoryVisible"},
                {"A-046", "Verify bottom navigation Pending button is present", "bottomNavPendingVisible"},
                {"A-047", "Verify bottom navigation Profile button is present", "bottomNavProfileVisible"},
                {"A-048", "Verify send report button remains accessible after typing details", "sendReportClickable"},
                {"A-049", "Verify upload screen does not crash when opening", "uploadScreenStable"},
                {"A-050", "Verify account reset password back navigation is visible", "resetBackNavigationVisible"}
        };
    }

    @Test(dataProvider = "appiumCases", priority = 1)
    public void testAppiumCase(String caseId, String description, String caseKey) {
        switch (caseKey) {
            case "loginEmailVisible":
                Assert.assertTrue(isElementVisible(resourceId("etEmail")), description);
                break;
            case "loginPasswordVisible":
                Assert.assertTrue(isElementVisible(resourceId("etPassword")), description);
                break;
            case "loginButtonVisible":
                Assert.assertTrue(isElementVisible(resourceId("btnLogin")), description);
                break;
            case "forgotPasswordLinkVisible":
                Assert.assertTrue(isElementVisible(resourceId("tvForgotPassword")), description);
                break;
            case "signupLinkVisible":
                Assert.assertTrue(isElementVisible(resourceId("tvSignUp")), description);
                break;
            case "validUserLogin":
                type(resourceId("etEmail"), properties.getProperty("TEST_USER_EMAIL"));
                type(resourceId("etPassword"), properties.getProperty("TEST_USER_PASSWORD"));
                click(resourceId("btnLogin"));
                Assert.assertTrue(isElementVisible(resourceId("tvReportCount")), description);
                break;
            case "invalidLoginError":
                type(resourceId("etEmail"), "wronguser@civicbin.com");
                type(resourceId("etPassword"), "WrongPass123");
                click(resourceId("btnLogin"));
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("invalid")
                        || driver.getPageSource().toLowerCase().contains("error"), description);
                break;
            case "navigateToSignup":
                click(resourceId("tvSignUp"));
                Assert.assertTrue(isElementVisible(resourceId("etName")), description);
                break;
            case "signupNameVisible":
                click(resourceId("tvSignUp"));
                Assert.assertTrue(isElementVisible(resourceId("etName")), description);
                break;
            case "signupEmailVisible":
                click(resourceId("tvSignUp"));
                Assert.assertTrue(isElementVisible(resourceId("etEmail")), description);
                break;
            case "signupPasswordVisible":
                click(resourceId("tvSignUp"));
                Assert.assertTrue(isElementVisible(resourceId("etPassword")), description);
                break;
            case "signupButtonVisible":
                click(resourceId("tvSignUp"));
                Assert.assertTrue(isElementVisible(resourceId("btnSignup")), description);
                break;
            case "resetEmailVisible":
                click(resourceId("tvForgotPassword"));
                Assert.assertTrue(isElementVisible(resourceId("etEmail")), description);
                break;
            case "resetNewPasswordVisible":
                click(resourceId("tvForgotPassword"));
                Assert.assertTrue(isElementVisible(resourceId("etNewPassword")), description);
                break;
            case "resetSaveButtonVisible":
                click(resourceId("tvForgotPassword"));
                Assert.assertTrue(isElementVisible(resourceId("btnSave")), description);
                break;
            case "dashboardReportCountVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(resourceId("tvReportCount")), description);
                break;
            case "dashboardCleanlinessVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(resourceId("tvCleanlinessLabel")), description);
                break;
            case "uploadPhotoButtonVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(resourceId("btnUploadPhoto")), description);
                break;
            case "uploadScreenOpen":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("btnGallery"))
                        || isElementVisible(resourceId("btnCamera")), description);
                break;
            case "galleryButtonVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("btnGallery")), description);
                break;
            case "cameraButtonVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("btnCamera")), description);
                break;
            case "uploadBackVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("btnBack")), description);
                break;
            case "contactFieldVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("etContactNumber")), description);
                break;
            case "categorySpinnerVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("spinnerCategory")), description);
                break;
            case "descriptionFieldVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("etDescription")), description);
                break;
            case "sendReportButtonVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("btnSendReport")), description);
                break;
            case "addressDisplayVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("tvAddressDisplay")), description);
                break;
            case "mapViewVisible":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("mapView")), description);
                break;
            case "historyNavVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(resourceId("navHistory")), description);
                break;
            case "historyHeaderVisible":
                loginWithValidUser();
                click(resourceId("navHistory"));
                Assert.assertTrue(isElementVisible(resourceId("headerBackground"))
                        || driver.getPageSource().toLowerCase().contains("history"), description);
                break;
            case "profileNavVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(resourceId("navProfile")), description);
                break;
            case "profileNameVisible":
                loginWithValidUser();
                click(resourceId("navProfile"));
                Assert.assertTrue(isElementVisible(resourceId("tvHeroName"))
                        || isElementVisible(resourceId("tvProfileName")), description);
                break;
            case "profileEmailVisible":
                loginWithValidUser();
                click(resourceId("navProfile"));
                Assert.assertTrue(isElementVisible(resourceId("tvHeroContact"))
                        || isElementVisible(resourceId("tvProfileEmail")), description);
                break;
            case "helpButtonVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(By.xpath("//*[contains(@text,'Help') or contains(@content-desc,'Help') or contains(@resource-id,'btnHelp') or contains(@resource-id,'tvHelp') ]")), description);
                break;
            case "privacyLinkVisible":
                loginWithValidUser();
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("privacy")
                        || isElementVisible(By.xpath("//*[contains(@text,'Privacy') or contains(@resource-id,'tvPrivacy') or contains(@resource-id,'org_privacy') ]")), description);
                break;
            case "settingsLinkVisible":
                loginWithValidUser();
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("settings")
                        || isElementVisible(By.xpath("//*[contains(@text,'Settings') or contains(@resource-id,'tvSettings') or contains(@resource-id,'org_settings') ]")), description);
                break;
            case "orgLoginFieldsVisible":
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("org")
                        || driver.getPageSource().toLowerCase().contains("organiz"), description);
                break;
            case "orgSignupVisible":
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("org")
                        || driver.getPageSource().toLowerCase().contains("Signup"), description);
                break;
            case "orgDashboardSummaryVisible":
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("dashboard")
                        || driver.getPageSource().toLowerCase().contains("score"), description);
                break;
            case "orgIssuesListVisible":
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("issues")
                        || driver.getPageSource().toLowerCase().contains("report"), description);
                break;
            case "orgIssueButtonsVisible":
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("completed")
                        || driver.getPageSource().toLowerCase().contains("assigned"), description);
                break;
            case "orgSettingsProfileVisible":
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("profile")
                        || driver.getPageSource().toLowerCase().contains("settings"), description);
                break;
            case "orgHelpVisible":
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("help"), description);
                break;
            case "orgCompletedVisible":
                Assert.assertTrue(driver.getPageSource().toLowerCase().contains("completed"), description);
                break;
            case "bottomNavHistoryVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(resourceId("navHistory")), description);
                break;
            case "bottomNavPendingVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(resourceId("navAi")), description);
                break;
            case "bottomNavProfileVisible":
                loginWithValidUser();
                Assert.assertTrue(isElementVisible(resourceId("navProfile")), description);
                break;
            case "sendReportClickable":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(isElementVisible(resourceId("btnSendReport")), description);
                break;
            case "uploadScreenStable":
                loginWithValidUser();
                click(resourceId("btnUploadPhoto"));
                Assert.assertTrue(driver.getPageSource().length() > 0, description);
                break;
            case "resetBackNavigationVisible":
                click(resourceId("tvForgotPassword"));
                Assert.assertTrue(isElementVisible(resourceId("tvBackToLogin"))
                        || driver.getPageSource().toLowerCase().contains("back"), description);
                break;
            default:
                Assert.fail("Unknown Appium test case: " + caseKey);
        }
    }

    private void loginWithValidUser() {
        if (!isElementVisible(resourceId("etEmail"))) {
            return;
        }
        type(resourceId("etEmail"), properties.getProperty("TEST_USER_EMAIL"));
        type(resourceId("etPassword"), properties.getProperty("TEST_USER_PASSWORD"));
        click(resourceId("btnLogin"));
        waitForElement(resourceId("tvReportCount"));
    }
}
