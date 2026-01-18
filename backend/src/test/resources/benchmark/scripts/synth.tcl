read_verilog INPUT_VERILOG_PLACEHOLDER

hierarchy -check -auto-top
synth -flatten

techmap

dfflibmap -liberty asap7/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib

abc -liberty asap7/asap7sc7p5t_AO_LVT_TT_ccs_211120.lib \
    -liberty asap7/asap7sc7p5t_INVBUF_LVT_TT_ccs_220122.lib \
    -liberty asap7/asap7sc7p5t_OA_LVT_TT_ccs_211120.lib \
    -liberty asap7/asap7sc7p5t_SIMPLE_LVT_TT_ccs_211120.lib \
    -liberty asap7/asap7sc7p5t_SEQ_LVT_TT_ccs_220123.lib

clean

tee -o OUTPUT_STATS_PLACEHOLDER stat
write_verilog OUTPUT_VERILOG_PLACEHOLDER
