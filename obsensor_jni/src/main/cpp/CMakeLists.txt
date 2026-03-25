# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
project(cpp)

# Android 15+ 16KB memory page alignment support (backward compatible with 4KB devices)
if(ANDROID)
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
endif()

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared_ libraries with your APK.

file(GLOB_RECURSE JNI_SOURCE_FILES
        ${CMAKE_CURRENT_SOURCE_DIR}/utils/*.h
        ${CMAKE_CURRENT_SOURCE_DIR}/utils/*.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/jdatatype/*.h
        ${CMAKE_CURRENT_SOURCE_DIR}/jdatatype/*.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/obsensor_jni*.h
        ${CMAKE_CURRENT_SOURCE_DIR}/obsensor_jni*.cpp
)
add_library(obsensor_jni SHARED ${JNI_SOURCE_FILES})

target_compile_definitions(obsensor_jni PRIVATE BUILD_NET_PORT)

target_include_directories(obsensor_jni
        PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}
        PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/sensorsdk/include)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

add_library(OrbbecSDK SHARED IMPORTED)
set_target_properties(OrbbecSDK PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs/${ANDROID_ABI}/libOrbbecSDK.so)

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

        OrbbecSDK

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
