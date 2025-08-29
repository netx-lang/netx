cmake_minimum_required(VERSION 3.28)

add_executable(nxsim_tests test_sized_mpint.cpp)
target_compile_options(nxsim_tests PRIVATE -Wall -Wextra -O0 -g)
target_link_libraries(nxsim_tests PRIVATE nxsim GTest::gtest GTest::gtest_main)
target_include_directories(nxsim_tests PRIVATE ${GTest_INCLUDE_DIRS})

add_test(NAME nxsim_tests COMMAND nxsim_tests)
