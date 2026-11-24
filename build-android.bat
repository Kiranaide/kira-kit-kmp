@echo off
setlocal

rem T3 on this Windows host inherits a JAVA_HOME pointing to JDK 11.
if exist "%ProgramFiles%\Java\jdk-21\bin\javac.exe" (
    set "JAVA_HOME=%ProgramFiles%\Java\jdk-21"
) else if exist "%ProgramFiles%\Java\jdk-17\bin\javac.exe" (
    set "JAVA_HOME=%ProgramFiles%\Java\jdk-17"
)

set "TASK=:androidApp:assembleDebug"
set "APK_OUT=androidApp\build\outputs\apk\debug\androidApp-debug.apk"
if /I "%~1"=="--release" (
    set "TASK=:androidApp:assembleRelease"
    set "APK_OUT=androidApp\build\outputs\apk\release\androidApp-release.apk"
    shift
) else if /I "%~1"=="--benchmark" (
    set "TASK=:androidApp:assembleBenchmark"
    set "APK_OUT=androidApp\build\outputs\apk\benchmark\androidApp-benchmark.apk"
    shift
)

pushd "%~dp0" || exit /b 1
call "%~dp0gradlew.bat" %TASK% %*
set "GRADLE_EXIT_CODE=%ERRORLEVEL%"
if %GRADLE_EXIT_CODE% equ 0 (
    echo.
    echo Android APK ready: %~dp0%APK_OUT%
)
popd
exit /b %GRADLE_EXIT_CODE%
