//
// Created by czm233 on 12/11/24.
//

#include <format>
#include <fstream>
#include <iostream>
#include <random>

#include "circuit/circuit_parser.h"
#include "circuit/parallel/partition.h"
#include "circuit/waveform_dump.h"

int main() {
    const std::string path = "/home/czm233/hardscript/src/test/resources/testcases/expanded/fft.json";
    //std::cin >> path;
    nxon::dumping_parse_context parsed("./wavedump");
    nxon::parse_circuit(parsed, nlohmann::json::parse(std::ifstream(path)));
    parsed.init_dump();

    using namespace std::chrono;
    using nxon::value_t;
    const auto start = high_resolution_clock::now();

    parsed.circuit.update(1, {1, 0});
    parsed.advance_input_clocks();
    parsed.circuit.update(1, {1, 1});
    parsed.update_by_name("x[0].real", {24, 10});
    parsed.update_by_name("x[1].real", {24, 20});
    parsed.update_by_name("x[2].real", {24, 30});
    parsed.update_by_name("x[3].real", {24, 40});
    parsed.update_by_name("x[4].real", {24, 10});
    parsed.update_by_name("x[5].real", {24, 20});
    parsed.update_by_name("x[6].real", {24, 30});
    parsed.update_by_name("x[7].real", {24, 40});
    parsed.advance_input_clocks();
    parsed.circuit.update(2, {1, 1});
    for (int i = 0; i != 65536; ++i) {
        parsed.advance_input_clocks();
        for (int j = 0; j != 8; ++j) {
            auto real_name = std::format("x[{}].real", j);
            auto imag_name = std::format("x[{}].imag", j);

            auto real = parsed.get_by_name(real_name);
            auto imag = parsed.get_by_name(imag_name);

            if (real > value_t{24, 0x3FFFFF}) {
                parsed.update_by_name(real_name, {24, 0});
            } else {
                const unsigned step_real[8] = {1, 1, 31, 1, 23, 1, 6, 1};
                parsed.update_by_name(real_name, real + value_t{24, step_real[j]});
            }

            if (imag > value_t{24, 0x3FFFFF}) {
                parsed.update_by_name(imag_name, {24, 0});
            } else {
                const unsigned step_imag[8] = {2, 5, 3, 6, 4, 8, 11, 7};
                parsed.update_by_name(imag_name, imag + value_t{24, step_imag[j]});
            }
        }
        if (i == 65535) {
            for (int j = 0; j != 8; ++j) {
                std::cout << parsed.get_by_name(std::format("y[{}].real", j)) << " "
                        << parsed.get_by_name(std::format("y[{}].imag", j))
                        << std::endl;
            }
            std::cout << std::endl;
        }
    }

    const auto end = high_resolution_clock::now();
    std::cout << duration_cast<milliseconds>(end - start).count() << std::endl;
}
