#!/bin/bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS_DIR="${ROOT_DIR}/scripts"
BUILD_DIR="${ROOT_DIR}/build_quartus"
SEQUENTIAL_CONF="${SCRIPTS_DIR}/sequential.conf"

usage() {
    cat <<EOF
Usage: $(basename "$0") [benchmark]

Options:
    -s, --sequential   Run only the sequential benchmarks listed in scripts/sequential.conf
    -h, --help         Show this help message

Run all benchmarks in the current directory or a single benchmark when the
optional argument is provided. The argument can be either the JSON filename
or the benchmark basename without the .json suffix.

Environment:
    QUARTUS_FAMILY            Target FPGA family (default: Cyclone IV E)
    QUARTUS_DEVICE            Target FPGA device (default: EP4CE115F29C7)
    QUARTUS_CLOCK_PERIOD_NS   Clock period used for timing reports (default: 10.000)
EOF
}

source "${SCRIPTS_DIR}/lib_quartus.sh"

load_sequential_jsons() {
    local -n _out=$1

    if [ ! -f "$SEQUENTIAL_CONF" ]; then
        echo "Error: sequential config not found at $SEQUENTIAL_CONF" >&2
        exit 1
    fi

    while IFS= read -r line; do
        [[ -z "$line" || "$line" =~ ^# ]] && continue
        _out+=("$line")
    done < "$SEQUENTIAL_CONF"
}

process_json() {
    local json_file="$1"
    local basename
    local work_dir
    local origin_v
    local opted_json
    local opted_v

    basename=$(basename "$json_file" .json)
    work_dir="${BUILD_DIR}/${basename}"
    mkdir -p "$work_dir"

    origin_v="${work_dir}/${basename}_origin.v"
    opted_json="${work_dir}/${basename}_opted.json"
    opted_v="${work_dir}/${basename}_opted.v"

    nx dump verilog "$json_file" --output "$origin_v" >/dev/null
    echo "[1/4] Origin Verilog: ${basename}"

    nx opt "$json_file" -o "$opted_json" >/dev/null
    echo "[2/4] Optimize: ${basename}"

    nx dump verilog "$opted_json" --output "$opted_v" >/dev/null
    echo "[3/4] Opted Verilog: ${basename}"

    synthesize_design_quartus "$origin_v" "${work_dir}/${basename}_origin" &
    local quartus_origin_pid=$!
    synthesize_design_quartus "$opted_v" "${work_dir}/${basename}_opted" &
    local quartus_opted_pid=$!

    wait "$quartus_origin_pid" "$quartus_opted_pid"
    echo "[4/4] Quartus compile: ${basename}"
}

main() {
    mkdir -p "$BUILD_DIR"
    local json_files=()
    local single_benchmark=""
    local sequential_only=false
    local -A pid_to_benchmark=()
    local failed_benchmarks=()

    while [ "$#" -gt 0 ]; do
        case "$1" in
            -s|--sequential)
                sequential_only=true
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            *)
                if [ -n "$single_benchmark" ]; then
                    echo "Error: multiple benchmark arguments provided" >&2
                    usage
                    exit 1
                fi
                single_benchmark="$1"
                shift
                ;;
        esac
    done

    if [ "$sequential_only" = true ]; then
        load_sequential_jsons json_files

        if [ -n "$single_benchmark" ]; then
            local candidate="$single_benchmark"
            if [[ "$candidate" != *.json ]]; then
                candidate="${candidate}.json"
            fi

            local filtered=()
            for entry in "${json_files[@]}"; do
                if [ "$entry" = "$candidate" ]; then
                    filtered+=("$entry")
                fi
            done

            if [ ${#filtered[@]} -eq 0 ]; then
                echo "Error: benchmark '$single_benchmark' not listed in ${SEQUENTIAL_CONF}" >&2
                exit 1
            fi
            json_files=("${filtered[@]}")
        fi
    elif [ -n "$single_benchmark" ]; then
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

    if [ ${#json_files[@]} -gt 0 ]; then
        for candidate in "${json_files[@]}"; do
            if [ ! -f "$candidate" ] && [ ! -f "${ROOT_DIR}/${candidate}" ]; then
                echo "Error: benchmark '$candidate' not found" >&2
                exit 1
            fi
        done
    fi

    if [ ${#json_files[@]} -eq 0 ]; then
        echo "Error: no .json benchmarks found" >&2
        exit 1
    fi

    local pids=()
    for json_file in "${json_files[@]}"; do
        process_json "$json_file" &
        pids+=($!)
        pid_to_benchmark[$!]="$json_file"
    done

    for pid in "${pids[@]}"; do
        if ! wait "$pid" 2>/dev/null; then
            failed_benchmarks+=("${pid_to_benchmark[$pid]}")
        fi
    done

    if [ "${#failed_benchmarks[@]}" -gt 0 ]; then
        echo "Error: Quartus flow failed for: ${failed_benchmarks[*]}" >&2
        exit 1
    fi

    python3 "${SCRIPTS_DIR}/generate_batch_report.py" "$BUILD_DIR" "${BUILD_DIR}/summary.txt"
}

main "$@"
