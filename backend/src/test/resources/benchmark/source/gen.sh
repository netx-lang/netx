nx compile ADPCM.nx --top ADPCM --output ../ADPCM.json
nx compile FIR.nx --top FIR --attrs S=8  --output ../FIR_8.json
nx compile FMA.nx --top FMA --output ../FMA.json
nx compile SMUL.nx --top SMUL --output ../SMUL.json
nx compile MCM.nx --top MCM --attrs 'args=[3, 7, 21]'  --output ../MCM_3_7_21.json
nx compile MCM.nx --top MCM --attrs 'args=[5, 93]'     --output ../MCM_5_93.json
nx compile MCM.nx --top MCM --attrs 'args=[7, 19, 31]' --output ../MCM_7_19_31.json

nx compile EPFL/adder.nx --top ADDER --output ../adder.json
nx compile EPFL/bar.nx --top BARREL_SHIFTER --attrs stage=7 --output ../bar.json
nx compile EPFL/max.nx --top MAX --output ../max.json
nx compile EPFL/cavlc.nx --top CAVLC --output ../cavlc.json

nx compile genmul/genmul_3_3.nx --top Mult_3_3 --output ../genmul_3_3.json
nx compile genmul/genmul_5_5.nx --top Mult_5_5 --output ../genmul_5_5.json

python3 ../scripts/verilog_to_nx.py LGSynth89/frg2_orig.v
python3 ../scripts/verilog_to_nx.py LGSynth89/C432_orig.v
nx compile LGSynth89/frg2_orig.nx --top frg2 --output ../frg2.json
nx compile LGSynth89/C432_orig.nx --top C432 --output ../C432.json

python3 ../scripts/verilog_to_nx.py LGSynth91/i7_orig.v
python3 ../scripts/verilog_to_nx.py LGSynth91/C5315_orig.v
nx compile LGSynth91/i7_orig.nx --top i7 --output ../i7.json
nx compile LGSynth91/C5315_orig.nx --top C5315 --output ../C5315.json

python3 ../scripts/verilog_to_nx.py ISCAS85/c2670.v
python3 ../scripts/verilog_to_nx.py ISCAS85/c7552.v
nx compile ISCAS85/c2670.nx --top c2670 --output ../c2670.json
nx compile ISCAS85/c7552.nx --top c7552 --output ../c7552.json

nx compile opencore/qdiv.nx --top qdiv --output ../qdiv.json
nx compile ITC99/b12.nx --top b12 --output ../b12.json