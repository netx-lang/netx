//
// Created by czm233 on 12/11/24.
//

#include <fstream>
#include <iostream>
#include <random>

#include "circuit/circuit_parser.h"
#include "circuit/parallel/partition.h"

int main() {
    std::string path;
    std::cin >> path;
    auto parsed = nxon::parse_circuit(nlohmann::json::parse(std::ifstream(path)));

    using namespace std::chrono;
    const auto start = high_resolution_clock::now();

    std::cout << parsed.get_by_name("hex[0]") << '\n';
    for (auto i = 0; i < 50000; ++i) {
        for (auto j = 0; j < 500000; ++j) parsed.flip_input_clocks();
        std::cout << parsed.get_by_name("hex[0]") << '\n';
    }

    const auto end = high_resolution_clock::now();
    std::cout << duration_cast<milliseconds>(end - start).count() << std::endl;
}
