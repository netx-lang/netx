cmake_minimum_required(VERSION 3.28)

add_executable(nxsim_tests
        test_sized_mpint.cpp
        test_rule.cpp
        test_rv32i.cpp)
set(CMAKE_CXX_FLAGS "-Wall -Wextra -Werror -fno-rtti")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -ffast-math -march=native")
set(CMAKE_CXX_FLAGS_DEBUG "-ggdb -fsanitize=address,undefined -fno-omit-frame-pointer -fsanitize-trap=all -O0")
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO "-Og -ggdb")
target_link_libraries(nxsim_tests PRIVATE nxsim GTest::gtest GTest::gtest_main)
target_include_directories(nxsim_tests PRIVATE ${GTest_INCLUDE_DIRS})

set(RESOURCE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/resources")
target_compile_definitions(nxsim_tests PRIVATE RESOURCE_PATH="${RESOURCE_PATH}")

add_test(NAME nxsim_tests COMMAND nxsim_tests)


if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU" AND CMAKE_INTERPROCEDURAL_OPTIMIZATION)
    target_link_options(nxsim_tests PRIVATE -Wno-error=maybe-uninitialized)
endif()