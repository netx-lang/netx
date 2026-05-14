#include <chrono>
#include <cmath>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <limits>
#include <optional>
#include <random>
#include <filesystem>
#include <string>
#include <thread>
#include <unordered_map>
#include <vector>

#include <nlohmann/json.hpp>

#include <boost/multiprecision/cpp_int.hpp>
#if defined(NXSIM_MC_BACKEND_U64)
#define NX_BACKEND uint64_t
#define NXSIM_MC_BACKEND_NAME "uint64"
#elif defined(NXSIM_MC_BACKEND_U128)
#define NX_BACKEND boost::multiprecision::uint128_t
#define NXSIM_MC_BACKEND_NAME "uint128"
#elif defined(NXSIM_MC_BACKEND_U256)
#define NX_BACKEND boost::multiprecision::uint256_t
#define NXSIM_MC_BACKEND_NAME "uint256"
#else
#define NX_BACKEND boost::multiprecision::cpp_int
#define NXSIM_MC_BACKEND_NAME "cpp_int"
#endif
#include "simulation.h"

namespace {
    using json = nlohmann::json;

    struct options {
        std::string path;
        uint64_t samples = 1ull << 26;
        uint64_t seed = 0x4e5853494dULL;
        size_t threads = std::thread::hardware_concurrency();
        bool snapshot_powers_of_two = false;
        std::string config_path;
    };

    enum class input_kind {
        random,
        bernoulli,
        constant,
        range
    };

    struct input_policy {
        input_kind kind = input_kind::random;
        double p_high = 0.5;
        uint64_t value = 0;
        uint64_t range_start = 0;
        uint64_t range_end = 0;
    };

    struct simulator_config {
        std::unordered_map<std::string, input_policy> input_policies;
        struct word_case_value {
            uint64_t unsigned_value = 0;
            std::string bit_string;
            bool is_bit_string = false;
        };
        struct input_case {
            double probability = 0.0;
            std::unordered_map<std::string, bool> signals;
            std::unordered_map<std::string, word_case_value> words;
        };
        std::vector<input_case> cases;
        double case_probability_total = 0.0;
        uint64_t reset_cycles = 0;
        std::unordered_map<std::string, bool> reset_signals;
    };

    class progress_bar {
        static constexpr uint64_t interval = 1024;
        static constexpr size_t width = 32;

        std::string label;
        uint64_t total;
        uint64_t last_reported = 0;

    public:
        progress_bar(std::string path, const uint64_t samples)
            : label(std::filesystem::path(path).stem().string()), total(samples) {
        }

        void update(const uint64_t completed, const bool force = false) {
            if (total == 0) return;
            if (completed == last_reported && (!force || completed == total)) return;
            if (!force && completed != total && completed - last_reported < interval) return;
            last_reported = completed;

            const double fraction = static_cast<double>(completed) / static_cast<double>(total);
            const auto filled = std::min(
                width,
                static_cast<size_t>(std::floor(fraction * static_cast<double>(width)))
            );
            std::cerr << '\r' << std::left << std::setw(24) << label
                      << " [" << NXSIM_MC_BACKEND_NAME << "] [";
            for (size_t i = 0; i < width; ++i) {
                std::cerr << (i < filled ? '=' : ' ');
            }
            std::cerr << "] " << completed << '/' << total
                      << " (" << static_cast<unsigned>(std::floor(fraction * 100.0)) << "%)"
                      << std::flush;
            if (completed == total) {
                std::cerr << std::endl;
            }
        }
    };

