package com.civicbin.reporting;

public class ReportTestResult {
    public final String testCase;
    public final String description;
    public final String status;
    public final String executedAt;

    public ReportTestResult(String testCase, String description, String status, String executedAt) {
        this.testCase = testCase;
        this.description = description;
        this.status = status;
        this.executedAt = executedAt;
    }
}
