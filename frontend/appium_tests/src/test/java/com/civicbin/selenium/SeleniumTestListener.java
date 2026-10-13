package com.civicbin.selenium;

import com.civicbin.reporting.ReportTestResult;
import com.civicbin.reporting.UnifiedTestReportWriter;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SeleniumTestListener extends TestListenerAdapter {
    private final List<ReportTestResult> results = new ArrayList<>();

    @Override
    public void onTestSuccess(ITestResult result) {
        addResult(result, "PASS");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        addResult(result, "FAIL");
    }

    @Override
    public void onFinish(ITestContext context) {
        UnifiedTestReportWriter.writeResults("Selenium", results);
    }

    private void addResult(ITestResult result, String status) {
        Object[] params = result.getParameters();
        String testCase = params.length > 0 ? params[0].toString() : result.getName();
        String description = params.length > 1 ? params[1].toString() :
                result.getMethod().getDescription() != null ? result.getMethod().getDescription() : "End-to-end Selenium test";

        results.add(new ReportTestResult(
                testCase,
                description,
                status,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
    }
}
