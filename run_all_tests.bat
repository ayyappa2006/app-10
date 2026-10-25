@echo off
echo ====================================================
echo Running CivicBin E2E Testing Pipeline (Web + Mobile)
echo ====================================================

echo.
echo [1/4] Checking and installing Selenium dependencies...
cd selenium-tests
call npm install
echo.
echo [2/4] Running Selenium Web E2E tests...
call npm test
cd ..

echo.
echo [3/4] Checking and installing Appium Python dependencies...
cd appium-tests
python -m pip install -r requirements.txt
echo.
echo [4/4] Running Appium Mobile E2E tests (Python)...
python test_runner.py
cd ..

echo.
echo ====================================================
echo Testing complete! Reports generated successfully:
echo - Web: reports\CivicBin_Web_Selenium_Report.xlsx
echo - Mobile: reports\CivicBin_Mobile_Appium_Report.xlsx
echo ====================================================
pause
