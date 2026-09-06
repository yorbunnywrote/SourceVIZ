@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo [SourceVIZ] Building SourceVIZ 2.0 (Java 21)...

if not exist bin mkdir bin

dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% equ 0 (
    echo [SourceVIZ] Compilation successful!
    if exist "C:\Program Files\Java\jdk-21\bin\jar.exe" (
        "C:\Program Files\Java\jdk-21\bin\jar.exe" cfe sourceviz.jar com.sourceviz.Main -C bin .
        echo [SourceVIZ] Generated sourceviz.jar successfully!
    )
) else (
    echo [SourceVIZ] Compilation failed!
    exit /b 1
)
