cmake_minimum_required(VERSION 3.28)
project(simulator_cpp_rewrite VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)

# Disable FindBoost module
if(POLICY CMP0167)
    cmake_policy(SET CMP0167 NEW)
endif()
find_package(Boost 1.70 REQUIRED)
find_package(OpenMP REQUIRED)
find_package(nlohmann_json REQUIRED)

add_subdirectory(src)