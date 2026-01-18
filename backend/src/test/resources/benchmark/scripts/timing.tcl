read_liberty asap7/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib
read_liberty asap7/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib
read_liberty asap7/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib
read_liberty asap7/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib
read_liberty asap7/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib

read_verilog $::env(NETLIST_FILE)

link_design $::env(TOP_MODULE)

set_driving_cell -lib_cell INVx1_ASAP7_75t_R [all_inputs]
set_load 0.01 [all_outputs]

report_checks -unconstrained -format full_clock_expanded -digits 4 -fields {input_pin net fanout slew incr_delay arrival} -endpoint_count 1

report_checks -unconstrained -format summary -digits 4 -endpoint_count 10

exit
