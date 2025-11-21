package com.gateway.templateservice.parser;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.*;

public class ExcelParser {
    public static List<Map<String, String>> parse(InputStream is) throws IOException {
        List<Map<String, String>> out = new ArrayList<>();
        
        try {
            System.out.println("📊 ExcelParser: Starting to parse Excel file");
            
            Workbook wb = WorkbookFactory.create(is);
            System.out.println("📊 ExcelParser: Workbook created successfully");
            
            Sheet sheet = wb.getSheetAt(0);
            System.out.println("📊 ExcelParser: Got first sheet: " + sheet.getSheetName());
            
            Iterator<Row> iter = sheet.iterator();
            if (!iter.hasNext()) {
                System.out.println("⚠️ ExcelParser: No rows found in sheet");
                return out;
            }
            
            Row headerRow = iter.next();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell c = headerRow.getCell(i);
                String header = c == null ? "" : c.toString();
                headers.add(header);
            }
            System.out.println("📊 ExcelParser: Headers found: " + headers);
            
            while (iter.hasNext()) {
                Row r = iter.next();
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell c = r.getCell(i);
                    row.put(headers.get(i), c == null ? "" : c.toString());
                }
                out.add(row);
            }
            
            System.out.println("📊 ExcelParser: Successfully parsed " + out.size() + " rows");
            wb.close();
            
        } catch (Exception e) {
            System.err.println("❌ ExcelParser Error: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to parse Excel file: " + e.getMessage(), e);
        }
        
        return out;
    }
}
