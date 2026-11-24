@echo off
setlocal

rem T3 on this Windows host inherits a JAVA_HOME pointing to JDK 11.
if exist "%ProgramFiles%\Java\jdk-21\bin\javac.exe" (
    set "JAVA_HOME=%ProgramFiles%\Java\jdk-21"
)

pushd "%~dp0" || exit /b 1
call "%~dp0gradlew.bat" :gallery:run %*
set "GRADLE_EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %GRADLE_EXIT_CODE%
