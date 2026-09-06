// GraphGenerator.cpp - Улучшенная генерация DOT (кластер по файлам, метки, цвета)
#include "GraphGenerator.h"
#include <sstream>
#include <fstream>
#include <algorithm>
#include <iostream>
#include <cstdlib>
#include <map>
#include <set>
#include <unordered_map>
#include <unordered_set>
#include <filesystem>
#include <functional>

static std::string escapeDot(const std::string& s) {
    std::string r;
    r.reserve(s.size() * 2);
    for (unsigned char uc : s) {
        char c = static_cast<char>(uc);
        if (c == '"') { r += "\\\""; }
        else if (c == '\n' || c == '\r') { r += ' '; }
        else r += c;
    }
    return r;
}

static std::string makeId(const std::string& s) {
    // Уникальный, безопасный для DOT id: заменяем не-алфанум на '_'
    std::string out;
    out.reserve(s.size());
    for (unsigned char uc : s) {
        char c = static_cast<char>(uc);
        if (std::isalnum(static_cast<unsigned char>(c))) out.push_back(c);
        else out.push_back('_');
    }
    return out;
}

namespace fs = std::filesystem;

std::string GraphGenerator::generateDot(const std::vector<Entity>& entities, const std::vector<Relationship>& relationships) {
    std::ostringstream out;
    out << "digraph G {\n";
    out << "  rankdir=LR;\n";
    out << "  node [fontname=\"Arial\"];\n\n";

    // group entities by file (use full path as key)
    std::map<std::string, std::vector<const Entity*>> byFile;
    for (const auto& e : entities) {
        byFile[e.getSourceFile()].push_back(&e);
    }

    // map from pair(file, entityName) -> nodeId
    std::unordered_map<std::string, std::string> nodeMap; // key = file + '|' + name
    std::unordered_set<std::string> addedFileNodes;

    int clusterId = 0;
    for (auto& kv : byFile) {
        const std::string& fullPath = kv.first;
        std::string clusterName = "cluster_" + makeId(fullPath + std::to_string(clusterId++));
        std::string shortFile = fs::path(fullPath).filename().string();
        out << "  subgraph \"" << clusterName << "\" {\n";
        out << "    label = \"" << escapeDot(shortFile) << "\";\n";
        out << "    style=filled; color=lightgrey; node [style=filled, fillcolor=white];\n";

        for (auto* pe : kv.second) {
            // unique id based on fullPath + entity name
            std::string key = pe->getSourceFile() + "|" + pe->getName();
            std::string nodeId = makeId(pe->getSourceFile() + ":" + pe->getName());
            std::string shortLabel = escapeDot(pe->getDisplayName());
            std::string fileLine = fs::path(pe->getSourceFile()).filename().string() + ":" + std::to_string(pe->getLineNumber());

            // node shape/color use getters
            out << "    \"" << nodeId << "\" [label=\"" << shortLabel << "\\n(" << escapeDot(fileLine) << ")\""
                << " shape=" << getEntityShape(pe->getType())
                << " color=" << getEntityColor(pe->getType())
                << "];\n";

            nodeMap.emplace(key, nodeId);
        }

        out << "  }\n\n";
    }

    // edges (relationships)
    out << "  // edges\n";
    // We'll create file-level nodes on demand (short name as label), id = makeId(file)
    for (const auto& r : relationships) {
        // Relationship fields in project: fromFile, fromEntity, toFile, toEntity, type
        std::string fromKey = r.fromFile + "|" + r.fromEntity;
        std::string toKey   = r.toFile   + "|" + r.toEntity;

        std::string fromNodeId;
        std::string toNodeId;

        auto fit = nodeMap.find(fromKey);
        if (fit != nodeMap.end()) {
            fromNodeId = fit->second;
        } else {
            // fallback to file node
            fromNodeId = makeId(r.fromFile);
            if (!addedFileNodes.count(fromNodeId)) {
                out << "  \"" << fromNodeId << "\" [label=\"" << escapeDot(fs::path(r.fromFile).filename().string())
                    << "\", shape=folder, fillcolor=lightyellow, style=filled];\n";
                addedFileNodes.insert(fromNodeId);
            }
        }

        auto tit = nodeMap.find(toKey);
        if (tit != nodeMap.end()) {
            toNodeId = tit->second;
        } else {
            toNodeId = makeId(r.toFile);
            if (!addedFileNodes.count(toNodeId)) {
                out << "  \"" << toNodeId << "\" [label=\"" << escapeDot(fs::path(r.toFile).filename().string())
                    << "\", shape=folder, fillcolor=lightyellow, style=filled];\n";
                addedFileNodes.insert(toNodeId);
            }
        }

        std::string label = escapeDot(r.type);
        std::string style = "solid";
        if (r.type == "include") style = "dashed";
        else if (r.type == "call") style = "dotted";
        else if (r.type == "inheritance") style = "bold";

        out << "  \"" << fromNodeId << "\" -> \"" << toNodeId << "\" [label=\"" << label << "\", style=" << style << "];\n";
    }

    out << "}\n";
    return out.str();
}

std::string GraphGenerator::FileDependencyGenerator(const std::vector<Relationship>& relationships) {
    // build unique edges between files
    std::set<std::pair<std::string, std::string>> edges;
    for (const auto& r : relationships) {
        if (r.fromFile != r.toFile) edges.insert({ r.fromFile, r.toFile });
    }

    std::ostringstream out;
    out << "digraph Files {\n  rankdir=LR;\n  node [shape=folder,fontname=\"Arial\"];\n";
    for (const auto& e : edges) {
        out << "  \"" << escapeDot(fs::path(e.first).filename().string()) << "\" -> \"" << escapeDot(fs::path(e.second).filename().string()) << "\";\n";
    }
    out << "}\n";
    return out.str();
}

bool GraphGenerator::saveDotToFile(const std::string& dotContent, const std::string& fileName) {
    std::ofstream out(fileName);
    if (!out.is_open()) return false;
    out << dotContent;
    out.close();
    return true;
}

bool GraphGenerator::saveDotToPng(const std::string& dotFile, const std::string& pngFile) {
    // проверим доступность dot
    std::string cmd = "dot -Tpng \"" + dotFile + "\" -o \"" + pngFile + "\"";
    int rc = std::system(cmd.c_str());
    return (rc == 0);
}
