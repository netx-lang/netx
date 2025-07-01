//
// Created by czm233 on 7/1/25.
//

#ifndef WAVEFORM_DUMP_H
#define WAVEFORM_DUMP_H
#include "circuit_parser.h"
#include <filesystem>
#include <fstream>
#include <unordered_map>
#include <format>

namespace nxon {
    struct dumping_parse_context : parse_context {
    private:
        using path = std::filesystem::path;

        struct waveform_info {
            std::ofstream out;
            size_t time;
            value_batch state;

            explicit waveform_info(std::ofstream out) : out(std::move(out)), time(0) {
            }
        };

        path output_dir;
        std::unordered_map<id_t, waveform_info> dump_waves;

        void dump_wave(waveform_info &wave) const {
            auto &[os, time, state] = wave;
            os << '#' << time << '\n';
            for (size_t id = 0; id < members_total; ++id) {
                const auto new_value = circuit.get_value(id);
                if (state.contains(id) && state.at(id) == new_value) continue;
                os << 'b' << new_value.to_binary_string() << ' ' << id << '\n';
                state[id] = new_value;
            }
        }

        void output_prelude(std::ostream &os) const {
            os <<
                    R"($version NETX-SIMULATOR 0.1 $end
$timescale 1 ns $end
$scope module TOP $end
)";
            for (size_t id = 0; id < members_total; ++id) {
                if (auto size = size_map.at(id); size > 1)
                    os << std::format("$var wire {} {} {}[{}:0] $end\n",
                                      size, id, name_of_map.at(id), size - 1);
                else
                    os << std::format("$var wire 1 {} {} $end\n",
                                      id, name_of_map.at(id));
            }
            os << "$upscope $end\n";
        }

        void advance_and_dump(const id_t id) {
            auto &wave = dump_waves.at(id);
            ++wave.time;
            dump_wave(wave);
        }

    public:
        explicit dumping_parse_context(path output_dir) : output_dir(std::move(output_dir)) {
            std::filesystem::create_directories(this->output_dir);
        }

        void init_dump() {
            for (auto &id: circuit_input_clocks) {
                auto file_path = output_dir / std::format("{}.vcd", id);
                std::ofstream file_out(file_path);
                output_prelude(file_out);
                waveform_info wave(std::move(file_out));
                dump_wave(wave);
                dump_waves.emplace(id, std::move(wave));
            }
        }

        void flip_input_clocks() {
            circuit.flip_batch(circuit_input_clocks);
            for (auto &id: circuit_input_clocks)
                advance_and_dump(id);
        }

        void flip_by_name(const std::string &name) {
            const auto id = name_map.at(name);
            circuit.flip(id);

            if (!circuit_input_clocks.contains(id)) return;
            advance_and_dump(id);
        }
    };
}

#endif //WAVEFORM_DUMP_H
