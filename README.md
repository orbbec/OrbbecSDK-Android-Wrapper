# User Guide

## Build Tools
### Android studio
Android studio **Giraffe | 2022.3.1 Patch 1**
download link [Android studio](https://developer.android.com/studio)

### NDK
**version:** 21.4.7075529

### CMake
**version:** 3.18.1

### gradle
gradle/wrapper/gradle-wrapper.properties
```txt
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
```

### gradle pulgins
build.gradle
```groovy
plugins {
id 'com.android.application' version '8.1.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.8.10' apply false
    id 'com.android.library' version '8.1.0' apply false
}
```

# library and example
## sensorsdk
Orbbec basic sdk implementation with c & cpp, android wrapper import sensorsdk as so.

### so files
**path:** obsensor_jni/libs

### include headers
**path:** obsensor_jni/src/main/cpp/sensorsdk
_Note_: To short include path in native code, module of 'obsensor_jni' import as path 'obsensor_jni/src/main/cpp/sensorsdk/include/libobsensor'

## obsensor_jni
Android wrapper implementation with jni which is forward or transfer data between java and native sensorsdk. 

## Support android version
```groovy
minSdk 24
//noinspection ExpiredTargetSdkVersion
targetSdk 27
```
**targetSdkVersion** 27 to fixed bug 'Android 10 Devices Do NOT Support USB Camera Connection' which fixed on android 11.
\[reference 01] [Android 10 sdk28 usb camera device class bug.](https://forums.oneplus.com/threads/android-10-sdk28-usb-camera-device-class-bug.1258389/)
\[reference 02] [Android 10 Devices Do NOT Support USB Camera Connection.](https://www.camerafi.com/notice-android-10-devices-do-not-support-usb-camera-connection/)

## example
Example of sensorsdk android wrapper

## Support android version
```groovy
minSdk 24
//noinspection ExpiredTargetSdkVersion
targetSdk 27
```
**targetSdkVersion** 27 to fixed bug 'Android 10 Devices Do NOT Support USB Camera Connection' which fixed on android 11.

# Support orbbec device
OrbbecSDK：v1.6.3
Publish: 2023-06-31
Support device list (firmware version):
|Class|Product|Firmware|
|-|-|-|
|UVC Device|Astra+ & Astra+s|V1.0.20|
||Femto|V1.6.9|
||Femto-W|V1.1.8|
||Femto-Live|V1.1.1|
||Astra2|V2.8.20|
||Gemini2|V1.4.60|
||Gemini2L|V1.4.32|
|OpenNI|Gemini||
||Dabai DW||
||Dabai DCW||
||Dabai DC1||
||Astra Mini||
||AstraMini S||
||Astra Mini Pro||
||Dabai||
||Dabai Pro||
||Deeya||
||Astra Plus||
||Dabai D1||
||A1 Pro||
||Gemini E||
||Gemini E Lite||

# Quick start