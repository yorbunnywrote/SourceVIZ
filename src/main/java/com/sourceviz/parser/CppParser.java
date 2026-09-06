package com.sourceviz.parser;

import com.sourceviz.model.*;
import com.sourceviz.scanner.ProjectScanner;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CppParser implements LanguageParser {

    private static final Pattern INCLUDE_PATTERN = Pattern.compile(
            "^\\s*#include\\s*(<([^>]+)>|\"([^\"]+)\")"
    );

    private static final Pattern CLASS_STRUCT_PATTERN = Pattern.compile(
            "(class|struct)\\s+([a-zA-Z0-9_]+)(?:\\s*:\\s*([^{;]+))?\\s*\\{"
    );

    private static final Pattern FUNCTION_HEADER_PATTERN = Pattern.compile(
            "(?:([a-zA-Z0-9_:<>&*\\s]+)\\s+)?([a-zA-Z0-9_]+::)?([a-zA-Z0-9_~]+)\\s*\\(([^)]*)\\)\\s*(?:const)?\\s*(?:override)?\\s*\\{"
    );

    @Override
    public List<CodeEntity> parseEntities(Path filePath, String content, ProjectScanner scanner) {
        List<CodeEntity> entities = new ArrayList<>();
        String[] lines = content.split("\r?\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            int lineNum = i + 1;

            // 1. #include
            Matcher incMatcher = INCLUDE_PATTERN.matcher(line);
            if (incMatcher.find()) {
                String header = incMatcher.group(2) != null ? incMatcher.group(2) : incMatcher.group(3);
                CodeEntity entity = new CodeEntity(header, EntityType.INCLUDE, filePath.toString(), lineNum);
                entity.setTargetFile(scanner.resolveCppHeader(filePath, header));
                entities.add(entity);
            }
        }

        // 2. Classes and Structs
        Matcher csMatcher = CLASS_STRUCT_PATTERN.matcher(content);
        while (csMatcher.find()) {
            String kind = csMatcher.group(1);
            String name = csMatcher.group(2);
            int lineNum = getLineNumber(content, csMatcher.start());

            EntityType type = "struct".equalsIgnoreCase(kind) ? EntityType.STRUCT : EntityType.CLASS;
            CodeEntity entity = new CodeEntity(name, type, filePath.toString(), lineNum);
            entities.add(entity);
        }

        // 3. Functions and Methods
        List<FunctionDef> functions = extractFunctions(content);
        for (FunctionDef fn : functions) {
            CodeEntity entity = new CodeEntity(fn.name, EntityType.FUNCTION, filePath.toString(), fn.startLine);
            if (fn.scope != null && !fn.scope.isEmpty()) {
                entity.setType(EntityType.METHOD);
                entity.setParentScope(fn.scope);
            }
            entity.setSignature(fn.returnType + " " + (fn.scope != null ? fn.scope + "::" : "") + fn.name + "(" + fn.params + ")");
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public List<Relationship> parseRelationships(Path filePath, String content, List<CodeEntity> entities, ProjectScanner scanner) {
        List<Relationship> relationships = new ArrayList<>();
        String currentFile = filePath.toString();

        // 1. Include relationships
        String[] lines = content.split("\r?\n");
        for (String line : lines) {
            Matcher incMatcher = INCLUDE_PATTERN.matcher(line.trim());
            if (incMatcher.find()) {
                String header = incMatcher.group(2) != null ? incMatcher.group(2) : incMatcher.group(3);
                String resolvedTarget = scanner.resolveCppHeader(filePath, header);
                relationships.add(new Relationship(
                        filePath.getFileName().toString(),
                        scanner.resolveCppHeader(filePath, header),
                        "include",
                        currentFile,
                        resolvedTarget
                ));
            }
        }

        // 2. Inheritance
        Matcher csMatcher = CLASS_STRUCT_PATTERN.matcher(content);
        while (csMatcher.find()) {
            String className = csMatcher.group(2);
            String bases = csMatcher.group(3);
            if (bases != null && !bases.isBlank()) {
                String[] parts = bases.split(",");
                for (String part : parts) {
                    String base = part.replaceAll("(public|protected|private|virtual)", "").trim();
                    if (!base.isEmpty()) {
                        relationships.add(new Relationship(className, base, "inheritance", currentFile, ""));
                    }
                }
            }
        }

        // 3. Function Calls
        List<FunctionDef> functions = extractFunctions(content);
        for (FunctionDef fn : functions) {
            String callerName = fn.scope != null && !fn.scope.isEmpty() ? fn.scope + "::" + fn.name : fn.name;

            // Simple search for calls to other known functions/entities
            for (CodeEntity target : entities) {
                if (target.getType() == EntityType.FUNCTION || target.getType() == EntityType.METHOD) {
                    String targetName = target.getName();
                    if (!targetName.equals(fn.name) && targetName.length() > 2) {
                        Pattern callPat = Pattern.compile("\\b" + Pattern.quote(targetName) + "\\s*\\(");
                        if (callPat.matcher(fn.body).find()) {
                            relationships.add(new Relationship(callerName, target.getDisplayName(), "call", currentFile, target.getSourceFile()));
                        }
                    }
                }
            }
        }

        return relationships;
    }

    @Override
    public List<MethodFlowchart> parseFlowcharts(Path filePath, String content) {
        List<MethodFlowchart> flowcharts = new ArrayList<>();
        List<FunctionDef> functions = extractFunctions(content);

        for (FunctionDef fn : functions) {
            MethodFlowchart chart = ControlFlowAnalyzer.buildFlowchart(
                    fn.name,
                    fn.scope != null ? fn.scope : "",
                    filePath.toString(),
                    fn.startLine,
                    fn.endLine,
                    fn.body
            );
            flowcharts.add(chart);
        }

        return flowcharts;
    }

    // ==========================================
    // Function Extractor Helper
    // ==========================================

    private static class FunctionDef {
        String returnType;
        String scope;
        String name;
        String params;
        int startLine;
        int endLine;
        String body;
    }

    private List<FunctionDef> extractFunctions(String content) {
        List<FunctionDef> result = new ArrayList<>();
        Matcher m = FUNCTION_HEADER_PATTERN.matcher(content);

        while (m.find()) {
            String rawRet = m.group(1);
            String rawScope = m.group(2);
            String name = m.group(3);
            String params = m.group(4);

            // Filter out keywords that match regex accidentally
            if ("if".equals(name) || "while".equals(name) || "for".equals(name) || "switch".equals(name) || "catch".equals(name)) {
                continue;
            }

            int braceStart = m.end() - 1; // '{' position
            int bodyEnd = findMatchingBrace(content, braceStart);
            if (bodyEnd == -1) continue;

            int startLine = getLineNumber(content, m.start());
            int endLine = getLineNumber(content, bodyEnd);

            String body = content.substring(braceStart + 1, bodyEnd);

            FunctionDef fn = new FunctionDef();
            fn.returnType = rawRet != null ? rawRet.trim() : "void";
            fn.scope = rawScope != null ? rawScope.replace("::", "").trim() : "";
            fn.name = name;
            fn.params = params != null ? params.trim() : "";
            fn.startLine = startLine;
            fn.endLine = endLine;
            fn.body = body;

            result.add(fn);
        }

        return result;
    }

    private static int findMatchingBrace(String s, int openIndex) {
        int depth = 1;
        boolean inStr = false;
        char quote = 0;

        for (int i = openIndex + 1; i < s.length(); i++) {
            char c = s.charAt(i);

            if (!inStr && (c == '"' || c == '\'')) {
                inStr = true;
                quote = c;
            } else if (inStr && c == quote && s.charAt(i - 1) != '\\') {
                inStr = false;
            } else if (!inStr) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    private static int getLineNumber(String text, int charIndex) {
        int line = 1;
        for (int i = 0; i < charIndex && i < text.length(); i++) {
            if (text.charAt(i) == '\n') line++;
        }
        return line;
    }
}
