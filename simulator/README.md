# NetX Simulator (nxsim)

## Overview

nxsim operates on NXON (NetX Object Notation) files, which represent compiled hardware circuits. This simulator
supports:

- **Interactive CLI** for manual circuit testing
- **Programmable C++ API** for custom simulation workflows
- **Native modelling** for acceleration and modelling IP protected components
- **Parallel simulation** with automatic circuit partitioning
- **VCD waveform dumping** for visualization and debugging

---

## Command-Line Interface

### Basic Usage

```bash
nxsim_cli <path_to_nxon_file> [<waveform_output_directory>]
```

- **Without waveform dumping:**
  ```bash
  nxsim_cli circuit.nxon
  ```

- **With waveform dumping:**
  ```bash
  nxsim_cli circuit.nxon ./waveforms
  ```

  When a waveform directory is specified, nxsim automatically generates VCD files for each input clock signal.

### CLI Commands

The interactive CLI provides commands for inspecting and manipulating circuit state:

#### Wire Inspect and Manipulation by ID

- **`iset <wire_id> <value>`** - Set wire value using numeric ID
    - Value formats: `10` (decimal), `10d` (decimal), `ABh` (hex), `001b` (binary), `71o` (octal)
    - Example: `iset 5 FFh`

- **`iget <wire_id>`** - Get wire value by ID
    - Example: `iget 5`

- **`iflip <wire_id>`** - Flip (toggle) wire value by ID
    - Example: `iflip 5`

#### Wire Manipulation by Name

- **`set <wire_name> <value>`** - Set wire value using symbolic name
    - Example: `set data_in 42`

- **`get <wire_name>`** - Get wire value by name
    - Example: `get data_out`

- **`flip <wire_name>`** - Flip wire value by name
    - Example: `flip enable`

#### Clock Control

- **`fc`** - Flip all input clocks (single edge)
    - Advances simulation by one clock edge

- **`ac`** - Advance input clocks (flip twice, full cycle)
    - Advances simulation by one complete clock cycle

---

## C++ API

### Including and Configuration

nxsim provides an all-in-one header to include the entire library:

```cpp
#include <nxsim/simulation.h>
```

And it is allowed to configure the library by defining some macros before including the header:

#### Custom Backend

nxsim has a configurable arithmetic backend for wire values to achieve better performance when the width is limited.

By default, it uses `uint64_t` as backend.

To use a custom backend, define `NX_BACKEND` before including nxsim headers.

```cpp
#include <boost/multiprecision/cpp_int.hpp>
#define NX_BACKEND boost::multiprecision::cpp_int
#include <nxsim/simulation.h>
```

**Recommended backends:**

- **Primitive types:** `uint32_t`, `uint64_t` (fast, limited width)
- **Fixed-width multiprecision:** `boost::multiprecision::uint128_t`, `boost::multiprecision::uint256_t`, etc.
- **Arbitrary precision:** `boost::multiprecision::cpp_int` (unlimited width, slower)

#### Number of Threads

nxsim implements parallel simulation with OpenMP. While more threads leads to better parallelism, it also incurs more
synchronization overhead.

By default, nxsim uses 8 threads. The best number of threads depends on your machine and the structure of your circuit.

To configure the thread count, define `NX_NUM_THREADS` before including nxsim headers.

```cpp
#define NX_NUM_THREADS 16
#include <nxsim/simulation.h>
```

#### KaHIP Partitioner

nxsim optionally depends on KaHIP to provide an alternative circuit partitioner.

If CMake is used to add nxsim to your project, this partitioner is automatically enabled whenever possible.

In case CMake is not used, you can add `-lkahip` to the linker flags, and define `NX_KAHIP` before including nxsim
headers to enable it manually.

```cpp
#define NX_KAHIP
#include <nxsim/simulation.h>
```

### Basic Usage

#### Parsing

To get a circuit from a NXON file, use `parse_circuit`.

