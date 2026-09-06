package com.sourceviz.model;

public class FlowNode {
    private final String id;
    private final FlowNodeType type;
    private final String label;
    private final String codeSnippet;
    private final int lineNumber;

    public FlowNode(String id, FlowNodeType type, String label, String codeSnippet, int lineNumber) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.codeSnippet = codeSnippet != null ? codeSnippet : "";
        this.lineNumber = lineNumber;
    }

    public String getId() { return id; }
    public FlowNodeType getType() { return type; }
    public String getLabel() { return label; }
    public String getCodeSnippet() { return codeSnippet; }
    public int getLineNumber() { return lineNumber; }

    @Override
    public String toString() {
        return id + " [" + type + "]: " + label;
    }
}
