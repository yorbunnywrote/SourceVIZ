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

public class DotExporter {

    public static void exportFullGraph(CodeAnalyzer analyzer, Path outputFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph FullGraph {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    node [fontname=\"Arial\", fontsize=10, style=filled, fillcolor=white];\n");
        sb.append("    edge [fontname=\"Arial\", fontsize=9];\n\n");

        // Group entities by source file
        Map<String, List<CodeEntity>> byFile = new LinkedHashMap<>();
        for (CodeEntity e : analyzer.getEntities()) {
            byFile.computeIfAbsent(e.getSourceFile(), k -> new ArrayList<>()).add(e);
        }

        Map<String, String> nodeMap = new HashMap<>(); // key: file|entityName -> nodeId
        Set<String> addedFileNodes = new HashSet<>();

        int clusterIdx = 0;
        for (Map.Entry<String, List<CodeEntity>> entry : byFile.entrySet()) {
            String file = entry.getKey();
            String shortFile = MermaidExporter.getFileNameOrTarget(file);
            String clusterId = "cluster_" + clusterIdx++;

            sb.append(String.format("    subgraph \"%s\" {\n", clusterId));
            sb.append(String.format("        label = \"%s\";\n", escapeDot(shortFile)));
            sb.append("        style=filled; color=\"#E0E0E0\"; fillcolor=\"#F5F5F5\";\n");

            for (CodeEntity e : entry.getValue()) {
                String key = file + "|" + e.getName();
                String nodeId = "node_" + e.getId();
                nodeMap.put(key, nodeId);

                String shape = getEntityShape(e.getType());
                String color = getEntityColor(e.getType());
                String label = escapeDot(e.getDisplayName()) + "\\n(" + escapeDot(shortFile) + ":" + e.getLineNumber() + ")";

                sb.append(String.format("        \"%s\" [label=\"%s\", shape=%s, color=\"%s\", fillcolor=\"%s\"];\n",
                        nodeId, label, shape, color, color + "22"));
            }

            sb.append("    }\n\n");
        }

        // Edges
        sb.append("    // Связи и зависимости\n");
        for (Relationship r : analyzer.getRelationships()) {
            String fromKey = r.getFromFile() + "|" + r.getFromEntity();
            String toKey = r.getToFile() + "|" + r.getToEntity();

            String fromNode = nodeMap.get(fromKey);
            if (fromNode == null) {
                fromNode = makeId(r.getFromFile());
                if (addedFileNodes.add(fromNode)) {
                    String shortF = MermaidExporter.getFileNameOrTarget(r.getFromFile());
                    sb.append(String.format("    \"%s\" [label=\"%s\", shape=folder, fillcolor=\"#FFF9C4\"];\n",
                            fromNode, escapeDot(shortF)));
                }
            }

            String toNode = nodeMap.get(toKey);
            if (toNode == null) {
                toNode = makeId(r.getToFile());
                if (addedFileNodes.add(toNode)) {
                    String shortT = MermaidExporter.getFileNameOrTarget(r.getToFile());
                    sb.append(String.format("    \"%s\" [label=\"%s\", shape=folder, fillcolor=\"#FFF9C4\"];\n",
                            toNode, escapeDot(shortT)));
                }
            }

            String style = switch (r.getType()) {
                case "include", "import" -> "dashed";
                case "call" -> "dotted";
                case "inheritance", "implements" -> "bold";
                default -> "solid";
            };

            sb.append(String.format("    \"%s\" -> \"%s\" [label=\"%s\", style=%s];\n",
                    fromNode, toNode, escapeDot(r.getType()), style));
        }

        sb.append("}\n");

        if (outputFile.getParent() != null) Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
    }

    public static void exportFileDependencies(CodeAnalyzer analyzer, Path outputFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph FileDependencies {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    node [shape=box, fontname=\"Arial\", fontsize=10, fillcolor=\"#E1F5FE\", style=\"filled,rounded\"];\n");
        sb.append("    edge [fontname=\"Arial\", fontsize=9, color=\"#546E7A\"];\n\n");

        Set<String> edges = new LinkedHashSet<>();
        Set<String> nodes = new LinkedHashSet<>();

        for (Relationship r : analyzer.getRelationships()) {
            if (r.getFromFile() != null && r.getToFile() != null && !r.getFromFile().equals(r.getToFile())) {
                String from = MermaidExporter.getFileNameOrTarget(r.getFromFile());
                String to = MermaidExporter.getFileNameOrTarget(r.getToFile());
                nodes.add(from);
                nodes.add(to);
                edges.add(String.format("    \"%s\" -> \"%s\";", escapeDot(from), escapeDot(to)));
            }
        }

        for (String node : nodes) {
            sb.append(String.format("    \"%s\" [label=\"%s\"];\n", escapeDot(node), escapeDot(node)));
        }
        sb.append("\n");
        for (String edge : edges) {
            sb.append(edge).append("\n");
        }
        sb.append("}\n");

        if (outputFile.getParent() != null) Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, sb.toString(), StandardCharsets.UTF_8);
    }

    public static void exportFlowchart(MethodFlowchart chart, Path outputFile) throws IOException {
        if (outputFile.getParent() != null) Files.createDirectories(outputFile.getParent());
        Files.writeString(outputFile, chart.toDot(), StandardCharsets.UTF_8);
    }

    private static String getEntityShape(EntityType type) {
        return switch (type) {
            case CLASS -> "box";
            case INTERFACE -> "component";
            case STRUCT -> "polygon,sides=6";
            case RECORD -> "box";
            case FUNCTION, METHOD -> "ellipse";
            case INCLUDE, IMPORT -> "note";
            default -> "oval";
        };
    }

    private static String getEntityColor(EntityType type) {
        return switch (type) {
            case CLASS -> "#1E88E5";
            case INTERFACE -> "#00897B";
            case STRUCT -> "#8E24AA";
            case RECORD -> "#3949AB";
            case FUNCTION, METHOD -> "#43A047";
            case INCLUDE, IMPORT -> "#FB8C00";
            default -> "#757575";
        };
    }

    private static String escapeDot(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ");
    }

    private static String makeId(String s) {
        return "f_" + Math.abs(s.hashCode());
    }
}
