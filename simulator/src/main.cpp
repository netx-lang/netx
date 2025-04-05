//
// Created by czm233 on 12/11/24.
//

#include <fstream>
#include <iostream>

#include "circuit/circuit_parser.h"
#include "circuit/parallel/partition.h"

int main() {
    std::string path;
    std::cin >> path;
    auto [members_total, circuit, circuit_input, circuit_output, size_map] = hxon::parse_circuit(nlohmann::json::parse(std::ifstream(path)));

    auto parts = partition(circuit);
    for (auto [id, part] : parts) std::cout << id << ": " << part << std::endl;

    using namespace std::chrono;
    const auto begin = steady_clock::now();

    std::cout << circuit.get_value(2) << std::endl;
    for (unsigned i = 1; i <= 10; ++i) {
        for (unsigned j = 1; j <= 500000; ++j) {
            circuit.flip_clocks();
            circuit.flip_clocks();
        }
        std::cout << circuit.get_value(2) << std::endl;
    }

    const auto end = steady_clock::now();
    std::cout << duration_cast<milliseconds>(end - begin).count() << std::endl;
}
