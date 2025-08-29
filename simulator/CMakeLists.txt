cmake_minimum_required(VERSION 3.28)
project(nxsim VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_FLAGS "-Wall -Wextra -Werror -fno-rtti")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -ffast-math")
set(CMAKE_CXX_FLAGS_DEBUG "-ggdb -fsanitize=address,undefined -fno-omit-frame-pointer -fsanitize-trap=all -O0")
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO "-Og -ggdb")
set(CMAKE_INTERPROCEDURAL_OPTIMIZATION ON)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

# Disable FindBoost module
if (POLICY CMP0167)
    cmake_policy(SET CMP0167 NEW)
endif ()
find_package(Boost 1.80 REQUIRED)
find_package(nlohmann_json REQUIRED)
find_package(OpenMP REQUIRED)
find_package(GMP QUIET)

add_subdirectory(src)

option(TESTING "Enable testing" ON)
if(TESTING)
    find_package(GTest REQUIRED)
    enable_testing()
    add_subdirectory(tests)
endif()

include(CMakePackageConfigHelpers)
write_basic_package_version_file(
        "${CMAKE_CURRENT_BINARY_DIR}/nxsimConfigVersion.cmake"
        VERSION ${PROJECT_VERSION}
        COMPATIBILITY SameMajorVersion
)

install(EXPORT nxsimTargets
        FILE nxsimTargets.cmake
        NAMESPACE nxon::
        DESTINATION lib/cmake/nxsim
)

configure_package_config_file(
        "${CMAKE_CURRENT_SOURCE_DIR}/cmake/nxsimConfig.cmake.in"
        "${CMAKE_CURRENT_BINARY_DIR}/nxsimConfig.cmake"
        INSTALL_DESTINATION lib/cmake/nxsim
)

install(FILES
        "${CMAKE_CURRENT_BINARY_DIR}/nxsimConfig.cmake"
        "${CMAKE_CURRENT_BINARY_DIR}/nxsimConfigVersion.cmake"
        DESTINATION lib/cmake/nxsim
)