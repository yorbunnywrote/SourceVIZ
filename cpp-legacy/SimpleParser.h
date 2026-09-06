#pragma once
#include <string>
#include<vector>
#include <unordered_map>

struct CodeEntity {
	std::string name;
	std::string file;
	std::string type; // "class", "function", "include", "variable"
	int line;
	std::string targetFile; // для include: какой файл включается
};

struct FileDependency {
	std::string fromFile;  // какой файл включает
	std::string toFile; // какой файл включается
	std::string includeType; // "system" или "local"
};

class SimpleParser {
public:
	SimpleParser() = default;

	// Парсит файл и возвращает найденные сущности
	std::vector<CodeEntity> parserFile(const std::string& FilePath);

	//Получить зависимости между файлами 
	const std::vector<FileDependency>& getDependency() const { return dependencies; }

	void setProjectScanner(class ProjectScanner* scanner) { projectScanner = scanner; }

private:
	std::vector<FileDependency> dependencies;
	class ProjectScanner* projectScanner = nullptr;

	// Обрабатывает #include директиву
	void processInclude(const std::string& line, const std::string& filePath, int lineNumber, std::vector<CodeEntity>& entities);
};
