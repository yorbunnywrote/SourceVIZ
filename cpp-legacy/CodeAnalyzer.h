#pragma once
#include <string>
#include <vector>
#include "Entity.h"
#include "ProjectScanner.h"
#include "SimpleParser.h"
#include "GraphGenerator.h"

class CodeAnalyzer {
private:
    // Используем прямые объекты вместо умных указателей
    ProjectScanner projectScanner;
    SimpleParser parser;
    GraphGenerator graphGenerator;

    std::vector<Entity> allEntities;
    std::vector<Relationship> allRelationships;
    std::string projectPath;

public:
    CodeAnalyzer() = default; // Конструктор по умолчанию
    ~CodeAnalyzer() = default;

    bool analyzeProject(const std::string& projectPath);
    void generateDependencyGraphs(const std::string& outputDir = "output");
    void showStatistics() const;

    const std::vector<Entity>& getEntities() const { return allEntities; }
    const std::vector<Relationship>& getRelationships() const { return allRelationships; }

private:
    void collectAllData();
};