    struct input_driver {
        std::string signal;
        std::string compiled_signal;
        std::string internal_signal;
        nxon::id_t id = 0;
        size_t width = 1;
        bool clock = false;
        input_policy policy;
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
            } else if (arg == "--config" && i + 1 < argc) {
                result.config_path = argv[++i];
            } else if (arg == "--snapshot" || arg == "--snapshot-powers-of-two") {
                result.snapshot_powers_of_two = true;
            } else if (result.path.empty()) {
                result.path = arg;
            } else {
                throw std::invalid_argument("Unexpected argument: " + arg);
            }
        }
        if (result.path.empty()) {
            throw std::invalid_argument("Usage: nxsim_mc <path to NXON file> [--samples N] [--seed S] [--threads T] [--config PATH] [--snapshot]");
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

    double normalized_unsigned_value(const nxon::value_t &value) {
        const auto width = value.size();
        if (width == 0) return 0.0;
        if (width == 1) return static_cast<bool>(value) ? 1.0 : 0.0;

        const double range = std::ldexp(1.0, static_cast<int>(width)) - 1.0;
        double bit_weight = 1.0 / range;
        double normalized = 0.0;
        for (size_t bit = 0; bit < width; ++bit) {
            if (static_cast<bool>(value.extract_unchecked(bit, bit))) {
                normalized += bit_weight;
            }
            bit_weight *= 2.0;
        }
        return normalized;
    }

    nxon::value_t bernoulli_value(const size_t width, const double p_high, std::mt19937_64 &rng) {
        if (p_high <= 0.0) return nxon::value_t(width, 0ull);
        if (p_high >= 1.0) return nxon::value_t(width, ~0ull);
        if (p_high == 0.5) return random_value(width, rng);

        nxon::value_t value(width);
        std::bernoulli_distribution dist(p_high);
        for (size_t bit = 0; bit < width; ++bit) {
            if (dist(rng)) {
                value.assign_slice_unchecked(bit, bit, nxon::value_t{1, 1u});
            }
        }
        return value;
    }

    input_policy parse_input_policy(const json &entry) {
        input_policy policy;
        if (entry.contains("word")) {
            const auto &word = entry.at("word");
            const auto kind = word.value("kind", "uniform");
            if (kind == "constant") {
                policy.kind = input_kind::constant;
                policy.value = word.value("value", 0ull);
                return policy;
            }
            if (kind == "range") {
                policy.kind = input_kind::range;
                policy.range_start = word.value("start", 0ull);
                policy.range_end = word.value("end", policy.range_start);
                if (policy.range_end < policy.range_start) {
                    policy.kind = input_kind::random;
                }
                return policy;
            }
            policy.kind = input_kind::random;
            return policy;
        }

        policy.p_high = entry.value("pHigh", 0.5);
        if (policy.p_high <= 0.0 || policy.p_high >= 1.0) {
            policy.kind = input_kind::constant;
            policy.value = policy.p_high >= 1.0 ? ~0ull : 0ull;
        } else {
            policy.kind = input_kind::bernoulli;
        }
        return policy;
    }

    simulator_config::word_case_value parse_word_case_value(const json &entry) {
        simulator_config::word_case_value value;
        if (entry.is_string()) {
            value.bit_string = entry.get<std::string>();
            value.is_bit_string = true;
        } else {
            value.unsigned_value = entry.get<uint64_t>();
        }
        return value;
    }

    simulator_config load_simulator_config(const std::string &path) {
        simulator_config config;
        if (path.empty()) return config;

        std::ifstream in(path);
        if (!in) {
            throw std::runtime_error("Can't open config file " + path);
        }
        const auto root = json::parse(in);
        if (root.contains("inputs")) {
            for (const auto &[name, entry]: root.at("inputs").items()) {
                config.input_policies.emplace(name, parse_input_policy(entry));
            }
        }
        if (root.contains("cases")) {
            for (const auto &entry: root.at("cases")) {
                simulator_config::input_case input_case;
                input_case.probability = entry.value("probability", 0.0);
                if (entry.contains("signals")) {
                    for (const auto &[name, value]: entry.at("signals").items()) {
                        input_case.signals.emplace(name, value.get<bool>());
                    }
                }
                if (entry.contains("words")) {
                    for (const auto &[name, value]: entry.at("words").items()) {
                        input_case.words.emplace(name, parse_word_case_value(value));
                    }
                }
                if (input_case.probability > 0.0) {
                    config.case_probability_total += input_case.probability;
                    config.cases.push_back(std::move(input_case));
                }
            }
            if (config.case_probability_total <= 0.0) {
                config.cases.clear();
            }
        }
        if (root.contains("simulator")) {
            const auto &simulator = root.at("simulator");
            config.reset_cycles = simulator.value("resetCycles", 0ull);
            if (simulator.contains("resetSignals")) {
                for (const auto &[name, value]: simulator.at("resetSignals").items()) {
                    config.reset_signals.emplace(name, value.get<bool>());
                }
            }
        }
        return config;
    }

    const input_policy *find_input_policy(
        const std::unordered_map<std::string, input_policy> &policies,
        const std::string &public_signal,
        const std::string &compiled_signal,
        const std::string &internal_signal
    ) {
        if (const auto iter = policies.find(public_signal); iter != policies.end()) {
            return &iter->second;
        }
        if (const auto iter = policies.find(compiled_signal); iter != policies.end()) {
            return &iter->second;
        }
        if (const auto iter = policies.find(internal_signal); iter != policies.end()) {
            return &iter->second;
        }
        return nullptr;
    }

    const bool *find_reset_signal(
        const std::unordered_map<std::string, bool> &reset_signals,
        const std::string &public_signal,
        const std::string &compiled_signal,
        const std::string &internal_signal
    ) {
        if (const auto iter = reset_signals.find(public_signal); iter != reset_signals.end()) {
            return &iter->second;
        }
        if (const auto iter = reset_signals.find(compiled_signal); iter != reset_signals.end()) {
            return &iter->second;
        }
        if (const auto iter = reset_signals.find(internal_signal); iter != reset_signals.end()) {
            return &iter->second;
        }
        return nullptr;
    }

    nxon::value_t policy_value(const input_policy &policy, const size_t width, std::mt19937_64 &rng) {
        switch (policy.kind) {
            case input_kind::constant:
                return nxon::value_t(width, policy.value);
            case input_kind::range: {
                std::uniform_int_distribution<uint64_t> dist(policy.range_start, policy.range_end);
                return nxon::value_t(width, dist(rng));
            }
            case input_kind::bernoulli:
                return bernoulli_value(width, policy.p_high, rng);
            case input_kind::random:
                return random_value(width, rng);
        }
        return random_value(width, rng);
    }

    std::vector<input_driver> build_input_drivers(
        nxon::parse_context &ctx,
        const std::vector<std::string> &inputs,
        const std::vector<std::string> &compiled_inputs,
        const std::unordered_map<std::string, input_policy> &policies,
        const std::unordered_map<std::string, bool> &reset_signals
    ) {
        std::vector<input_driver> drivers;
        drivers.reserve(inputs.size());
        for (size_t index = 0; index < inputs.size(); ++index) {
            const auto &input = inputs[index];
            const auto id = ctx.require_name(input);
            auto internal_signal = input;
            if (const auto iter = ctx.name_of_map.find(id); iter != ctx.name_of_map.end()) {
                internal_signal = iter->second;
            }
            const auto compiled_signal = index < compiled_inputs.size()
                ? compiled_inputs[index]
                : internal_signal;
            const auto *iter = find_input_policy(policies, input, compiled_signal, internal_signal);
            input_policy policy;
            if (iter != nullptr) {
                policy = *iter;
            }
            if (const auto *reset_iter = find_reset_signal(reset_signals, input, compiled_signal, internal_signal)) {
                policy.kind = input_kind::constant;
                policy.value = *reset_iter ? 0ull : ~0ull;
            }
            drivers.push_back({
                .signal = input,
                .compiled_signal = compiled_signal,
                .internal_signal = internal_signal,
                .id = id,
                .width = ctx.isize(id),
                .clock = ctx.circuit_input_clocks.contains(id),
                .policy = policy
            });
        }
        return drivers;
    }

    const simulator_config::input_case *sample_case(
        const simulator_config &config,
        std::mt19937_64 &rng
    ) {
        if (config.cases.empty() || config.case_probability_total <= 0.0) {
            return nullptr;
        }
        std::uniform_real_distribution<double> dist(0.0, config.case_probability_total);
        const auto target = dist(rng);
        double cumulative = 0.0;
        for (const auto &input_case: config.cases) {
            cumulative += input_case.probability;
            if (target <= cumulative) {
                return &input_case;
            }
        }
        return &config.cases.back();
    }

    std::string bit_signal_name(const std::string &signal, const size_t bit) {
        if (signal.find('[') != std::string::npos) {
            return signal;
        }
        return std::format("{}[{}]", signal, bit);
    }

    bool word_case_bit(const simulator_config::word_case_value &word, const size_t bit) {
        if (word.is_bit_string) {
            if (bit >= word.bit_string.size()) return false;
            const auto ch = word.bit_string[word.bit_string.size() - 1 - bit];
            return ch == '1';
        }
        return bit < uint64_t{64} && ((word.unsigned_value >> bit) & 1ull) != 0;
    }

    std::optional<bool> case_signal_bit(
        const simulator_config::input_case &input_case,
        const std::string &public_signal,
        const std::string &compiled_signal,
        const std::string &internal_signal,
        const size_t bit
    ) {
        if (const auto iter = input_case.words.find(public_signal); iter != input_case.words.end()) {
            return word_case_bit(iter->second, bit);
        }
        if (const auto iter = input_case.words.find(compiled_signal); iter != input_case.words.end()) {
            return word_case_bit(iter->second, bit);
        }
        if (const auto iter = input_case.words.find(internal_signal); iter != input_case.words.end()) {
            return word_case_bit(iter->second, bit);
        }
        if (const auto iter = input_case.signals.find(bit_signal_name(public_signal, bit)); iter != input_case.signals.end()) {
            return iter->second;
        }
        if (const auto iter = input_case.signals.find(bit_signal_name(compiled_signal, bit)); iter != input_case.signals.end()) {
            return iter->second;
        }
        if (const auto iter = input_case.signals.find(bit_signal_name(internal_signal, bit)); iter != input_case.signals.end()) {
            return iter->second;
        }
        if (const auto iter = input_case.signals.find(public_signal); iter != input_case.signals.end()) {
            return iter->second;
        }
        if (const auto iter = input_case.signals.find(compiled_signal); iter != input_case.signals.end()) {
            return iter->second;
        }
        if (const auto iter = input_case.signals.find(internal_signal); iter != input_case.signals.end()) {
            return iter->second;
        }
        return std::nullopt;
    }

    nxon::value_t case_value(
        const simulator_config::input_case &input_case,
        const input_driver &driver
    ) {
        nxon::value_t value(driver.width);
        for (size_t bit = 0; bit < driver.width; ++bit) {
            const auto bit_value = case_signal_bit(
                input_case,
                driver.signal,
                driver.compiled_signal,
                driver.internal_signal,
                bit
            ).value_or(false);
            if (bit_value) {
                value.assign_slice_unchecked(bit, bit, nxon::value_t{1, 1u});
            }
        }
        return value;
    }

    void drive_inputs(
        nxon::parse_context &ctx,
        const std::vector<input_driver> &drivers,
        const simulator_config &config,
        std::mt19937_64 &rng,
        const bool include_clocks
    ) {
        const auto *input_case = sample_case(config, rng);
        nxon::value_batch batch;
        batch.reserve(drivers.size());
        for (const auto &driver: drivers) {
            if (driver.clock && !include_clocks) continue;
            batch.emplace(
                driver.id,
                input_case == nullptr
                    ? policy_value(driver.policy, driver.width, rng)
                    : case_value(*input_case, driver)
            );
        }
        if (!batch.empty()) {
            ctx.iset_batch(batch);
        }
    }

    void drive_reset_signals(
        nxon::parse_context &ctx,
        const std::unordered_map<std::string, bool> &reset_signals,
        const bool asserted
    ) {
        nxon::value_batch batch;
        batch.reserve(reset_signals.size());
        for (const auto &[signal, active_high]: reset_signals) {
            const auto id = ctx.require_name(signal);
            const auto width = ctx.isize(id);
            const auto high = asserted == active_high;
            batch.emplace(id, nxon::value_t(width, high ? ~0ull : 0ull));
        }
        if (!batch.empty()) {
            ctx.iset_batch(batch);
        }
    }

    void initialize_simulation_context(
        nxon::parse_context &ctx,
        const std::vector<input_driver> &inputs,
        const simulator_config &config,
        const uint64_t seed
    ) {
        std::mt19937_64 rng(seed);
        drive_inputs(ctx, inputs, config, rng, false);
        if (config.reset_cycles == 0 || config.reset_signals.empty()) {
            return;
        }

        drive_reset_signals(ctx, config.reset_signals, true);
        for (uint64_t cycle = 0; cycle < config.reset_cycles; ++cycle) {
            ctx.advance_input_clocks();
        }
        drive_reset_signals(ctx, config.reset_signals, false);
    }

    void advance_random_cycles(
        nxon::parse_context &ctx,
        const std::vector<input_driver> &inputs,
        const simulator_config &config,
        std::mt19937_64 &rng,
        const uint64_t cycles
    ) {
        for (uint64_t cycle = 0; cycle < cycles; ++cycle) {
            drive_inputs(ctx, inputs, config, rng, false);
            ctx.advance_input_clocks();
        }
    }

    struct wire_stat {
        std::string signal;
        nxon::id_t node_id = 0;
        size_t width = 1;
        double expectation = 0.0;
    };

    struct sample_accumulator {
        std::vector<double> sums;
        uint64_t observations = 0;
    };

    json snapshot_wires_json(
        nxon::parse_context &ctx,
        const std::vector<wire_stat> &wires,
        const sample_accumulator &accumulator
    ) {
        json result = json::array();
        const double scale = accumulator.observations == 0
            ? 0.0
            : 1.0 / static_cast<double>(accumulator.observations);
        for (size_t i = 0; i < wires.size(); ++i) {
            result.push_back({
                {"signal", wires[i].signal},
                {"node_id", wires[i].node_id},
                {"width", ctx.isize(wires[i].node_id)},
                {"expectation", accumulator.sums[i] * scale}
            });
        }
        return result;
    }

    std::vector<wire_stat> accumulator_probabilities(
        nxon::parse_context &ctx,
        const std::vector<wire_stat> &wires,
        const sample_accumulator &accumulator
    ) {
        std::vector<wire_stat> result;
        result.reserve(wires.size());
        const double scale = accumulator.observations == 0
            ? 0.0
            : 1.0 / static_cast<double>(accumulator.observations);
        for (size_t i = 0; i < wires.size(); ++i) {
            result.push_back({
                .signal = wires[i].signal,
                .node_id = wires[i].node_id,
                .width = ctx.isize(wires[i].node_id),
                .expectation = accumulator.sums[i] * scale
            });
        }
        return result;
    }

    std::vector<uint64_t> snapshot_targets(const uint64_t observations) {
        std::vector<uint64_t> targets;
        if (observations == 0) {
            return targets;
        }
        uint64_t current = 1;
        while (current < observations) {
            targets.push_back(current);
            if (current > std::numeric_limits<uint64_t>::max() / 2) {
                break;
            }
            current *= 2;
        }
        targets.push_back(observations);
        return targets;
    }

    void observe_wires(
        nxon::parse_context &ctx,
        const std::vector<wire_stat> &wires,
        sample_accumulator &result
    ) {
        for (size_t i = 0; i < wires.size(); ++i) {
            const auto value = ctx.iget(wires[i].node_id);
            result.sums[i] += normalized_unsigned_value(value);
        }
        result.observations += 1;
    }

    sample_accumulator sample_worker(
        nxon::parse_context &ctx,
        const std::vector<input_driver> &inputs,
        const std::vector<wire_stat> &wires,
        const uint64_t samples,
        const uint64_t seed,
        const simulator_config &config,
        const uint64_t start_sample
    ) {
        std::mt19937_64 rng(seed);
        sample_accumulator result{
            .sums = std::vector<double>(wires.size(), 0.0)
        };

        drive_inputs(ctx, inputs, config, rng, false);
        advance_random_cycles(ctx, inputs, config, rng, start_sample);
        for (uint64_t iter = 0; iter < samples; ++iter) {
            drive_inputs(ctx, inputs, config, rng, false);
            ctx.advance_input_clocks();
            observe_wires(ctx, wires, result);
        }
        return result;
    }

    std::vector<wire_stat> sample_probabilities(
        nxon::parse_context &main_ctx,
        const std::vector<input_driver> &inputs,
        const std::vector<wire_stat> &wires,
        const uint64_t samples,
        const uint64_t seed,
        const size_t threads,
        const simulator_config &config
    ) {
        initialize_simulation_context(main_ctx, inputs, config, seed);

        const size_t worker_count = std::max<size_t>(
            1,
            std::min<size_t>(threads, static_cast<size_t>(std::max<uint64_t>(1, samples)))
        );
        std::vector<std::thread> workers;
        std::vector<sample_accumulator> partials(worker_count);
        std::vector<nxon::parse_context> contexts;
        workers.reserve(worker_count > 0 ? worker_count - 1 : 0);
        contexts.reserve(worker_count > 0 ? worker_count - 1 : 0);

        // Clone worker-local simulation contexts before any thread starts mutating
        // circuit state. Copying `main_ctx` concurrently with live sampling would
        // race on internal vectors/maps and can corrupt allocator state.
        for (size_t worker = 1; worker < worker_count; ++worker) {
            contexts.push_back(main_ctx);
        }

        const uint64_t base = samples / worker_count;
        const uint64_t extra = samples % worker_count;
        for (size_t worker = 1; worker < worker_count; ++worker) {
            const uint64_t worker_samples = base + (worker < extra ? 1 : 0);
            const uint64_t start_sample =
                base * worker + std::min<uint64_t>(static_cast<uint64_t>(worker), extra);
            workers.emplace_back([&, worker, worker_samples, start_sample] {
                partials[worker] = sample_worker(
                    contexts[worker - 1],
                    inputs,
                    wires,
                    worker_samples,
                    seed + 0x9e3779b97f4a7c15ULL * static_cast<uint64_t>(worker),
                    config,
                    start_sample
                );
            });
        }
        const uint64_t main_samples = base + (0 < extra ? 1 : 0);
        partials[0] = sample_worker(main_ctx, inputs, wires, main_samples, seed, config, 0);
        for (auto &worker : workers) {
            worker.join();
        }

        std::vector<double> sums(wires.size(), 0.0);
        uint64_t observations = 0;
        for (const auto &partial : partials) {
            observations += partial.observations;
            for (size_t i = 0; i < wires.size(); ++i) {
                sums[i] += partial.sums[i];
            }
        }

        std::vector<wire_stat> result;
        result.reserve(wires.size());
        const double scale = observations == 0 ? 0.0 : 1.0 / static_cast<double>(observations);
        for (size_t i = 0; i < wires.size(); ++i) {
            result.push_back({
                .signal = wires[i].signal,
                .node_id = wires[i].node_id,
                .width = main_ctx.isize(wires[i].node_id),
                .expectation = sums[i] * scale
            });
        }
        return result;
    }

    struct snapshot_sample_result {
        std::vector<wire_stat> probabilities;
        json snapshots = json::array();
    };

    snapshot_sample_result sample_probabilities_with_snapshots(
        nxon::parse_context &ctx,
        const std::vector<input_driver> &inputs,
        const std::vector<wire_stat> &wires,
        const uint64_t samples,
        const uint64_t seed,
        const simulator_config &config,
        const std::string &source_path,
        const std::chrono::steady_clock::time_point parsed,
        const std::chrono::steady_clock::time_point started
    ) {
        initialize_simulation_context(ctx, inputs, config, seed);
        std::mt19937_64 rng(seed);
        drive_inputs(ctx, inputs, config, rng, false);

        const uint64_t observations = samples;
        const auto targets = snapshot_targets(observations);
        size_t next_target_index = 0;

        sample_accumulator accumulator{
            .sums = std::vector<double>(wires.size(), 0.0)
        };
        json snapshots = json::array();
        progress_bar progress(source_path, samples);

        auto maybe_record_snapshot = [&] {
            while (next_target_index < targets.size()
                   && accumulator.observations >= targets[next_target_index]) {
                const auto now = std::chrono::steady_clock::now();
                snapshots.push_back({
                    {"samples", accumulator.observations},
                    {"effective_cycles", accumulator.observations},
                    {"runtime_ms", std::chrono::duration_cast<std::chrono::milliseconds>(now - parsed).count()},
                    {"parse_ms", std::chrono::duration_cast<std::chrono::milliseconds>(parsed - started).count()},
                    {"wires", snapshot_wires_json(ctx, wires, accumulator)}
                });
                ++next_target_index;
            }
        };

        for (uint64_t sample = 0; sample < samples; ++sample) {
            drive_inputs(ctx, inputs, config, rng, false);
            ctx.advance_input_clocks();
            observe_wires(ctx, wires, accumulator);
            maybe_record_snapshot();
            progress.update(accumulator.observations);
        }
        progress.update(accumulator.observations, true);

        return {
            .probabilities = accumulator_probabilities(ctx, wires, accumulator),
            .snapshots = std::move(snapshots)
        };
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

        std::vector<std::string> input_names;
        input_names.reserve(root.at("top_input").size());
        for (const auto &item : root.at("top_input")) {
            input_names.push_back(item.get<std::string>());
        }
        std::vector<std::string> compiled_input_names;
        compiled_input_names.reserve(root.at("design").at("input").size());
        for (const auto &item : root.at("design").at("input")) {
            compiled_input_names.push_back(item.get<std::string>());
        }
        const auto simulator_config = load_simulator_config(opts.config_path);
        const auto inputs = build_input_drivers(
            ctx,
            input_names,
            compiled_input_names,
            simulator_config.input_policies,
            simulator_config.reset_signals
        );

        std::vector<wire_stat> wires;
        wires.reserve(ctx.name_of_map.size());
        for (const auto &[id, name] : ctx.name_of_map) {
            wires.push_back({
                .signal = name,
                .node_id = id
            });
        }
        std::ranges::sort(wires, {}, [](const auto &entry) { return entry.node_id; });

        std::vector<wire_stat> probabilities;
        json snapshots = json::array();
        if (opts.snapshot_powers_of_two) {
            const auto snapshot_result = sample_probabilities_with_snapshots(
                ctx,
                inputs,
                wires,
                opts.samples,
                opts.seed,
                simulator_config,
                opts.path,
                parsed,
                started
            );
            probabilities = snapshot_result.probabilities;
            snapshots = snapshot_result.snapshots;
        } else {
            probabilities = sample_probabilities(
                ctx,
                inputs,
                wires,
                opts.samples,
                opts.seed,
                opts.threads,
                simulator_config
            );
        }
        const auto finished = std::chrono::steady_clock::now();

        json result;
        result["source"] = opts.path;
        result["samples"] = opts.samples;
        result["effective_cycles"] = opts.samples;
        result["seed"] = opts.seed;
        result["threads"] = opts.threads;
        result["backend"] = NXSIM_MC_BACKEND_NAME;
        result["reset_cycles"] = simulator_config.reset_cycles;
        result["snapshot_powers_of_two"] = opts.snapshot_powers_of_two;
        result["parse_ms"] = std::chrono::duration_cast<std::chrono::milliseconds>(parsed - started).count();
        result["runtime_ms"] = std::chrono::duration_cast<std::chrono::milliseconds>(finished - parsed).count();
        result["wires"] = json::array();
        for (const auto &wire : probabilities) {
            result["wires"].push_back({
                {"signal", wire.signal},
                {"node_id", wire.node_id},
                {"width", wire.width},
                {"expectation", wire.expectation}
            });
        }
        result["snapshots"] = std::move(snapshots);

        std::cout << result.dump(2) << std::endl;
        return 0;
    } catch (const std::exception &ex) {
        std::cerr << ex.what() << std::endl;
        return 1;
    }
}
