cmake_minimum_required(VERSION 3.28)
project(nxsim_src VERSION 1.0 LANGUAGES CXX)

add_library(nxsim INTERFACE simulation.h)
# BUILD_INTERFACE: Boost belongs to the build, not to the installed package. Without
# this the exported CMake config bakes a machine-specific path (e.g. /usr/include or
# C:/boost) into INTERFACE_INCLUDE_DIRECTORIES, which breaks consumers elsewhere.
# SYSTEM: Boost/nlohmann are third-party headers, so their warnings must not become
# errors under our -Werror (AppleClang warns about infinity() in Boost.Graph when
# -ffast-math is on, for instance; -isystem silences that, -I does not).
target_include_directories(nxsim SYSTEM INTERFACE $<BUILD_INTERFACE:${Boost_INCLUDE_DIRS}>)
if (KAHIP)
    message("Link KaHIP to nxsim")
    target_link_libraries(nxsim INTERFACE kahip)
    target_compile_definitions(nxsim INTERFACE NX_KAHIP)
endif ()
target_link_libraries(nxsim INTERFACE nlohmann_json::nlohmann_json OpenMP::OpenMP_CXX)

add_subdirectory(circuit)

add_executable(nxsim_cli cli.cpp)
target_link_libraries(nxsim_cli PRIVATE nxsim cli::cli)

# Suppress Warning when using GCC with Boost and LTO
if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU" AND CMAKE_INTERPROCEDURAL_OPTIMIZATION)
    target_link_options(nxsim_cli PRIVATE -Wno-error=maybe-uninitialized)
endif()

install(TARGETS nxsim nxsim_cli
        EXPORT nxsimTargets
        RUNTIME DESTINATION bin
        LIBRARY DESTINATION lib
        ARCHIVE DESTINATION lib
)

install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/
        DESTINATION include/nxsim
        FILES_MATCHING PATTERN "*.h"
)
