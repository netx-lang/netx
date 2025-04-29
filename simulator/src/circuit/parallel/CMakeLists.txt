target_sources(nxsim PRIVATE partition.cpp)
target_include_directories(nxsim PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}>
        $<INSTALL_INTERFACE:include/nxsim/circuit>
)