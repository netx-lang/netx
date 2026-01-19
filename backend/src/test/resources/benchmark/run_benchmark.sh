#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS_DIR="${ROOT_DIR}/scripts"
BUILD_DIR="${ROOT_DIR}/build"

MAX_JOBS=${MAX_JOBS:-$(nproc 2>/dev/null || echo 4)}

usage() {
    cat <<EOF
Usage: $(basename "$0") [benchmark]

Run all benchmarks in the current directory or a single benchmark when the
optional argument is provided. The argument can be either the JSON filename
or the benchmark basename without the .json suffix.
EOF
}

source "${SCRIPTS_DIR}/lib_synth.sh"
source "${SCRIPTS_DIR}/lib_sta.sh"

process_json() {
    local json_file="$1"
    local basename=$(basename "$json_file" .json)
    local work_dir="${BUILD_DIR}/${basename}"
    mkdir -p "$work_dir"

    local origin_v="${work_dir}/${basename}_origin.v"
    local opted_json="${work_dir}/${basename}_opted.json"
    local opted_v="${work_dir}/${basename}_opted.v"

    nx dump verilog "$json_file" --output "$origin_v" >/dev/null
    echo "[1/5] Origin Verilog: ${basename}"

    nx opt "$json_file" -o "$opted_json" >/dev/null
    echo "[2/5] Optimize: ${basename}"

    nx dump verilog "$opted_json" --output "$opted_v" >/dev/null
    echo "[3/5] Opted Verilog: ${basename}"

    # Run synthesis and timing analysis in parallel for origin and opted
    synthesize_design "$origin_v" "${work_dir}/${basename}_origin" &
    local synth_origin_pid=$!
    synthesize_design "$opted_v" "${work_dir}/${basename}_opted" &
    local synth_opted_pid=$!
    
    wait "$synth_origin_pid" "$synth_opted_pid"
    echo "[4/5] Yosys synthesis: ${basename}"

    analyze_timing "$origin_v" "${work_dir}/${basename}_origin" &
    local sta_origin_pid=$!
    analyze_timing "$opted_v" "${work_dir}/${basename}_opted" &
    local sta_opted_pid=$!
    
    wait "$sta_origin_pid" "$sta_opted_pid"
    echo "[5/5] OpenSTA analysis: ${basename}"
}
  
main() {
    mkdir -p "$BUILD_DIR"
    local json_files=()
    local single_benchmark=""

    # Parse arguments
    if [ "$#" -gt 0 ]; then
        single_benchmark="$1"
    fi

    if [ -n "$single_benchmark" ]; then
        local candidate="$single_benchmark"
        if [[ "$candidate" != *.json ]]; then
            candidate="${candidate}.json"
        fi

        if [ ! -f "$candidate" ]; then
            echo "Error: benchmark '$single_benchmark' not found (expected $candidate)" >&2
            usage
            exit 1
        fi
        json_files=("$candidate")
    else
        json_files=(*.json)
    fi

    if [ ${#json_files[@]} -eq 0 ]; then
        echo "Error: no .json benchmarks found" >&2
        exit 1
    fi

    local pids=()
    for json_file in "${json_files[@]}"; do
        while [ "${#pids[@]}" -ge "$MAX_JOBS" ]; do
            for i in "${!pids[@]}"; do
                if ! kill -0 "${pids[$i]}" 2>/dev/null; then
                    wait "${pids[$i]}" 2>/dev/null || true
                    unset 'pids[$i]'
                fi
            done
            pids=("${pids[@]}")
            [ "${#pids[@]}" -ge "$MAX_JOBS" ] && sleep 0.1
        done
        
        process_json "$json_file" &
        pids+=($!)
    done

    for pid in "${pids[@]}"; do
        wait "$pid" 2>/dev/null || true
    done

    python3 "${SCRIPTS_DIR}/generate_batch_report.py" "$BUILD_DIR" "${BUILD_DIR}/summary.txt"
}

main "$@"