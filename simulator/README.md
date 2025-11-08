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
#include "simulation.h"
```

And it is allowed to configure the library by defining some macros before including the header:

#### Custom Backend

nxsim has a configurable arithmetic backend for wire values to achieve better performance when the width is limited.

By default, it uses `uint64_t` as backend.

To use a custom backend, define `NX_BACKEND` before including nxsim headers.

```cpp
#include <boost/multiprecision/cpp_int.hpp>
#define NX_BACKEND boost::multiprecision::cpp_int
#include "simulation.h"
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
#include "simulation.h"
```

#### KaHIP Partitioner

nxsim optionally depends on KaHIP to provide an alternative circuit partitioner.

If CMake is used to add nxsim to your project, this partitioner is automatically enabled whenever possible.

In case CMake is not used, you can define `NX_KAHIP` before including nxsim headers to enable it manually.

```cpp
#define NX_KAHIP
#include "simulation.h"
```

### Basic Usage

#### Parsing

To get a circuit from a NXON file, use `nxon::parse_circuit`.

```cpp
#include "simulation.h"

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

nxsim provides several wrappers around `nxon::parse_context` to provide additional functionalities.

To use a wrapper, pass it as an additional parameter to `nxon::parse_circuit`.

```cpp
custon_parse_context ctx; 
nxon::parse_circuit(ctx, input);
```

#### Waveform Dumping

`nxon::dumping_parse_context` is a wrapper that provides waveform dumping functionality.

After parsing, it is needed to call `init_dump` to initialize the VCD files before any manipulation. It automatically
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

There are two wrappers around this: `nxon::partitioned_parse_context` (Sequential Partitioned) and
`nxon::parallel_parse_context` (Parallel Execution). Both require calling `init_partition` to perform partitioning
before any manipulation.

```cpp
nxon::partitioned_parse_context ctx;
nxon::parse_circuit(ctx, input);
ctx.init_partition();

// Operations automatically propagate across partitions
ctx.update_by_name("input", value);
```

You may wonder what a `nxon::paritioned_parse_context` without parallel execution can achieve. In fact, the propagation
order of updates in `nxon::parse_context` is not always ideal, which may lead to many redundant updates. When the
circuit is partitioned, unlike `nxon::parse_context`, updates will first propagate in their current part of circuit,
reach a local fixed point, and then propagate to the others. For some circuits, this change in order can reduce the
total number of updates by up to 30%.

##### Partitioner

`nxon::partitioned_parse_context` and `nxon::parallel_parse_context` accept a partitioner as template parameter to
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

#### Sources, Sinks, and Rules

nxsim uses a **rule-based simulation model** where circuit behavior is defined by rules that read from sources, do
calculation and write to sinks.

##### **Source (`source_t`)**

A source represents a **read operation** from the circuit state:

- **Dependencies:** Set of wire IDs that may be read
- **Length:** Width of the value produced
- **Operation:** Extracts and potentially transforms wire values

**Built-in source types:**

```cpp
// Direct wire access
auto src = data_factory::id_source(wire_id, bit_width);

// Constant value
auto const_src = data_factory::constant_source(nxon::value_t::parse_fixed(8, "42"));

// Bit selection (slice)
auto slice_src = data_factory::select_source(wire_id, from_bit, to_bit);
```

##### **Sink (`sink_t`)**

A sink represents a **write operation** to the circuit state:

- **Outcomes:** Set of wire IDs that may be modified
- **Length:** Expected width of incoming values
- **Operations:**
    - `check()`: Returns true if writing would change the current value
    - `put()`: Writes the value to circuit state

**Built-in sink types:**

```cpp
// Direct wire write
auto sink = data_factory::id_sink(wire_id, bit_width);

// Bit selection write (modifies slice of a wire)
auto slice_sink = data_factory::select_sink(wire_id, from_bit, to_bit);

// Sink adapters (automatic width conversion)
auto signed_sink = data_factory::signed_resize_sink_adapter(underlying_sink); // Resize as signed integer
auto unsigned_sink = data_factory::unsigned_resize_sink_adapter(underlying_sink); // Resize as unsigned integer
```

##### **Rule (`rule_t`)**

A rule defines a **computational relationship** between sources and sinks:

- **Triggers:** Wire IDs that activate this rule when changed
- **Dependencies:** Wire IDs that this rule may depend on (superset of triggers)
- **Outcomes:** Wire IDs that may be written
- **Operation:** Computes output values from input values

Usually, the task of a rule is:

1. Read values from dependencies via sources
2. Computes new output values
3. If outputs differ from current values (checked via `sink.check()`), they are written
4. Return the set of wire IDs whose values are actually modified

#### Create and Use Native Rules

TODO