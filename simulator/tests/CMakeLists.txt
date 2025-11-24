cmake_minimum_required(VERSION 3.28)

set(CMAKE_CXX_FLAGS "-Wall -Wextra -Werror -fno-rtti")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -ffast-math -march=native")
set(CMAKE_CXX_FLAGS_DEBUG "-ggdb -fsanitize=address,undefined -fno-omit-frame-pointer -fsanitize-trap=all -O0")
set(CMAKE_CXX_FLAGS_RELWITHDEBINFO "-Og -ggdb")

add_executable(nxsim_unit_tests test_sized_mpint.cpp test_rule.cpp)
target_link_libraries(nxsim_unit_tests PRIVATE nxsim GTest::gtest GTest::gtest_main)
target_include_directories(nxsim_unit_tests PRIVATE ${GTest_INCLUDE_DIRS})

set(RESOURCE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/resources")
add_executable(nxsim_rv32i_test test_rv32i.cpp)
target_link_libraries(nxsim_rv32i_test PRIVATE nxsim GTest::gtest GTest::gtest_main)
target_include_directories(nxsim_rv32i_test PRIVATE ${GTest_INCLUDE_DIRS})
target_compile_definitions(nxsim_rv32i_test PRIVATE RESOURCE_PATH="${RESOURCE_PATH}")

add_test(NAME nxsim_unit_tests COMMAND nxsim_unit_tests)
add_test(NAME nxsim_rv32i_test COMMAND nxsim_rv32i_test)

if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU" AND CMAKE_INTERPROCEDURAL_OPTIMIZATION)
    target_link_options(nxsim_rv32i_test PRIVATE -Wno-error=maybe-uninitialized)
endif()