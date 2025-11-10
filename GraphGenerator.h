#pragma once
#include <string>
#include <vector>
#include "Entity.h"

class GraphGenerator {
public:
    std::string generateDot(const std::vector<Entity>& entities, const std::vector<Relationship>& relationships);
    std::string FileDependencyGenerator(const std::vector<Relationship>& relationships);
    bool saveDotToFile(const std::string& dotContent, const std::string& fileName);
    bool saveDotToPng(const std::string& dotFile, const std::string& pngFile);

private:
    // Inline реализации
    std::string getEntityColor(EntityType type) {
        switch (type) {
        case EntityType::CLASS: return "darkgreen";
        case EntityType::FUNCTION: return "darkblue";
        case EntityType::STRUCT: return "darkred";
        case EntityType::INCLUDE: return "orange";
        case EntityType::VARIABLE: return "gray";
        default: return "black";
        }
    }

    std::string getEntityShape(EntityType type) {
        switch (type) {
        case EntityType::CLASS: return "ellipse";
        case EntityType::FUNCTION: return "box";
        case EntityType::STRUCT: return "diamond";
        case EntityType::INCLUDE: return "folder";
        case EntityType::VARIABLE: return "note";
        default: return "oval";
        }
    }

    std::string escapeDotString(const std::string& str) {
        std::string r;
        r.reserve(str.size() * 2);
        for (unsigned char uc : str) {
            char c = static_cast<char>(uc);
            if (c == '"') { r += "\\\""; }
            else if (c == '\n' || c == '\r') { r += ' '; }
            else r += c;
        }
        return r;
    }
};