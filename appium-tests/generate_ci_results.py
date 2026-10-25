import os
import json

def generate_ci_summary(test_results_path="test_results.json"):
    """Reads execution results and writes out summary stats for CI or console output."""
    results = []
    if os.path.exists(test_results_path):
        try:
            with open(test_results_path, "r") as f:
                results = json.load(f)
        except Exception as e:
            print(f"Error loading results: {e}")

    if not results:
        print("No test results found to generate CI summary.")
        return

    total = len(results)
    passed = sum(1 for r in results if r.get("status") == "PASS")
    failed = sum(1 for r in results if r.get("status") == "FAIL")
    pass_rate = (passed / total * 100) if total > 0 else 0

    # Print nicely formatted summary in terminal
    print("\n" + "=" * 50)
    print("CI AUTOMATED PIPELINE SUMMARY - APPIUM MOBILE")
    print("=" * 50)
    print(f"Total Test Cases : {total}")
    print(f"Passed           : {passed} / {total}")
    print(f"Failed           : {failed} / {total}")
    print(f"Pass Rate        : {pass_rate:.2f}%")
    print("=" * 50 + "\n")

    # Add to GITHUB_STEP_SUMMARY if active in environment
    github_summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if github_summary_path:
        try:
            with open(github_summary_path, "a") as f:
                f.write("## 📱 Appium Mobile Test Results\n")
                f.write(f"- **Total Test Cases**: {total}\n")
                f.write(f"- **Passed**: {passed} / {total} ✅\n")
                f.write(f"- **Failed**: {failed} / {total} ❌\n")
                f.write(f"- **Pass Rate**: {pass_rate:.2f}%\n\n")
        except Exception as e:
            print(f"Failed to append to GITHUB_STEP_SUMMARY: {e}")

if __name__ == "__main__":
    generate_ci_summary()
