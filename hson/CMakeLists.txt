cmake_minimum_required(VERSION 3.28)

# Project name and version
project(simulator VERSION 1.0 LANGUAGES CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3")

option(BUILD_TESTS "Build tests" ON)

# Fetch nlohmann/json
include(FetchContent)

FetchContent_Declare(
        nlohmann_json
        GIT_REPOSITORY https://github.com/nlohmann/json.git
        GIT_TAG v3.11.3
)
FetchContent_MakeAvailable(nlohmann_json)

if(BUILD_TESTS)
    FetchContent_Declare(
            googletest
            GIT_REPOSITORY https://github.com/google/googletest.git
            GIT_TAG release-1.12.1
    )
    FetchContent_MakeAvailable(googletest)
endif()
        
file(GLOB SOURCES
        "src/main/cpp/simulator/*.cpp"
        "src/main/cpp/simulator/*.h"
        "src/main/cpp/simulator/model/*.cpp"
        "src/main/cpp/simulator/model/*.h")
list(FILTER SOURCES EXCLUDE REGEX "src/main/cpp/simulator/main.cpp")

add_executable(simulator
        src/main/cpp/simulator/main.cpp ${SOURCES})

target_link_libraries(simulator PRIVATE nlohmann_json::nlohmann_json)

target_include_directories(simulator PRIVATE ${nlohmann_json_SOURCE_DIR}/include)


if(BUILD_TESTS)
    file(GLOB TEST_SOURCES "src/test/cpp/simulator/*.cpp")

    add_executable(simulator_tests ${TEST_SOURCES} ${SOURCES})

    target_link_libraries(simulator_tests PRIVATE
            gtest
            gtest_main
            nlohmann_json::nlohmann_json
    )

    target_include_directories(simulator_tests PRIVATE
            "src/main/cpp/simulator"
            "src/main/cpp/simulator/model"
            ${nlohmann_json_SOURCE_DIR}/include
    )

    include(GoogleTest)
    gtest_discover_tests(simulator_tests)
endif()