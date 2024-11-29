cmake_minimum_required(VERSION 3.28)
project(simulator_cpp_src VERSION 1.0 LANGUAGES CXX)

add_library(simulator_cpp)

target_sources(simulator_cpp PRIVATE Frontend.cpp Frontend.h
        Simulator.cpp Simulator.h)
add_subdirectory(model)

target_link_libraries(simulator_cpp PRIVATE nlohmann_json::nlohmann_json)

add_executable(simulator_cpp_exe main.cpp)
target_link_libraries(simulator_cpp_exe PRIVATE simulator_cpp)