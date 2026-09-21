#!/usr/bin/env bash
#
# nxsim benchmark runner.
#
# Measures the shared netx-lang/benchmarks design corpus against one or two
# revisions of the simulator.
#
#     compare.sh <ref>            measure one revision
#     compare.sh <base> <head>    compare two revisions
#
# nxsim is header-only, so a revision is a git worktree whose simulator/src is
# put on the include path as <nxsim/...>. Each design is compiled to NXON once
# and fed to the revisions, so only the simulator code differs.
#
# The default is Callgrind with cache simulation: instruction and miss counts
# are deterministic (no frequency noise) and the pinned cache model makes them
# machine-independent. BENCH_MODE=wall measures wall clock instead.
#
# Environment:
#     NETX_BENCHMARKS_DIR  checkout of netx-lang/benchmarks (required)
#     NX_BIN               path to the `nx` compiler (default: nx)
#     VALGRIND             path to valgrind (default: valgrind)
#     BENCH_MODE           callgrind | wall (default: callgrind)
#     BENCH_FORMAT         md | json | gh-action (default: md)
#     BENCH_ITERS          paired iterations in wall mode (default: 3)
#     BENCH_DESIGNS        space-separated design list (default: all)
#     BENCH_TIMEOUT        per-run timeout in seconds (default: 900)
#     BENCH_METRICS        callgrind events to report
#     BENCH_I1/BENCH_D1/BENCH_LL   callgrind cache model
#
set -euo pipefail

if [ $# -ge 2 ]; then
    BASE_REF=$1
    HEAD_REF=$2
else
    BASE_REF=""
    HEAD_REF=${1:-}
fi
if [ -z "$HEAD_REF" ]; then
    sed -n '2,30p' "$0" | sed 's/^# \{0,1\}//'
    exit 2
fi

repo=$(git rev-parse --show-toplevel)
bench_dir=${NETX_BENCHMARKS_DIR:?set NETX_BENCHMARKS_DIR to a netx-lang/benchmarks checkout}
nx_bin=${NX_BIN:-nx}
valgrind_bin=${VALGRIND:-valgrind}
mode=${BENCH_MODE:-callgrind}
format=${BENCH_FORMAT:-md}
iters=${BENCH_ITERS:-3}
timeout_s=${BENCH_TIMEOUT:-900}
designs=${BENCH_DESIGNS:-"rv32i ddr2 ethmac sha256 axi fft aes128 chain fanin fanout register_bank"}
METRICS=${BENCH_METRICS:-"Ir D1mr D1mw DLmr DLmw Bcm Bim"}

have_base=0
[ -n "$BASE_REF" ] && have_base=1
if [ "$format" != gh-action ] && [ "$have_base" = 0 ]; then
    echo "BENCH_FORMAT=$format needs a base ref (usage: compare.sh <base> <head>)" >&2
    exit 2
fi

# A fixed cache model keeps callgrind independent of the host's detected caches,
# so absolute counts from different machines are comparable.
I1_MODEL=${BENCH_I1:-32768,8,64}
D1_MODEL=${BENCH_D1:-32768,8,64}
LL_MODEL=${BENCH_LL:-8388608,16,64}

case "$mode" in callgrind | wall) ;; *) echo "BENCH_MODE must be callgrind|wall" >&2; exit 2 ;; esac
case "$format" in md | json | gh-action) ;; *) echo "BENCH_FORMAT must be md|json|gh-action" >&2; exit 2 ;; esac

base_sha=""
head_sha=$(git -C "$repo" rev-parse "$HEAD_REF")
[ "$have_base" = 1 ] && base_sha=$(git -C "$repo" rev-parse "$BASE_REF")

# ---------------------------------------------------------------------------
# Per-design manifest. One uniform invocation for every design, so the runner
# does not depend on the designs' own Makefiles.
# ---------------------------------------------------------------------------
design_dir() { case $1 in axi) echo "axi/axi_test" ;; chain | fanin | fanout | register_bank) echo "shapes" ;; *) echo "$1" ;; esac; }
design_top() {
    case $1 in
        rv32i) echo CORE ;; ddr2) echo DDR2 ;; ethmac) echo ETHMAC ;;
        sha256) echo SHA256 ;; axi) echo AXI_RAM_TEST_TOP ;;
        fft) echo fft8 ;; aes128) echo AES128 ;;
        chain) echo CHAIN ;; fanin) echo FANIN ;;
        fanout) echo FANOUT ;; register_bank) echo REGISTER_BANK ;;
    esac
}
design_attrs() {
    case $1 in
        ethmac) echo "frame_capacity=9" ;;
        chain) echo "length=4096" ;; fanin) echo "arity=16384" ;;
        fanout) echo "count=16384" ;; register_bank) echo "count=4096" ;;
        *) echo "" ;;
    esac
}
design_src() {
    case $1 in
        axi) echo "axi/axi_test/main.cpp" ;;
        chain | fanin | fanout | register_bank) echo "shapes/main.cpp" ;;
        *) echo "$1/main.cpp" ;;
    esac
}
design_flags() { case $1 in fft) echo "-fopenmp" ;; aes128) echo "-lcrypto" ;; *) echo "" ;; esac; }
design_cwd() { case $1 in rv32i) echo "$bench_dir/rv32i" ;; *) echo /tmp ;; esac; }

