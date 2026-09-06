// ProjectScanner.cpp - улучшенна€ реализаци€ сканировани€ и resolveHeader
#include "ProjectScanner.h"
#include <filesystem>
#include <set>
#include <iostream>


namespace fs = std::filesystem;

std::vector<std::string> ProjectScanner::scanProject(const std::string& projectPath) {
    std::vector<std::string> result;
    std::set<std::string> exts = { ".cpp", ".c", ".cc", ".cxx", ".h", ".hpp" };

    try {
        fs::path root(projectPath);
        if (!fs::exists(root)) {
            std::cerr << "ProjectScanner: path not found: " << projectPath << std::endl;
            return result;
        }

        for (auto& p : fs::recursive_directory_iterator(root)) {
            if (!p.is_regular_file()) continue;
            auto e = p.path().extension().string();
            std::transform(e.begin(), e.end(), e.begin(), ::tolower);
            if (exts.count(e)) {
                result.push_back(p.path().string());
            }
        }
    }
    catch (std::exception& ex) {
        std::cerr << "ProjectScanner error: " << ex.what() << std::endl;
    }
    return result;
}

std::string ProjectScanner::resolveHeader(const std::string& includingFile, const std::string& headerName) {
    // headerName could be "file.h" or <file.h> or relative path
    std::string cleaned = headerName;
    // remove angle brackets or quotes
    if (!cleaned.empty() && (cleaned.front() == '<' || cleaned.front() == '\"')) cleaned.erase(0, 1);
    if (!cleaned.empty() && (cleaned.back() == '>' || cleaned.back() == '\"')) cleaned.pop_back();

    fs::path inclPath = fs::path(includingFile).parent_path() / cleaned;
    if (fs::exists(inclPath)) return fs::canonical(inclPath).string();

    // try relative to project root(s) Ч if you have additional include directories, add here
    // fallback: return cleaned as-is to indicate unresolved external include
    return cleaned;
}
