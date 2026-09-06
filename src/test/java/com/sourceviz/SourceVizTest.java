package com.sourceviz;

import com.sourceviz.analyzer.CodeAnalyzer;
import com.sourceviz.exporter.DotExporter;
import com.sourceviz.exporter.HtmlReportExporter;
import com.sourceviz.exporter.MermaidExporter;
import com.sourceviz.model.CodeEntity;
import com.sourceviz.model.FlowNodeType;
import com.sourceviz.model.MethodFlowchart;
import com.sourceviz.parser.ControlFlowAnalyzer;
import com.sourceviz.parser.CppParser;
import com.sourceviz.parser.JavaCodeParser;
import com.sourceviz.scanner.ProjectScanner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SourceVizTest {

    public static void main(String[] args) {
        System.out.println(">>> Запуск тестов SourceVIZ 2.0...");
        int passed = 0;
        int total = 0;

        total++;
        if (testControlFlowAnalyzer()) {
            System.out.println("  [PASS] testControlFlowAnalyzer");
            passed++;
        } else {
            System.err.println("  [FAIL] testControlFlowAnalyzer");
        }

        total++;
        if (testCppParser()) {
            System.out.println("  [PASS] testCppParser");
            passed++;
        } else {
            System.err.println("  [FAIL] testCppParser");
        }

        total++;
        if (testJavaParser()) {
            System.out.println("  [PASS] testJavaParser");
            passed++;
        } else {
            System.err.println("  [FAIL] testJavaParser");
        }

        total++;
        if (testAnalyzeOriginalCppProject()) {
            System.out.println("  [PASS] testAnalyzeOriginalCppProject");
            passed++;
        } else {
            System.err.println("  [FAIL] testAnalyzeOriginalCppProject");
        }

        System.out.printf("\nРезультаты: %d / %d тестов успешно пройдены.%n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }

    private static boolean testControlFlowAnalyzer() {
        String sampleCode = """
            int x = 10;
            System.out.println("Start");
            if (x > 5) {
                x = x * 2;
                while (x > 0) {
                    x--;
                }
            } else {
                x = 0;
            }
            return x;
        """;

        MethodFlowchart fc = ControlFlowAnalyzer.buildFlowchart("calculate", "TestService", "Test.java", 10, 25, sampleCode);

        boolean hasStart = fc.getNodes().stream().anyMatch(n -> n.getType() == FlowNodeType.START);
        boolean hasCondition = fc.getNodes().stream().anyMatch(n -> n.getType() == FlowNodeType.CONDITION);
        boolean hasLoop = fc.getNodes().stream().anyMatch(n -> n.getType() == FlowNodeType.LOOP_CONDITION);
        boolean hasIO = fc.getNodes().stream().anyMatch(n -> n.getType() == FlowNodeType.INPUT_OUTPUT);
        boolean hasEnd = fc.getNodes().stream().anyMatch(n -> n.getType() == FlowNodeType.END);

        String mmd = fc.toMermaid();
        String dot = fc.toDot();

        return hasStart && hasCondition && hasLoop && hasIO && hasEnd && mmd.contains("flowchart TD") && dot.contains("digraph");
    }

    private static boolean testCppParser() {
        String cppCode = """
            #include <iostream>
            #include "Calculator.h"

            class MathService : public IService {
            public:
                int compute(int a, int b) {
                    if (a < 0) {
                        return 0;
                    }
                    return a + b;
                }
            };
        """;

        CppParser parser = new CppParser();
        ProjectScanner scanner = new ProjectScanner(Paths.get("."));
        Path dummyPath = Paths.get("MathService.cpp");

        List<CodeEntity> entities = parser.parseEntities(dummyPath, cppCode, scanner);
        boolean hasIncludes = entities.stream().anyMatch(e -> e.getName().contains("Calculator.h"));
        boolean hasClass = entities.stream().anyMatch(e -> e.getName().equals("MathService"));
        boolean hasMethod = entities.stream().anyMatch(e -> e.getName().equals("compute"));

        List<MethodFlowchart> flowcharts = parser.parseFlowcharts(dummyPath, cppCode);
        boolean hasFlowchart = !flowcharts.isEmpty() && flowcharts.get(0).getMethodName().equals("compute");

        return hasIncludes && hasClass && hasMethod && hasFlowchart;
    }

    private static boolean testJavaParser() {
        String javaCode = """
            package com.example;
            import java.util.List;
            import com.example.models.User;

            public class UserService extends BaseService implements IUserService {
                public boolean isValidUser(String name, int age) {
                    if (name == null || name.isEmpty()) {
                        return false;
                    }
                    for (int i = 0; i < age; i++) {
                        System.out.println(i);
                    }
                    return true;
                }
            }
        """;

        JavaCodeParser parser = new JavaCodeParser();
        ProjectScanner scanner = new ProjectScanner(Paths.get("."));
        Path dummyPath = Paths.get("UserService.java");

        List<CodeEntity> entities = parser.parseEntities(dummyPath, javaCode, scanner);
        boolean hasClass = entities.stream().anyMatch(e -> e.getName().equals("UserService"));
        boolean hasMethod = entities.stream().anyMatch(e -> e.getName().equals("isValidUser"));

        List<MethodFlowchart> flowcharts = parser.parseFlowcharts(dummyPath, javaCode);
        boolean hasFlowchart = !flowcharts.isEmpty() && flowcharts.get(0).getMethodName().equals("isValidUser");

        return hasClass && hasMethod && hasFlowchart;
    }

    private static boolean testAnalyzeOriginalCppProject() {
        Path originalCppDir = Paths.get("..", "SourceVIZ-main", "SourceVIZ-main");
        if (!Files.isDirectory(originalCppDir)) {
            System.out.println("    (Skip: original C++ directory not found at " + originalCppDir + ")");
            return true;
        }

        CodeAnalyzer analyzer = new CodeAnalyzer(originalCppDir);
        boolean ok = analyzer.analyze();
        if (!ok) return false;

        boolean hasEntities = !analyzer.getEntities().isEmpty();
        boolean hasFlowcharts = !analyzer.getFlowcharts().isEmpty();

        // Check that exports work on this data
        try {
            Path testOut = Paths.get("test-output");
            Files.createDirectories(testOut);
            HtmlReportExporter.generateReport(analyzer, testOut.resolve("report.html"));
            DotExporter.exportFullGraph(analyzer, testOut.resolve("full.dot"));
            MermaidExporter.exportToFile(MermaidExporter.generateArchitectureMermaid(analyzer), testOut.resolve("arch.mmd"));
            // Clean up
            Files.deleteIfExists(testOut.resolve("report.html"));
            Files.deleteIfExists(testOut.resolve("full.dot"));
            Files.deleteIfExists(testOut.resolve("arch.mmd"));
            Files.deleteIfExists(testOut);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return hasEntities && hasFlowcharts;
    }
}
