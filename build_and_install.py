#!/usr/bin/env python3
import subprocess
import os
import sys
import shutil

os.chdir(r'C:\Users\данил\Desktop\myproject\smartup')

# Find adb in common Android SDK locations
def find_adb():
    # Common Android SDK paths
    possible_paths = [
        os.path.expandvars(r'%ANDROID_SDK_ROOT%\platform-tools\adb.exe'),
        os.path.expandvars(r'%ANDROID_HOME%\platform-tools\adb.exe'),
        os.path.expandvars(r'%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe'),
        os.path.expandvars(r'%ProgramFiles%\Android\Sdk\platform-tools\adb.exe'),
        os.path.expandvars(r'%ProgramFiles(x86)%\Android\Sdk\platform-tools\adb.exe'),
    ]
    
    # Check PATH
    adb = shutil.which('adb')
    if adb:
        return adb
    
    # Check common paths
    for path in possible_paths:
        if os.path.exists(path):
            return path
    
    return None

print("=" * 80)
print("BUILDING ANDROID APP...")
print("=" * 80)

# Build
print("\n[Step 1] Running: gradlew.bat clean assembleDebug")
print("-" * 80)

build_result = subprocess.run(
    ['gradlew.bat', 'clean', 'assembleDebug'],
    capture_output=True,
    text=True,
    timeout=600
)

print(build_result.stdout)
if build_result.stderr:
    print("STDERR:", build_result.stderr)

if build_result.returncode != 0:
    print(f"\n❌ Build failed with code {build_result.returncode}")
    sys.exit(1)

print("\n✅ Build completed successfully!")

print("\n" + "=" * 80)
print("BUILD SUCCESSFUL - Installing to emulator...")
print("=" * 80)

# Install APK
print("\n[Step 2] Running: adb install -r app\\build\\outputs\\apk\\debug\\app-debug.apk")
print("-" * 80)

# Find adb
adb_path = find_adb()
if not adb_path:
    print("\n❌ ERROR: adb not found! Please ensure Android SDK is installed and ANDROID_SDK_ROOT or ANDROID_HOME environment variable is set.")
    sys.exit(1)

print(f"Using adb from: {adb_path}")

apk_path = r'app\build\outputs\apk\debug\app-debug.apk'

try:
    result = subprocess.run(
        [adb_path, 'install', '-r', apk_path],
        capture_output=True,
        text=True,
        timeout=120
    )
    
    print(result.stdout)
    if result.stderr:
        print("STDERR:", result.stderr)
    
    if result.returncode != 0:
        print(f"\n❌ Install failed with code {result.returncode}")
        sys.exit(1)
    
    print("\n✅ APK installed successfully!")
    
except subprocess.TimeoutExpired:
    print("\n❌ APK installation timed out after 2 minutes")
    sys.exit(1)
except Exception as e:
    print(f"\n❌ Error during APK installation: {e}")
    sys.exit(1)

print("\n" + "=" * 80)
print("APK INSTALLED - Starting app...")
print("=" * 80)

# Start app
print("\n[Step 3] Running: adb shell am start -n com.frovexsoftware.smartup/.MainActivity")
print("-" * 80)

try:
    result = subprocess.run(
        [adb_path, 'shell', 'am', 'start', '-n', 'com.frovexsoftware.smartup/.MainActivity'],
        capture_output=True,
        text=True,
        timeout=30
    )
    
    print(result.stdout)
    if result.stderr:
        print("STDERR:", result.stderr)
    
    if result.returncode != 0:
        print(f"\n❌ Launch failed with code {result.returncode}")
        sys.exit(1)
    
    print("\n✅ App launched successfully!")
    
except subprocess.TimeoutExpired:
    print("\n❌ App launch timed out after 30 seconds")
    sys.exit(1)
except Exception as e:
    print(f"\n❌ Error during app launch: {e}")
    sys.exit(1)

print("\n" + "=" * 80)
print("✅ ALL STEPS COMPLETED SUCCESSFULLY!")
print("=" * 80)
print("\nSummary:")
print("  ✅ Build: gradlew.bat clean assembleDebug - SUCCESS")
print("  ✅ Install: adb install - SUCCESS")
print("  ✅ Launch: adb shell am start - SUCCESS")
print("\nThe app should now be running on your emulator/device.")
