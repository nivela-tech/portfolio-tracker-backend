package com.portfolio.tracker.service;

import com.opencsv.CSVWriter;
import com.portfolio.tracker.model.PortfolioEntry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Service
public class ExportService {

    public ByteArrayInputStream exportEntriesToXlsx(List<PortfolioEntry> entries) throws IOException {
        String[] columns = {"ID", "Date Added", "Type", "Currency", "Amount", "Country", "Source", "Notes", "Account Name"};
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Portfolio Entries");

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
            }

            int rowIdx = 1;
            for (PortfolioEntry entry : entries) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.getId());
                row.createCell(1).setCellValue(entry.getDateAdded() != null ? entry.getDateAdded().toString() : "");
                row.createCell(2).setCellValue(entry.getType() != null ? entry.getType().toString() : "");
                row.createCell(3).setCellValue(entry.getCurrency());
                row.createCell(4).setCellValue(entry.getAmount() != null ? entry.getAmount().doubleValue() : 0);
                row.createCell(5).setCellValue(entry.getCountry());
                row.createCell(6).setCellValue(entry.getSource());
                row.createCell(7).setCellValue(entry.getNotes());
                row.createCell(8).setCellValue(entry.getAccount() != null ? entry.getAccount().getName() : "N/A");
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportEntriesToCsv(List<PortfolioEntry> entries) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            String[] header = {"ID", "Date Added", "Type", "Currency", "Amount", "Country", "Source", "Notes", "Account Name"};
            csvWriter.writeNext(header);

            for (PortfolioEntry entry : entries) {
                csvWriter.writeNext(new String[]{
                        String.valueOf(entry.getId()),
                        entry.getDateAdded() != null ? entry.getDateAdded().toString() : "",
                        entry.getType() != null ? entry.getType().toString() : "",
                        entry.getCurrency(),
                        entry.getAmount() != null ? entry.getAmount().toString() : "0",
                        entry.getCountry(),
                        entry.getSource(),
                        entry.getNotes(),
                        entry.getAccount() != null ? entry.getAccount().getName() : "N/A"
                });
            }
            return new ByteArrayInputStream(stringWriter.toString().getBytes());
        }
    }
}
