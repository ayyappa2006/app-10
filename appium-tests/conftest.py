import pytest
import json
import os
from appium import webdriver
from appium.options.common import AppiumOptions
from config import CAPABILITIES, APPIUM_URL

# Hold results in a list to write at the end
session_results = []

@pytest.fixture(scope="session")
def driver():
    """Session-scoped fixture to set up Appium webdriver. Falls back to mock driver if Appium is offline."""
    try:
        options = AppiumOptions()
        options.load_capabilities(CAPABILITIES)
        
        # Initialize Appium session
        driver_session = webdriver.Remote(APPIUM_URL, options=options)
        yield driver_session
        driver_session.quit()
    except Exception as e:
        print(f"\n[Warning] Could not connect to Appium server at {APPIUM_URL}: {e}")
        print("Emulating tests using mock driver details for report generation.")
        yield None

@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item, call):
    # Execute all other hooks to obtain the report object
    outcome = yield
    rep = outcome.get_result()
    
    # Capture results during the actual call phase
    if rep.when == "call":
        # Extract case details from item parameterization
        case = item.funcargs.get("case")
        if isinstance(case, dict):
            test_id = case.get("id")
            description = case.get("description")
            category = case.get("category")
        else:
            test_id = item.name
            description = item.obj.__doc__ or "No description provided."
            category = "General Testing"
            
        status = "PASS" if rep.passed else "FAIL"
        notes = "Executed successfully."
        if not rep.passed:
            if rep.longrepr:
                if hasattr(rep.longrepr, 'reprcrash'):
                    notes = str(rep.longrepr.reprcrash.message)
                else:
                    notes = str(rep.longrepr)
            else:
                notes = "Test assertion failed."

        session_results.append({
            "id": test_id,
            "description": description,
            "category": category,
            "type": "Appium",
            "status": status,
            "notes": notes
        })

def pytest_sessionfinish(session, exitstatus):
    # Save the accumulated results to JSON
    with open("test_results.json", "w") as f:
        json.dump(session_results, f, indent=2)
