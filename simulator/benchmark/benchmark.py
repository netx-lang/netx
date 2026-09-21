#!/usr/bin/env python3
"""Measure one revision, or compare two revisions using identical NXON inputs."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import shlex
import shutil
import signal
import statistics
import subprocess
import sys
import tempfile
import threading
import time
import tomllib

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = Path(__file__).with_name("designs.toml")
METRICS = "Ir D1mr D1mw DLmr DLmw Bcm Bim"
FLAGS = ["-O3", "-ffast-math", "-fno-finite-math-only", "-std=c++23"]


def capture(*command):
    return subprocess.check_output(command, text=True, stderr=subprocess.PIPE).strip()


def execute(command, *, cwd, log, timeout, stdin=None):
    """Bound the entire process group, including compiler/driver subprocesses."""
    with log.open("w") as output, (stdin or Path(os.devnull)).open("rb") as input_file:
        output.write(shlex.join(map(str, command)) + "\n")
        output.flush()
        process = subprocess.Popen(command, cwd=cwd, stdin=input_file, stdout=output,
                                   stderr=subprocess.STDOUT, start_new_session=True)
        expired = threading.Event()

        def stop():
            try:
                os.killpg(process.pid, signal.SIGKILL)
            except ProcessLookupError:
                pass

        def expire():
            expired.set()
            stop()

        timer = threading.Timer(timeout, expire)
        timer.daemon = True
        timer.start()
        try:
            # wait(timeout=...) polls in Python and can distort short wall runs.
            code = process.wait()
        except BaseException:
            stop()
            process.wait()
            raise
        finally:
            timer.cancel()
            timer.join()
        if expired.is_set():
            raise subprocess.TimeoutExpired(command, timeout)
        if code:
            raise subprocess.CalledProcessError(code, command)


def parse_callgrind(path, metrics):
    events, summary = None, None
    for line in path.read_text().splitlines():
        fields = line.split()
        if fields and fields[0] == "events:":
            events = fields[1:]
        elif fields and fields[0] == "summary:":
            summary = list(map(int, fields[1:]))
    if events is None or summary is None or len(events) != len(summary):
        raise ValueError(f"incomplete Callgrind summary: {path}")
    values = dict(zip(events, summary))
    if any(metric not in values for metric in metrics) or any(v < 0 for v in values.values()):
        raise ValueError(f"invalid or missing Callgrind events: {path}")
    return {metric: values[metric] for metric in metrics}


def unit(metric):
    return {"Ir": "instructions", "Bcm": "mispredicts", "Bim": "mispredicts", "ms": "ms"}.get(metric, "misses")


def render(report, format):
    if format == "json":
        return json.dumps(report, indent=2, allow_nan=False)
    if format == "gh-action":
        # Partial runs must never produce publishable measurements.
        if any(row["status"] != "ok" for row in report["benchmarks"]):
            raise ValueError("cannot export an incomplete run to github-action-benchmark")
        return json.dumps([
            {"name": f'{row["design"]}.{metric}', "unit": unit(metric), "value": value}
            for row in report["benchmarks"] for metric, value in row["head"].items()
        ], indent=2)
    lines = ["## nxsim benchmark", "", f'Mode: `{report["mode"]}`; ratios are head/base.', "",
             "| design | metric | base | head | ratio |", "|---|---|---:|---:|---:|"]
    for row in report["benchmarks"]:
        if row["status"] != "ok":
            lines.append(f'| {row["design"]} | failed | — | — | — |')
            continue
        for metric, value in row["head"].items():
            base = row.get("base", {}).get(metric, "—")
            ratio = row.get("ratio", {}).get(metric)
            lines.append(f'| {row["design"]} | {metric} | {base} | {value} | {ratio if ratio is not None else "—"} |')
    return "\n".join(lines)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("refs", nargs="+", help="HEAD or BASE HEAD")
    parser.add_argument("--format", choices=("md", "json", "gh-action"), default=os.getenv("BENCH_FORMAT", "md"))
    parser.add_argument("--mode", choices=("callgrind", "wall"), default=os.getenv("BENCH_MODE", "callgrind"))
    parser.add_argument("--designs", default=os.getenv("BENCH_DESIGNS"))
    parser.add_argument("--iters", type=int, default=os.getenv("BENCH_ITERS", "3"))
    parser.add_argument("--timeout", type=float, default=os.getenv("BENCH_TIMEOUT", "900"))
    parser.add_argument("--metrics", default=os.getenv("BENCH_METRICS", METRICS))
    parser.add_argument("--benchmarks-dir", default=os.getenv("NETX_BENCHMARKS_DIR"))
    parser.add_argument("--nx", default=os.getenv("NX_BIN", "nx"))
    parser.add_argument("--cxx", default=os.getenv("CXX", "c++"))
    parser.add_argument("--valgrind", default=os.getenv("VALGRIND", "valgrind"))
    parser.add_argument("--output-dir", type=Path, help="Persistent reports and logs (must be empty)")
    args = parser.parse_intermixed_args(argv)
    if len(args.refs) not in (1, 2) or (args.format == "gh-action" and len(args.refs) != 1):
        parser.error("provide one revision, or BASE HEAD for md/json comparison")
    if args.iters < 1 or not 0 < args.timeout < float("inf"):
        parser.error("iters and timeout must be positive and finite")
    if not args.benchmarks_dir:
        parser.error("set NETX_BENCHMARKS_DIR or --benchmarks-dir")
    corpus = Path(args.benchmarks_dir).resolve()
    config = tomllib.loads(MANIFEST.read_text())
    names = args.designs.split() if args.designs else config["default_designs"]
    if not names or len(set(names)) != len(names) or any(n not in config["design"] for n in names):
        parser.error("designs must be unique names from designs.toml")
    metrics = args.metrics.split() if args.mode == "callgrind" else ["ms"]
    if not metrics or len(set(metrics)) != len(metrics):
        parser.error("metrics must be nonempty and unique")
    for name in names:
        design = config["design"][name]
        for path in (design["source"], design["directory"] + "/_netx.toml"):
            if not (corpus / path).is_file():
                parser.error(f"missing corpus input: {corpus / path}")
    tools = {"nx": args.nx, "cxx": args.cxx}
    if args.mode == "callgrind":
        tools["valgrind"] = args.valgrind
    for name, command in tools.items():
        executable = shutil.which(command)
        if executable is None:
            parser.error(f"command not found: {command}")
        tools[name] = str(Path(executable).absolute())
    refs = dict(zip(["head"] if len(args.refs) == 1 else ["base", "head"], args.refs))
    revisions = {name: {"ref": ref, "sha": capture("git", "-C", str(ROOT), "rev-parse", "--verify", ref + "^{commit}")}
                 for name, ref in refs.items()}
    cache = {key: os.getenv("BENCH_" + key, value) for key, value in
             {"I1": "32768,8,64", "D1": "32768,8,64", "LL": "8388608,16,64"}.items()}
    output = args.output_dir.resolve() if args.output_dir else Path(tempfile.mkdtemp(prefix="nxsim-results-"))
    if output.exists() and any(output.iterdir()):
        parser.error(f"output directory must be empty: {output}")
    output.mkdir(parents=True, exist_ok=True)
    report = {"schema_version": 1, "revisions": revisions, "mode": args.mode,
              "corpus_sha": capture("git", "-C", str(corpus), "rev-parse", "HEAD"),
              "corpus_dirty": bool(capture("git", "-C", str(corpus), "status", "--porcelain")),
              "manifest_sha256": hashlib.sha256(MANIFEST.read_bytes()).hexdigest(),
              "tools": {name: {"path": command, "version": capture(command, "--version")}
                        for name, command in tools.items()},
              "compiler_flags": FLAGS, "cache_model": cache if args.mode == "callgrind" else None,
              "timeout_seconds": args.timeout, "iterations": args.iters if args.mode == "wall" else 1,
              "benchmarks": []}
    print(f"reports and logs: {output}", file=sys.stderr)
    with tempfile.TemporaryDirectory(prefix="nxsim-build-") as scratch:
        scratch = Path(scratch)
        worktrees = []
        try:
            for name, revision in revisions.items():
                tree = scratch / name
                subprocess.run(["git", "-C", str(ROOT), "worktree", "add", "--detach", str(tree), revision["sha"]],
                               check=True, stdout=sys.stderr)
                worktrees.append(tree)
                include = scratch / f"include-{name}"
                include.mkdir()
                (include / "nxsim").symlink_to(tree / "simulator/src", target_is_directory=True)
            for name in names:
                design = config["design"][name]
                row = {"design": name, "status": "failed"}
                report["benchmarks"].append(row)
                directory = output / name
                directory.mkdir()
                nxon = directory / "input.json"
                stage = "compile"
                try:
                    command = [tools["nx"], "compile", "_netx.toml", "--top", design["top"]]
                    if design.get("attributes"):
                        command += ["--attrs", *design["attributes"]]
                    execute(command + ["--minimal", "--output", str(nxon)], cwd=corpus / design["directory"],
                            log=directory / "compile.log", timeout=args.timeout)
                    row["nxon_sha256"] = hashlib.sha256(nxon.read_bytes()).hexdigest()
                    binaries = {}
                    for ref in refs:
                        stage = f"build-{ref}"
                        binary = scratch / f"{name}.{ref}"
                        execute([tools["cxx"], *FLAGS, f"-I{scratch / ('include-' + ref)}",
                                 str(corpus / design["source"]), "-o", str(binary), *design.get("flags", [])],
                                cwd=scratch, log=directory / f"{stage}.log", timeout=args.timeout)
                        binaries[ref] = str(binary)
                    samples = {ref: [] for ref in refs}
                    cwd = corpus / design.get("working_directory", design["directory"])
                    # Alternate A/B and B/A to reduce ordering bias for wall measurements.
                    for iteration in range(args.iters if args.mode == "wall" else 1):
                        order = list(refs) if iteration % 2 == 0 else list(reversed(refs))
                        for ref in order:
                            stage = f"run-{ref}-{iteration}"
                            command = [binaries[ref]]
                            cg = directory / f"{ref}.callgrind"
                            if args.mode == "callgrind":
                                command = [tools["valgrind"], "--tool=callgrind", "--cache-sim=yes", "--branch-sim=yes",
                                           *[f"--{k}={v}" for k, v in cache.items()], f"--callgrind-out-file={cg}", *command]
                            start = time.monotonic_ns()
                            execute(command, cwd=cwd, log=directory / f"{stage}.log", timeout=args.timeout, stdin=nxon)
                            elapsed = (time.monotonic_ns() - start) / 1e6
                            if args.mode == "callgrind":
                                row[ref] = parse_callgrind(cg, metrics)
                            else:
                                samples[ref].append(elapsed)
                    if args.mode == "wall":
                        row["samples_ms"] = samples
                        for ref in refs:
                            row[ref] = {"ms": statistics.median(samples[ref])}
                    if "base" in refs:
                        row["ratio"] = {m: row["head"][m] / row["base"][m] if row["base"][m] else None for m in metrics}
                        if args.mode == "wall":
                            row["ratio"]["ms"] = statistics.median(h / b for b, h in zip(samples["base"], samples["head"]))
                    row["status"] = "ok"
                    print(f"done {name}", file=sys.stderr)
                except (OSError, ValueError, subprocess.SubprocessError) as exc:
                    row.update(stage=stage, error=str(exc))
                    print(f"failed {name} at {stage}: {exc}; logs: {directory}", file=sys.stderr)
                (output / "result.json").write_text(json.dumps(report, indent=2) + "\n")
        finally:
            for tree in reversed(worktrees):
                subprocess.run(["git", "-C", str(ROOT), "worktree", "remove", "--force", str(tree)],
                               check=True, stdout=sys.stderr)
    failed = any(row["status"] != "ok" for row in report["benchmarks"])
    (output / "summary.md").write_text(render(report, "md") + "\n")
    if not failed:
        if len(refs) == 1:
            (output / "bench.json").write_text(render(report, "gh-action") + "\n")
        print(render(report, args.format))
    elif args.format != "gh-action":
        print(render(report, args.format))
    return 1 if failed else 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError, subprocess.SubprocessError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        raise SystemExit(1)
