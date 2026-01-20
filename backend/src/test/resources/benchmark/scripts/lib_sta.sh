#!/bin/bash

LIB_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${LIB_SCRIPT_DIR}/.." && pwd)"

: ${CLOCK_PERIOD:=10.0}

analyze_timing() {
    local design=$1
    local basename=$(basename "$design" .v)
    local output_dir="${2:-${WORKSPACE_ROOT}/build/${basename}}"

    export NETLIST_FILE="${output_dir}/${basename}_synth.v"
    export TOP_MODULE=$(grep "^module" "$NETLIST_FILE" | head -1 | awk '{print $2}' | sed 's/(.*$//')
    export CLOCK_PERIOD=${CLOCK_PERIOD}

    (
        cd "${WORKSPACE_ROOT}" && \
        sta -exit "${LIB_SCRIPT_DIR}/timing.tcl"
    ) > "${output_dir}/${basename}_timing.txt" 2>&1 || true
}
