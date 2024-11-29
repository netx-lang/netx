cmake_minimum_required(VERSION 3.28)
project(simulator_cpp_test VERSION 1.0 LANGUAGES CXX)

include_directories("${simulator_cpp_src_SOURCE_DIR}")
add_executable(simulator_cpp_tests BitVectorTest.cpp ScheduleQueueTest.cpp)
target_link_libraries(simulator_cpp_tests GTest::gtest_main nlohmann_json::nlohmann_json simulator_cpp)

include(GoogleTest)
gtest_discover_tests(simulator_cpp_tests)