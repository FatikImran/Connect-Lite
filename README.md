# Connect Lite (Scope-Locked)

A JavaFX-based event management application for Assignment 1.

## Project Structure

- `src/` - Java source code and SQL scripts
- `resources/fxml/` - UI screens (FXML)
- `resources/css/` - UI theme styles
- `out/` - compiled classes (generated after build)
- `compile.bat` - compile script
- `run.bat` - run script

## Prerequisites

1. **Java JDK 25** (or compatible JDK with JavaFX 25 setup)
2. **JavaFX SDK** installed locally
3. **MySQL server** running and accessible
4. `mysql-connector-j-8.0.33.jar` (already included in this folder)

## JavaFX Path Configuration

Both scripts use this JavaFX path:

`E:\Softwares\Coding Softwares\openjfx-25.0.1_windows-x64_bin-sdk\javafx-sdk-25.0.1`

If your path is different, update `JAVAFX_PATH` in:

- `compile.bat`
- `run.bat`

## Database Configuration

Database connection is configured in:

- `src/com/connect/util/DatabaseConnection.java`

Current host:

- `jdbc:mysql://192.168.100.93:3306/connect`
- user: `root`
- password: empty string

Make sure MySQL is reachable at that IP and the `connect` database exists.

## How to Run

From this folder (`updated code (scope locked)`):

1. Compile:
   - `./compile.bat`
2. Run:
   - `./run.bat`

On Windows PowerShell, use:

- `./compile.bat`
- `./run.bat`

## Notes

- Do not commit `out/` (build output).
- UI resources in this scope-locked version were aligned to the old project’s selected screens while keeping current logic intact.
