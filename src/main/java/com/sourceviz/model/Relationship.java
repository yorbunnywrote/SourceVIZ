package com.sourceviz.model;

import java.util.Objects;

public class Relationship {
    private String fromEntity;
    private String toEntity;
    private String type; // include, import, call, inheritance, implements
    private String fromFile;
    private String toFile;

    public Relationship() {}

    public Relationship(String fromEntity, String toEntity, String type, String fromFile, String toFile) {
        this.fromEntity = fromEntity != null ? fromEntity : "";
        this.toEntity = toEntity != null ? toEntity : "";
        this.type = type != null ? type : "";
        this.fromFile = fromFile != null ? fromFile : "";
        this.toFile = toFile != null ? toFile : "";
    }

    public String getFromEntity() { return fromEntity; }
    public void setFromEntity(String fromEntity) { this.fromEntity = fromEntity; }

    public String getToEntity() { return toEntity; }
    public void setToEntity(String toEntity) { this.toEntity = toEntity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFromFile() { return fromFile; }
    public void setFromFile(String fromFile) { this.fromFile = fromFile; }

    public String getToFile() { return toFile; }
    public void setToFile(String toFile) { this.toFile = toFile; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relationship that)) return false;
        return Objects.equals(fromEntity, that.fromEntity) &&
               Objects.equals(toEntity, that.toEntity) &&
               Objects.equals(type, that.type) &&
               Objects.equals(fromFile, that.fromFile) &&
               Objects.equals(toFile, that.toFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromEntity, toEntity, type, fromFile, toFile);
    }

    @Override
    public String toString() {
        return fromEntity + " -> " + toEntity + " [" + type + "]";
    }
}
