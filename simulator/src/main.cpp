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
    auto [members_total, circuit, circuit_input, circuit_output, circuit_input_clocks, size_map] = hxon::parse_circuit(nlohmann::json::parse(std::ifstream(path)));

    auto partitioned = partition(circuit);
    auto workers = work_on_partition(partitioned);

    using namespace std::chrono;

    std::default_random_engine generator(std::random_device{}());
    std::uniform_int_distribution<unsigned> distribution(0, 8);
    for (unsigned i = 0; i < 100; ++i) {
        auto x = distribution(generator), y = distribution(generator);
        partitioned.broadcast(0, hxon::value_t(8, x));
        partitioned.broadcast(1, hxon::value_t(8, y));
        circuit.update(0, hxon::value_t(8, x));
        circuit.update(1, hxon::value_t(8, y));
        std::this_thread::sleep_for(milliseconds(500));
        auto res = partitioned.get_value(2);
        if (x + y == res.operator long long())
            std::cout << "Passed " << x + y << " vs " << res << std::endl;
        else {
            std::cout << "Rejected " << x + y << " vs " << res << std::endl;
            for (id_t id = 0; id < members_total; ++id) {
                auto pv = partitioned.get_value(id);
                auto sv = circuit.get_value(id);
                if (pv == sv) continue;
                std::cout << id << ": " << pv << " vs " << sv << std::endl;
            }
            for (size_t part_id = 0; part_id < partitioned.num_parts; ++part_id) {
                auto& q = partitioned.circuit_parts[part_id]->updates.queue;
                if (q.empty()) continue;
                std::cout << '(' << part_id << ')' << " non empty queue" << std::endl;
            }
            for (id_t id = 0; id < members_total; ++id) {
                auto sv = circuit.get_value(id);
                for (size_t part_id = 0; part_id < partitioned.num_parts; ++part_id) {
                    auto& values = partitioned.circuit_parts[part_id]->part.values;
                    if (!values.contains(id)) continue;
                    auto pv = values.at(id);
                    if (pv == sv) continue;
                    std::cout << id << '(' << part_id << ')' << ": " << pv << " vs " << sv << std::endl;
                }
            }
        }
    }
}
