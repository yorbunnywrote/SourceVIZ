package com.sourceviz.parser;

import com.sourceviz.model.*;
import com.sourceviz.scanner.ProjectScanner;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaCodeParser implements LanguageParser {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+(?:static\\s+)?([a-zA-Z0-9_.*]+)\\s*;");

    private static final Pattern TYPE_DECL_PATTERN = Pattern.compile(
            "(?:public|protected|private|static|final|abstract|sealed|non-sealed|\\s)*\\b(class|interface|enum|record)\\s+([a-zA-Z0-9_]+)(?:<[^>]+>)?(?:\\s+extends\\s+([a-zA-Z0-9_.,\\s<>]+))?(?:\\s+implements\\s+([a-zA-Z0-9_.,\\s<>]+))?\\s*\\{"
    );

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "(?:public|protected|private|static|final|synchronized|native|abstract|default|\\s)*" +
            "(?:<[^>]+>\\s*)?" +
            "([a-zA-Z0-9_<>,\\[\\]]+)\\s+" +
            "([a-zA-Z0-9_]+)\\s*" +
            "\\(([^)]*)\\)\\s*" +
            "(?:throws\\s+[a-zA-Z0-9_,\\s]+)?\\s*\\{"
    );

    @Override
    public List<CodeEntity> parseEntities(Path filePath, String content, ProjectScanner scanner) {
        List<CodeEntity> entities = new ArrayList<>();
        String[] lines = content.split("\r?\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            int lineNum = i + 1;

            Matcher impMatcher = IMPORT_PATTERN.matcher(line);
            if (impMatcher.find()) {
                String imp = impMatcher.group(1);
                CodeEntity entity = new CodeEntity(imp, EntityType.IMPORT, filePath.toString(), lineNum);
                entity.setTargetFile(scanner.resolveJavaImport(imp));
                entities.add(entity);
            }
        }

        // Classes / Interfaces / Records / Enums
        Matcher typeMatcher = TYPE_DECL_PATTERN.matcher(content);
        while (typeMatcher.find()) {
            String kind = typeMatcher.group(1);
            String name = typeMatcher.group(2);
            int lineNum = getLineNumber(content, typeMatcher.start());

            EntityType type = switch (kind.toLowerCase()) {
                case "interface" -> EntityType.INTERFACE;
                case "record" -> EntityType.RECORD;
                default -> EntityType.CLASS;
            };

            CodeEntity entity = new CodeEntity(name, type, filePath.toString(), lineNum);
            entities.add(entity);
        }

        // Methods
        List<JavaMethodDef> methods = extractMethods(content);
        for (JavaMethodDef m : methods) {
            CodeEntity entity = new CodeEntity(m.name, EntityType.METHOD, filePath.toString(), m.startLine);
            entity.setParentScope(m.className);
            entity.setSignature(m.returnType + " " + m.name + "(" + m.params + ")");
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public List<Relationship> parseRelationships(Path filePath, String content, List<CodeEntity> entities, ProjectScanner scanner) {
        List<Relationship> relationships = new ArrayList<>();
        String currentFile = filePath.toString();

        // 1. Imports
        String[] lines = content.split("\r?\n");
        for (String line : lines) {
            Matcher impMatcher = IMPORT_PATTERN.matcher(line.trim());
            if (impMatcher.find()) {
                String imp = impMatcher.group(1);
                String resolvedTarget = scanner.resolveJavaImport(imp);
                relationships.add(new Relationship(
                        filePath.getFileName().toString(),
                        resolvedTarget,
                        "import",
                        currentFile,
                        resolvedTarget
                ));
            }
        }

        // 2. Inheritance & Implements
        Matcher typeMatcher = TYPE_DECL_PATTERN.matcher(content);
        while (typeMatcher.find()) {
            String className = typeMatcher.group(2);
            String ext = typeMatcher.group(3);
            String impl = typeMatcher.group(4);

            if (ext != null && !ext.isBlank()) {
                for (String base : ext.split(",")) {
                    String cleanBase = base.replaceAll("<.*>", "").trim();
                    if (!cleanBase.isEmpty()) {
                        relationships.add(new Relationship(className, cleanBase, "inheritance", currentFile, ""));
                    }
                }
            }

            if (impl != null && !impl.isBlank()) {
                for (String iface : impl.split(",")) {
                    String cleanIface = iface.replaceAll("<.*>", "").trim();
                    if (!cleanIface.isEmpty()) {
                        relationships.add(new Relationship(className, cleanIface, "implements", currentFile, ""));
                    }
                }
            }
        }

        // 3. Method calls
        List<JavaMethodDef> methods = extractMethods(content);
        for (JavaMethodDef m : methods) {
            String callerName = (m.className.isEmpty() ? "" : m.className + "::") + m.name;

            for (CodeEntity target : entities) {
                if (target.getType() == EntityType.METHOD || target.getType() == EntityType.FUNCTION) {
                    String targetName = target.getName();
                    if (!targetName.equals(m.name) && targetName.length() > 2) {
                        Pattern callPat = Pattern.compile("\\b" + Pattern.quote(targetName) + "\\s*\\(");
                        if (callPat.matcher(m.body).find()) {
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
        List<JavaMethodDef> methods = extractMethods(content);

        for (JavaMethodDef m : methods) {
            MethodFlowchart chart = ControlFlowAnalyzer.buildFlowchart(
                    m.name,
                    m.className,
                    filePath.toString(),
                    m.startLine,
                    m.endLine,
                    m.body
            );
            flowcharts.add(chart);
        }

        return flowcharts;
    }

    // ==========================================
    // Java Method Extractor
    // ==========================================

    private static class JavaMethodDef {
        String returnType;
        String className;
        String name;
        String params;
        int startLine;
        int endLine;
        String body;
    }

    private List<JavaMethodDef> extractMethods(String content) {
        List<JavaMethodDef> result = new ArrayList<>();

        // Find enclosing class names
        Matcher typeMatcher = TYPE_DECL_PATTERN.matcher(content);
        List<TypeRegion> types = new ArrayList<>();
        while (typeMatcher.find()) {
            String name = typeMatcher.group(2);
            int braceStart = content.indexOf('{', typeMatcher.start());
            if (braceStart != -1) {
                int braceEnd = findMatchingBrace(content, braceStart);
                if (braceEnd != -1) {
                    types.add(new TypeRegion(name, braceStart, braceEnd));
                }
            }
        }

        Matcher m = METHOD_PATTERN.matcher(content);
        while (m.find()) {
            String retType = m.group(1);
            String name = m.group(2);
            String params = m.group(3);

            // Ignore keywords / control flow false positives
            if (Set.of("if", "for", "while", "switch", "catch", "new", "return").contains(name)) {
                continue;
            }

            int braceStart = m.end() - 1;
            int bodyEnd = findMatchingBrace(content, braceStart);
            if (bodyEnd == -1) continue;

            int startLine = getLineNumber(content, m.start());
            int endLine = getLineNumber(content, bodyEnd);
            String body = content.substring(braceStart + 1, bodyEnd);

            // Determine parent class
            String enclosingClass = "";
            for (TypeRegion tr : types) {
                if (m.start() > tr.start && bodyEnd <= tr.end) {
                    enclosingClass = tr.name;
                    break;
                }
            }

            JavaMethodDef def = new JavaMethodDef();
            def.returnType = retType != null ? retType.trim() : "";
            def.className = enclosingClass;
            def.name = name;
            def.params = params != null ? params.trim() : "";
            def.startLine = startLine;
            def.endLine = endLine;
            def.body = body;

            result.add(def);
        }

        return result;
    }

    private record TypeRegion(String name, int start, int end) {}

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
