//
// Created by czm233 on 12/11/24.
//

#include <fstream>
#include <iostream>

#include "circuit/circuit_parser.h"

int main() {
    auto [members_total, circuit, size_map] = hson::parse_circuit(nlohmann::json::parse(std::ifstream("test.json")));
    std::cout << circuit.get_value(2) << std::endl;
    for (unsigned i = 1; i <= 100; ++i) {
        circuit.flip_clocks();
        std::cout << circuit.get_value(2) << std::endl;
    }
}
