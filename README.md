
# Quick start
## download source
```
git clone https://github.com/orbbec/OrbbecSDK-Android-Wrapper.git
```

## import project
1. Open Android studio
2. Menu: File --> open, and select project directory
3. Click Ok button
4. wait gradle sync complete

![](doc/readme-images/Open-module-Android-wrapper.png)

## run example

### build example
![](doc/readme-images/run-example.png)

### Main UI
![](doc/readme-images/Example-HelloOrbbec.png)

Click 'DepthViewer' to show depth sensor stream.

### DepthViewer
![](doc/readme-images/Example-DepthViewer.png)

# Build Tools
## Android studio
Android studio **Giraffe | 2022.3.1 Patch 1**
download link [Android studio](https://developer.android.com/studio)

## NDK
**version:** 21.4.7075529

## CMake
**version:** 3.18.1

## gradle
gradle/wrapper/gradle-wrapper.properties
```txt
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
```

## gradle pulgins
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

## so files
**path:** obsensor_jni/libs

## include headers
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


# QA
## LintModelSeverity has been compiled by a more recent version of the Java 
```
An exception occurred applying plugin request [id: 'com.android.application']
> Failed to apply plugin 'com.android.internal.application'.
   > Could not create an instance of type com.android.build.gradle.internal.dsl.ApplicationExtensionImpl$AgpDecorated.
      > Could not create an instance of type com.android.build.gradle.internal.dsl.LintImpl$AgpDecorated.
         > Could not generate a decorated class for type LintImpl$AgpDecorated.
            > com/android/tools/lint/model/LintModelSeverity has been compiled by a more recent version of the Java Runtime (class file version 61.0), this version of the Java Runtime only recognizes class file versions up to 55.0

* Try:
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
```

**Reason**: AGP(android gradle plugin) is v8.1.0, need jdk 17, Please update android studio to new version, and check gradle jdk version.
Android studio --> File --> Settings --> Build,Execution,Deployment --> Build Tools --> Gradle, check Gradle Projects -> Gradle JDK 

**reference**: 
https://developer.android.com/studio/releases#android_gradle_plugin_and_android_studio_compatibility

https://developer.android.com/build/releases/gradle-plugin