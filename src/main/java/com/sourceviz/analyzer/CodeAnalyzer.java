package com.sourceviz.analyzer;

import com.sourceviz.model.*;
import com.sourceviz.parser.*;
import com.sourceviz.scanner.ProjectScanner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CodeAnalyzer {

    private final ProjectScanner scanner;
    private final CppParser cppParser = new CppParser();
    private final JavaCodeParser javaParser = new JavaCodeParser();

    private final List<CodeEntity> allEntities = new ArrayList<>();
    private final List<Relationship> allRelationships = new ArrayList<>();
    private final List<MethodFlowchart> allFlowcharts = new ArrayList<>();

    private int parsedCppFiles = 0;
    private int parsedJavaFiles = 0;

    public CodeAnalyzer(Path projectRoot) {
        this.scanner = new ProjectScanner(projectRoot);
    }

    public boolean analyze() {
        allEntities.clear();
        allRelationships.clear();
        allFlowcharts.clear();
        parsedCppFiles = 0;
        parsedJavaFiles = 0;

        try {
            System.out.println("==> Шаг 1: Сканирование файлов проекта...");
            scanner.scan();

            List<Path> allFiles = scanner.getAllFiles();
            System.out.printf("    Найдено файлов C/C++: %d, Java: %d (всего: %d)%n",
                    scanner.getCppFiles().size(), scanner.getJavaFiles().size(), allFiles.size());

            if (allFiles.isEmpty()) {
                System.err.println("В указанной папке не найдено файлов C/C++ или Java.");
                return false;
            }

            System.out.println("==> Шаг 2: Анализ сущностей и блок-схем методов...");
            int entityId = 1;

            // Phase A: Parse entities & flowcharts
            for (Path file : allFiles) {
                String content = readFileContent(file);
                LanguageParser parser = scanner.isCppFile(file) ? cppParser : javaParser;

                if (scanner.isCppFile(file)) parsedCppFiles++;
                else parsedJavaFiles++;

                List<CodeEntity> entities = parser.parseEntities(file, content, scanner);
                for (CodeEntity e : entities) {
                    e.setId(entityId++);
                    allEntities.add(e);
                }

                List<MethodFlowchart> flowcharts = parser.parseFlowcharts(file, content);
                allFlowcharts.addAll(flowcharts);
            }

            System.out.println("==> Шаг 3: Построение связей (вызовы, наследование, включения)...");
            // Phase B: Parse relationships with knowledge of all entities
            for (Path file : allFiles) {
                String content = readFileContent(file);
                LanguageParser parser = scanner.isCppFile(file) ? cppParser : javaParser;
                List<Relationship> relationships = parser.parseRelationships(file, content, allEntities, scanner);
                allRelationships.addAll(relationships);
            }

            System.out.println("==> Анализ успешно завершен!");
            return true;

        } catch (Exception e) {
            System.err.println("Ошибка при анализе проекта: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String readFileContent(Path file) {
        // Try UTF-8 first, fallback to Windows-1251 (CP1251) if invalid
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            try {
                return Files.readString(file, Charset.forName("windows-1251"));
            } catch (IOException ex) {
                try {
                    return Files.readString(file, StandardCharsets.ISO_8859_1);
                } catch (IOException ignored) {
                    return "";
                }
            }
        }
    }

    public ProjectScanner getScanner() { return scanner; }
    public List<CodeEntity> getEntities() { return Collections.unmodifiableList(allEntities); }
    public List<Relationship> getRelationships() { return Collections.unmodifiableList(allRelationships); }
    public List<MethodFlowchart> getFlowcharts() { return Collections.unmodifiableList(allFlowcharts); }
    public int getParsedCppFiles() { return parsedCppFiles; }
    public int getParsedJavaFiles() { return parsedJavaFiles; }

    public Map<String, Integer> getEntityStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (CodeEntity e : allEntities) {
            String kind = e.getType().name();
            stats.put(kind, stats.getOrDefault(kind, 0) + 1);
        }
        return stats;
    }

    public Map<String, Integer> getRelationshipStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (Relationship r : allRelationships) {
            String type = r.getType().isEmpty() ? "other" : r.getType();
            stats.put(type, stats.getOrDefault(type, 0) + 1);
        }
        return stats;
    }
}
