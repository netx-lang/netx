#!/bin/bash

LIB_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${LIB_SCRIPT_DIR}/.." && pwd)"

# Optional OpenROAD post-processing for fanout/timing repair
# Uses docker if openroad command is not available
repair_design() {
    local synth_verilog=$1
    local output_dir=$2
    local basename=$(basename "$synth_verilog" _synth.v)
    
    local openroad_cmd="openroad -no_init -exit -threads 8"

    # Check if openroad is available locally; otherwise use docker with user mapping
    if ! command -v openroad &> /dev/null; then
        if command -v docker &> /dev/null; then
            local uid_gid="$(id -u):$(id -g)"
            openroad_cmd="docker run --rm --user ${uid_gid} -v ${WORKSPACE_ROOT}:${WORKSPACE_ROOT} -w ${WORKSPACE_ROOT} openroad openroad -no_init -exit"
        else
            # openroad not available, skip
            return 0
        fi
    fi
    
    # Create a temporary output file
    local or_output="${output_dir}/${basename}_or.v"
    local or_script="${output_dir}/${basename}_or.tcl"
    local timing_report="${output_dir}/${basename}_timing.txt"
    local or_log="${output_dir}/${basename}_or.log"
    
    # Detect module name from netlist
    local module_name=$(grep "^module" "$synth_verilog" | head -1 | sed 's/^module \([^(]*\).*/\1/')
    if [[ -z "$module_name" ]]; then
        module_name="top"
    fi
    
    # Generate per-design OpenROAD TCL by replacing placeholders in template
    sed -e "s|NETLIST_PLACEHOLDER|${synth_verilog}|g" \
        -e "s|OUTPUT_PLACEHOLDER|${or_output}|g" \
        -e "s|PDK_DIR_PLACEHOLDER|${WORKSPACE_ROOT}/asap7sc7p5t_28|g" \
        -e "s|MODULE_NAME_PLACEHOLDER|${module_name}|g" \
        -e "s|TIMING_REPORT_PLACEHOLDER|${timing_report}|g" \
        "${LIB_SCRIPT_DIR}/timing.tcl" > "$or_script"
    
    $openroad_cmd "$or_script" >"$or_log" 2>&1 || true
    
    if [[ -f "$or_output" && $(stat -f%z "$or_output" 2>/dev/null || stat -c%s "$or_output" 2>/dev/null || echo 0) -gt 0 ]]; then
        # Success: replace original with optimized version
        mv "$or_output" "$synth_verilog"
        return 0
    else
        # Fallback: keep original
        rm -f "$or_output" 2>/dev/null
        return 0
    fi
}