tmp=$(mktemp -d)
cleanup() {
    if [ -n "${BENCH_KEEP_TMP:-}" ]; then
        log "kept $tmp"
    else
        rm -rf "$tmp"
    fi
    git -C "$repo" worktree prune >/dev/null 2>&1 || true
}
trap cleanup EXIT

mkdir -p "$tmp/inc-base" "$tmp/inc-head" "$tmp/json" "$tmp/bin" "$tmp/log"
git -C "$repo" worktree add --detach --force "$tmp/head" "$head_sha" >/dev/null
ln -s "$tmp/head/simulator/src" "$tmp/inc-head/nxsim"

# Which revisions to measure. gh-action only needs the revision under test.
refs=(head)
if [ "$have_base" = 1 ] && [ "$format" != gh-action ]; then
    refs=(base head)
    git -C "$repo" worktree add --detach --force "$tmp/base" "$base_sha" >/dev/null
    ln -s "$tmp/base/simulator/src" "$tmp/inc-base/nxsim"
fi

log() { printf '%s\n' "$*" >&2; }

compile_design() { # name
    local name=$1 dir top attrs
    dir=$(design_dir "$name"); top=$(design_top "$name"); attrs=$(design_attrs "$name")
    local -a attr_args=()
    if [ -n "$attrs" ]; then
        attr_args=(--attrs)
        # shellcheck disable=SC2206
        attr_args+=($attrs)
    fi
    (cd "$bench_dir/$dir" && "$nx_bin" compile _netx.toml --top "$top" "${attr_args[@]}" \
        --minimal --output "$tmp/json/$name.json") >"$tmp/log/compile-$name.log" 2>&1
}

build_bin() { # name ref
    local name=$1 ref=$2 src flags
    src=$(design_src "$name"); flags=$(design_flags "$name")
    # shellcheck disable=SC2086
    c++ -O3 -ffast-math -fno-finite-math-only -std=c++23 \
        -I"$tmp/inc-$ref" $flags -o "$tmp/bin/$name.$ref" "$bench_dir/$src" \
        >"$tmp/log/build-$name-$ref.log" 2>&1
}

run_wall_once() { # name ref -> milliseconds
    local name=$1 ref=$2 cwd t0 t1
    cwd=$(design_cwd "$name")
    t0=$(date +%s%N)
    (cd "$cwd" && timeout "$timeout_s" "$tmp/bin/$name.$ref" \
        <"$tmp/json/$name.json" >/dev/null 2>&1) || true
    t1=$(date +%s%N)
    echo $(((t1 - t0) / 1000000))
}

run_callgrind() { # name ref
    local name=$1 ref=$2 cwd out
    cwd=$(design_cwd "$name")
    out="$tmp/log/cg-$name-$ref"
    (cd "$cwd" && timeout "$timeout_s" "$valgrind_bin" --tool=callgrind \
        --cache-sim=yes --branch-sim=yes \
        --I1="$I1_MODEL" --D1="$D1_MODEL" --LL="$LL_MODEL" \
        --callgrind-out-file="$out.out" \
        "$tmp/bin/$name.$ref" <"$tmp/json/$name.json" >/dev/null) >"$out.log" 2>&1 || true
}

# Read one event total from a callgrind output file via its self-describing
# `events:`/`summary:` header, so we never scrape the stderr summary.
cg_get() { # outfile metric -> value (empty if missing)
    awk -v want="$2" '
        /^events:/  { for (i = 2; i <= NF; i++) idx[$i] = i - 1; next }
        /^summary:/ { for (i = 2; i <= NF; i++) val[i - 1] = $i; found = 1 }
        END {
            if (!found || !(want in idx)) exit 1
            print val[idx[want]]
        }
    ' "$1" 2>/dev/null || true
}

median() { sort -n | awk '{a[NR]=$1} END { if (NR) print a[int((NR + 1) / 2)] }'; }

declare -A results=()
declare -A status=()

