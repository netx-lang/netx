cmake_minimum_required(VERSION 3.28)

# Project name and version
project(simulator VERSION 1.0 LANGUAGES CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Fetch nlohmann/json
include(FetchContent)

FetchContent_Declare(
        nlohmann_json
        GIT_REPOSITORY https://github.com/nlohmann/json.git
        GIT_TAG v3.11.3 # Specify the desired version
)

FetchContent_MakeAvailable(nlohmann_json)

file(GLOB SOURCES
        "src/main/cpp/simulator/*.cpp"
        "src/main/cpp/simulator/*.h"
        "src/main/cpp/simulator/model/*.cpp"
        "src/main/cpp/simulator/model/*.h")

# Add the executable
add_executable(simulator ${SOURCES})

# Link the nlohmann/json library
target_link_libraries(simulator PRIVATE nlohmann_json::nlohmann_json)

# Include directories (optional, nlohmann/json is header-only)
target_include_directories(simulator PRIVATE ${nlohmann_json_SOURCE_DIR}/include)
