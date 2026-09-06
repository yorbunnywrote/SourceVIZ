package com.sourceviz.parser;

import com.sourceviz.model.CodeEntity;
import com.sourceviz.model.MethodFlowchart;
import com.sourceviz.model.Relationship;
import com.sourceviz.scanner.ProjectScanner;

import java.nio.file.Path;
import java.util.List;

public interface LanguageParser {
    List<CodeEntity> parseEntities(Path filePath, String content, ProjectScanner scanner);
    List<Relationship> parseRelationships(Path filePath, String content, List<CodeEntity> entities, ProjectScanner scanner);
    List<MethodFlowchart> parseFlowcharts(Path filePath, String content);
}
