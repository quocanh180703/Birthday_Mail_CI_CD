package com.example.reader.service;

import com.example.reader.model.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Locale;

@Service
public class ExcelService {
    public List<Employee> readFromStream(InputStream in) throws Exception {
        List<Employee> list = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if (!rows.hasNext()) {
                return list;
            }

            Row headerRow = rows.next();
            Map<String, Integer> columns = resolveColumns(headerRow);

            while (rows.hasNext()) {
                Row r = rows.next();
                
                Integer nameIdx = columns.get("name");
                Integer emailIdx = columns.get("email");
                Integer dobIdx = columns.get("dob");
                Integer imgIdx = columns.get("imagepath");

                if (nameIdx == null || emailIdx == null || dobIdx == null) {
                    continue;
                }

                String name = getStringCell(r.getCell(nameIdx));
                String email = getStringCell(r.getCell(emailIdx));
                LocalDate dob = getDateCell(r.getCell(dobIdx));
                String imagePath = (imgIdx != null) ? getStringCell(r.getCell(imgIdx)) : null;

                if (name == null || email == null || dob == null) continue;
                
                list.add(new Employee(name, email, dob, imagePath));
            }
        }
        return list;
    }

    private Map<String, Integer> resolveColumns(Row headerRow) {
        Map<String, Integer> columns = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell).trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            if (header.equals("name") || header.equals("fullname")) {
                columns.put("name", cell.getColumnIndex());
            } else if (header.equals("email") || header.equals("mail")) {
                columns.put("email", cell.getColumnIndex());
            } else if (header.equals("dob") || header.equals("birthday") || header.equals("dateofbirth")) {
                columns.put("dob", cell.getColumnIndex());
            } else if (header.equals("imagepath") || header.equals("image") || header.equals("photo")) {
                columns.put("imagepath", cell.getColumnIndex());
            }
        }
        return columns;
    }

    private String getStringCell(Cell c) {
        if (c == null) return null;
        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(c);
        return value == null ? null : value.trim();
    }

    private LocalDate getDateCell(Cell c) {
        if (c == null) return null;

        if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
            try {
                Date d = c.getDateCellValue();
                return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (Exception e) {
                return null;
            }
        }

        String s = getStringCell(c);
        if (s == null || s.isBlank()) return null;

        s = s.trim();

        String[] patterns = {"dd/MM/yyyy"};

        for (String pattern : patterns) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {
            }
        }

        System.err.println("Không thể đọc ngày sinh: " + s);
        return null;
    }
}
