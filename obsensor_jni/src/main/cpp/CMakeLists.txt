# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.15.0)
project(cpp)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared_ libraries with your APK.

# include sensorsdk
set(OB_LIBS_DIR ${OBSENSOR_JNI_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI})
add_library(sensorsdk SHARED IMPORTED)
set_target_properties(sensorsdk PROPERTIES IMPORTED_LOCATION ${OB_LIBS_DIR}/libOrbbecSDK.so)
include_directories(sensorsdk/include)

add_library( # Sets the name of the library.
        obsensor_jni

        # Sets the library as a shared_ library.
        SHARED

        # Provides a relative path to your source file(s).
        utils/LocalUtils.h
        utils/LocalUtils.cpp
        jdatatype/DataBundle.h
        jdatatype/DataBundle.cpp
        jdatatype/NetworkConfig.h
        jdatatype/NetworkConfig.cpp
        jdatatype/DepthWorkMode.h
        jdatatype/DepthWorkMode.cpp
        jdatatype/DeviceInfo.h
        jdatatype/DeviceInfo.cpp
        jdatatype/MultiDeviceSyncConfig.h
        jdatatype/MultiDeviceSyncConfig.cpp
        jdatatype/TimestampResetConfig.h
        jdatatype/TimestampResetConfig.cpp
        obsensor_jni.h
        obsensor_jni.cpp
        )

target_compile_definitions(obsensor_jni PRIVATE BUILD_NET_PORT)

target_include_directories(obsensor_jni
        PUBLIC ${OB_ROOT_DIR}/libobsensor/include
        PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.


target_link_libraries( # Specifies the target library.
        obsensor_jni

        sensorsdk

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})