for name in $designs; do
    if ! compile_design "$name"; then
        log "skip $name: nx compile failed (see $tmp/log/compile-$name.log)"
        status[$name]=compile-failed
        continue
    fi
    ok=1
    for ref in "${refs[@]}"; do
        if ! build_bin "$name" "$ref"; then
            log "skip $name: build against $ref failed (see $tmp/log/build-$name-$ref.log)"
            status[$name]=build-failed
            ok=0
            break
        fi
    done
    [ "$ok" = 1 ] || continue

    if [ "$mode" = callgrind ]; then
        for ref in "${refs[@]}"; do
            run_callgrind "$name" "$ref"
            for metric in $METRICS; do
                results[$name,$ref,$metric]=$(cg_get "$tmp/log/cg-$name-$ref.out" "$metric")
            done
        done
        if [ -z "${results[$name,head,Ir]:-}" ]; then
            log "skip $name: callgrind produced no summary (see $tmp/log/cg-$name-head.log)"
            status[$name]=callgrind-failed
            continue
        fi
    else
        for ref in "${refs[@]}"; do
            vals=()
            for _ in $(seq 1 "$iters"); do vals+=("$(run_wall_once "$name" "$ref")"); done
            results[$name,$ref,ms]=$(printf '%s\n' "${vals[@]}" | median)
        done
    fi
    status[$name]=ok
    log "done $name"
done

[ "$mode" = callgrind ] && metric_list=$METRICS || metric_list="ms"

# ---------------------------------------------------------------------------
# Report
# ---------------------------------------------------------------------------
ratio() { awk -v a="$1" -v b="$2" 'BEGIN { if (a + 0 > 0) printf "%.4f", b / a }'; }
unit_of() { case $1 in Ir) echo instructions ;; Bcm | Bim) echo mispredicts ;; ms) echo ms ;; *) echo misses ;; esac; }

emit_gh_action() {
    local first=1
    printf '['
    for name in $designs; do
        [ "${status[$name]:-}" = ok ] || continue
        for metric in $metric_list; do
            v=${results[$name,head,$metric]:-}
            [ -n "$v" ] || continue
            [ "$first" = 1 ] || printf ','
            first=0
            printf '\n  {"name": "%s.%s", "unit": "%s", "value": %s}' \
                "$name" "$metric" "$(unit_of "$metric")" "$v"
        done
    done
    printf '\n]\n'
}

emit_json() {
    printf '{\n'
    printf '  "base": {"ref": "%s", "sha": "%s"},\n' "$BASE_REF" "$base_sha"
    printf '  "head": {"ref": "%s", "sha": "%s"},\n' "$HEAD_REF" "$head_sha"
    printf '  "mode": "%s",\n' "$mode"
    [ "$mode" = callgrind ] && printf '  "cache_model": {"I1": "%s", "D1": "%s", "LL": "%s"},\n' \
        "$I1_MODEL" "$D1_MODEL" "$LL_MODEL"
    printf '  "benchmarks": [\n'
    first=1
    for name in $designs; do
        [ "${status[$name]:-}" = ok ] || continue
        [ "$first" = 1 ] || printf ',\n'
        first=0
        printf '    {"design": "%s", "base": {' "$name"
        sep=""
        for metric in $metric_list; do
            printf '%s"%s": %s' "$sep" "$metric" "${results[$name,base,$metric]:-null}"; sep=", "
        done
        printf '}, "head": {'
        sep=""
        for metric in $metric_list; do
            printf '%s"%s": %s' "$sep" "$metric" "${results[$name,head,$metric]:-null}"; sep=", "
        done
        printf '}, "ratio": {'
        sep=""
        for metric in $metric_list; do
            r=$(ratio "${results[$name,base,$metric]:-}" "${results[$name,head,$metric]:-}")
            [ -n "$r" ] && printf '%s"%s": %s' "$sep" "$metric" "$r" || printf '%s"%s": null' "$sep" "$metric"
            sep=", "
        done
        printf '}}'
    done
    printf '\n  ]\n}\n'
}

emit_md() {
    printf '## nxsim benchmark: `%s` vs `%s`\n\n' "$BASE_REF" "$HEAD_REF"
    printf 'Mode: `%s`' "$mode"
    [ "$mode" = callgrind ] && printf ' (cache model I1=%s D1=%s LL=%s)' "$I1_MODEL" "$D1_MODEL" "$LL_MODEL"
    printf '. `ratio` is head/base; **< 1 means the PR is cheaper**.\n\n'
    printf '| design | metric | base | head | ratio |\n'
    printf '|---|---|---:|---:|---:|\n'
    for name in $designs; do
        if [ "${status[$name]:-}" != ok ]; then
            printf '| `%s` | — | — | — | %s |\n' "$name" "${status[$name]}"
            continue
        fi
        for metric in $metric_list; do
            printf '| `%s` | %s | %s | %s | %s |\n' "$name" "$metric" \
                "${results[$name,base,$metric]:-—}" "${results[$name,head,$metric]:-—}" \
                "$(ratio "${results[$name,base,$metric]:-}" "${results[$name,head,$metric]:-}")"
        done
    done
}

case "$format" in
    gh-action) emit_gh_action ;;
    json) emit_json ;;
    md) emit_md ;;
esac
