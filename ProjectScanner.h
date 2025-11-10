#pragma once
#include <string>
#include<vector>
#include <filesystem> 

class ProjectScanner {
public:
	// Сканирует проект и возвращает все C++ файлы
	std::vector<std::string> scanProject(const std::string& projectPath);

	// Находит заголовочный файл по имени (разрешает #include)
	std::string resolveHeader(const std::string& includingFile, const std::string& headerName);
private:
	void scanRecursive(const std::filesystem::path& path,
		std::vector<std::string>& result);
	   
    std::vector<std::string> allFiles; // все найденные файлы проекта
    std::string projectRoot; // корневая папка проекта
};
