@echo off
SETLOCAL EnableDelayedExpansion

:: Check for profile argument
set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=local

:: Display header with clear visual separation
echo ==================================================
echo      Portfolio Tracker - Run Application
echo      Profile: %PROFILE%
echo ==================================================
echo.

:: Database setup information
echo NOTE: Liquibase has been removed from the project.
echo Please ensure the database is set up manually before running the application.
echo See POSTGRES_SETUP.md for detailed instructions.
echo.
echo USAGE: run-app.bat [profile]
echo   profile - Spring profile to use (default: local)
echo   Examples:
echo     run-app.bat          (uses 'local' profile)
echo     run-app.bat prod     (uses 'prod' profile)
echo     run-app.bat dev      (uses 'dev' profile)
echo.

:: Set JAVA_HOME to Java 21
echo Setting JAVA_HOME to Java 21...
set JAVA_HOME=C:\Program Files\Java\jdk-21
echo Current JAVA_HOME: %JAVA_HOME%
echo.

:: Set Spring Profile to local for development
echo Setting Spring Profile to '%PROFILE%'...
set SPRING_PROFILES_ACTIVE=%PROFILE%
echo Current Spring Profile: %SPRING_PROFILES_ACTIVE%
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
