#pragma once
#include <string>
#include <vector>

enum class EntityType {
    CLASS,
    FUNCTION,
    STRUCT,
    VARIABLE,
    INCLUDE,
    NAMESPACE,
    UNKNOWN
};

struct Relationship {
    std::string fromEntity; // от кого связь
    std::string toEntity;   // к кому связь  
    std::string type;       // тип связи: "include", "call", "inheritance"
    std::string fromFile;   // файл источника
    std::string toFile;     // файл цели
};

class Entity {
protected:
    int id;
    std::string name;
    EntityType type;
    std::string sourceFile;
    int lineNumber;
    std::string targetFile; // для include: какой файл включается

public:
    // Конструктор
    Entity(const std::string& name, EntityType type,
        const std::string& file, int line)
        : name(name), type(type), sourceFile(file), lineNumber(line), id(0) {
    }

    virtual ~Entity() = default;

    // Геттеры
    int getId() const { return id; }
    std::string getName() const { return name; }
    EntityType getType() const { return type; }
    std::string getSourceFile() const { return sourceFile; }
    int getLineNumber() const { return lineNumber; }
    std::string getTargetFile() const { return targetFile; }

    // Сеттеры
    void setId(int newId) { id = newId; }
    void setTargetFile(const std::string& target) { targetFile = target; }

    std::string toString() const {
        return name + " (" + sourceFile + ":" + std::to_string(lineNumber) + ")";
    }

    std::string getDisplayName() const {
        return name;
    }
};