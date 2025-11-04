cmake_minimum_required(VERSION 3.28)
project(nxsim_src VERSION 1.0 LANGUAGES CXX)

add_library(nxsim INTERFACE simulation.h)
target_include_directories(nxsim INTERFACE ${Boost_INCLUDE_DIRS})
if (KAHIP)
    message("Link KaHIP to nxsim")
    target_link_libraries(nxsim INTERFACE kahip)
    target_compile_definitions(nxsim INTERFACE NX_KAHIP)
endif ()
target_link_libraries(nxsim INTERFACE nlohmann_json::nlohmann_json OpenMP::OpenMP_CXX)

add_subdirectory(circuit)

add_executable(nxsim_exe main.cpp cli.cpp)
target_link_libraries(nxsim_exe PRIVATE nxsim)

# Suppress Warning when using GCC with Boost and LTO
if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU" AND CMAKE_INTERPROCEDURAL_OPTIMIZATION)
    target_link_options(nxsim_exe PRIVATE -Wno-error=maybe-uninitialized)
endif()

install(TARGETS nxsim nxsim_exe
        EXPORT nxsimTargets
        RUNTIME DESTINATION bin
        LIBRARY DESTINATION lib
        ARCHIVE DESTINATION lib
)

install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/
        DESTINATION include/nxsim
        FILES_MATCHING PATTERN "*.h"
)