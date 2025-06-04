:: Gradle Wrapper Execution Script for Windows
:: Maintains original behavior with improved structure

@echo off
setlocal enabledelayedexpansion

:: Initialize core settings
set SCRIPT_DIR=%~dp0
if "!SCRIPT_DIR!"=="" set SCRIPT_DIR=.
set APP_NAME=%~n0
set GRADLE_HOME=%SCRIPT_DIR%

:: Normalize path (remove . and ..)
for %%i in ("%GRADLE_HOME%") do set GRADLE_HOME=%%~fi

:: Configure default JVM options
set JVM_DEFAULT_OPTS="-Xmx64m" "-Xms64m"

:: Locate Java runtime
call :locate_java_runtime
if !ERRORLEVEL! neq 0 exit /b !ERRORLEVEL!

:: Prepare execution environment
set WRAPPER_JAR=%GRADLE_HOME%\gradle\wrapper\gradle-wrapper.jar

:: Execute Gradle with configured settings
"%JAVA_BIN%" %JVM_DEFAULT_OPTS% %JAVA_OPTS% %GRADLE_OPTS% ^
    "-Dorg.gradle.appname=%APP_NAME%" ^
    -classpath "%WRAPPER_JAR%" ^
    org.gradle.wrapper.GradleWrapperMain %*

:: Store return code for proper exit
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1

:: Clean up and exit
endlocal & exit /b %EXIT_CODE%

:: --------------------------------------------------
:: Subroutines
:: --------------------------------------------------

:locate_java_runtime
    if not defined JAVA_HOME goto check_system_java
    
    :: Try JAVA_HOME first
    set JAVA_HOME=%JAVA_HOME:"=%
    set JAVA_BIN="%JAVA_HOME%\bin\java.exe"
    
    if exist %JAVA_BIN% (
        %JAVA_BIN% -version >nul 2>&1
        goto :eof
    )
    
    echo. 1>&2
    echo ERROR: Invalid JAVA_HOME: %JAVA_HOME% 1>&2
    echo. 1>&2
    goto java_not_found

:check_system_java
    set JAVA_BIN=java.exe
    %JAVA_BIN% -version >nul 2>&1
    if %ERRORLEVEL% equ 0 goto :eof
    
    echo. 1>&2
    echo ERROR: Java not found in system PATH 1>&2
    echo. 1>&2

:java_not_found
    echo Please set JAVA_HOME to your Java installation directory 1>&2
    exit /b 1
