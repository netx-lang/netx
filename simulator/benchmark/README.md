# nxsim benchmarks

This directory contains the benchmark runner for `nxsim`. The benchmark designs
live in the pinned `netx-lang/benchmarks` repository; `designs.toml` describes
which designs to run and how to build their drivers.

## Requirements

- Python 3.11+
- Git
- A C++23 compiler
- Boost and nlohmann/json
- Valgrind for the default Callgrind mode
- A built `nx` compiler with access to `netx-std`

The current benchmark corpus also expects OpenMP, OpenSSL, and
`/usr/bin/sha256sum` for some designs.

## Usage

```bash
./gradlew installDist

export NETX_BENCHMARKS_DIR=/path/to/benchmarks
export NX_BIN="$PWD/build/install/nx/bin/nx"
export CXX=g++-15

# Run one revision.
python3 simulator/benchmark/benchmark.py HEAD

# Compare two revisions.
python3 simulator/benchmark/benchmark.py upstream/main HEAD --format json

# Run a smaller selection.
python3 simulator/benchmark/benchmark.py HEAD \
  --designs rv32i fft --output-dir /tmp/nxsim-benchmark
```

The default mode is Callgrind. Use `--mode wall` for a rough wall-clock
measurement. Available output formats are `md`, `json`, and `gh-action`.
The GitHub Actions workflow uses the last format for
`github-action-benchmark`.

Each run writes a result directory containing:

- `result.json` with revisions, tool versions, inputs, and measurements;
- `summary.md` with a readable table;
- `bench.json` for a successful single-revision run;
- per-design logs and raw Callgrind output.

A failed or incomplete design makes the runner exit with a nonzero status and
will not produce `bench.json`. Temporary binaries and Git worktrees are removed
after the run.

The benchmark corpus revision is pinned in `.github/workflows/bench.yml`. Update
that revision deliberately when the corpus changes.

## Validation

```bash
python3 -m unittest discover -s simulator/benchmark -p 'test_*.py'
actionlint .github/workflows/bench.yml
```
