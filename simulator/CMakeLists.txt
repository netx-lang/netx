cmake_minimum_required(VERSION 3.28)
project(nxsim VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_FLAGS "-Wall -Wextra -Werror -fno-rtti")
# `-march=native` is fine locally but produces binaries that can die with SIGILL on
# other people's CPUs, so it stays opt-in and must never be used for releases.
option(NXSIM_NATIVE_ARCH "Tune the build for the host CPU (local builds only)" OFF)

# `-ffast-math` implies `-ffinite-math-only`, which makes
# std::numeric_limits<T>::infinity() undefined behavior — and Boost.Graph's A*
# (numeric_values.hpp) legitimately uses infinity to mean "no path". Newer AppleClang
# diagnoses that as -Wnan-infinity-disabled, and -Werror above turns it into a build
# failure from a third-party header we cannot change.
#
# We restore finite-math semantics rather than suppressing the warning: a `-Wno-…`
# flag is itself an error on a compiler that does not know it (verified), whereas
# `-fno-finite-math-only` keeps the rest of -ffast-math and works everywhere.
if (NXSIM_NATIVE_ARCH)
    set(CMAKE_CXX_FLAGS_RELEASE "-O3 -ffast-math -fno-finite-math-only -march=native")
else ()
    set(CMAKE_CXX_FLAGS_RELEASE "-O3 -ffast-math -fno-finite-math-only")
endif ()
set(CMAKE_CXX_FLAGS_DEBUG "-ggdb -fsanitize=address,undefined -fno-omit-frame-pointer -fsanitize-trap=all -O0")
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO "-Og -ggdb")
set(CMAKE_INTERPROCEDURAL_OPTIMIZATION ON)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

# Disable FindBoost module
if (POLICY CMP0167)
    cmake_policy(SET CMP0167 NEW)
endif ()
# nxsim only uses Boost's *header-only* libraries (Boost.Multiprecision, Boost.Graph),
# so a compiled Boost — and therefore Boost's CMake package — is not needed. Pointing
# at a plain header tree avoids both the long "compile all of Boost" step on CI and the
# BoostConfig requirement that CMP0167 (below) enforces and that headers-only Boost
# distributions do not ship.
set(NXSIM_BOOST_INCLUDE_DIR "" CACHE PATH
        "Directory containing the boost/ headers; skips find_package(Boost)")
if (NXSIM_BOOST_INCLUDE_DIR)
    if (NOT EXISTS "${NXSIM_BOOST_INCLUDE_DIR}/boost/version.hpp")
        message(FATAL_ERROR
                "NXSIM_BOOST_INCLUDE_DIR='${NXSIM_BOOST_INCLUDE_DIR}' does not contain boost/version.hpp")
    endif ()
    file(READ "${NXSIM_BOOST_INCLUDE_DIR}/boost/version.hpp" NXSIM_BOOST_VERSION_HPP)
    string(REGEX MATCH "BOOST_VERSION ([0-9]+)" _m "${NXSIM_BOOST_VERSION_HPP}")
    set(_nxsim_boost_version "${CMAKE_MATCH_1}")
    if (_nxsim_boost_version LESS 108000)
        message(FATAL_ERROR "Boost ${_nxsim_boost_version} is too old; nxsim needs >= 1.80")
    endif ()
    string(REGEX MATCH "BOOST_LIB_VERSION \"([0-9_]+)\"" _m "${NXSIM_BOOST_VERSION_HPP}")
    message(STATUS "Using Boost headers from ${NXSIM_BOOST_INCLUDE_DIR} (Boost ${CMAKE_MATCH_1})")
    set(Boost_INCLUDE_DIRS "${NXSIM_BOOST_INCLUDE_DIR}")
else ()
    find_package(Boost 1.80 REQUIRED)
endif ()

find_package(nlohmann_json REQUIRED)

# OpenMP needs its flags in place *before* find_package(), otherwise the lookup
# has already failed. AppleClang ships no OpenMP and rejects the
# `-fopenmp=libomp` form, so point CMake at Homebrew's libomp; prefer the static
# archive so released binaries do not depend on a Homebrew install.
if (APPLE AND CMAKE_CXX_COMPILER_ID MATCHES "Clang")
    find_program(BREW_EXECUTABLE brew)
    if (BREW_EXECUTABLE)
        execute_process(
                COMMAND ${BREW_EXECUTABLE} --prefix libomp
                OUTPUT_VARIABLE LIBOMP_PREFIX
                OUTPUT_STRIP_TRAILING_WHITESPACE
                ERROR_QUIET
        )
    endif ()
    if (LIBOMP_PREFIX AND EXISTS "${LIBOMP_PREFIX}/include/omp.h")
        if (EXISTS "${LIBOMP_PREFIX}/lib/libomp.a")
            set(OpenMP_omp_LIBRARY "${LIBOMP_PREFIX}/lib/libomp.a")
            message(STATUS "Using static Homebrew libomp at ${LIBOMP_PREFIX}")
        else ()
            set(OpenMP_omp_LIBRARY "${LIBOMP_PREFIX}/lib/libomp.dylib")
            message(STATUS "Using Homebrew libomp at ${LIBOMP_PREFIX}")
        endif ()
        set(OpenMP_C_FLAGS "-Xpreprocessor -fopenmp -I${LIBOMP_PREFIX}/include")
        set(OpenMP_CXX_FLAGS "-Xpreprocessor -fopenmp -I${LIBOMP_PREFIX}/include")
        set(OpenMP_C_LIB_NAMES "omp")
        set(OpenMP_CXX_LIB_NAMES "omp")
    else ()
        message(WARNING "libomp not found — run `brew install libomp`")
    endif ()
elseif (CMAKE_CXX_COMPILER_ID MATCHES "Clang")
    set(OpenMP_CXX_FLAGS "-fopenmp=libomp")
    set(OpenMP_CXX_LIB_NAMES "omp")
    set(OpenMP_omp_LIBRARY "omp")
endif ()

find_package(OpenMP REQUIRED)

# The KaHIP partitioner is optional and off by default: it changes how the shape
# of the optimizer's partitioning, so it should be an explicit, reproducible
# choice rather than "whatever happens to be installed on this machine" —
# otherwise two release builds silently behave differently. Enable it locally
# with -DNXSIM_KAHIP=ON.
option(NXSIM_KAHIP "Use the KaHIP graph partitioner if it is installed" OFF)

if (NXSIM_KAHIP)
    find_library(KAHIP kahip)
    if (KAHIP)
        message(STATUS "Found KaHIP at ${KAHIP}")
    else ()
        message(WARNING "NXSIM_KAHIP=ON but KaHIP was not found; continuing without it")
    endif ()
endif ()

include(FetchContent)
FetchContent_Declare(cli
        GIT_REPOSITORY https://github.com/daniele77/cli.git
        GIT_TAG v2.2.0
        EXCLUDE_FROM_ALL
)
FetchContent_MakeAvailable(cli)

add_subdirectory(src)

option(TESTING "Enable testing" ON)
if (TESTING)
    find_package(GTest REQUIRED)
    enable_testing()
    add_subdirectory(tests)
endif ()

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