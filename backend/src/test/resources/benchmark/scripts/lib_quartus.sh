#!/bin/bash

LIB_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "${LIB_SCRIPT_DIR}/.." && pwd)"

QUARTUS_MAP="${QUARTUS_MAP:-quartus_map}"
QUARTUS_FIT="${QUARTUS_FIT:-quartus_fit}"
QUARTUS_STA="${QUARTUS_STA:-quartus_sta}"

QUARTUS_FAMILY="${QUARTUS_FAMILY:-Cyclone IV E}"
QUARTUS_DEVICE="${QUARTUS_DEVICE:-EP4CE115F29C7}"
QUARTUS_CLOCK_PERIOD_NS="${QUARTUS_CLOCK_PERIOD_NS:-10.000}"
QUARTUS_THREADS="${QUARTUS_THREADS:-8}"

detect_top_module() {
    local design="$1"

    awk '
        /^[[:space:]]*module[[:space:]]+[A-Za-z_][A-Za-z0-9_$]*/ {
            for (i = 1; i <= NF; ++i) {
                if ($i == "module" && (i + 1) <= NF) {
                    name = $(i + 1)
                    sub(/\(.*/, "", name)
                    print name
                    exit
                }
            }
        }
    ' "$design"
}

detect_clock_port() {
    local design="$1"

    awk '
        BEGIN {
            split("clk clock i_clk aclk CLOCK_50 Clk", names)
            for (i in names) {
                wanted[names[i]] = 1
            }
        }
        /^[[:space:]]*input([[:space:]]+(wire|reg|logic))?/ {
            raw = $0
            line = $0
            sub(/\/\/.*/, "", line)
            gsub(/[\[\],;()]/, " ", line)
            n = split(line, fields, /[[:space:]]+/)
            candidate = ""
            for (i = 1; i <= n; ++i) {
                if (fields[i] ~ /^_[A-Za-z0-9$]+$/ || fields[i] ~ /^[A-Za-z][A-Za-z0-9_$]*$/) {
                    candidate = fields[i]
                }
                if (fields[i] in wanted) {
                    print fields[i]
                    exit
                }
            }
            if (candidate != "" && (raw ~ /\/\/[[:space:]]*CLOCK/ || raw ~ /[:.]([Cc]lk|[Cc]lock)([[:space:]]|$)/ || raw ~ /[:.][^[:space:]]*[.]([Cc]lk|[Cc]lock)([[:space:]]|$)/)) {
                print candidate
                exit
            }
        }
    ' "$design"
}

list_input_ports() {
    local design="$1"

    awk '
        /^[[:space:]]*input([[:space:]]+(wire|reg|logic))?/ {
            line = $0
            sub(/\/\/.*/, "", line)
            gsub(/[\[\],;()]/, " ", line)
            n = split(line, fields, /[[:space:]]+/)
            for (i = 1; i <= n; ++i) {
                if (fields[i] == "input" || fields[i] == "wire" || fields[i] == "reg" || fields[i] == "logic") {
                    continue
                }
                if (fields[i] ~ /^-?[0-9]+$/) {
                    continue
                }
                if (fields[i] ~ /^_[A-Za-z0-9$]+$/ || fields[i] ~ /^[A-Za-z][A-Za-z0-9_$]*$/) {
                    print fields[i]
                }
            }
        }
    ' "$design" | awk '!seen[$0]++'
}

list_top_ports() {
    local design="$1"

    awk '
        /^[[:space:]]*input([[:space:]]+(wire|reg|logic))?/ || /^[[:space:]]*output([[:space:]]+(wire|reg|logic))?/ || /^[[:space:]]*inout([[:space:]]+(wire|reg|logic))?/ {
            line = $0
            sub(/\/\/.*/, "", line)
            gsub(/[\[\],;()]/, " ", line)
            n = split(line, fields, /[[:space:]]+/)
            for (i = 1; i <= n; ++i) {
                if (fields[i] == "input" || fields[i] == "output" || fields[i] == "inout" || fields[i] == "wire" || fields[i] == "reg" || fields[i] == "logic") {
                    continue
                }
                if (fields[i] ~ /^-?[0-9]+$/) {
                    continue
                }
                if (fields[i] ~ /^_[A-Za-z0-9$]+$/ || fields[i] ~ /^[A-Za-z][A-Za-z0-9_$]*$/) {
                    print fields[i]
                }
            }
        }
    ' "$design" | awk '!seen[$0]++'
}

extract_logic_elements() {
    local fit_summary="$1"

    sed -n 's/^Total logic elements : \(.*\)$/\1/p' "$fit_summary" | \
        head -n1 | grep -Eo '[0-9][0-9,]*' | head -n1 | tr -d ','
}

extract_delay_ns() {
    local sta_rpt="$1"
    local sta_summary="$2"
    local fmax_mhz=""
    local slack_ns=""

    if [[ -f "$sta_rpt" ]]; then
        fmax_mhz=$(
            awk -F';' '
                /^; Slow .* Model Fmax Summary/ { in_fmax = 1; next }
                in_fmax && /^; [0-9.]+ MHz/ {
                    gsub(/^[ \t]+|[ \t]+$/, "", $2)
                    print $2
                    exit
                }
            ' "$sta_rpt"
        )
    fi

    if [[ -n "$fmax_mhz" ]]; then
        awk -v fmax="$fmax_mhz" 'BEGIN { if (fmax > 0) printf "%.3f\n", 1000.0 / fmax }'
        return 0
    fi

    if [[ -f "$sta_summary" ]]; then
        slack_ns=$(sed -n 's/^Slack : \(-\?[0-9.]\+\)$/\1/p' "$sta_summary" | head -n1)
    fi

    if [[ -n "$slack_ns" ]]; then
        awk -v period="$QUARTUS_CLOCK_PERIOD_NS" -v slack="$slack_ns" \
            'BEGIN { printf "%.3f\n", period - slack }'
    fi
}

synthesize_design_quartus() {
    local design="$1"
    local output_dir="${2:-${WORKSPACE_ROOT}/build/$(basename "$design" .v)}"
    local basename
    local top_module
    local clock_port
    local project
    local qsf_file
    local sdc_file
    local fit_summary
    local sta_summary
    local sta_rpt
    local logic_elements
    local delay_ns
    local compat_area
    local compat_timing
    local ports=()
    local input_ports=()
    local fallback_input_port=""

    basename=$(basename "$design" .v)
    top_module=$(detect_top_module "$design")
    if [[ -z "$top_module" ]]; then
        echo "Error: unable to detect top module in $design" >&2
        return 1
    fi

    clock_port=$(detect_clock_port "$design")
    mapfile -t ports < <(list_top_ports "$design")
    mapfile -t input_ports < <(list_input_ports "$design")
    if [[ ${#input_ports[@]} -gt 0 ]]; then
        fallback_input_port="${input_ports[0]}"
    fi
    project="$basename"

    mkdir -p "${output_dir}/output_files"
    qsf_file="${output_dir}/${project}.qsf"
    sdc_file="${output_dir}/${project}.sdc"

    cat > "$qsf_file" <<EOF
set_global_assignment -name FAMILY "${QUARTUS_FAMILY}"
set_global_assignment -name DEVICE ${QUARTUS_DEVICE}
set_global_assignment -name PROJECT_OUTPUT_DIRECTORY output_files
set_global_assignment -name TOP_LEVEL_ENTITY ${top_module}
set_global_assignment -name NUM_PARALLEL_PROCESSORS ${QUARTUS_THREADS}
set_global_assignment -name VERILOG_FILE ${design}
EOF

    for port in "${ports[@]}"; do
        cat >> "$qsf_file" <<EOF
set_instance_assignment -name VIRTUAL_PIN ON -to ${port}
set_instance_assignment -name VIRTUAL_PIN ON -to ${port}[*]
EOF
    done

    if [[ -n "$clock_port" ]]; then
        cat > "$sdc_file" <<EOF
create_clock -period ${QUARTUS_CLOCK_PERIOD_NS} [get_ports {${clock_port}}]
EOF
    elif [[ -n "$fallback_input_port" ]]; then
        cat > "$sdc_file" <<EOF
create_clock -period ${QUARTUS_CLOCK_PERIOD_NS} [get_ports {${fallback_input_port}}]
set_input_delay 0.000 -clock [get_clocks {${fallback_input_port}}] [remove_from_collection [all_inputs] [get_ports {${fallback_input_port}}]]
set_output_delay 0.000 -clock [get_clocks {${fallback_input_port}}] [all_outputs]
EOF
    else
        cat > "$sdc_file" <<EOF
create_clock -name benchmark_clk -period ${QUARTUS_CLOCK_PERIOD_NS}
set_input_delay 0.000 -clock benchmark_clk [all_inputs]
set_output_delay 0.000 -clock benchmark_clk [all_outputs]
EOF
    fi

    cat >> "$qsf_file" <<EOF
set_global_assignment -name SDC_FILE ${sdc_file}
EOF

    (
        cd "$output_dir"
        "$QUARTUS_MAP" "$project" >/dev/null 2>&1
        "$QUARTUS_FIT" --read_settings_files=off --write_settings_files=off "$project" -c "$project" >/dev/null 2>&1
        "$QUARTUS_STA" "$project" -c "$project" >/dev/null 2>&1
    )

    fit_summary="${output_dir}/output_files/${project}.fit.summary"
    sta_summary="${output_dir}/output_files/${project}.sta.summary"
    sta_rpt="${output_dir}/output_files/${project}.sta.rpt"
    compat_area="${output_dir}/${basename}_area.txt"
    compat_timing="${output_dir}/${basename}_or.log"

    logic_elements=$(extract_logic_elements "$fit_summary")
    if [[ -n "$logic_elements" ]]; then
        printf "Chip area for module '\\%s': %s.000\n" "$top_module" "$logic_elements" > "$compat_area"
    fi

    delay_ns=$(extract_delay_ns "$sta_rpt" "$sta_summary")
    if [[ -n "$delay_ns" ]]; then
        printf "           %s   data arrival time\n" "$delay_ns" > "$compat_timing"
    fi
}
