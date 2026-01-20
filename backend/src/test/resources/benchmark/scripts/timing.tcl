read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib
read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib
read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib
read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib
read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib

read_verilog $::env(NETLIST_FILE)

set clock_period_ns [expr {double($::env(CLOCK_PERIOD))}]

link_design $::env(TOP_MODULE)
# Parse the netlist to find which input ports drive .CLK signals on flip-flops
set clock_ports {}
set netlist_file $::env(NETLIST_FILE)

if {[file exists $netlist_file]} {
    set fp [open $netlist_file r]
    set content [read $fp]
    close $fp
    
    set clk_signals [regexp -all -inline -line {\.CLK\(([_a-zA-Z0-9\[\]]+)\)} $content]
    
    foreach match $clk_signals {
        set clk_signal $match
        if {$clk_signal ne "" && [string first "CLK" $clk_signal] == -1} {
            if {[catch {set port_obj [get_ports $clk_signal]} err] == 0} {
                if {[lsearch $clock_ports $port_obj] == -1} {
                    lappend clock_ports $port_obj
                    puts "Detected clock port from netlist: $clk_signal"
                }
            }
        }
    }
}

if {[llength $clock_ports] > 0} {
    foreach clk_port $clock_ports {
        set port_name [get_name $clk_port]
        set clock_name "clk_[regsub -all {[^a-zA-Z0-9_]} $port_name _]"
        puts "Creating clock: $clock_name on port: $port_name with period ${clock_period_ns}ns"
        create_clock -name $clock_name -period $clock_period_ns $clk_port
    }
    puts "Number of clocks created: [llength $clock_ports]"
} else {
    puts "No clock ports detected - treating as combinational design"
}

set input_ports [all_inputs]
if {[llength $input_ports] > 0} {
    set_driving_cell -lib_cell INVx1_ASAP7_75t_L $input_ports
}

set output_ports [all_outputs]
if {[llength $output_ports] > 0} {
    set_load 0.00 $output_ports
}

if {[llength $clock_ports] > 0} {
    report_checks -path_delay max -unconstrained -format full_clock_expanded -digits 4 -fields {input_pin net fanout slew incr_delay arrival}
    report_checks -path_delay max -unconstrained -format summary -digits 4
} else {
    report_checks -unconstrained -format full_clock_expanded -digits 4 -fields {input_pin net fanout slew incr_delay arrival}
    report_checks -unconstrained -format summary -digits 4
}

exit
