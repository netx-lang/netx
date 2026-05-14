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

add_executable(nxsim_cli cli.cpp)
target_link_libraries(nxsim_cli PRIVATE nxsim cli::cli)

add_executable(nxsim_mc mc.cpp)
target_link_libraries(nxsim_mc PRIVATE nxsim)

add_executable(nxsim_mc_u64 mc.cpp)
target_compile_definitions(nxsim_mc_u64 PRIVATE NXSIM_MC_BACKEND_U64)
target_link_libraries(nxsim_mc_u64 PRIVATE nxsim)

add_executable(nxsim_mc_u128 mc.cpp)
target_compile_definitions(nxsim_mc_u128 PRIVATE NXSIM_MC_BACKEND_U128)
target_link_libraries(nxsim_mc_u128 PRIVATE nxsim)

add_executable(nxsim_mc_u256 mc.cpp)
target_compile_definitions(nxsim_mc_u256 PRIVATE NXSIM_MC_BACKEND_U256)
target_link_libraries(nxsim_mc_u256 PRIVATE nxsim)

# Suppress Warning when using GCC with Boost and LTO
if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU" AND CMAKE_INTERPROCEDURAL_OPTIMIZATION)
    target_link_options(nxsim_cli PRIVATE -Wno-error=maybe-uninitialized)
    target_link_options(nxsim_mc PRIVATE -Wno-error=maybe-uninitialized)
    target_link_options(nxsim_mc_u64 PRIVATE -Wno-error=maybe-uninitialized)
    target_link_options(nxsim_mc_u128 PRIVATE -Wno-error=maybe-uninitialized)
    target_link_options(nxsim_mc_u256 PRIVATE -Wno-error=maybe-uninitialized)
endif()

install(TARGETS nxsim nxsim_cli nxsim_mc nxsim_mc_u64 nxsim_mc_u128 nxsim_mc_u256
        EXPORT nxsimTargets
        RUNTIME DESTINATION bin
        LIBRARY DESTINATION lib
        ARCHIVE DESTINATION lib
)

install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/
        DESTINATION include/nxsim
        FILES_MATCHING PATTERN "*.h"
)
