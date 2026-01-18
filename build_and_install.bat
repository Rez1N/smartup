@echo off
cd /d C:\Users\данил\Desktop\myproject\smartup
echo Building Android App...
call gradlew.bat clean assembleDebug
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)
echo.
echo Build successful! Installing to emulator...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    echo Install failed!
    pause
    exit /b 1
)
echo.
echo APK installed! Starting app...
adb shell am start -n com.frovexsoftware.smartup/.MainActivity
echo.
echo Done!
pause
