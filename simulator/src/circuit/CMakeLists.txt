add_subdirectory(parallel)
target_sources(nxsim PRIVATE rule.cpp circuit_parser.cpp)
target_include_directories(nxsim PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}>
        $<INSTALL_INTERFACE:include/nxsim/circuit>
)