package com.sourceviz.exporter;

import com.sourceviz.analyzer.CodeAnalyzer;
import com.sourceviz.model.CodeEntity;
import com.sourceviz.model.EntityType;
import com.sourceviz.model.MethodFlowchart;
import com.sourceviz.model.Relationship;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MermaidExporter {

    public static String generateArchitectureMermaid(CodeAnalyzer analyzer) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph LR\n");

        // Group entities by file
        Map<String, List<CodeEntity>> byFile = new LinkedHashMap<>();
        for (CodeEntity e : analyzer.getEntities()) {
            byFile.computeIfAbsent(e.getSourceFile(), k -> new ArrayList<>()).add(e);
        }

        int clusterIdx = 0;
        for (Map.Entry<String, List<CodeEntity>> entry : byFile.entrySet()) {
            String shortFile = getFileNameOrTarget(entry.getKey());
            String clusterId = "sub_" + clusterIdx++;
            sb.append(String.format("    subgraph %s [\"%s\"]\n", clusterId, escapeMermaid(shortFile)));

            for (CodeEntity e : entry.getValue()) {
                String id = "node_" + e.getId();
                String label = escapeMermaid(e.getDisplayName());
                switch (e.getType()) {
                    case CLASS, INTERFACE, STRUCT -> sb.append(String.format("        %s[\"%s\"]\n", id, label));
                    case FUNCTION, METHOD -> sb.append(String.format("        %s([\"%s\"])\n", id, label));
                    default -> sb.append(String.format("        %s[\"%s\"]\n", id, label));
                }
            }
            sb.append("    end\n\n");
        }

        // Add relationships
        Map<String, String> entityToNodeId = new HashMap<>();
        for (CodeEntity e : analyzer.getEntities()) {
            entityToNodeId.put(e.getSourceFile() + "|" + e.getName(), "node_" + e.getId());
            entityToNodeId.put(e.getName(), "node_" + e.getId());
        }

        for (Relationship r : analyzer.getRelationships()) {
            String fromId = entityToNodeId.get(r.getFromFile() + "|" + r.getFromEntity());
            if (fromId == null) fromId = entityToNodeId.get(r.getFromEntity());

            String toId = entityToNodeId.get(r.getToFile() + "|" + r.getToEntity());
            if (toId == null) toId = entityToNodeId.get(r.getToEntity());

            if (fromId != null && toId != null && !fromId.equals(toId)) {
                String label = escapeMermaid(r.getType());
                if ("inheritance".equalsIgnoreCase(r.getType()) || "implements".equalsIgnoreCase(r.getType())) {
                    sb.append(String.format("    %s ==>|%s| %s\n", fromId, label, toId));
                } else if ("call".equalsIgnoreCase(r.getType())) {
                    sb.append(String.format("    %s -.->|%s| %s\n", fromId, label, toId));
                } else {
                    sb.append(String.format("    %s -->|%s| %s\n", fromId, label, toId));
                }
            }
        }

        return sb.toString();
    }

    public static String generateFileDependenciesMermaid(CodeAnalyzer analyzer) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");

        Set<String> edges = new LinkedHashSet<>();
        for (Relationship r : analyzer.getRelationships()) {
            if (r.getFromFile() != null && r.getToFile() != null && !r.getFromFile().equals(r.getToFile())) {
                String from = getFileNameOrTarget(r.getFromFile());
                String to = getFileNameOrTarget(r.getToFile());
                String fromId = makeId(from);
                String toId = makeId(to);
                edges.add(String.format("    %s[\"%s\"] --> %s[\"%s\"]", fromId, escapeMermaid(from), toId, escapeMermaid(to)));
            }
        }

        if (edges.isEmpty()) {
            sb.append("    no_deps[\"Зависимости между файлами не обнаружены\"]\n");
        } else {
            for (String edge : edges) {
                sb.append(edge).append("\n");
            }
        }

        return sb.toString();
    }

    public static void exportToFile(String mermaidContent, Path outputFile) throws IOException {
        if (outputFile.getParent() != null) Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, mermaidContent, StandardCharsets.UTF_8);
    }

    public static String getFileNameOrTarget(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) return "";
        try {
            return Paths.get(pathStr).getFileName().toString();
        } catch (Exception e) {
            int lastSlash = Math.max(pathStr.lastIndexOf('/'), pathStr.lastIndexOf('\\'));
            if (lastSlash >= 0) return pathStr.substring(lastSlash + 1);
            return pathStr;
        }
    }

    private static String escapeMermaid(String s) {
        if (s == null) return "";
        return s.replace("\"", "'")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", " ");
    }

    private static String makeId(String s) {
        return "f_" + Math.abs(s.hashCode());
    }
}
