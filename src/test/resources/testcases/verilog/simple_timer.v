// @(1:10)
module top_1(
  clk_50, \hex[0] , \hex[1] , \hex[2] , \hex[3] , \hex[4] , \hex[5] , \hex[6] , \hex[7] 
);
  output [6:0] \hex[0] , \hex[1] , \hex[2] , \hex[3] , \hex[4] , \hex[5] , \hex[6] , \hex[7] ; // @(3:6), @(3:6), @(3:6), @(3:6), @(3:6), @(3:6), @(3:6), @(3:6)
  input clk_50; // @(2:6)
  wire ms_clk, s_clk, m_clk, h_clk; // @(7:6), @(7:14), @(7:21), @(7:28)
  wire \$p_0 ;
  wire [3:0] \$p_3 , \$p_4 , \$p_5 , \$p_6 , \$p_7 , \$p_8 , \$p_9 , \$p_10 ;
  wire [31:0] ms; // @(8:6)
  wire [31:0] \$i_7 , \$i1_10 , \$i_11 , \$i1_14 , \$i_15 , \$i1_18 , \$i_19 , \$i1_22 , \$i_23 , \$i1_26 , \$i_27 , \$i1_30 , \$i_31 , \$i1_34 , \$i_35 , \$p_1 , \$p_2 ;
  reg [31:0] \$p_11 ;
  clock_divider_1 \$inst_1 (clk_50, ms_clk);
  MUX_1 \$inst_4 (\$p_0 , \$p_1 , 32'h00000000, \$p_2 );
  hex_trigger_1 \$inst_8 (\$p_3 , \hex[0] );
  hex_trigger_1 \$inst_12 (\$p_4 , \hex[1] );
  hex_trigger_1 \$inst_16 (\$p_5 , \hex[2] );
  hex_trigger_1 \$inst_20 (\$p_6 , \hex[3] );
  hex_trigger_1 \$inst_24 (\$p_7 , \hex[4] );
  hex_trigger_1 \$inst_28 (\$p_8 , \hex[5] );
  hex_trigger_1 \$inst_32 (\$p_9 , \hex[6] );
  hex_trigger_1 \$inst_36 (\$p_10 , \hex[7] );
  assign \$p_0  = ms == 32'h0083D5FF;
  assign \$p_1  = ms + 32'h00000001;
  assign \$i_7  = ms % 32'h0000000A;
  assign \$p_3  = $signed(\$i_7 );
  assign \$i1_10  = ms % 32'h00000064;
  assign \$i_11  = \$i1_10  / 32'h0000000A;
  assign \$p_4  = $signed(\$i_11 );
  assign \$i1_14  = ms % 32'h000003E8;
  assign \$i_15  = \$i1_14  / 32'h00000064;
  assign \$p_5  = $signed(\$i_15 );
  assign \$i1_18  = ms % 32'h00001770;
  assign \$i_19  = \$i1_18  / 32'h000003E8;
  assign \$p_6  = $signed(\$i_19 );
  assign \$i1_22  = ms % 32'h0000EA60;
  assign \$i_23  = \$i1_22  / 32'h00001770;
  assign \$p_7  = $signed(\$i_23 );
  assign \$i1_26  = ms % 32'h00057E40;
  assign \$i_27  = \$i1_26  / 32'h0000EA60;
  assign \$p_8  = $signed(\$i_27 );
  assign \$i1_30  = ms % 32'h0036EE80;
  assign \$i_31  = \$i1_30  / 32'h00057E40;
  assign \$p_9  = $signed(\$i_31 );
  assign \$i1_34  = ms % 32'h05265C00;
  assign \$i_35  = \$i1_34  / 32'h0036EE80;
  assign \$p_10  = $signed(\$i_35 );
  assign ms = \$p_11 ;
  always @(posedge ms_clk) begin
    \$p_11  <= \$p_2 ;
  end
endmodule

// @(33:10)
module hex_trigger_1(
  num, code
);
  output [6:0] code; // @(35:6)
  input [3:0] num; // @(34:6)
  MUX_3 \$inst_1 (num, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E, code);
endmodule

// @(57:10)
module MUX_3(
  code, \input[0] , \input[1] , \input[2] , \input[3] , \input[4] , \input[5] , \input[6] , \input[7] , \input[8] , \input[9] , \input[10] , \input[11] , \input[12] , \input[13] , \input[14] , \input[15] , out
);
  output [6:0] out; // @(61:19)
  input [3:0] code; // @(59:9)
  input [6:0] \input[0] , \input[1] , \input[2] , \input[3] , \input[4] , \input[5] , \input[6] , \input[7] ; // @(61:9), @(61:9), @(61:9), @(61:9), @(61:9), @(61:9), @(61:9), @(61:9)
  input [6:0] \input[8] , \input[9] , \input[10] , \input[11] , \input[12] , \input[13] , \input[14] , \input[15] ; // @(61:9), @(61:9), @(61:9), @(61:9), @(61:9), @(61:9), @(61:9), @(61:9)
  wire \$i_2 , \$i_5 , \$i_8 , \$i_11 , \$i_14 , \$i_17 , \$i_20 , \$i_23 , \$i_26 , \$i_29 , \$i_32 , \$i_35 , \$i_38 , \$i_41 , \$i_44 , \$i_47 ;
  wire [15:0] mask; // @(60:9)
  wire [6:0] \$i2_4 , \$i2_7 , \$i2_10 , \$i2_13 , \$i2_16 , \$i2_19 , \$i2_22 , \$i2_25 , \$i2_28 , \$i2_31 , \$i2_34 , \$i2_37 , \$i2_40 , \$i2_43 , \$i2_46 , \$i2_49 , \$i1_50 , \$i2_50 , \$i3_50 , \$i4_50 , \$i5_50 , \$i6_50 , \$i7_50 , \$i8_50 , \$i9_50 , \$i10_50 , \$i11_50 , \$i12_50 , \$i13_50 , \$i14_50 , \$i15_50 , \$i16_50 ;
  DECODER_2 \$inst_1 (code, mask);
  assign \$i2_4  = $signed(\$i_2 );
  assign \$i_2  = mask[0];
  assign \$i1_50  = \input[0]  & \$i2_4 ;
  assign \$i2_7  = $signed(\$i_5 );
  assign \$i_5  = mask[1];
  assign \$i2_50  = \input[1]  & \$i2_7 ;
  assign \$i2_10  = $signed(\$i_8 );
  assign \$i_8  = mask[2];
  assign \$i3_50  = \input[2]  & \$i2_10 ;
  assign \$i2_13  = $signed(\$i_11 );
  assign \$i_11  = mask[3];
  assign \$i4_50  = \input[3]  & \$i2_13 ;
  assign \$i2_16  = $signed(\$i_14 );
  assign \$i_14  = mask[4];
  assign \$i5_50  = \input[4]  & \$i2_16 ;
  assign \$i2_19  = $signed(\$i_17 );
  assign \$i_17  = mask[5];
  assign \$i6_50  = \input[5]  & \$i2_19 ;
  assign \$i2_22  = $signed(\$i_20 );
  assign \$i_20  = mask[6];
  assign \$i7_50  = \input[6]  & \$i2_22 ;
  assign \$i2_25  = $signed(\$i_23 );
  assign \$i_23  = mask[7];
  assign \$i8_50  = \input[7]  & \$i2_25 ;
  assign \$i2_28  = $signed(\$i_26 );
  assign \$i_26  = mask[8];
  assign \$i9_50  = \input[8]  & \$i2_28 ;
  assign \$i2_31  = $signed(\$i_29 );
  assign \$i_29  = mask[9];
  assign \$i10_50  = \input[9]  & \$i2_31 ;
  assign \$i2_34  = $signed(\$i_32 );
  assign \$i_32  = mask[10];
  assign \$i11_50  = \input[10]  & \$i2_34 ;
  assign \$i2_37  = $signed(\$i_35 );
  assign \$i_35  = mask[11];
  assign \$i12_50  = \input[11]  & \$i2_37 ;
  assign \$i2_40  = $signed(\$i_38 );
  assign \$i_38  = mask[12];
  assign \$i13_50  = \input[12]  & \$i2_40 ;
  assign \$i2_43  = $signed(\$i_41 );
  assign \$i_41  = mask[13];
  assign \$i14_50  = \input[13]  & \$i2_43 ;
  assign \$i2_46  = $signed(\$i_44 );
  assign \$i_44  = mask[14];
  assign \$i15_50  = \input[14]  & \$i2_46 ;
  assign \$i2_49  = $signed(\$i_47 );
  assign \$i_47  = mask[15];
  assign \$i16_50  = \input[15]  & \$i2_49 ;
  assign out = \$i1_50  | \$i2_50  | \$i3_50  | \$i4_50  | \$i5_50  | \$i6_50  | \$i7_50  | \$i8_50  | \$i9_50  | \$i10_50  | \$i11_50  | \$i12_50  | \$i13_50  | \$i14_50  | \$i15_50  | \$i16_50 ;
endmodule

// @(68:10)
module DECODER_2(
  code, out
);
  output [15:0] out; // @(71:9)
  input [3:0] code; // @(70:9)
  wire \$i_1 , \$i_2 , \$i_3 , \$i_4 , \$i1_5 , \$i2_5 , \$i3_5 , \$i4_5 , \$i2_13 , \$i3_13 , \$i4_13 , \$i1_17 , \$i3_17 , \$i4_17 , \$i3_20 , \$i4_20 , \$i1_24 , \$i2_24 , \$i4_24 , \$i2_27 , \$i4_27 , \$i1_30 , \$i4_30 , \$i4_32 , \$i1_36 , \$i2_36 , \$i3_36 , \$i2_39 , \$i3_39 , \$i1_42 , \$i3_42 , \$i3_44 , \$i1_47 , \$i2_47 , \$i2_49 , \$i1_51 ;
  assign \$i1_5  = ~\$i_1 ;
  assign \$i2_5  = ~\$i_2 ;
  assign \$i3_5  = ~\$i_3 ;
  assign \$i4_5  = ~\$i_4 ;
  assign out[0] = \$i1_5  & \$i2_5  & \$i3_5  & \$i4_5 ;
  assign \$i_1  = code[0];
  assign \$i_2  = code[1];
  assign \$i_3  = code[2];
  assign \$i_4  = code[3];
  assign \$i2_13  = ~\$i_2 ;
  assign \$i3_13  = ~\$i_3 ;
  assign \$i4_13  = ~\$i_4 ;
  assign out[1] = \$i_1  & \$i2_13  & \$i3_13  & \$i4_13 ;
  assign \$i1_17  = ~\$i_1 ;
  assign \$i3_17  = ~\$i_3 ;
  assign \$i4_17  = ~\$i_4 ;
  assign out[2] = \$i1_17  & \$i_2  & \$i3_17  & \$i4_17 ;
  assign \$i3_20  = ~\$i_3 ;
  assign \$i4_20  = ~\$i_4 ;
  assign out[3] = \$i_1  & \$i_2  & \$i3_20  & \$i4_20 ;
  assign \$i1_24  = ~\$i_1 ;
  assign \$i2_24  = ~\$i_2 ;
  assign \$i4_24  = ~\$i_4 ;
  assign out[4] = \$i1_24  & \$i2_24  & \$i_3  & \$i4_24 ;
  assign \$i2_27  = ~\$i_2 ;
  assign \$i4_27  = ~\$i_4 ;
  assign out[5] = \$i_1  & \$i2_27  & \$i_3  & \$i4_27 ;
  assign \$i1_30  = ~\$i_1 ;
  assign \$i4_30  = ~\$i_4 ;
  assign out[6] = \$i1_30  & \$i_2  & \$i_3  & \$i4_30 ;
  assign \$i4_32  = ~\$i_4 ;
  assign out[7] = \$i_1  & \$i_2  & \$i_3  & \$i4_32 ;
  assign \$i1_36  = ~\$i_1 ;
  assign \$i2_36  = ~\$i_2 ;
  assign \$i3_36  = ~\$i_3 ;
  assign out[8] = \$i1_36  & \$i2_36  & \$i3_36  & \$i_4 ;
  assign \$i2_39  = ~\$i_2 ;
  assign \$i3_39  = ~\$i_3 ;
  assign out[9] = \$i_1  & \$i2_39  & \$i3_39  & \$i_4 ;
  assign \$i1_42  = ~\$i_1 ;
  assign \$i3_42  = ~\$i_3 ;
  assign out[10] = \$i1_42  & \$i_2  & \$i3_42  & \$i_4 ;
  assign \$i3_44  = ~\$i_3 ;
  assign out[11] = \$i_1  & \$i_2  & \$i3_44  & \$i_4 ;
  assign \$i1_47  = ~\$i_1 ;
  assign \$i2_47  = ~\$i_2 ;
  assign out[12] = \$i1_47  & \$i2_47  & \$i_3  & \$i_4 ;
  assign \$i2_49  = ~\$i_2 ;
  assign out[13] = \$i_1  & \$i2_49  & \$i_3  & \$i_4 ;
  assign \$i1_51  = ~\$i_1 ;
  assign out[14] = \$i1_51  & \$i_2  & \$i_3  & \$i_4 ;
  assign out[15] = \$i_1  & \$i_2  & \$i_3  & \$i_4 ;
endmodule

// @(23:10)
module clock_divider_1(
  clk_50, clk
);
  output clk; // @(24:14)
  input clk_50; // @(24:6)
  wire \$p_0 , \$p_3 , \$p_4 ;
  wire [31:0] count; // @(25:6)
  wire [31:0] \$p_1 , \$p_2 ;
  reg [31:0] \$p_5 ;
  reg \$p_6 ;
  MUX_1 \$inst_3 (\$p_0 , \$p_1 , 32'h00000000, \$p_2 );
  MUX_2 \$inst_6 (\$p_0 , clk, \$p_3 , \$p_4 );
  assign \$p_0  = count == 32'h0003D08F;
  assign \$p_1  = count + 32'h00000001;
  assign \$p_3  = ~clk;
  assign count = \$p_5 ;
  assign clk = \$p_6 ;
  always @(posedge clk_50) begin
    \$p_5  <= \$p_2 ;
    \$p_6  <= \$p_4 ;
  end
endmodule

// @(57:10)
module MUX_2(
  code, \input[0] , \input[1] , out
);
  output out; // @(61:19)
  input code, \input[0] , \input[1] ; // @(59:9), @(61:9), @(61:9)
  wire \$i_2 , \$i2_4 , \$i_5 , \$i2_7 , \$i1_8 , \$i2_8 ;
  wire [1:0] mask; // @(60:9)
  DECODER_1 \$inst_1 (code, mask);
  assign \$i2_4  = $signed(\$i_2 );
  assign \$i_2  = mask[0];
  assign \$i1_8  = \input[0]  & \$i2_4 ;
  assign \$i2_7  = $signed(\$i_5 );
  assign \$i_5  = mask[1];
  assign \$i2_8  = \input[1]  & \$i2_7 ;
  assign out = \$i1_8  | \$i2_8 ;
endmodule

// @(57:10)
module MUX_1(
  code, \input[0] , \input[1] , out
);
  output [31:0] out; // @(61:19)
  input code; // @(59:9)
  input [31:0] \input[0] , \input[1] ; // @(61:9), @(61:9)
  wire \$i_2 , \$i_5 ;
  wire [31:0] \$i2_4 , \$i2_7 , \$i1_8 , \$i2_8 ;
  wire [1:0] mask; // @(60:9)
  DECODER_1 \$inst_1 (code, mask);
  assign \$i2_4  = $signed(\$i_2 );
  assign \$i_2  = mask[0];
  assign \$i1_8  = \input[0]  & \$i2_4 ;
  assign \$i2_7  = $signed(\$i_5 );
  assign \$i_5  = mask[1];
  assign \$i2_8  = \input[1]  & \$i2_7 ;
  assign out = \$i1_8  | \$i2_8 ;
endmodule

// @(68:10)
module DECODER_1(
  code, out
);
  output [1:0] out; // @(71:9)
  input code; // @(70:9)
  wire \$i_1 , \$i1_2 ;
  assign \$i1_2  = ~\$i_1 ;
  assign out[0] = \$i1_2 ;
  assign \$i_1  = code[0];
  assign out[1] = \$i_1 ;
endmodule

