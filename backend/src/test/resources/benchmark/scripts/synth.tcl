read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib
read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib
read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib
read_liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib
read_liberty -ignore_miss_func asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib

read_verilog INPUT_VERILOG_PLACEHOLDER

hierarchy -check -auto-top
proc; fsm; opt; memory; opt; techmap; flatten; opt -full

dfflibmap -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib

abc -dff \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib

clean

tee -o OUTPUT_STATS_PLACEHOLDER stat \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib \
    -liberty asap7sc7p5t_28/LIB/CCS/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib
write_verilog OUTPUT_VERILOG_PLACEHOLDER
