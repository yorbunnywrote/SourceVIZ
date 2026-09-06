#include "SimpleParser.h" 
#include "ProjectScanner.h"
#include <fstream>   
#include <regex>     
#include <iostream>  

std::vector<CodeEntity> SimpleParser::parserFile(const std::string& filePath) {
	// Создаем вектор (список) для хранения найденных сущностей
	std::vector<CodeEntity> entities;

	// Создаем объект для чтения файла. ifstream = input file stream
	std::ifstream file(filePath);

	if (!file.is_open()) {
		std::cerr << "Error: Cannot open file " << filePath << std::endl;
		return entities;
	}

	std::string line; // переменная для хранения одной строки файла


	int lineNumber = 0; // счетчик строк (чтобы знать, в какой строке нашли сущность)

	// Читаем файл построчно. getline читает одну строку и сохраняет в переменную 'line'
	while (std::getline(file, line)) {
		lineNumber++; 
		
		   //Ищем классы и убираем лишние пробелы для упрощения анализа
		std::string cleanLine = line;
		cleanLine.erase(0, cleanLine.find_first_not_of(" \t")); // убираем пробелы слева

		// ========== ПОИСК #INCLUDE ДИРЕКТИВ ==========
		if (cleanLine.find("#include") == 0) {
			processInclude(line, filePath, lineNumber, entities);
		}


		std::regex classRegex(R"(class\s+(\w+))");
		std::smatch match;

		// Ищем совпадение с шаблоном класса в текущей строке
		if (std::regex_search(line, match, classRegex)) {

			CodeEntity entity;
			entity.name = match[1]; // match[0] - вся найденная строка, 
			// match[1] - то что в первых скобках (имя класса)
			entity.type = "Class";
			entity.file = filePath; // Запоминаем в каком файле нашли
			entity.line = lineNumber; // в какой строке
			entities.push_back(entity);
		}


		std::regex functionRegex(R"((\w+)\s+(\w+)\s*\([^)]*\)\s*\{)");
			if (std::regex_search(line, match, functionRegex)) {
				CodeEntity entity;
				entity.name = match[1];
				entity.type = "function";
				entity.file = filePath;
				entity.line = lineNumber;
				entities.push_back(entity);
			}
		
}
	file.close();
return entities;
}

void SimpleParser::processInclude(const std::string& line, const std::string& filePath,
    int lineNumber, std::vector<CodeEntity>& entities) {
   
	std::regex includeLocalRegex("#include\\s+\"([^\"]+)\"");  // #include "file.h"
        std::regex includeSystemRegex(R"(#include\s+<([^>]+)>)");   // #include <file>

    std::smatch match;
    std::string headerName;
    std::string includeType;

    if (std::regex_search(line, match, includeLocalRegex)) {
        headerName = match[1];
        includeType = "local";
    }
    else if (std::regex_search(line, match, includeSystemRegex)) {
        headerName = match[1];
        includeType = "system";
    }
    else {
        return; // не распознали include
    }

    // Создаем сущность для include
    CodeEntity entity;
    entity.name = headerName;
    entity.type = "include";
    entity.file = filePath;
    entity.line = lineNumber;

    // Пытаемся найти реальный файл
    if (projectScanner && includeType == "local") {
        entity.targetFile = projectScanner->resolveHeader(filePath, headerName);
    }
    else {
        entity.targetFile = headerName; // для системных заголовков оставляем как есть
    }

    entities.push_back(entity);

    // Сохраняем зависимость между файлами
    FileDependency dependency;
    dependency.fromFile = filePath;
    dependency.toFile = entity.targetFile;
    dependency.includeType = includeType;
    dependencies.push_back(dependency);

    std::cout << "Found " << includeType << " include: " << headerName
        << " -> " << entity.targetFile << std::endl;
}