```cpp
#include <nxsim/simulation.h>

// Parse from file
std::ifstream input("circuit.nxon");
auto context = nxon::parse_circuit(input);
```

#### Inspecting and Manipulating

nxsim provides several methods to inspect and manipulate circuit state:

- `get`: Get the value of the wire with given id
- `get_by_name`: Get the value of the wire with given name
- `update`: Update the value of the wire with given id
- `update_by_name`: Update the value of the wire with given name
- `flip`: Flip the wire with given id
- `flip_by_name`: Flip the wire with given name
- `flip_input_clocks`: Flip all input clocks
- `advance_input_clocks`: Advance all input clocks by one cycle (flip twice)

The value of a wire is a `nxon::value_t`, which is an integer with custom width.

```cpp
// Access wire value
auto value = context.get_by_name("output_signal");

// Update wire value
context.update_by_name("input_signal", nxon::value_t{5 /* width */, 3 /* value */});

// Flip wire
context.flip_by_name("clock");

// Advance clocks
context.advance_input_clocks();  // Full cycle (two edges)
context.flip_input_clocks();     // Single edge
```

#### Batched Manipulation

There are also batched manipulation methods:

- `flip_batch`: Flip wires with given ids
- `update_batch`: Update values of wires with given ids

```cpp
// Flip wires
context.flip_batch({1, 2, 3});

// Update wires
context.update_batch({{1, nxon::value_t{5, 3}}, {2, nxon::value_t{5, 4}}});
```

#### Cached Manipulation

Batched operations apply multiple updates at once to reduce overhead. But they are not as convenient as direct
operations.

To address this, nxsim provides cached manipulation methods:

- `cached_flip`
- `cached_flip_batch`
- `cached_flip_by_name`
- `cached_update`
- `cached_update_batch`
- `cached_update_by_name`

The usage of these cached operations are almost the same as direct operations, but they are not applied immediately.
Instead, they are cached and applied at once when `apply_cache` is called.

```cpp
// Cache operations
context.cached_flip(1);
context.cached_update(2, nxon::value_t{5, 3});

// Apply all cached operations at once
context.apply_cache();
```

### Parse Context Wrappers

nxsim provides several wrappers around `parse_context` to provide additional functionalities.

To use a wrapper, pass it as an additional parameter to `parse_circuit`.

```cpp
custon_parse_context ctx; 
nxon::parse_circuit(ctx, input);
```

#### Waveform Dumping

`dumping_parse_context` is a wrapper that provides waveform dumping functionality.

After parsing, it is required to call `init_dump` to initialize the VCD files before any manipulation. It automatically
generates separate VCD waveforms under the given directory for every input clock, and tracks all wire state changes
synchronized to clock edges.

```cpp
nxon::dumping_parse_context ctx("./waveforms");
nxon::parse_circuit(ctx, input);
ctx.init_dump();  // Initialize VCD files

// Automatically dump wires on clock edges
ctx.flip_by_name("clk");  // Triggers waveform update
```

#### Partitioning and Parallel Simulation

nxsim supports automatic circuit partitioning and parallel execution on partitioned circuit.

There are two wrappers around this: `partitioned_parse_context` (Sequential Partitioned) and
`parallel_parse_context` (Parallel Execution). Both require calling `init_partition` to perform partitioning
before any manipulation. To use parallel execution, you need to link against OpenMP by adding the `-fopenmp` flag
when compiling your C++ code (automatically configured when using nxsim with CMake).

```cpp
nxon::partitioned_parse_context ctx;
nxon::parse_circuit(ctx, input);
ctx.init_partition();

// Operations automatically propagate across partitions
ctx.update_by_name("input", value);
```

You may wonder what a `paritioned_parse_context` without parallel execution can achieve. In fact, the propagation
order of updates in `parse_context` is not always ideal, which may lead to many redundant updates. When the
circuit is partitioned, unlike `parse_context`, updates will first propagate in their current part of circuit,
reach a local fixed point, and then propagate to the others. For some circuits, this change in order can reduce the
total number of updates by up to 30%.

