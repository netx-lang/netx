#!/bin/bash

LIB_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${LIB_SCRIPT_DIR}/.." && pwd)"

: ${CLOCK_PERIOD:=10.0}
: ${OPENROAD_DOCKER:=openroad}

analyze_timing() {
    local design=$1
    local basename=$(basename "$design" .v)
    local output_dir="${2:-${WORKSPACE_ROOT}/build/${basename}}"

    export NETLIST_FILE="${output_dir}/${basename}_synth.v"
    export TOP_MODULE=$(grep "^module" "$NETLIST_FILE" | head -1 | awk '{print $2}' | sed 's/(.*$//')
    export CLOCK_PERIOD=${CLOCK_PERIOD}

    docker run --rm \
        -v "${WORKSPACE_ROOT}:${WORKSPACE_ROOT}" \
        -w "${WORKSPACE_ROOT}" \
        -e NETLIST_FILE="${NETLIST_FILE}" \
        -e TOP_MODULE="${TOP_MODULE}" \
        -e CLOCK_PERIOD="${CLOCK_PERIOD}" \
        "${OPENROAD_DOCKER}" \
        openroad "${LIB_SCRIPT_DIR}/timing.tcl" > "${output_dir}/${basename}_timing.txt" 2>&1 || true
}
