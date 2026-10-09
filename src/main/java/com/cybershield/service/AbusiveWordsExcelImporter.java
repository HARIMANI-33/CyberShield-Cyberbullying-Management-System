package com.cybershield.service;

import com.cybershield.model.AbusiveWord;
import com.cybershield.repository.AbusiveWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Service
public class AbusiveWordsExcelImporter {

    private static final Logger log = LoggerFactory.getLogger(AbusiveWordsExcelImporter.class);
    private static final String DATASET_FILENAME = "CyberShield_Abusive_Words_Severity_Dataset (2).xlsx";

    private final AbusiveWordRepository abusiveWordRepository;

    public AbusiveWordsExcelImporter(AbusiveWordRepository abusiveWordRepository) {
        this.abusiveWordRepository = abusiveWordRepository;
    }

    @Transactional
    public ImportResult importDatasetFromExcel() {
        ImportResult result = new ImportResult();
        try {
            byte[] fileBytes = loadExcelBytes();
            if (fileBytes == null || fileBytes.length == 0) {
                log.warn("Excel dataset file {} not found on root or classpath.", DATASET_FILENAME);
                return result;
            }

            Map<String, byte[]> xmlEntries = extractXmlEntries(fileBytes);
            List<String> sharedStrings = parseSharedStrings(xmlEntries.get("xl/sharedStrings.xml"));
            byte[] sheetBytes = xmlEntries.get("xl/worksheets/sheet1.xml");

            if (sheetBytes == null) {
                log.warn("Worksheet 1 (sheet1.xml) not found in Excel archive.");
                return result;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(sheetBytes));

            NodeList rows = doc.getElementsByTagName("row");
            log.info("Reading Excel dataset: {} rows found in sheet.", rows.getLength());

            for (int i = 0; i < rows.getLength(); i++) {
                Element row = (Element) rows.item(i);
                String rIdx = row.getAttribute("r");
                if ("1".equals(rIdx) || i == 0) {
                    continue; // Skip header row
                }

                NodeList cells = row.getElementsByTagName("c");
                String keyword = "";
                String category = "";
                String scoreStr = "";
                String severity = "";
                String recommendedAction = "";

                for (int j = 0; j < cells.getLength(); j++) {
                    Element cell = (Element) cells.item(j);
                    String ref = cell.getAttribute("r");
                    String cellVal = extractCellValue(cell, sharedStrings);

                    if (ref.startsWith("A")) {
                        keyword = cellVal;
                    } else if (ref.startsWith("B")) {
                        category = cellVal;
                    } else if (ref.startsWith("C")) {
                        scoreStr = cellVal;
                    } else if (ref.startsWith("D")) {
                        severity = cellVal;
                    } else if (ref.startsWith("E")) {
                        recommendedAction = cellVal;
                    }
                }

                if (keyword == null || keyword.trim().isEmpty()) {
                    continue;
                }

                keyword = keyword.trim();
                int score = 3;
                try {
                    if (scoreStr != null && !scoreStr.trim().isEmpty()) {
                        score = Integer.parseInt(scoreStr.trim());
                    }
                } catch (NumberFormatException nfe) {
                    log.debug("Non-integer score '{}' for '{}', defaulting to 3", scoreStr, keyword);
                }

                String language = detectLanguage(keyword, category);
                if (severity == null || severity.trim().isEmpty()) {
                    severity = determineSeverityFromScore(score);
                }

                Optional<AbusiveWord> existingOpt = abusiveWordRepository.findByKeywordIgnoreCase(keyword);
                if (existingOpt.isPresent()) {
                    // Update existing record to prevent duplicate entries
                    AbusiveWord existing = existingOpt.get();
                    existing.setCategory(category);
                    existing.setScore(score);
                    existing.setSeverity(severity);
                    existing.setRecommendedAction(recommendedAction);
                    existing.setLanguage(language);
                    existing.setActive(true);
                    abusiveWordRepository.save(existing);
                    result.updatedCount++;
                } else {
                    // Insert new record
                    AbusiveWord newWord = new AbusiveWord(
                            keyword,
                            language,
                            category,
                            severity,
                            score,
                            recommendedAction,
                            true
                    );
                    abusiveWordRepository.save(newWord);
                    result.insertedCount++;
                }
                result.totalProcessed++;
            }

            log.info("Excel dataset import complete. Total records: {}, Inserted: {}, Updated: {}",
                    result.totalProcessed, result.insertedCount, result.updatedCount);

        } catch (Exception e) {
            log.error("Failed to import Excel dataset: {}", e.getMessage(), e);
        }
        return result;
    }

    private byte[] loadExcelBytes() {
        try {
            File rootFile = new File(DATASET_FILENAME);
            if (rootFile.exists() && rootFile.isFile()) {
                return java.nio.file.Files.readAllBytes(rootFile.toPath());
            }

            InputStream cpStream = getClass().getResourceAsStream("/" + DATASET_FILENAME);
            if (cpStream != null) {
                try (cpStream) {
                    return cpStream.readAllBytes();
                }
            }
        } catch (Exception e) {
            log.warn("Error loading Excel dataset file: {}", e.getMessage());
        }
        return null;
    }

    private Map<String, byte[]> extractXmlEntries(byte[] zipBytes) throws Exception {
        Map<String, byte[]> map = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    map.put(entry.getName(), baos.toByteArray());
                }
                zis.closeEntry();
            }
        }
        return map;
    }

    private List<String> parseSharedStrings(byte[] bytes) {
        List<String> list = new ArrayList<>();
        if (bytes == null || bytes.length == 0) return list;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(bytes));
            NodeList siList = doc.getElementsByTagName("si");
            for (int i = 0; i < siList.getLength(); i++) {
                Element si = (Element) siList.item(i);
                NodeList tList = si.getElementsByTagName("t");
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < tList.getLength(); j++) {
                    sb.append(tList.item(j).getTextContent());
                }
                list.add(sb.toString());
            }
        } catch (Exception e) {
            log.warn("Failed to parse shared strings: {}", e.getMessage());
        }
        return list;
    }

    private String extractCellValue(Element cell, List<String> sharedStrings) {
        String t = cell.getAttribute("t");
        NodeList isList = cell.getElementsByTagName("is");
        if (isList.getLength() > 0) {
            Element is = (Element) isList.item(0);
            NodeList tList = is.getElementsByTagName("t");
            if (tList.getLength() > 0) {
                return tList.item(0).getTextContent();
            }
        }

        NodeList vList = cell.getElementsByTagName("v");
        if (vList.getLength() > 0) {
            String val = vList.item(0).getTextContent();
            if ("s".equals(t) && val != null && !val.isBlank()) {
                try {
                    int idx = Integer.parseInt(val.trim());
                    if (idx >= 0 && idx < sharedStrings.size()) {
                        return sharedStrings.get(idx);
                    }
                } catch (NumberFormatException ignored) {}
            }
            return val;
        }
        return "";
    }

    private String detectLanguage(String keyword, String category) {
        if (category != null && category.toLowerCase().contains("tamil")) {
            return "Tamil";
        }
        for (char c : keyword.toCharArray()) {
            if (c >= '\u0B80' && c <= '\u0BFF') {
                return "Tamil";
            }
        }
        return "English";
    }

    private String determineSeverityFromScore(int score) {
        if (score >= 9) return "CRITICAL";
        if (score >= 6) return "HIGH";
        if (score >= 3) return "MEDIUM";
        return "LOW";
    }

    public static class ImportResult {
        public int totalProcessed = 0;
        public int insertedCount = 0;
        public int updatedCount = 0;
    }
}
