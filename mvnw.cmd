@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script for Windows
@REM This script allows running Maven via the Maven Wrapper.
@REM It will download the Maven Wrapper JAR if necessary, then use it to
@REM download and run the configured Maven distribution.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Copyright 2014-2024 The Apache Software Foundation
@REM Licensed under the Apache License, Version 2.0
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set "BASEDIR=%~dp0"
if "%BASEDIR%" == "" set "BASEDIR=."

set "WRAPPER_DIR=%BASEDIR%.mvn\wrapper"
set "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"
set "WRAPPER_PROPS=%WRAPPER_DIR%\maven-wrapper.properties"

@REM Locate Java executable
if not "%JAVA_HOME%" == "" (
  if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVACMD=%JAVA_HOME%\bin\java.exe"
  ) else (
    echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
    exit /b 1
  )
) else (
  for %%j in (java.exe) do (
    set "JAVACMD=%%~$PATH:j"
  )
)
if "%JAVACMD%" == "" (
  echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
  exit /b 1
)

@REM Function-like label to download a file using curl or powershell
:download
set "URL=%~1"
set "DEST=%~2"
where curl >nul 2>nul
if %ERRORLEVEL%==0 (
  curl -fsSL -o "%DEST%" "%URL%"
  if %ERRORLEVEL%==0 exit /b 0
)
@REM Fallback to PowerShell
powershell -NoProfile -Command "try { Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile '%DEST%'; exit 0 } catch { exit 1 }"
exit /b %ERRORLEVEL%

@if not exist "%WRAPPER_JAR%" (
  if not exist "%WRAPPER_PROPS%" (
    echo ERROR: %WRAPPER_PROPS% not found. Please check your project setup. 1>&2
    exit /b 1
  )
  for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPS%") do (
    if "%%A"=="wrapperUrl" set "WRAPPER_URL=%%B"
  )
  if "%WRAPPER_URL%"=="" (
    echo ERROR: wrapperUrl not set in %WRAPPER_PROPS% 1>&2
    exit /b 1
  )
  if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
  echo Downloading Maven Wrapper from: %WRAPPER_URL%
  call :download "%WRAPPER_URL%" "%WRAPPER_JAR%"
  if not "%ERRORLEVEL%"=="0" (
    echo ERROR: Failed to download %WRAPPER_URL% 1>&2
    exit /b 1
  )
)

@REM Execute Maven Wrapper
set CLASSPATH=%WRAPPER_JAR%
"%JAVACMD%" %MAVEN_OPTS% -classpath "%CLASSPATH%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
