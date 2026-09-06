package com.sourceviz.model;

public enum EntityType {
    CLASS,
    INTERFACE,
    STRUCT,
    RECORD,
    FUNCTION,
    METHOD,
    INCLUDE,
    IMPORT,
    VARIABLE,
    UNKNOWN;

    public String getDisplayName() {
        return name().toLowerCase();
    }
}
