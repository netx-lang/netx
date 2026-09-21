# nxsim benchmark

Benchmark runner for the simulator. The design corpus and the performance suite
live in [`netx-lang/benchmarks`](https://github.com/netx-lang/benchmarks); this
directory only holds the runner.

nxsim is header-only, so a revision is a git worktree whose `simulator/src` is
put on the include path as `<nxsim/...>`. Each design is compiled to NXON once
and fed to the revisions, so only the simulator code differs.

Default mode is Callgrind with cache simulation: instruction and miss counts are
deterministic (no frequency noise) yet still show cache locality, and the pinned
cache model makes them machine-independent. `BENCH_MODE=wall` is a rough sanity
check only.

## Usage

```bash
NETX_BENCHMARKS_DIR=/path/to/benchmarks \
NX_BIN=$PWD/build/install/nx/bin/nx \
    simulator/benchmark/compare.sh <ref>            # one revision
    simulator/benchmark/compare.sh <base> <head>    # compare two
```

Needs a C++23 compiler with Boost + nlohmann/json, `valgrind`, and a
`netx-lang/benchmarks` checkout. `BENCH_FORMAT` selects the output: `md`
(default, table), `json` (base/head/ratio), `gh-action` (flat array for
[`github-action-benchmark`](https://github.com/benchmark-action/github-action-benchmark),
single revision).

Other env: `BENCH_MODE`, `BENCH_DESIGNS`, `BENCH_ITERS`, `BENCH_TIMEOUT`,
`BENCH_METRICS`, `BENCH_I1`/`BENCH_D1`/`BENCH_LL`.

CI (`.github/workflows/bench.yml`) runs `BENCH_FORMAT=gh-action` on a bounded
subset and lets `github-action-benchmark` handle the baseline, PR comment and
alerts; the full corpus is available via `workflow_dispatch`.
