//
// Created by czm233 on 12/11/24.
//

#include <fstream>
#include <iostream>

#include "circuit/circuit_parser.h"

int main() {
    std::string path;
    std::cin >> path;
    auto [members_total, circuit, size_map] = hxon::parse_circuit(nlohmann::json::parse(std::ifstream(path)));
    std::cout << circuit.get_value(2) << std::endl;
    for (unsigned i = 1; i <= 100; ++i) {
        circuit.flip_clocks();
        std::cout << circuit.get_value(2) << std::endl;
    }
}
