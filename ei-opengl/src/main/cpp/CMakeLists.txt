# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("ei-opengl")

# Debug log 开关，LogUtils 根据宏是否定义决定是否打开日志开关
add_definitions(-D DEBUG_LOG_ON)

# 将指定的文件放入 native_src_files 变量中，其中 CMAKE_SOURCE_DIR 代表 CMakeLists.txt 所在目
# GLOB_RECURSE 代表包含子目
file(GLOB_RECURSE native_src_files "${CMAKE_SOURCE_DIR}/*.cpp")

# 库名（对应生成的 so 文件名），动态库（SHARED）还是静态库（STATIC），库源文件路径
add_library(${CMAKE_PROJECT_NAME} SHARED
        ${native_src_files})

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        GLESv3
        log)