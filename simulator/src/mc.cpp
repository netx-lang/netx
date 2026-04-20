#include <chrono>
#include <fstream>
#include <iostream>
#include <random>
#include <string>
#include <thread>
#include <vector>

#include <nlohmann/json.hpp>

#include "simulation.h"

namespace {
    using json = nlohmann::json;

    struct options {
        std::string path;
        uint64_t samples = 1ull << 26;
        uint64_t seed = 0x4e5853494dULL;
        size_t threads = std::thread::hardware_concurrency();
        bool outputs_only = true;
    };

    options parse_args(const int argc, char *argv[]) {
        options result;
        for (int i = 1; i < argc; ++i) {
            const std::string arg = argv[i];
            if ((arg == "--samples" || arg == "-n") && i + 1 < argc) {
                result.samples = std::stoull(argv[++i]);
            } else if (arg == "--seed" && i + 1 < argc) {
                result.seed = std::stoull(argv[++i]);
            } else if ((arg == "--threads" || arg == "-j") && i + 1 < argc) {
                result.threads = std::stoull(argv[++i]);
            } else if (arg == "--scope" && i + 1 < argc) {
                const std::string scope = argv[++i];
                if (scope == "outputs") {
                    result.outputs_only = true;
                } else if (scope == "all-named") {
                    result.outputs_only = false;
                } else {
                    throw std::invalid_argument("Expected --scope outputs|all-named");
                }
            } else if (result.path.empty()) {
                result.path = arg;
            } else {
                throw std::invalid_argument("Unexpected argument: " + arg);
            }
        }
        if (result.path.empty()) {
            throw std::invalid_argument("Usage: nxsim_mc <path to NXON file> [--samples N] [--seed S] [--threads T] [--scope outputs|all-named]");
        }
        if (result.threads == 0) result.threads = 1;
        return result;
    }

    bool is_wire_ref(const std::string &name) {
        return name.find('@') != std::string::npos;
    }

    std::string synthetic_output_name(const size_t index) {
        return std::format("__mc_out_{}__", index);
    }

    json normalize_outputs(json root) {
        auto &design = root.at("design");
        auto &outputs = design.at("output");
        auto &members = design.at("members");
        auto &top_outputs = root.at("top_output");
        const auto total = root.at("total").get<unsigned>();

        unsigned next_id = total;
        for (size_t i = 0; i < outputs.size(); ++i) {
            const std::string source = outputs.at(i).get<std::string>();
            if (is_wire_ref(source)) {
                continue;
            }
            const auto top_name = top_outputs.at(i).get<std::string>();
            const std::string wire_id = std::format("{}@{}", synthetic_output_name(i), next_id++);
            members.push_back({
                {"type", "wire"},
                {"width", nxon::value_t::parse(source).size()},
                {"id", wire_id},
                {"loc", std::format("normalized-output:{}", top_name)}
            });
            members.push_back({
                {"type", "builtin"},
                {"id", "TO"},
                {"input", json::array({source})},
                {"output", json::array({wire_id})},
                {"loc", std::format("normalized-output:{}", top_name)}
            });
            outputs.at(i) = wire_id;
        }
        root["total"] = next_id;
        return root;
    }

    nxon::value_t random_value(const size_t width, std::mt19937_64 &rng) {
        nxon::value_t value(width);
        for (size_t bit = 0; bit < width; ++bit) {
            if ((rng() & 1ull) != 0) {
                value.assign_slice_unchecked(bit, bit, nxon::value_t{1, 1u});
            }
        }
        return value;
    }

    struct wire_stat {
        std::string signal;
        size_t width = 1;
        double expectation = 0.0;
    };

    struct sample_accumulator {
        std::vector<uint64_t> high_counts;
        std::vector<double> sums;
    };

    sample_accumulator sample_worker(
        nxon::parse_context &ctx,
        const std::vector<std::string> &inputs,
        const std::vector<std::pair<std::string, nxon::id_t>> &wires,
        const uint64_t samples,
        const uint64_t seed
    ) {
        std::mt19937_64 rng(seed);
        sample_accumulator result{
            .high_counts = std::vector<uint64_t>(wires.size(), 0),
            .sums = std::vector<double>(wires.size(), 0.0)
        };

        for (uint64_t iter = 0; iter < samples; ++iter) {
            for (const auto &input : inputs) {
                ctx.set(input, random_value(ctx.size(input), rng));
            }
            for (size_t i = 0; i < wires.size(); ++i) {
                const auto &[name, id] = wires[i];
                const auto value = ctx.iget(id);
                if (static_cast<bool>(value)) {
                    result.high_counts[i] += 1;
                }
                result.sums[i] += static_cast<double>(static_cast<uint64_t>(value));
            }
        }
        return result;
    }