##### Partitioner

`partitioned_parse_context` and `parallel_parse_context` accept a partitioner as template parameter to
perform circuit partitioning. nxsim provides two partitioners:

**Semantic Partitioner (Default):**

This partitioner uses the circuit hierarchy in source code and data flow analysis to create partitions, and accepts a
parameter to control the minimum part size (5% of the total number of wires by default).

```cpp
nxon::partitioned_parse_context<nxon::semantic_partitioner<0.01 /* 1% of the total number of wires */>> ctx;
```

**KaHIP Partitioner (Optional):**

This partitioner uses the KaHIP graph partitioning library (requires `NX_KAHIP` flag) to create partitions.

```cpp
nxon::partitioned_parse_context<nxon::kahip_partitioner> ctx;
```

##### Partition Visualization

Export partition structure as Graphviz DOT format:

```cpp
std::ofstream dot_file("partition.dot");
ctx.export_dot(dot_file);
```

Generates a colored graph showing partition assignments. Render with:

```bash
dot -Tsvg partition.dot -o partition.svg
```

### Native Modelling

#### Clocks, Sources, Sinks, and Rules

nxsim uses a **rule-based simulation model** where circuit behavior is defined by rules that read from sources, do
calculation and write to sinks.

##### Source (`source_t`)

A source represents a **read operation** from the circuit state:

- **Dependencies:** Set of wire IDs that may be read
- **Length:** Width of the value produced
- **Operation (`get()`):** Extracts and potentially transforms wire values

There are some built-in source types:

```cpp
// Direct wire access
auto src = data_factory::id_source(wire_id, bit_width);

// Constant value
auto const_src = data_factory::constant_source(nxon::value_t::parse_fixed(8, "42"));

// Bit selection (slice)
auto slice_src = data_factory::select_source(wire_id, from_bit, to_bit);
```

##### Clock (`clock_t`)

A clock represents a **clock signal** in the circuit:

- **Dependencies:** Set of wire IDs that may be read
- **Operation (`triggered()`):** Check if the clock is triggered

There is one built-in clock type:

```cpp
// Direct clock
auto clk = data_factory::id_clock(wire_id, pos_edge);
```

##### Sink (`sink_t`)

A sink represents a **write operation** to the circuit state:

- **Outcomes:** Set of wire IDs that may be modified
- **Length:** Expected width of incoming values
- **Operations:**
    - `check()`: Returns true if writing would change the current value
    - `put()`: Writes the value to circuit state

There are some built-in sink types:

```cpp
// Direct wire write
auto sink = data_factory::id_sink(wire_id, bit_width);

// Bit selection write (modifies a slice of a wire)
auto slice_sink = data_factory::select_sink(wire_id, from_bit, to_bit);

// Sink adapters (automatic width conversion)
auto signed_sink = data_factory::signed_resize_sink_adapter(underlying_sink); // Resize as signed integer
auto unsigned_sink = data_factory::unsigned_resize_sink_adapter(underlying_sink); // Resize as unsigned integer
```

##### Rule (`rule_t`)

A rule defines a **computational relationship** between sources and sinks:

- **Triggers:** Wire IDs that activate this rule when changed
- **Dependencies:** Wire IDs that this rule may depend on (superset of triggers)
- **Outcomes:** Wire IDs that may be written
- **Operation (`perform()`):** Computes output values from input values and reports affected wires

Usually, the task of a rule is:

1. Read values from dependencies via sources
2. Computes new output values
3. If outputs differ from current values (checked via `sink.check()`), they are written
4. Return the set of wire IDs whose values are actually modified

To create custom rule, inherit from `impl::rule_impl`, provide triggers, dependencies and outcomes in the constructor,
and implement the `perform()` method.

For simple binary and unary operations, there are some built-in universal rule implementations.

