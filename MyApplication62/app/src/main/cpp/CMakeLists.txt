# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("myapplication")
set(cmake_dir ${CMAKE_SOURCE_DIR})
#
add_definitions(-std=c++11)
add_definitions("-Wall")
add_definitions(-DUNICODE -D_UNICODE)
#
add_definitions(-DOS_ANDROID)
add_definitions(-D__ANDROID__)
add_definitions(-D__ARM_NEON__)
add_definitions(-D__ARM_NEON)
#
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -O3 -frtti -fPIC -fpermissive")
#
## openmp
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fopenmp")
#
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fomit-frame-pointer -fstrict-aliasing -ffast-math")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fomit-frame-pointer -fstrict-aliasing -ffast-math")
#
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden")
#
include_directories(
        ${cmake_dir}/
        ${cmake_dir}/include/
        ${cmake_dir}/ncnn/)
#link_directories(${CMAKE_CURRENT_SOURCE_DIR}/lib/)
#
add_library(ncnn STATIC IMPORTED)
set_property(
        TARGET
        ncnn
        PROPERTY
        IMPORTED_LOCATION
        ${cmake_dir}/lib/${ANDROID_ABI}/libncnn.a)
#
file(GLOB_RECURSE src_files ${cmake_dir}/*.cpp)

set(opencv_libs ${cmake_dir}/../jniLibs/${ANDROID_ABI}/libopencv_java3.so)

add_library(${PROJECT_NAME} SHARED ${src_files})
target_link_libraries(${PROJECT_NAME} android jnigraphics z m dl log atomic ncnn ${opencv_libs})

#
##
##add ncnn
#set(ncnn_path D:\\Ncnn\\ncnn_project)
#include_directories(${cmake_dir}\\ocr\\ncnn\\include\\ncnn)
#link_directories(${ncnn_path}\\ocr\\ncnn\\lib)
#set(ncnn_lib ${ncnn_path}\\ocr\\ncnn\\lib\\libncnn.a)
#add_library (ncnn STATIC IMPORTED)
#
#set_target_properties(ncnn PROPERTIES IMPORTED_LOCATION ${ncnn_lib})
##set(ncnn_lib ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a/libncnn.a)
##add_library (ncnn_lib STATIC IMPORTED)
##set_target_properties(ncnn_lib PROPERTIES IMPORTED_LOCATION ${ncnn_lib})
#
##add opencv
##
#set(OpenCV_DIR D:/opencv/opencv-3.4.3-android-sdk/OpenCV-android-sdk/sdk/native/jni)
#find_package(OpenCV REQUIRED)
#set(opencv_libs ${cmake_dir}/../jniLibs/${ANDROID_ABI}/libopencv_java3.so)
#message(STATUS "OpenCV library status:")
#message(STATUS "    version: ${OpenCV_VERSION}")
#message(STATUS "    libraries: ${OpenCV_LIBS}")
#message(STATUS "    include path: ${OpenCV_INCLUDE_DIRS}")
#include_directories(${cmake-dir}/ ${OpenCV_INCLUDE_DIRS})
#
#add_definitions(-std=c++11)
#add_definitions("-Wall")
#add_definitions(-DUNICODE -D_UNICODE)
## Creates and names a library, sets it as either STATIC
## or SHARED, and provides the relative paths to its source code.
## You can define multiple libraries, and CMake builds them for you.
## Gradle automatically packages shared libraries with your APK.
#FIND_PACKAGE( OpenMP REQUIRED)
#if(OPENMP_FOUND)
#    message("OPENMP 3.4.3 FOUND")
#    #openmp
#    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${OpenMP_C_FLAGS}")
#    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${OpenMP_CXX_FLAGS}")
#    set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} ${OpenMP_EXE_LINKER_FLAGS}")
#endif()
#include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/ncnn)
#include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/include)
#link_directories(${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/lib)
#
#add_library( # Sets the name of the library.
#             native-lib
#
#             # Sets the library as a shared library.
#             SHARED
#
#             # Provides a relative path to your source file(s).
#             native-lib.cpp #ll.cpp
#        )
#
## Searches for a specified prebuilt library and stores the path as a
## variable. Because CMake includes system libraries in the search path by
## default, you only need to specify the name of the public NDK library
## you want to add. CMake verifies that the library exists before
## completing its build.
#
#find_library( # Sets the name of the path variable.
#              log-lib
#              android
#              # Specifies the name of the NDK library that
#              # you want CMake to locate.
#              log )
#
## Specifies libraries CMake should link to your target library. You
## can link multiple libraries, such as libraries you define in this
## build script, prebuilt third-party libraries, or system libraries.
#
#target_link_libraries( # Specifies the target library.
#                       native-lib
#                       ncnn
#                      ${OpenCV_LIBS}
#                       android
#                       jnigraphics
#                       # Links the target library to the log library
#                       # included in the NDK.
#                       ${log-lib} )