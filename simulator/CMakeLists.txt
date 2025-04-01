cmake_minimum_required(VERSION 3.28)
project(simulator VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_FLAGS "-Wall -Wextra -Werror -fno-rtti")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -Ofast")

# Disable FindBoost module
if (POLICY CMP0167)
    cmake_policy(SET CMP0167 NEW)
endif ()
find_package(Boost 1.70 REQUIRED)
find_package(nlohmann_json REQUIRED)

include(FetchContent)
FetchContent_Declare(
        libcoro
        GIT_REPOSITORY "https://github.com/jbaldwin/libcoro.git"
        GIT_TAG "v0.14.1"
)
set(LIBCORO_FEATURE_NETWORKING OFF)
set(LIBCORO_FEATURE_TLS OFF)
FetchContent_MakeAvailable(libcoro)

add_subdirectory(src)