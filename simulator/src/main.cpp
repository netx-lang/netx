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

    parsed.flip_by_name("rst");
    parsed.advance_input_clocks();
    parsed.flip_by_name("rst");
    for (unsigned i = 0; i < 65536; ++i) {
        parsed.update_by_name("D", nxon::value_t{32, i * i});
        parsed.flip_by_name("rst");
        parsed.advance_input_clocks();
        parsed.flip_by_name("rst");
        for (auto j = 0; j < 16; ++j) parsed.advance_input_clocks();
        if (const auto res = static_cast<unsigned>(parsed.get_by_name("Q")); res != i)
            std::cout << "ERROR on " << i << '\n';
    }

    const auto end = high_resolution_clock::now();
    std::cout << duration_cast<milliseconds>(end - start).count() << std::endl;
}
