cmake_minimum_required(VERSION 3.28)
project(nxsim_src VERSION 1.0 LANGUAGES CXX)

add_library(nxsim)
target_link_libraries(nxsim PRIVATE nlohmann_json::nlohmann_json kahip OpenMP::OpenMP_CXX)

add_subdirectory(circuit)

add_executable(nxsim_exe main.cpp cli.cpp)
target_link_libraries(nxsim_exe PRIVATE nxsim)

install(TARGETS nxsim nxsim_exe
        EXPORT nxsimTargets
        RUNTIME DESTINATION bin
        LIBRARY DESTINATION lib
        ARCHIVE DESTINATION lib
)

install(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/circuit/
        DESTINATION include/nxsim
        FILES_MATCHING PATTERN "*.h"
)