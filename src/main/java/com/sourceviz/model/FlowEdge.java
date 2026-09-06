package com.sourceviz.model;

public class FlowEdge {
    private final String fromId;
    private final String toId;
    private final String label;

    public FlowEdge(String fromId, String toId, String label) {
        this.fromId = fromId;
        this.toId = toId;
        this.label = label != null ? label : "";
    }

    public FlowEdge(String fromId, String toId) {
        this(fromId, toId, "");
    }

    public String getFromId() { return fromId; }
    public String getToId() { return toId; }
    public String getLabel() { return label; }

    @Override
    public String toString() {
        if (label.isEmpty()) return fromId + " --> " + toId;
        return fromId + " -- " + label + " --> " + toId;
    }
}
