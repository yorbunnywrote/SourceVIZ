package com.sourceviz;

import com.sourceviz.analyzer.CodeAnalyzer;
import com.sourceviz.exporter.DotExporter;
import com.sourceviz.exporter.HtmlReportExporter;
import com.sourceviz.exporter.MermaidExporter;
import com.sourceviz.model.MethodFlowchart;

import java.awt.Desktop;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        printBanner();

        Path projectPath = null;
        Path outputDir = Paths.get("output");
        boolean openBrowser = true;

        if (args.length > 0) {
            projectPath = Paths.get(cleanPath(args[0]));
            for (int i = 1; i < args.length; i++) {
                if ("--output".equals(args[i]) && i + 1 < args.length) {
                    outputDir = Paths.get(cleanPath(args[++i]));
                } else if ("--no-open".equals(args[i])) {
                    openBrowser = false;
                }
            }
        } else {
            projectPath = askUserProjectPath();
        }

        if (projectPath == null || !Files.isDirectory(projectPath)) {
            System.err.println("❌ Ошибка: Указанный путь не существует или не является папкой: " + projectPath);
            return;
        }

        System.out.println("🔍 Запуск анализа проекта: " + projectPath.toAbsolutePath());
        long startTime = System.currentTimeMillis();

        CodeAnalyzer analyzer = new CodeAnalyzer(projectPath);
        if (!analyzer.analyze()) {
            System.err.println("❌ Анализ проекта завершился с ошибкой.");
            return;
        }

        try {
            Files.createDirectories(outputDir);
            System.out.println("\n💾 Генерация отчетов и диаграмм...");

            // 1. Interactive HTML Report
            Path htmlReport = outputDir.resolve("report.html");
            HtmlReportExporter.generateReport(analyzer, htmlReport);
            System.out.println("   ✓ Интерактивный HTML-отчет: " + htmlReport.toAbsolutePath());

            // 2. Mermaid Diagrams
            Path archMmd = outputDir.resolve("architecture.mmd");
            MermaidExporter.exportToFile(MermaidExporter.generateArchitectureMermaid(analyzer), archMmd);
            Path fileMmd = outputDir.resolve("file_dependencies.mmd");
            MermaidExporter.exportToFile(MermaidExporter.generateFileDependenciesMermaid(analyzer), fileMmd);
            System.out.println("   ✓ Диаграммы Mermaid (.mmd): " + archMmd.getFileName() + ", " + fileMmd.getFileName());

            // 3. Graphviz DOT files
            Path fullDot = outputDir.resolve("full_graph.dot");
            DotExporter.exportFullGraph(analyzer, fullDot);
            Path fileDot = outputDir.resolve("file_dependencies.dot");
            DotExporter.exportFileDependencies(analyzer, fileDot);
            System.out.println("   ✓ Графы Graphviz (.dot):    " + fullDot.getFileName() + ", " + fileDot.getFileName());

            // 4. Individual Flowcharts
            Path flowchartsDir = outputDir.resolve("flowcharts");
            Files.createDirectories(flowchartsDir);
            for (MethodFlowchart fc : analyzer.getFlowcharts()) {
                String safeName = fc.getFullName().replaceAll("[^a-zA-Z0-9_]", "_");
                DotExporter.exportFlowchart(fc, flowchartsDir.resolve(safeName + ".dot"));
                MermaidExporter.exportToFile(fc.toMermaid(), flowchartsDir.resolve(safeName + ".mmd"));
            }
            System.out.println("   ✓ Блок-схемы методов (.dot, .mmd): " + analyzer.getFlowcharts().size() + " шт. в " + flowchartsDir.getFileName());

            long duration = System.currentTimeMillis() - startTime;

            printSummary(analyzer, duration, htmlReport);

            if (openBrowser) {
                tryOpenBrowser(htmlReport);
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка при экспорте диаграмм: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printBanner() {
        System.out.println("=================================================================");
        System.out.println("⚡ SourceVIZ 2.0 (Java Edition) - Анализ Кода и Генератор Блок-Схем");
        System.out.println("   Поддержка языков: C/C++, Java 21");
        System.out.println("   Форматы: Интерактивный HTML, Mermaid (.mmd), Graphviz (.dot)");
        System.out.println("=================================================================\n");
    }

    private static Path askUserProjectPath() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите путь к проекту (или нажмите Enter для анализа родительской папки): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return Paths.get("..").toAbsolutePath().normalize();
        }

        return Paths.get(cleanPath(input));
    }

    private static String cleanPath(String raw) {
        String clean = raw.trim();
        if (clean.startsWith("\"") && clean.endsWith("\"")) {
            clean = clean.substring(1, clean.length() - 1);
        }
        return clean;
    }

    private static void printSummary(CodeAnalyzer analyzer, long durationMs, Path reportFile) {
        System.out.println("\n======================= СТАТИСТИКА АНАЛИЗА =======================");
        System.out.printf("Время обработки:              %d мс%n", durationMs);
        System.out.printf("Файлов C/C++:                 %d%n", analyzer.getParsedCppFiles());
        System.out.printf("Файлов Java:                  %d%n", analyzer.getParsedJavaFiles());
        System.out.printf("Всего сущностей в коде:       %d%n", analyzer.getEntities().size());
        for (Map.Entry<String, Integer> entry : analyzer.getEntityStatistics().entrySet()) {
            System.out.printf("  - %-25s : %d%n", entry.getKey(), entry.getValue());
        }
        System.out.printf("Всего связей (call/include):  %d%n", analyzer.getRelationships().size());
        System.out.printf("Сгенерировано блок-схем:      %d%n", analyzer.getFlowcharts().size());
        System.out.println("==================================================================");
        System.out.println("🚀 Отчет готов: " + reportFile.toAbsolutePath());
        System.out.println("   Дважды щелкните по report.html, чтобы открыть интерактивные схемы!");
    }

    private static void tryOpenBrowser(Path htmlFile) {
        try {
            File f = htmlFile.toFile();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(f.toURI());
            } else {
                // Windows fallback
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", f.getAbsolutePath()});
            }
        } catch (Exception ignored) {
            // Non-critical, user can open manually
        }
    }
}
