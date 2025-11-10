#include <iostream>
#include <filesystem>
#include "CodeAnalyzer.h"

namespace fs = std::filesystem;

std::string getUserProject() {
    std::string path;

    while (true) {
        std::cout << "Enter project path ";
        std::getline(std::cin, path);

        // Убираем кавычки если пользователь их ввел
        if (!path.empty() && path.front() == '"' && path.back() == '"') {
            path = path.substr(1, path.length() - 2);
        }

        if (fs::exists(path) && fs::is_directory(path)) {
            return path;
        }
        else {
            std::cout << "Error: Path " <<path << "' does not exist or is not a directory." << std::endl;
            std::cout << "Please try again." << std::endl;
        }

    }
}

int main() {
    std::cout << "=== SourceVIZ - C++  Code Analyzer ===" << std::endl;
    std::string projectPath = getUserProject();
    std::cout << "Analyzing project: " << projectPath << std::endl;

    CodeAnalyzer analyzer;

    if (analyzer.analyzeProject(projectPath)) {
        // Генерируем графы
        analyzer.generateDependencyGraphs();

        std::cout << "\n=== NEXT STEPS ===" << std::endl;
        std::cout << "1. Open 'output/full_graph.png' to see the complete dependency graph" << std::endl;
        std::cout << "2. Open 'output/file_dependencies.png' to see file relationships" << std::endl;
        std::cout << "3. If PNG files are not generated, install Graphviz and run again" << std::endl;
    }
    else {
        std::cerr << "Project analysis failed!" << std::endl;
    }

    std::cout << "\nPress Enter to exit...";
    std::cin.get();

    return 0;
}