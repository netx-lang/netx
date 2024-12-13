cmake_minimum_required(VERSION 3.28)
project(simulator_cpp_rewrite_src VERSION 1.0 LANGUAGES CXX)

add_library(simulator_cpp_rewrite)

target_sources(simulator_cpp_rewrite PRIVATE cli.cpp)
add_subdirectory(circuit)

add_executable(simulator_cpp_rewrite_exe main.cpp)
target_link_libraries(simulator_cpp_rewrite_exe PRIVATE simulator_cpp_rewrite)