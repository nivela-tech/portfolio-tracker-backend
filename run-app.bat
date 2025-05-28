@echo off
SETLOCAL EnableDelayedExpansion

:: Display header with clear visual separation
echo ==================================================
echo      Portfolio Tracker - Run Application
echo ==================================================
echo.

:: Database setup information
echo NOTE: Liquibase has been removed from the project.
echo Please ensure the database is set up manually before running the application.
echo See POSTGRES_SETUP.md for detailed instructions.
echo.

:: Set JAVA_HOME to Java 21
echo Setting JAVA_HOME to Java 21...
set JAVA_HOME=C:\Program Files\Java\jdk-21
echo Current JAVA_HOME: %JAVA_HOME%
echo.

:: Display Java version
echo Checking Java version:
echo --------------------------------------------------
"%JAVA_HOME%\bin\java" -version
echo --------------------------------------------------
echo.

:: Run the application
echo Starting application...
echo Use Ctrl+C to stop the application
echo --------------------------------------------------
call gradlew clean build
call gradlew bootRun
echo --------------------------------------------------
echo.

:end
echo Application stopped.
echo ==================================================

ENDLOCAL
