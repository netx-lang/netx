//
// Created by czm233 on 12/11/24.
//

#include <chrono>
#include <format>
#include <fstream>
#include <iostream>

#define SIZED_MPINT_BACKEND boost::multiprecision::uint128_t
#include "circuit/parallel.h"
#include "circuit/circuit_parser.h"

int main() {
    const std::string path = "/home/czm233/draft/NetX-FFT/fft.json";
    nxon::parallel_parse_context parsed;
    nxon::parse_circuit(parsed, std::ifstream(path));
    parsed.init_parallel();

    using namespace std::chrono;
    using nxon::value_t;
    const auto start = high_resolution_clock::now();

    parsed.broadcast(1, {1, 0});
    parsed.run_to_fixed();
    parsed.advance_input_clocks();
    parsed.broadcast(1, {1, 1});
    parsed.broadcast_by_name("x[0].real", {24, 10});
    parsed.broadcast_by_name("x[1].real", {24, 20});
    parsed.broadcast_by_name("x[2].real", {24, 30});
    parsed.broadcast_by_name("x[3].real", {24, 40});
    parsed.broadcast_by_name("x[4].real", {24, 10});
    parsed.broadcast_by_name("x[5].real", {24, 20});
    parsed.broadcast_by_name("x[6].real", {24, 30});
    parsed.broadcast_by_name("x[7].real", {24, 40});
    parsed.run_to_fixed();
    parsed.advance_input_clocks();
    parsed.broadcast(2, {1, 1});
    parsed.run_to_fixed();
    for (int i = 0; i != 65536; ++i) {
        parsed.advance_input_clocks();
        for (int j = 0; j != 8; ++j) {
            auto real_name = std::format("x[{}].real", j);
            auto imag_name = std::format("x[{}].imag", j);

            auto real = parsed.get_by_name(real_name);
            auto imag = parsed.get_by_name(imag_name);

            if (real > value_t{24, 0x3FFFFF}) {
                parsed.broadcast_by_name(real_name, {24, 0});
            } else {
                const unsigned step_real[8] = {1, 1, 31, 1, 23, 1, 6, 1};
                parsed.broadcast_by_name(real_name, real + value_t{24, step_real[j]});
            }

            if (imag > value_t{24, 0x3FFFFF}) {
                parsed.broadcast_by_name(imag_name, {24, 0});
            } else {
                const unsigned step_imag[8] = {2, 5, 3, 6, 4, 8, 11, 7};
                parsed.broadcast_by_name(imag_name, imag + value_t{24, step_imag[j]});
            }
        }
        parsed.run_to_fixed();
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
