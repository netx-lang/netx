# nxsim benchmarks

`benchmark.py` is the Python 3.11+ runner. It measures a
committed simulator revision, or compares two revisions with identical NXON
inputs. `designs.toml` owns the default selection, tops, parameters, driver paths
and extra linker/compiler options. No third-party Python packages are needed.

The design corpus lives in [netx-lang/benchmarks](https://github.com/netx-lang/benchmarks).
CI pins its commit in `bench.yml`; update that SHA and this manifest together.
The pinned corpus contains the seven default designs: rv32i, ddr2, ethmac,
sha256, axi, fft and aes128. It does not contain the previously listed `shapes`
drivers, so chain/fanin/fanout/register_bank are not advertised as working cases.

## Local usage

Requires Linux, Git, Python 3.11+, a C++23 compiler, Boost, nlohmann/json,
OpenMP (FFT), OpenSSL development files (AES), and `/usr/bin/sha256sum`
(SHA256 reference implementation in the pinned corpus). Callgrind mode also requires Valgrind.
The SHA256 driver hardcodes that path: it works on Ubuntu but needs a compatible
filesystem environment on NixOS. The runner does not patch the pinned driver.
The installed `nx` compiler needs JDK 25 and access to netx-std.

```bash
./gradlew installDist
export NETX_BENCHMARKS_DIR=/path/to/benchmarks
export NX_BIN="$PWD/build/install/nx/bin/nx"
export CXX=g++-15

# Single revision; md and json both support this.
python3 simulator/benchmark/benchmark.py HEAD --designs rv32i --output-dir /tmp/rv32i-result

# Both engines consume NXON generated once by the selected NX_BIN.
python3 simulator/benchmark/benchmark.py upstream/main HEAD --format json \
    --output-dir /tmp/compare-result

# Alternating A/B, B/A wall runs, with median paired ratios.
python3 simulator/benchmark/benchmark.py upstream/main HEAD --mode wall --iters 5
```

Output directories must be empty. Without `--output-dir`, the runner creates and
prints a persistent directory under the system temporary directory. It contains:

- `result.json`: revisions, corpus SHA/dirty state, tool versions, manifest and
  NXON hashes, metrics, wall samples, and explicit failure stages.
- `summary.md`: a readable table, including failed designs.
- `bench.json`: github-action-benchmark format, produced only for a complete,
  successful single-revision run.
- Per-design NXON inputs, compile/build/run logs and raw Callgrind output.

Only reports go to stdout; progress goes to stderr. Any selected design failing
compilation, execution, timeout or metric validation makes the command exit
nonzero. A partial run never produces `bench.json`. Reports and logs survive
failure; temporary binaries and Git worktrees are removed.

`--timeout` bounds each compilation, build and execution, including subprocesses.
`--format gh-action` accepts exactly one revision. `--help` lists all options.
Existing environment defaults remain available: `BENCH_MODE`, `BENCH_FORMAT`,
`BENCH_DESIGNS`, `BENCH_ITERS`, `BENCH_TIMEOUT`, `BENCH_METRICS`, `NX_BIN`, `CXX`,
`VALGRIND`, and `NETX_BENCHMARKS_DIR`. `BENCH_I1`, `BENCH_D1`, `BENCH_LL` override
cache models. There is no permissive CI mode or `BENCH_KEEP_TMP`: failures always
fail, and diagnostic output is always retained.

## CI and interpretation

The workflow separates execution from publishing:

- `measure` has read-only repository access and checks out without persisted
  credentials. Same-repository PRs can use the netx-std deploy key; fork PRs are
  skipped because that private dependency cannot be fetched with their token.
- `report-pr` downloads the successful report and comments with PR write
  permission. It does not execute PR scripts or update history.
- `publish` only runs on pushes to main and has contents write permission for
  github-action-benchmark's gh-pages history. Manual runs only produce artifacts
  and a job summary, even when dispatched on main.

The measurement job uploads diagnostics on failure as well as success. PR runs
cancel obsolete PR runs; main runs are not cancelled mid-publication. The action
is pinned to the v1.22.2 commit and uses its default `gh-pages` branch and
`dev/bench` history location. The first main publication initializes history;
before that, PR reporting explains the missing baseline and retains the measurement
artifact without attempting a comparison.

Callgrind reduces frequency and scheduling noise, but a fixed cache model does
**not** make results universally deterministic across machines. Compiler/library
versions, CPU-dependent library dispatch, memory layout and workload changes can
still alter counts. The runner records tool versions and input hashes so changes
can be investigated. The Ubuntu/PPA packages are versioned by major release, not
locked byte-for-byte; the corpus's netx-std version dependency is also resolved
by nx. Treat history as a regression signal, and confirm changes with a two-ref
run in one environment before attributing them to simulator code.

The CI builds nx from the current checkout, so compiler/backend changes may
change the NXON workload and now trigger PR measurements too. In contrast, a
local two-ref comparison compiles each design just once. Drivers are measured
end-to-end (JSON parsing, elaboration, simulation, checks and driver overhead),
not just simulator hot loops. Wall time includes process launch and logging and
is a local diagnostic, not a CI performance gate. All Callgrind metrics remain
available; the 5% alert is informational and does not fail a PR.

## Validation

```bash
python3 -m unittest discover -s simulator/benchmark -p 'test_*.py'
actionlint .github/workflows/bench.yml
```

Tests use isolated Git repositories and executable tool doubles to verify stdin,
include paths, failures, timeout, metric validation, output formats and worktree
cleanup. They do not require the private corpus dependency.
