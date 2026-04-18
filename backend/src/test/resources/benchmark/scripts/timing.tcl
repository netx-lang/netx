read_lef PDK_DIR_PLACEHOLDER/techlef_misc/asap7_tech_1x_201209.lef
read_lef PDK_DIR_PLACEHOLDER/LEF/asap7sc7p5t_28_L_1x_220121a.lef

read_verilog NETLIST_PLACEHOLDER

read_liberty PDK_DIR_PLACEHOLDER/LIB/CCS/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib
read_liberty PDK_DIR_PLACEHOLDER/LIB/CCS/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib
read_liberty PDK_DIR_PLACEHOLDER/LIB/CCS/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib
read_liberty PDK_DIR_PLACEHOLDER/LIB/CCS/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib
read_liberty PDK_DIR_PLACEHOLDER/LIB/CCS/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib

link_design MODULE_NAME_PLACEHOLDER

set has_sequential [expr {[llength [all_registers]] > 0}]

set netlist_path "NETLIST_PLACEHOLDER"
set design_dir   [file dirname $netlist_path]
set netlist_base [file rootname [file tail $netlist_path]]
set design_base  [regsub {_synth$} $netlist_base ""]
set area_file    [file join $design_dir "${design_base}_area.txt"]

set target_util 0.25
set placement_density 0.70

set aspect_ratio 1.0
set core_area ""
set core_bbox ""

set fp [open $area_file r]
set content [read $fp]
close $fp

if {![regexp {Chip area for module '[^']+':\s+([0-9\.]+)} $content -> cell_area]} {
    error "Failed to parse chip area from $area_file"
}

set core_area [expr {$cell_area / $target_util}]
set core_w [expr {sqrt($core_area * $aspect_ratio)}]
set core_h [expr {$core_w / $aspect_ratio}]
set core_bbox [list 0.0 0.0 $core_w $core_h]
initialize_floorplan -die_area $core_bbox -core_area $core_bbox -site asap7sc7p5t
global_placement -density $placement_density
detailed_placement

set_wire_rc -signal -capacitance 0.20e-15 -resistance 0.08
set_wire_rc -clock  -capacitance 0.20e-15 -resistance 0.08
estimate_parasitics -placement

if {$has_sequential} {
    set clk_port ""
    set netlist_file "NETLIST_PLACEHOLDER"
    
    if {[file exists $netlist_file]} {
        set fp [open $netlist_file r]
        set content [read $fp]
        close $fp
        
        set clk_matches [regexp -all -inline -line {\.CLK\(([_a-zA-Z0-9\[\]]+)\)} $content]
        set clk_signals [dict create]
        
        foreach {_ signal} $clk_matches {
            dict incr clk_signals $signal
        }
        
        if {[dict size $clk_signals] > 0} {
            set most_common ""
            set max_count 0
            dict for {sig count} $clk_signals {
                if {$count > $max_count} {
                    set max_count $count
                    set most_common $sig
                }
            }
            
            if {$most_common ne ""} {
                if {[catch {set clk_port [get_ports $most_common]} err] == 0} {
                    puts "Found clock port from netlist analysis: $most_common"
                } else {
                    puts "Clock signal $most_common is not a port, using first input"
                    set clk_ports [all_inputs]
                    if {[llength $clk_ports] > 0} {
                        set clk_port [lindex $clk_ports 0]
                    }
                }
            }
        }
    }
    
    if {$clk_port ne ""} {
        create_clock -period 10.0 $clk_port
    } else {
        set clk_ports [all_inputs]
        if {[llength $clk_ports] > 0} {
            set clk_port [lindex $clk_ports 0]
            puts "Using first input port as clock: [get_name $clk_port]"
            create_clock -period 10.0 $clk_port
        }
    }
} else {
    puts "Design is combinational - creating virtual clock"
    create_clock -period 10.0 -name vclk
    set_input_delay -clock vclk 0.0 [all_inputs]
    set_output_delay -clock vclk 0.0 [all_outputs]
}

set_driving_cell -lib_cell INVx1_ASAP7_75t_L [all_inputs]
set_load 0.0 [all_outputs]

set_max_fanout 10 [current_design]
set_max_capacitance 10.0 [current_design]
set_max_transition 1000.0 [current_design]

repair_design

repair_timing

if {$has_sequential} {
    set clk_port ""
    set netlist_file "NETLIST_PLACEHOLDER"
    
    if {[file exists $netlist_file]} {
        set fp [open $netlist_file r]
        set content [read $fp]
        close $fp
        
        set clk_matches [regexp -all -inline -line {\.CLK\(([_a-zA-Z0-9\[\]]+)\)} $content]
        set clk_signals [dict create]
        
        foreach {_ signal} $clk_matches {
            dict incr clk_signals $signal
        }
        
        if {[dict size $clk_signals] > 0} {
            set most_common ""
            set max_count 0
            dict for {sig count} $clk_signals {
                if {$count > $max_count} {
                    set max_count $count
                    set most_common $sig
                }
            }
            
            if {$most_common ne ""} {
                if {[catch {set clk_port [get_ports $most_common]} err] == 0} {
                    create_clock -period 10.0 $clk_port
                }
            }
        }
    }
} else {
    # Recreate virtual clock for combinational
    create_clock -period 10.0 -name vclk
    set_input_delay -clock vclk 0.0 [all_inputs]
    set_output_delay -clock vclk 0.0 [all_outputs]
}

# Report timing with fanout information
report_checks -path_delay max -digits 4 -format full_clock_expanded -fields {input_pin net fanout slew incr_delay arrival}

puts "OpenROAD timing analysis completed"
