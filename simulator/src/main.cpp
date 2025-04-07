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
    auto [members_total, circuit, circuit_input, circuit_output, circuit_input_clocks, size_map] = hxon::parse_circuit(
        nlohmann::json::parse(std::ifstream(path)));

    hxon::partitioned_circuit partitioned{circuit};
    partitioned.start_workers();

    using namespace std::chrono;

    std::default_random_engine generator(std::random_device{}());
    std::uniform_int_distribution<unsigned> distribution(0, 32);
    for (unsigned i = 0; i < 1000000; ++i) {
        auto x = distribution(generator), y = distribution(generator);
        partitioned.broadcast(0, hxon::value_t(8, x));
        partitioned.broadcast(1, hxon::value_t(8, y));
        circuit.update(0, hxon::value_t(8, x));
        circuit.update(1, hxon::value_t(8, y));
        std::this_thread::sleep_for(milliseconds(50));
        auto res = partitioned.get_value(2);
        size_t times = 0;
        while (x + y != static_cast<long long>(res)) {
            std::this_thread::sleep_for(milliseconds(50));
            ++times;
            res = partitioned.get_value(2);
            if (times % 10 == 0) std::cerr << times << std::endl;
        }
        if (i % 1000 == 0) std::cout << i << std::endl;
    }
}
