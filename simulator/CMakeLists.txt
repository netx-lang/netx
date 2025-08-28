cmake_minimum_required(VERSION 3.28)
project(nxsim VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_FLAGS "-Wall -Wextra -Werror -fno-rtti")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -ffast-math")
set(CMAKE_CXX_FLAGS_DEBUG "-ggdb -fsanitize=address,undefined -fno-omit-frame-pointer -fsanitize-trap=all -O0")
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO "-Og -ggdb")

include(CheckCXXCompilerFlag)
check_cxx_compiler_flag("-stdlib=libc++" HAS_LIBCXX)
if (HAS_LIBCXX)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif ()

# Disable FindBoost module
if (POLICY CMP0167)
    cmake_policy(SET CMP0167 NEW)
endif ()
find_package(Boost 1.80 REQUIRED)
find_package(nlohmann_json REQUIRED)
find_package(OpenMP REQUIRED)

add_subdirectory(src)

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