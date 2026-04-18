#!/bin/bash

LIB_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${LIB_SCRIPT_DIR}/.." && pwd)"
ABC_CONFIG="${LIB_SCRIPT_DIR}/abc_config.conf"

recompute_mapped_area() {
    local design="$1"
    local output_file="$2"

    (
        cd "$WORKSPACE_ROOT" && yosys -q -p "
            read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib
            read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib
            read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib
            read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib
            read_liberty -ignore_miss_func asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib
            read_verilog ${design}
            hierarchy -check -auto-top
            tee -o ${output_file} stat \
                -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib \
                -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib \
                -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib \
                -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib \
                -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib
        " >/dev/null 2>&1
    )
}

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
        abc_flags+=" -script +strash;,ifraig;,scorr;,dc2;,dretime;,retime,-o,-D,${delay_constraint};,strash;,&get,-n;,&dch,-f;,&nf,-D,${delay_constraint};,&put;,buffer;,upsize,-D,${delay_constraint};,dnsize,-D,${delay_constraint};,stime"
    else
        abc_flags+=" -script +strash;,ifraig;,scorr;,dc2;,dretime;,retime,-o,-D,500;,strash;,&get,-n;,&dch,-f;,&nf,-D,500;,&put;,buffer;,upsize,-D,500;,dnsize,-D,500;,stime"
    fi

    # Escape & in abc_flags for sed replacement
    abc_flags_escaped=$(printf '%s\n' "$abc_flags" | sed -e 's/[\/&]/\\&/g')
    
    sed -e "s|INPUT_VERILOG_PLACEHOLDER|$design|g" \
        -e "s|OUTPUT_VERILOG_PLACEHOLDER|$output_file|g" \
        -e "s|OUTPUT_STATS_PLACEHOLDER|$stat_file|g" \
        -e "s|ABC_FLAGS_PLACEHOLDER|$abc_flags_escaped|g" \
        "${LIB_SCRIPT_DIR}/synth.tcl" > "${output_dir}/synth_run.tcl"

    (cd "$WORKSPACE_ROOT" && yosys -s "${output_dir}/synth_run.tcl") >/dev/null 2>&1
}
