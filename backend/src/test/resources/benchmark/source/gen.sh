nx compile ADPCM.nx --top ADPCM --output ../ADPCM.json
nx compile FIR.nx --top FIR --attrs S=8  --output ../FIR_8.json
nx compile MCM.nx --top MCM --attrs 'args=[3, 7, 21]'  --output ../MCM_3_7_21.json
nx compile MCM.nx --top MCM --attrs 'args=[5, 93]'     --output ../MCM_5_93.json
nx compile MCM.nx --top MCM --attrs 'args=[7, 19, 31]' --output ../MCM_7_19_31.json