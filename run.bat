@echo off
setlocal
chcp 65001 >nul

if not exist bin\com\sourceviz\Main.class (
    echo [SourceVIZ] Binary not found. Running build.bat first...
    call build.bat
)

if exist sourceviz.jar (
    java -Dfile.encoding=UTF-8 -jar sourceviz.jar %*
) else (
    java -Dfile.encoding=UTF-8 -cp bin com.sourceviz.Main %*
)
