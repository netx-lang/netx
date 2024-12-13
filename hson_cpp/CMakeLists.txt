cmake_minimum_required(VERSION 3.28)

project(hson_cpp VERSION 1.0)

if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

add_subdirectory(simulator_cpp)
add_subdirectory(simulator_cpp_rewrite)