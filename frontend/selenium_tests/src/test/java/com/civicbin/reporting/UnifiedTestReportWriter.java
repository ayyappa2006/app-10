package com.civicbin.reporting;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class UnifiedTestReportWriter {
    private static final String REPORT_FILE = "CivicBin_Unified_Test_Report.xlsx";
    private static final String SHEET_NAME = "Unified Report";

    public static synchronized void writeResults(String source, List<ReportTestResult> results) {
        try {
            Path reportDir = resolveReportsDirectory();
            Path reportPath = reportDir.resolve(REPORT_FILE);

            try (Workbook workbook = Files.exists(reportPath)
                    ? new XSSFWorkbook(Files.newInputStream(reportPath))
                    : new XSSFWorkbook()) {
                Sheet sheet = workbook.getSheet(SHEET_NAME);
                if (sheet == null) {
                    sheet = workbook.createSheet(SHEET_NAME);
                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("Source");
                    header.createCell(1).setCellValue("Test Case");
                    header.createCell(2).setCellValue("Description");
                    header.createCell(3).setCellValue("Status");
                    header.createCell(4).setCellValue("Executed At");
                }

                int rowIndex = sheet.getLastRowNum() + 1;
                for (ReportTestResult result : results) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(source);
                    row.createCell(1).setCellValue(result.testCase);
                    row.createCell(2).setCellValue(result.description);
                    row.createCell(3).setCellValue(result.status);
                    row.createCell(4).setCellValue(result.executedAt);
                }

                try (FileOutputStream fos = new FileOutputStream(reportPath.toFile())) {
                    workbook.write(fos);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path resolveReportsDirectory() throws IOException {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path reports = findReportsDirectory(current);
        return Files.createDirectories(reports);
    }

    private static Path findReportsDirectory(Path current) {
        Path ancestor = current;
        while (ancestor != null) {
            Path reports = ancestor.resolve("reports");
            if (Files.exists(reports) || ancestor.getFileName().toString().equals("civic_android_frontend")) {
                return reports;
            }
            ancestor = ancestor.getParent();
        }
        return current.resolve("reports");
    }
}
