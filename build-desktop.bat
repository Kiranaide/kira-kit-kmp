@echo off
setlocal

rem T3 on this Windows host inherits a JAVA_HOME pointing to JDK 11.
if exist "%ProgramFiles%\Java\jdk-21\bin\javac.exe" (
    set "JAVA_HOME=%ProgramFiles%\Java\jdk-21"
) else if exist "%ProgramFiles%\Java\jdk-17\bin\javac.exe" (
    set "JAVA_HOME=%ProgramFiles%\Java\jdk-17"
)

set "TASK=:gallery:createDistributable"
set "OUT_DIR=gallery\build\compose\binaries\main\app\KiraKitGallery"
if /I "%~1"=="--installer" (
    set "TASK=:gallery:packageDistributionForCurrentOS"
    set "OUT_DIR=gallery\build\compose\binaries\main\msi"
    shift
)

pushd "%~dp0" || exit /b 1
call "%~dp0gradlew.bat" %TASK% %*
set "GRADLE_EXIT_CODE=%ERRORLEVEL%"
if %GRADLE_EXIT_CODE% equ 0 (
    echo.
    echo Desktop build ready: %~dp0%OUT_DIR%
)
popd
exit /b %GRADLE_EXIT_CODE%
