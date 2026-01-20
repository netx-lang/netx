#!/bin/bash

LIB_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${LIB_SCRIPT_DIR}/.." && pwd)"
ABC_CONFIG="${LIB_SCRIPT_DIR}/abc_config.conf"

synthesize_design() {
    local design=$1
    local basename=$(basename "$design" .v)
    local output_dir="${2:-${WORKSPACE_ROOT}/build/${basename}}"
    mkdir -p "$output_dir"

    local output_file="${output_dir}/${basename}_synth.v"
    local stat_file="${output_dir}/${basename}_area.txt"

    local delay_constraint=""
    if [[ -f "$ABC_CONFIG" ]]; then
        local config_key="${basename%_origin}"
        config_key="${config_key%_opted}"
        
        local config_value=$(grep "^${config_key}=" "$ABC_CONFIG" 2>/dev/null | cut -d'=' -f2)
        delay_constraint="$config_value"
    fi

    local abc_flags="-dff"
    if [[ -n "$delay_constraint" ]]; then
        abc_flags+=" -D ${delay_constraint}"
    fi
    if [[ "$basename" == *"fft"* ]]; then
        abc_flags+=" -fast"
    fi

    sed -e "s|INPUT_VERILOG_PLACEHOLDER|$design|g" \
        -e "s|OUTPUT_VERILOG_PLACEHOLDER|$output_file|g" \
        -e "s|OUTPUT_STATS_PLACEHOLDER|$stat_file|g" \
        -e "s|ABC_FLAGS_PLACEHOLDER|$abc_flags|g" \
        "${LIB_SCRIPT_DIR}/synth.tcl" > "${output_dir}/synth_run.tcl"

    (cd "$WORKSPACE_ROOT" && yosys -s "${output_dir}/synth_run.tcl") >/dev/null 2>&1
}
