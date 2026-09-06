# SourceVIZ 2.0 (Java Edition)

Модернизированная версия проекта **SourceVIZ**, полностью переписанная на **Java 21 LTS** с расширенным функционалом.

Программа анализирует исходный код проектов на **C/C++** и **Java**, строит графы зависимостей компонентов, генерирует пошаговые **алгоритмические блок-схемы методов и функций** (Control Flow Graphs) и формирует автономный **интерактивный HTML-отчет** для просмотра в браузере без необходимости устанавливать сторонние утилиты (Graphviz и др.).

---

## 🚀 Основные возможности

1. **Двуязычный анализ**:
   - Полная поддержка проектов на **C/C++** (`.cpp`, `.c`, `.h`, `.hpp`, `.cxx`).
   - Полная поддержка проектов на **Java** (`.java`).

2. **Два уровня диаграмм**:
   - **Архитектурный граф зависимостей**: связи между файлами (`#include`, `import`), классами, структурами, наследование (`extends`, `: public`), интерфейсы (`implements`) и вызовы функций.
   - **Алгоритмические блок-схемы (Flowcharts)**: пошаговая логика тела каждой функции/метода:
     - Начало / Конец (`START` / `END` / `RETURN`);
     - Условия ветвления (`if / else if / else`);
     - Циклы (`for`, `while`, `do-while`);
     - Ввод-вывод (`cin`, `cout`, `printf`, `System.out`);
     - Вызовы подпрограмм и вычисление цикломатической сложности кода.

3. **Форматы вывода**:
   - **Интерактивный HTML-отчет (`report.html`)**: удобный веб-дашборд с поиском по функциям, переключением графов и копированием кода в один клик.
   - **Mermaid (.mmd)**: совместим с GitHub Markdown, Notion, Obsidian.
   - **Graphviz (.dot)**: для пользователей, предпочитающих утилиту `dot`.

---

## 🛠 Требования

- **Java 21 LTS** (или выше).
- Внешние библиотеки или установка Graphviz **не требуются** (сборка выполняется стандартным `javac` / `jar`).

---

## ⚡ Быстрый старт (Windows)

### 1. Сборка
Дважды щелкните или запустите в терминале:
```cmd
build.bat
```
Скрипт скомпилирует исходный код и создаст исполняемый `sourceviz.jar`.

### 2. Запуск в интерактивном режиме
```cmd
run.bat
```
Программа запустится в консоли и предложит ввести путь к проекту.

### 3. Запуск с аргументами командной строки
```cmd
# Анализ проекта с указанием пути:
run.bat "C:\Projects\MyProject"

# Анализ с указанием папки вывода и без автоматического открытия браузера:
run.bat "C:\Projects\MyProject" --output "my_reports" --no-open
```

Либо напрямую через `java`:
```cmd
java -jar sourceviz.jar "C:\Projects\MyProject"
```

---

## 📁 Структура проекта

```
sourceviz-java/
├── pom.xml                                   # Конфигурация Maven
├── build.bat                                 # Скрипт быстрой компиляции и сборки JAR
├── run.bat                                   # Скрипт запуска
├── src/main/java/com/sourceviz/
│   ├── Main.java                             # Точка входа CLI
│   ├── model/
│   │   ├── EntityType.java                   # Типы сущностей (CLASS, FUNCTION, INCLUDE...)
│   │   ├── CodeEntity.java                   # Модель сущности кода
│   │   ├── Relationship.java                 # Связи (call, include, import, inheritance...)
│   │   ├── FlowNodeType.java                 # Типы блоков блок-схем (START, PROCESS, CONDITION...)
│   │   ├── FlowNode.java / FlowEdge.java     # Узлы и переходы блок-схем
│   │   └── MethodFlowchart.java              # Модель блок-схемы метода
│   ├── scanner/
│   │   └── ProjectScanner.java               # Рекурсивный поиск файлов, разрешение заголовков
│   ├── parser/
│   │   ├── LanguageParser.java               # Интерфейс анализатора
│   │   ├── CppParser.java                    # Парсер C/C++ файлов
│   │   ├── JavaCodeParser.java               # Парсер Java файлов
│   │   └── ControlFlowAnalyzer.java          # Построитель алгоритмических блок-схем
│   ├── analyzer/
│   │   └── CodeAnalyzer.java                 # Оркестратор анализа и сбора метрик
│   └── exporter/
│       ├── DotExporter.java                  # Экспорт в формат Graphviz DOT
│       ├── MermaidExporter.java              # Экспорт в формат Mermaid.js
│       └── HtmlReportExporter.java           # Генерация интерактивного HTML-отчета
└── src/test/java/com/sourceviz/
    └── SourceVizTest.java                    # Тестовый набор для верификации парсеров и CFG
```

---

## 🧪 Запуск тестов
Для запуска встроенных тестов выполните:
```cmd
javac -encoding UTF-8 -cp bin -d bin src\test\java\com\sourceviz\SourceVizTest.java
java -Dfile.encoding=UTF-8 -cp bin com.sourceviz.SourceVizTest
```
