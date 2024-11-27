// @(1:10)
module top_1(
  clk_50, hex_0, hex_1, hex_2, hex_3, hex_4, hex_5, hex_6, hex_7
);
  input clk_50;
  output hex_0;
  output hex_1;
  output hex_2;
  output hex_3;
  output hex_4;
  output hex_5;
  output hex_6;
  output hex_7;
  wire ms_clk; // @(9:6)
  wire clk_50; // @(2:6)
  wire [31:0] i_19;
  wire [31:0] i_27;
  wire s_clk; // @(9:14)
  wire [31:0] i_23;
  wire [31:0] i1_26;
  wire [31:0] i1_22;
  wire [3:0] p_0;
  wire p_2;
  wire [3:0] p_1;
  wire [6:0] hex_1; // @(4:7)
  wire [31:0] p_4;
  wire [6:0] hex_0; // @(4:7)
  wire [31:0] p_3;
  wire [31:0] ms; // @(10:6)
  wire [6:0] hex_3; // @(4:7)
  wire [3:0] p_6;
  wire [6:0] hex_2; // @(4:7)
  wire [3:0] p_5;
  wire [3:0] p_8;
  wire [3:0] p_7;
  wire [3:0] p_9;
  wire [31:0] i_7;
  wire h_clk; // @(9:28)
  wire [31:0] i_35;
  wire [31:0] i_15;
  wire [31:0] i_31;
  wire [31:0] i_11;
  wire [31:0] i1_18;
  wire [6:0] hex_5; // @(4:7)
  wire [6:0] hex_4; // @(4:7)
  wire [31:0] i1_34;
  wire [6:0] hex_7; // @(4:7)
  wire [3:0] p_10;
  wire [6:0] hex_6; // @(4:7)
  wire [31:0] i1_14;
  wire m_clk; // @(9:21)
  wire [31:0] i1_30;
  wire [31:0] i1_10;
  reg [31:0] p_11;
  hex_trigger_1 inst_16(p_0, hex_2);
  hex_trigger_1 inst_32(p_1, hex_6);
  clock_divider_1 inst_1(clk_50, ms_clk);
  MUX_1 inst_4(p_2, p_3, 32'h00000000, p_4);
  hex_trigger_1 inst_20(p_5, hex_3);
  hex_trigger_1 inst_36(p_6, hex_7);
  hex_trigger_1 inst_8(p_7, hex_0);
  hex_trigger_1 inst_24(p_8, hex_4);
  hex_trigger_1 inst_12(p_9, hex_1);
  hex_trigger_1 inst_28(p_10, hex_5);
  assign p_2 = ms == 32'h0083D5FF;
  assign p_3 = ms + 32'h00000001;
  assign i_7 = ms % 32'h0000000A;
  assign p_7 = $signed(i_7);
  assign i1_10 = ms % 32'h00000064;
  assign i_11 = i1_10 / 32'h0000000A;
  assign p_9 = $signed(i_11);
  assign i1_14 = ms % 32'h000003E8;
  assign i_15 = i1_14 / 32'h00000064;
  assign p_0 = $signed(i_15);
  assign i1_18 = ms % 32'h00001770;
  assign i_19 = i1_18 / 32'h000003E8;
  assign p_5 = $signed(i_19);
  assign i1_22 = ms % 32'h0000EA60;
  assign i_23 = i1_22 / 32'h00001770;
  assign p_8 = $signed(i_23);
  assign i1_26 = ms % 32'h00057E40;
  assign i_27 = i1_26 / 32'h0000EA60;
  assign p_10 = $signed(i_27);
  assign i1_30 = ms % 32'h0036EE80;
  assign i_31 = i1_30 / 32'h00057E40;
  assign p_1 = $signed(i_31);
  assign i1_34 = ms % 32'h05265C00;
  assign i_35 = i1_34 / 32'h0036EE80;
  assign p_6 = $signed(i_35);
  assign ms = p_11;
  always @(posedge ms_clk) begin
    p_11 <= p_4;
  end
endmodule

// @(35:10)
module hex_trigger_1(
  num, code
);
  input num;
  output code;
  wire [6:0] code; // @(37:6)
  wire [3:0] num; // @(36:6)
  MUX_3 inst_1(num, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E, code);
endmodule

// @(59:10)
module MUX_3(
  code, input_0, input_1, input_2, input_3, input_4, input_5, input_6, input_7, input_8, input_9, input_10, input_11, input_12, input_13, input_14, input_15, out
);
  input code;
  input input_0;
  input input_1;
  input input_2;
  input input_3;
  input input_4;
  input input_5;
  input input_6;
  input input_7;
  input input_8;
  input input_9;
  input input_10;
  input input_11;
  input input_12;
  input input_13;
  input input_14;
  input input_15;
  output out;
  wire [6:0] input_9; // @(65:16)
  wire [6:0] i14_34;
  wire [6:0] input_5; // @(65:16)
  wire [6:0] input_6; // @(65:16)
  wire [6:0] input_7; // @(65:16)
  wire [6:0] input_8; // @(65:16)
  wire [6:0] i10_34;
  wire [6:0] i2_33;
  wire [6:0] i2_34;
  wire [6:0] i2_31;
  wire [6:0] i6_34;
  wire [6:0] i13_34;
  wire [6:0] i2_5;
  wire [6:0] i2_3;
  wire [6:0] i2_9;
  wire [6:0] i2_7;
  wire [6:0] input_14; // @(65:16)
  wire [6:0] input_15; // @(65:16)
  wire [6:0] input_12; // @(65:16)
  wire [6:0] input_13; // @(65:16)
  wire [6:0] i1_34;
  wire [6:0] input_10; // @(65:16)
  wire [6:0] input_11; // @(65:16)
  wire [6:0] i9_34;
  wire [6:0] i5_34;
  wire [3:0] code; // @(61:9)
  wire [6:0] i12_34;
  wire [6:0] out; // @(64:10)
  wire [6:0] i16_34;
  wire [6:0] i2_19;
  wire [6:0] i2_17;
  wire [6:0] i2_15;
  wire [6:0] i2_13;
  wire [6:0] i2_11;
  wire [6:0] i8_34;
  wire [6:0] i4_34;
  wire [15:0] mask; // @(62:9)
  wire [6:0] i11_34;
  wire [6:0] i15_34;
  wire [6:0] i2_29;
  wire [6:0] input_1; // @(65:16)
  wire [6:0] input_2; // @(65:16)
  wire [6:0] i2_27;
  wire [6:0] input_3; // @(65:16)
  wire [6:0] input_4; // @(65:16)
  wire [6:0] i2_25;
  wire [6:0] i3_34;
  wire [6:0] i2_23;
  wire [6:0] i7_34;
  wire [6:0] input_0; // @(65:16)
  wire [6:0] i2_21;
  DECODER_2 inst_1(code, mask);
  assign i2_3 = $signed(mask[0]);
  assign i1_34 = input_0 & i2_3;
  assign i2_5 = $signed(mask[1]);
  assign i2_34 = input_1 & i2_5;
  assign i2_7 = $signed(mask[2]);
  assign i3_34 = input_2 & i2_7;
  assign i2_9 = $signed(mask[3]);
  assign i4_34 = input_3 & i2_9;
  assign i2_11 = $signed(mask[4]);
  assign i5_34 = input_4 & i2_11;
  assign i2_13 = $signed(mask[5]);
  assign i6_34 = input_5 & i2_13;
  assign i2_15 = $signed(mask[6]);
  assign i7_34 = input_6 & i2_15;
  assign i2_17 = $signed(mask[7]);
  assign i8_34 = input_7 & i2_17;
  assign i2_19 = $signed(mask[8]);
  assign i9_34 = input_8 & i2_19;
  assign i2_21 = $signed(mask[9]);
  assign i10_34 = input_9 & i2_21;
  assign i2_23 = $signed(mask[10]);
  assign i11_34 = input_10 & i2_23;
  assign i2_25 = $signed(mask[11]);
  assign i12_34 = input_11 & i2_25;
  assign i2_27 = $signed(mask[12]);
  assign i13_34 = input_12 & i2_27;
  assign i2_29 = $signed(mask[13]);
  assign i14_34 = input_13 & i2_29;
  assign i2_31 = $signed(mask[14]);
  assign i15_34 = input_14 & i2_31;
  assign i2_33 = $signed(mask[15]);
  assign i16_34 = input_15 & i2_33;
  assign out = i1_34 | i2_34 | i3_34 | i4_34 | i5_34 | i6_34 | i7_34 | i8_34 | i9_34 | i10_34 | i11_34 | i12_34 | i13_34 | i14_34 | i15_34 | i16_34;
endmodule

// @(69:10)
module DECODER_2(
  code, out
);
  input code;
  output out;
  wire [3:0] code; // @(71:9)
  wire i2_5;
  wire i1_5;
  wire i4_5;
  wire [15:0] out; // @(72:9)
  wire i3_5;
  assign i1_5 = ~code[0];
  assign i2_5 = ~code[1];
  assign i3_5 = ~code[2];
  assign i4_5 = ~code[3];
  assign out[0] = i1_5 & i2_5 & i3_5 & i4_5;
  assign out[1] = code[0] & i2_5 & i3_5 & i4_5;
  assign out[2] = i1_5 & code[1] & i3_5 & i4_5;
  assign out[3] = code[0] & code[1] & i3_5 & i4_5;
  assign out[4] = i1_5 & i2_5 & code[2] & i4_5;
  assign out[5] = code[0] & i2_5 & code[2] & i4_5;
  assign out[6] = i1_5 & code[1] & code[2] & i4_5;
  assign out[7] = code[0] & code[1] & code[2] & i4_5;
  assign out[8] = i1_5 & i2_5 & i3_5 & code[3];
  assign out[9] = code[0] & i2_5 & i3_5 & code[3];
  assign out[10] = i1_5 & code[1] & i3_5 & code[3];
  assign out[11] = code[0] & code[1] & i3_5 & code[3];
  assign out[12] = i1_5 & i2_5 & code[2] & code[3];
  assign out[13] = code[0] & i2_5 & code[2] & code[3];
  assign out[14] = i1_5 & code[1] & code[2] & code[3];
  assign out[15] = code[0] & code[1] & code[2] & code[3];
endmodule

// @(25:10)
module clock_divider_1(
  clk_50, clk
);
  input clk_50;
  output clk;
  wire p_0;
  wire [31:0] p_2;
  wire clk; // @(26:14)
  wire [31:0] p_1;
  wire p_4;
  wire p_3;
  wire [31:0] count; // @(27:6)
  wire clk_50; // @(26:6)
  reg [31:0] p_5;
  reg p_6;
  MUX_1 inst_3(p_0, p_1, 32'h00000000, p_2);
  MUX_2 inst_6(p_0, clk, p_3, p_4);
  assign p_0 = count == 32'h0003D08F;
  assign p_1 = count + 32'h00000001;
  assign p_3 = ~clk;
  assign count = p_5;
  assign clk = p_6;
  always @(posedge clk_50) begin
    p_5 <= p_2;
    p_6 <= p_4;
  end
endmodule

// @(59:10)
module MUX_2(
  code, input_0, input_1, out
);
  input code;
  input input_0;
  input input_1;
  output out;
  wire code; // @(61:9)
  wire input_1; // @(65:16)
  wire i2_5;
  wire i1_6;
  wire i2_3;
  wire [1:0] mask; // @(62:9)
  wire out; // @(64:10)
  wire input_0; // @(65:16)
  wire i2_6;
  DECODER_1 inst_1(code, mask);
  assign i2_3 = $signed(mask[0]);
  assign i1_6 = input_0 & i2_3;
  assign i2_5 = $signed(mask[1]);
  assign i2_6 = input_1 & i2_5;
  assign out = i1_6 | i2_6;
endmodule

// @(59:10)
module MUX_1(
  code, input_0, input_1, out
);
  input code;
  input input_0;
  input input_1;
  output out;
  wire code; // @(61:9)
  wire [31:0] input_1; // @(65:16)
  wire [31:0] i2_5;
  wire [31:0] i1_6;
  wire [31:0] i2_3;
  wire [1:0] mask; // @(62:9)
  wire [31:0] out; // @(64:10)
  wire [31:0] input_0; // @(65:16)
  wire [31:0] i2_6;
  DECODER_1 inst_1(code, mask);
  assign i2_3 = $signed(mask[0]);
  assign i1_6 = input_0 & i2_3;
  assign i2_5 = $signed(mask[1]);
  assign i2_6 = input_1 & i2_5;
  assign out = i1_6 | i2_6;
endmodule

// @(69:10)
module DECODER_1(
  code, out
);
  input code;
  output out;
  wire code; // @(71:9)
  wire i1_2;
  wire [1:0] out; // @(72:9)
  assign i1_2 = ~code[0];
  assign out[0] = i1_2;
  assign out[1] = code[0];
endmodule

