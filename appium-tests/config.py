import os

APPIUM_HOST = "127.0.0.1"
APPIUM_PORT = 4723
APPIUM_URL = f"http://{APPIUM_HOST}:{APPIUM_PORT}/"

# Path to debug APK in the workspace
APK_PATH = os.path.abspath(
    os.path.join(
        os.path.dirname(__file__),
        "../frontend/app/build/outputs/apk/debug/app-debug.apk"
    )
)

CAPABILITIES = {
    "platformName": "Android",
    "appium:automationName": "UIAutomator2",
    "appium:platformVersion": "11",
    "appium:deviceName": "AndroidEmulator",
    "appium:appPackage": "com.civicbin",
    "appium:appActivity": ".MainActivity",
    "appium:autoGrantPermissions": True,
    "appium:autoLaunch": True,
    "appium:noReset": False,
    "appium:fullReset": False,
    "appium:app": APK_PATH
}
