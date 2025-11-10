#include "CodeAnalyzer.h"
#include <iostream>
#include <filesystem>
#include <unordered_map>

namespace fs = std::filesystem;

bool CodeAnalyzer::analyzeProject(const std::string& projectPath) {
    this->projectPath = projectPath;
    allEntities.clear();
    allRelationships.clear();

    try {
        std::cout << "Step 1: Scanning project for files..." << std::endl;
        auto files = projectScanner.scanProject(projectPath); // Используем . вместо ->

        if (files.empty()) {
            std::cerr << "No C++ files found in project." << std::endl;
            return false;
        }

        std::cout << "Step 2: Parsing " << files.size() << " files..." << std::endl;
        parser.setProjectScanner(&projectScanner); // Передаем указатель на сканер

        for (const auto& file : files) {
            auto entities = parser.parserFile(file);

            // Не вставляем CodeEntity напрямую в vector<Entity>.
            // Преобразуем CodeEntity -> Entity и затем добавляем в allEntities.
            for (const auto& ce : entities) {
                EntityType et = EntityType::UNKNOWN;
                if (ce.type == "class")        et = EntityType::CLASS;
                else if (ce.type == "function") et = EntityType::FUNCTION;
                else if (ce.type == "struct")   et = EntityType::STRUCT;
                else if (ce.type == "include")  et = EntityType::INCLUDE;
                else if (ce.type == "variable") et = EntityType::VARIABLE;

                Entity e(ce.name, et, ce.file, ce.line);
                e.setTargetFile(ce.targetFile);
                allEntities.push_back(std::move(e));
            }
        }

        // Временно создаем тестовые зависимости для демонстрации
        if (allRelationships.empty() && !files.empty()) {
            // Создаем несколько тестовых зависимостей между файлами
            for (size_t i = 0; i < files.size() && i < 3; ++i) {
                for (size_t j = 0; j < files.size() && j < 3; ++j) {
                    if (i != j) {
                        Relationship rel;
                        rel.fromFile = files[i];
                        rel.toFile = files[j];
                        rel.type = "include";
                        rel.fromEntity = "File";
                        rel.toEntity = "File";
                        allRelationships.push_back(rel);
                    }
                }
            }
        }

        std::cout << "Step 3: Analysis complete!" << std::endl;
        showStatistics();

        return true;

    }
    catch (const std::exception& e) {
        std::cerr << "Analysis failed: " << e.what() << std::endl;
        return false;
    }
}

void CodeAnalyzer::generateDependencyGraphs(const std::string& outputDir) {
    // Создаем выходную директорию
    fs::create_directories(outputDir);

    std::cout << "Generating dependency graphs..." << std::endl;

    // Генерируем полный граф
    std::string fullGraphDot = graphGenerator.generateDot(allEntities, allRelationships);
    std::string fullGraphFile = outputDir + "/full_graph.dot";
    graphGenerator.saveDotToFile(fullGraphDot, fullGraphFile);

    // Генерируем граф файловых зависимостей
    std::string fileGraphDot = graphGenerator.FileDependencyGenerator(allRelationships);
    std::string fileGraphFile = outputDir + "/file_dependencies.dot";
    graphGenerator.saveDotToFile(fileGraphDot, fileGraphFile);

    // Пытаемся сгенерировать PNG
    graphGenerator.saveDotToPng(fullGraphFile, outputDir + "/full_graph.png");
    graphGenerator.saveDotToPng(fileGraphFile, outputDir + "/file_dependencies.png");

    std::cout << "Graphs saved to: " << outputDir << std::endl;
}

void CodeAnalyzer::showStatistics() const {
    std::cout << "\n=== ANALYSIS STATISTICS ===" << std::endl;
    std::cout << "Entities found: " << allEntities.size() << std::endl;
    std::cout << "Relationships found: " << allRelationships.size() << std::endl;

    // Подсчет по типам
    std::unordered_map<std::string, int> typeCount;
    for (const auto& entity : allEntities) {
        std::string typeStr;
        switch (entity.getType()) {
        case EntityType::CLASS: typeStr = "classes"; break;
        case EntityType::FUNCTION: typeStr = "functions"; break;
        case EntityType::STRUCT: typeStr = "structs"; break;
        case EntityType::INCLUDE: typeStr = "includes"; break;
        default: typeStr = "other";
        }
        typeCount[typeStr]++;
    }

    for (const auto& count : typeCount) {
        std::cout << "  " << count.first << ": " << count.second << std::endl;
    }
}

void CodeAnalyzer::collectAllData() {
    // TODO: Реализовать сбор всех данных из парсера
}