    std::vector<wire_stat> sample_probabilities(
        nxon::parse_context &main_ctx,
        const std::vector<std::string> &inputs,
        const std::vector<std::pair<std::string, nxon::id_t>> &wires,
        const uint64_t samples,
        const uint64_t seed,
        const size_t threads
    ) {
        const size_t worker_count = std::max<size_t>(
            1,
            std::min<size_t>(threads, static_cast<size_t>(std::max<uint64_t>(1, samples)))
        );
        std::vector<std::thread> workers;
        std::vector<sample_accumulator> partials(worker_count);
        workers.reserve(worker_count > 0 ? worker_count - 1 : 0);

        const uint64_t base = samples / worker_count;
        const uint64_t extra = samples % worker_count;
        for (size_t worker = 1; worker < worker_count; ++worker) {
            const uint64_t worker_samples = base + (worker < extra ? 1 : 0);
            workers.emplace_back([&, worker, worker_samples] {
                auto local_ctx = main_ctx;
                partials[worker] = sample_worker(
                    local_ctx,
                    inputs,
                    wires,
                    worker_samples,
                    seed + 0x9e3779b97f4a7c15ULL * static_cast<uint64_t>(worker)
                );
            });
        }
        const uint64_t main_samples = base + (0 < extra ? 1 : 0);
        partials[0] = sample_worker(main_ctx, inputs, wires, main_samples, seed);
        for (auto &worker : workers) {
            worker.join();
        }

        std::vector<uint64_t> high_counts(wires.size(), 0);
        std::vector<double> sums(wires.size(), 0.0);
        for (const auto &partial : partials) {
            for (size_t i = 0; i < wires.size(); ++i) {
                high_counts[i] += partial.high_counts[i];
                sums[i] += partial.sums[i];
            }
        }

        std::vector<wire_stat> result;
        result.reserve(wires.size());
        const double scale = samples == 0 ? 0.0 : 1.0 / static_cast<double>(samples);
        for (size_t i = 0; i < wires.size(); ++i) {
            result.push_back({
                .signal = wires[i].first,
                .width = main_ctx.isize(wires[i].second),
                .expectation = sums[i] * scale
            });
        }
        return result;
    }
}

int main(const int argc, char *argv[]) {
    try {
        const auto opts = parse_args(argc, argv);

        std::ifstream in(opts.path);
        if (!in) {
            std::cerr << "Can't open file " << opts.path << std::endl;
            return 2;
        }

        json root = json::parse(in);
        root = normalize_outputs(std::move(root));

        auto started = std::chrono::steady_clock::now();
        auto ctx = nxon::parse_circuit(root);
        auto parsed = std::chrono::steady_clock::now();

        std::vector<std::string> inputs;
        inputs.reserve(root.at("top_input").size());
        for (const auto &item : root.at("top_input")) {
            inputs.push_back(item.get<std::string>());
        }

        std::vector<std::pair<std::string, nxon::id_t>> wires;
        if (opts.outputs_only) {
            wires.reserve(root.at("top_output").size());
            for (const auto &item : root.at("top_output")) {
                const auto name = item.get<std::string>();
                wires.emplace_back(name, ctx.require_name(name));
            }
        } else {
            wires.reserve(ctx.name_of_map.size());
            for (const auto &[id, name] : ctx.name_of_map) {
                wires.emplace_back(name, id);
            }
        }
        std::ranges::sort(wires, {}, [](const auto &entry) { return entry.second; });

        const auto probabilities = sample_probabilities(ctx, inputs, wires, opts.samples, opts.seed, opts.threads);
        const auto finished = std::chrono::steady_clock::now();

        json result;
        result["source"] = opts.path;
        result["samples"] = opts.samples;
        result["seed"] = opts.seed;
        result["threads"] = opts.threads;
        result["scope"] = opts.outputs_only ? "outputs" : "all-named";
        result["parse_ms"] = std::chrono::duration_cast<std::chrono::milliseconds>(parsed - started).count();
        result["runtime_ms"] = std::chrono::duration_cast<std::chrono::milliseconds>(finished - parsed).count();
        result["wires"] = json::array();
        for (const auto &wire : probabilities) {
            result["wires"].push_back({
                {"signal", wire.signal},
                {"width", wire.width},
                {"expectation", wire.expectation}
            });
        }

        std::cout << result.dump(2) << std::endl;
        return 0;
    } catch (const std::exception &ex) {
        std::cerr << ex.what() << std::endl;
        return 1;
    }
}
