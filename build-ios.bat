@echo off
setlocal

rem T3 on this Windows host inherits a JAVA_HOME pointing to JDK 11.
if exist "%ProgramFiles%\Java\jdk-21\bin\javac.exe" (
    set "JAVA_HOME=%ProgramFiles%\Java\jdk-21"
) else if exist "%ProgramFiles%\Java\jdk-17\bin\javac.exe" (
    set "JAVA_HOME=%ProgramFiles%\Java\jdk-17"
)

pushd "%~dp0" || exit /b 1
call "%~dp0gradlew.bat" :kira-theme:compileKotlinIosArm64 :kira-theme:compileKotlinIosSimulatorArm64 :kira-components:compileKotlinIosArm64 :kira-components:compileKotlinIosSimulatorArm64 :gallery:compileKotlinIosArm64 :gallery:compileKotlinIosSimulatorArm64 %*
set "GRADLE_EXIT_CODE=%ERRORLEVEL%"
if %GRADLE_EXIT_CODE% equ 0 (
    echo.
    echo iOS Kotlin/Native targets compiled cleanly ^(iosArm64 + iosSimulatorArm64^).
    echo Note: Linking KiraGallery.framework and building iosApp\KiraGallery.xcodeproj requires macOS + Xcode ^(run ./build.sh ios on macOS^).
)
popd
exit /b %GRADLE_EXIT_CODE%
