package com.sourceviz.model;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MethodFlowchart {
    private final String methodName;
    private final String className;
    private final String sourceFile;
    private final int startLine;
    private final int endLine;
    private final List<FlowNode> nodes;
    private final List<FlowEdge> edges;

    public MethodFlowchart(String methodName, String className, String sourceFile, int startLine, int endLine) {
        this.methodName = methodName;
        this.className = className != null ? className : "";
        this.sourceFile = sourceFile;
        this.startLine = startLine;
        this.endLine = endLine;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public String getMethodName() { return methodName; }
    public String getClassName() { return className; }
    public String getSourceFile() { return sourceFile; }
    public int getStartLine() { return startLine; }
    public int getEndLine() { return endLine; }
    public List<FlowNode> getNodes() { return nodes; }
    public List<FlowEdge> getEdges() { return edges; }

    public void addNode(FlowNode node) {
        nodes.add(node);
    }

    public void addEdge(String from, String to) {
        edges.add(new FlowEdge(from, to));
    }

    public void addEdge(String from, String to, String label) {
        edges.add(new FlowEdge(from, to, label));
    }

    public String getFullName() {
        if (!className.isEmpty()) {
            return className + "::" + methodName;
        }
        return methodName;
    }

    public String getShortFileName() {
        if (sourceFile == null || sourceFile.isEmpty()) return "";
        try {
            return Paths.get(sourceFile).getFileName().toString();
        } catch (Exception e) {
            int lastSlash = Math.max(sourceFile.lastIndexOf('/'), sourceFile.lastIndexOf('\\'));
            if (lastSlash >= 0) return sourceFile.substring(lastSlash + 1);
            return sourceFile;
        }
    }

    public int getCyclomaticComplexity() {
        int decisions = 0;
        for (FlowNode n : nodes) {
            if (n.getType() == FlowNodeType.CONDITION || n.getType() == FlowNodeType.LOOP_CONDITION) {
                decisions++;
            }
        }
        return decisions + 1;
    }

    /**
     * Converts to Mermaid.js Flowchart syntax
     */
    public String toMermaid() {
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart TD\n");

        for (FlowNode node : nodes) {
            String safeText = sanitizeMermaid(node.getLabel());
            String lineInfo = node.getLineNumber() > 0 ? " (L" + node.getLineNumber() + ")" : "";
            String fullLabel = safeText + lineInfo;

            switch (node.getType()) {
                case START, END -> sb.append(String.format("    %s([\"%s\"])\n", node.getId(), fullLabel));
                case CONDITION, LOOP_CONDITION -> sb.append(String.format("    %s{\"%s\"}\n", node.getId(), fullLabel));
                case INPUT_OUTPUT -> sb.append(String.format("    %s[/\"%s\"/]\n", node.getId(), fullLabel));
                case SUBROUTINE -> sb.append(String.format("    %s[[\"%s\"]]\n", node.getId(), fullLabel));
                case MERGE -> sb.append(String.format("    %s(( ))\n", node.getId()));
                default -> sb.append(String.format("    %s[\"%s\"]\n", node.getId(), fullLabel));
            }
        }

        for (FlowEdge edge : edges) {
            if (edge.getLabel().isEmpty()) {
                sb.append(String.format("    %s --> %s\n", edge.getFromId(), edge.getToId()));
            } else {
                sb.append(String.format("    %s -->|\"%s\"| %s\n",
                        edge.getFromId(), sanitizeMermaid(edge.getLabel()), edge.getToId()));
            }
        }

        return sb.toString();
    }

    /**
     * Converts to Graphviz DOT syntax
     */
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph \"").append(getFullName().replaceAll("[^a-zA-Z0-9_]", "_")).append("\" {\n");
        sb.append("    rankdir=TB;\n");
        sb.append("    node [fontname=\"Arial\", fontsize=10];\n");
        sb.append("    edge [fontname=\"Arial\", fontsize=9];\n\n");

        for (FlowNode node : nodes) {
            String label = escapeDot(node.getLabel() + (node.getLineNumber() > 0 ? " (L" + node.getLineNumber() + ")" : ""));
            String shape = "box";
            String style = "filled";
            String color = "#E3F2FD";

            switch (node.getType()) {
                case START -> { shape = "oval"; color = "#C8E6C9"; }
                case END -> { shape = "oval"; color = "#FFCDD2"; }
                case CONDITION -> { shape = "diamond"; color = "#FFF9C4"; }
                case LOOP_CONDITION -> { shape = "hexagon"; color = "#FFE082"; }
                case INPUT_OUTPUT -> { shape = "parallelogram"; color = "#E1BEE7"; }
                case SUBROUTINE -> { shape = "component"; color = "#B2EBF2"; }
                case MERGE -> { shape = "point"; color = "#BDBDBD"; }
                default -> { shape = "box"; color = "#FFFFFF"; }
            }

            sb.append(String.format("    \"%s\" [label=\"%s\", shape=%s, style=\"%s\", fillcolor=\"%s\"];\n",
                    node.getId(), label, shape, style, color));
        }

        sb.append("\n");
        for (FlowEdge edge : edges) {
            if (edge.getLabel().isEmpty()) {
                sb.append(String.format("    \"%s\" -> \"%s\";\n", edge.getFromId(), edge.getToId()));
            } else {
                sb.append(String.format("    \"%s\" -> \"%s\" [label=\"%s\"];\n",
                        edge.getFromId(), edge.getToId(), escapeDot(edge.getLabel())));
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String sanitizeMermaid(String s) {
        if (s == null) return "";
        return s.replace("\"", "'")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", " ");
    }

    private String escapeDot(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
