@echo off
REM Run script for Connect Event Management System - Deliverable 1

REM Set JavaFX SDK path here (must match compile.bat)
set JAVAFX_PATH=E:\Softwares\Coding Softwares\openjfx-25.0.1_windows-x64_bin-sdk\javafx-sdk-25.0.1

REM Check if compilation was done
if not exist "out\com\connect\Main.class" (
    echo.
    echo ERROR: Application not compiled!
    echo Please run compile.bat first to compile the code.
    echo.
    pause
    exit /b 1
)

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

echo.
echo ============================================================
echo Running Connect Event Management System - Spring 1 (D1)
echo ============================================================
echo.

REM Run the application
java --module-path "%JAVAFX_PATH%\lib" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
    -cp ".;out;resources;mysql-connector-j-8.0.33.jar" ^
     com.connect.Main

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Application execution failed!
    echo.
)

pause
