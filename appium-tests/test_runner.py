import os
import sys
import subprocess

def main():
    print("==================================================")
    print("Starting CivicBin E2E Appium Mobile Test Suite...")
    print("==================================================")

    # Check and clean existing test results
    results_file = "test_results.json"
    if os.path.exists(results_file):
        os.remove(results_file)

    try:
        import pytest
    except ImportError:
        print("Error: pytest is required. Installing via pip...")
        subprocess.check_call([sys.executable, "-m", "pip", "install", "pytest", "Appium-Python-Client", "openpyxl"])
        import pytest

    # Run pytest on the tests directory
    print("\nRunning test cases under tests/ directory...")
    exit_code = pytest.main(["-v", "tests/"])

    # Compile the final reports
    print("\nCompiling reports...")
    try:
        from generate_mobile_report import generate_excel_report
        generate_excel_report(test_results_path=results_file)
    except Exception as e:
        print(f"Error compiling Excel report: {e}")

    try:
        from generate_ci_results import generate_ci_summary
        generate_ci_summary(test_results_path=results_file)
    except Exception as e:
        print(f"Error logging CI summary: {e}")

    sys.exit(exit_code)

if __name__ == "__main__":
    main()
