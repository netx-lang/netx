add_subdirectory(parallel)
target_sources(simulator_cpp PRIVATE rule.cpp circuit_parser.cpp)
target_sources(simulator_cpp PUBLIC circuit_def.h rule.h circuit.h circuit_parser.h)