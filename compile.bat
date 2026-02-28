@echo off
REM Compilation script for Connect Event Management System - Deliverable 1

REM Set JavaFX SDK path here (adjust if your JavaFX is in a different location)
set JAVAFX_PATH=E:\Softwares\Coding Softwares\openjfx-25.0.1_windows-x64_bin-sdk\javafx-sdk-25.0.1

REM Check if JavaFX exists
if not exist "%JAVAFX_PATH%\lib" (
    echo.
    echo ERROR: JavaFX SDK not found at %JAVAFX_PATH%
    echo Please download JavaFX SDK from: https://gluonhq.com/products/javafx/
    echo Extract it and update JAVAFX_PATH in this script
    echo.
    pause
    exit /b 1
)

REM Create output directory
if not exist "out" mkdir out

echo.
echo ============================================================
echo Compiling Connect Event Management System - Spring 1 (D1)
echo ============================================================
echo.

REM Compile all Java files
javac --module-path "%JAVAFX_PATH%\lib" ^
      --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
      -cp "." ^
      -d out ^
      src\com\connect\Main.java ^
      src\com\connect\controller\*.java ^
      src\com\connect\enums\*.java ^
      src\com\connect\model\*.java ^
      src\com\connect\repository\*.java ^
      src\com\connect\service\*.java ^
      src\com\connect\ui\*.java ^
      src\com\connect\util\*.java ^
      2>&1

if %ERRORLEVEL% neq 0 (
    echo.
    echo COMPILATION FAILED! Check errors above.
    echo.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo COMPILATION SUCCESSFUL!
echo ============================================================
echo.
echo To run the application, execute: run.bat
echo.
pause
