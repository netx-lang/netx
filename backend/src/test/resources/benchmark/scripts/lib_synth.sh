#!/bin/bash

LIB_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${LIB_SCRIPT_DIR}/.." && pwd)"

synthesize_design() {
    local design=$1
    local basename=$(basename "$design" .v)
    local output_dir="${2:-${WORKSPACE_ROOT}/build/${basename}}"
    mkdir -p "$output_dir"

    local output_file="${output_dir}/${basename}_synth.v"
    local stat_file="${output_dir}/${basename}_area.txt"

    # Determine abc flags based on benchmark name
    local abc_flags="-dff"
    if [[ "$basename" == *"fft"* ]]; then
        abc_flags="-dff -fast"
    fi

    sed -e "s|INPUT_VERILOG_PLACEHOLDER|$design|g" \
        -e "s|OUTPUT_VERILOG_PLACEHOLDER|$output_file|g" \
        -e "s|OUTPUT_STATS_PLACEHOLDER|$stat_file|g" \
        -e "s|ABC_FLAGS_PLACEHOLDER|$abc_flags|g" \
        "${LIB_SCRIPT_DIR}/synth.tcl" > "${output_dir}/synth_run.tcl"

    (cd "$WORKSPACE_ROOT" && yosys -s "${output_dir}/synth_run.tcl") >/dev/null 2>&1
}