```cpp
// Unary operation
auto unary_rule = nxon::rule_t{new nxon::impl::unary_rule(
    source, sink,
    [](const nxon::value_t& x) { return x.some_operation(); }
)};

// Binary operation
auto binary_rule = nxon::rule_t{new nxon::impl::binary_rule(
    source_a, source_b, sink,
    [](const nxon::value_t& a, const nxon::value_t& b) { return a.some_operation(b); }
)};
```

When implementing rules that triggers on a clock, you should use `value_storage::delay` to delay the write operation
until all rules in one round are performed, to ensure that rules do not interfere with each other on the same clock
edge.

For example, a custom register with synchronous reset can be implemented as follows:

```cpp
struct reg_rule final : nxon::impl::rule_impl {
    nxon::clock_t clk;
    nxon::source_t rst, in;
    nxon::sink_t out;

    reg_rule(nxon::clock_t clk, nxon::source_t rst, nxon::source_t in, nxon::sink_t out)
        : rule_impl(clk.dependencies(), clk.dependencies() + rst.dependencies() + in.dependencies(), out.outcomes()),
          clk(std::move(clk)), rst(std::move(rst)), in(std::move(in)), out(std::move(out)) {
    }

    nxon::impl::indirect_id_set perform(nxon::value_storage &values) const override {
        static nxon::id_set EMPTY_ID_SET;
        if (const auto rst_v = rst.get(values);
            static_cast<bool>(*rst_v)) [[likely]] {
            if (auto in_v = in.get(values); clk.triggered(values) && out.check(values, *in_v)) {
                values.delay([value = in_v.release(), out = out](auto &cur_values) {
                    out.put(cur_values, std::move(value));
                });
                return nxon::impl::indirect_id_set(out.outcomes());
            }
        }
        return nxon::impl::indirect_id_set(EMPTY_ID_SET);
    }
};
```

##### Potential Holder (`impl::potential_holder`, `impl::indirect_id_set`, `impl::indirect_value_t`)

To avoid unnecessary copies, the return types of `source.get()` and `rule.perform()` are `impl::potential_holder`.

`impl::potential_holder<T>` holds an instance of `T` or a constant reference to it, depending on the argument provided
upon construction. When it holds a reference, it is the responsibility of its creator to ensure that the reference lives
long enough. And users should not store the reference for later use, nor assume its validity after any modifications to
the circuit state.

To get a persistent instance out of a `potential_holder`, use `potential_holder.release()`. It returns a `T` obtained
via move constructor (when holding an instance) or copy constructor (when holding a reference), and invalidates the
potential holder.

#### Using Custom Rules

nxsim allows you to pass a `native_map` to `parse_circuit`. When the parser sees a component, it checks `native_map`with
its `id`.

- If found, it invokes the factory: `factory(context, json)` and adds the returned `rule_t` to the circuit.
- Otherwise, it recursively parses sub-members as usual.

With that, you can replace some components with your custom rules. When writing a custom rule factory, you may want to:

- Get component inputs, outputs and attributes: `json["input"]`, `json["output"]`, `json["attribute"]`
- Use sources/sinks/clocks parsing helpers: `parse_source()`, `parse_sink()`, `parse_clock()`
- Create temporary wires: `ctx.new_tmp_wire()`

For example, to use the custom register rule implemented in the previous section, you can do the following:

```cpp
nxon::rule_t parse_reg(const nxon::parse_context &ctx, const nlohmann::json &json) {
    const auto &input = json["input"];
    const auto &output = json["output"];

    return nxon::rule_t{new reg_rule(
        nxon::parse_clock(input.at(0), true),
        parse_source(input.at(1), ctx),
        parse_source(input.at(2), ctx),
        parse_sink(output.at(0), ctx)
    )};
}

parse_circuit(ctx, nlohmann::json::parse(json), {
    {"reg", std::function(parse_reg)}
});
```