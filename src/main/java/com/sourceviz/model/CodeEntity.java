package com.sourceviz.model;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CodeEntity {
    private int id;
    private String name;
    private EntityType type;
    private String sourceFile;
    private int lineNumber;
    private String targetFile;
    private String signature;
    private String parentScope;

    public CodeEntity(String name, EntityType type, String sourceFile, int lineNumber) {
        this.name = name;
        this.type = type;
        this.sourceFile = sourceFile;
        this.lineNumber = lineNumber;
        this.targetFile = "";
        this.signature = name;
        this.parentScope = "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EntityType getType() { return type; }
    public void setType(EntityType type) { this.type = type; }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public String getTargetFile() { return targetFile; }
    public void setTargetFile(String targetFile) { this.targetFile = targetFile; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getParentScope() { return parentScope; }
    public void setParentScope(String parentScope) { this.parentScope = parentScope; }

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

    public String getDisplayName() {
        if (parentScope != null && !parentScope.isEmpty()) {
            return parentScope + "::" + name;
        }
        return name;
    }

    @Override
    public String toString() {
        return name + " [" + type + "] (" + getShortFileName() + ":" + lineNumber + ")";
    }
}
