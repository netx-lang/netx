# NetX

[![CI](https://github.com/pascal-lab/NetX/actions/workflows/ci.yml/badge.svg)](https://github.com/pascal-lab/NetX/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![C++](https://img.shields.io/badge/C++-23-blue.svg)](https://en.cppreference.com/)

NetX is a novel hardware description language (HDL) that employs a schematic programming model to intuitively ensure code-to-circuit consistency. In NetX, code is arranged to resemble circuit schematics, enabling a "what you design is what you get" paradigm that empowers designers to manipulate the exact structure of circuits without obscuration.

The NetX toolchain includes a compiler, a package manager, a simulator, and a visualizer implemented in this repository.

## Build and Installation

### Prerequisites

- Java Development Kit (JDK) 25 or later
- C++23 or later compliant compiler
- CMake 3.28 or later
- Boost 1.80 or later
- [nlohmann/json](https://github.com/nlohmann/json) 3.11.3 or later

For Ubuntu/Debian-based systems, you can install the prerequisites using:

```bash
sudo apt update
sudo apt install -y openjdk-25-jdk g++-15 cmake libboost-all-dev nlohmann-json3-dev
```

### Installation

To install NetX, use the cross-platform installation script:

```bash
# Install to default location (~/.local/netx)
./install.sh

# Install to custom location (Make sure you have write permissions to the directory)
INSTALL_PREFIX=/usr/local ./install.sh
```

The installation script will build the NetX toolchain and install it to the specified location. By default, it installs
to `~/.local/netx`. Also, the script will automatically configure your environment variables to your shell configuration
file (`.bashrc`, `.zshrc`, `config.fish`, etc.), so you can use the `nx` command directly after installation, including:

- `PATH` - for the `nx` and `nxsim` command
- `CPLUS_INCLUDE_PATH` and `C_INCLUDE_PATH` - for C++ compiler include paths
- `LIBRARY_PATH` - for linker library search paths
- `LD_LIBRARY_PATH` - for runtime library loading

After installation, either restart your terminal or run:
```bash
source ~/.bashrc  # or ~/.zshrc, depending on your shell
```

To uninstall NetX completely (remove the files and cleanup environment variables):

```bash
./uninstall.sh
```

## Usage
### Compilation
The `nx` command-line tool is the main entry point for using NetX.

```bash
Usage: NetX [-hV] [COMMAND]
Main CLI for the NetX tool
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  compile  Compile the NetX source code into NXON representation
  dump     Dump NetX source code or NXON representation into backend formats
```

Here, we have the following subcommands:

- `compile` compiles the NetX source code into a JSON representation (NXON).

  ```shell
  Usage: NetX compile [-m] [-o=<output>] [-t=<top>] [-a=<args>...]... SOURCE
  Compile the NetX source code into NXON representation
        SOURCE              the source file to compile
    -a, --args=<args>...    arguments to pass to the top component, in the form
                              of --args a=true b=2 c=2'b01
    -m, --minimal           output NXON in a single line without formatting
    -o, --output=<output>   output file
    -t, --top=<top>         name of the top componen
  ```

- `dump` dumps the NetX source code to backend representations. Currently only Verilog and DOT formats are supported,
  but more formats will be added in the future.

    ```shell
    Usage: NetX dump [COMMAND]
    Dump NetX source code or NXON representation into backend formats
    Commands:
      verilog  Dump to Verilog format
      dot      Dump to DOT format (Graphviz)
    ```

    - For Verilog, use `nx dump verilog SOURCE` to dump the NetX source code or NXON representation into Verilog format.

    - For DOT, use `nx dump dot SOURCE` to dump the NetX source code or NXON representation into DOT format. Note that
      visualization requires [Graphviz](https://graphviz.gitlab.io/) for further rendering:

      ```shell
      nx dump dot my_design.nx -o output.dot
      dot -T pdf output.dot -o output.pdf
      ```

### Clock Types

NetX rejects implicit conversions between clock and bit signals. Use `AS_BIT` to
manipulate a clock as a one-bit value and `AS_CLK` to convert a one-bit value into
a clock explicitly:

```text
wire clk_in, clk_out of clock();
wire gated of bit(1);
gated <> AND <> [AS_BIT <> clk_in, enable];
clk_out <> AS_CLK <> gated;
```

Both primitives are zero-cost conversions and produce direct connections in the
generated hardware. Clock-to-clock connections, register clock inputs, and clock
forwarding through inferred wires, `TO`, or `DUP` remain supported.

### Package Mangement

The package manager is built into the `nx` command, allowing you to manage NetX packages easily.
For example, given a project with a `_next.toml` file:

```toml
[project]
name = "std"
version = "0.1.0"
include = [ "src", "test" ]
```
You can compile the project with the following command:
```bash
nx compile _next.toml --top arith_test.sqrt32_test
```
This will compile the project with the specified top-level component `sqrt32_test` under the package `arith_test`.
Moreover, you can import other packages by adding them to the `_next.toml` file, like:

```toml
[dependencies]
std = { version = "0.1.0", git = "git@github.com:pascal-lab/NetX-std.git" }
```
The package manager will automatically download the package from the specified Git repository, resolve its dependencies, and make it available for compilation.

### Simulation

#### Interactive Simulation

The NetX simulator supports interactive simulation using the `nxsim_cli` command, given the compiled JSON hardware representation (NXON).

```bash
nx compile my_design.nx --minimal -o circuit.nxon
nxsim_cli circuit.nxon
```

#### Using the nxsim Library

You can also use the `nxsim` library to build your own simulation testbench. The library provides a C++ API to interact with the compiled NetX design, allowing you to simulate and test your designs programmatically.

`nxsim` is a header-only library, so you only need to include the relevant headers in your C++ code.

```cpp
#include <nxsim/simulation.h>
```

To enable parallel simulation, remember to link against OpenMP by adding the `-fopenmp` flag when compiling your C++ code.
For more details on how to use the `nxsim` library, refer to [NetX Simulator Documentation](https://github.com/pascal-lab/NetX/blob/main/simulator/README.md).


Check the testbenches in the [NetX standard library](https://github.com/pascal-lab/NetX-std) for examples. More examples
can be found in [NetX-FFT](https://github.com/naiiren/NetX-FFT)
and [NetX-RISC-V](https://github.com/naiiren/NetX-RISC-V).
