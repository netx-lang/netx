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
  wire [31:0] o_6;
  wire [31:0] o_29;
  wire s_clk; // @(9:14)
  wire [31:0] o_9;
  wire [31:0] o_26;
  wire [31:0] o_25;
  wire [31:0] o_22;
  wire [31:0] o_21;
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
  wire h_clk; // @(9:28)
  wire [31:0] o_18;
  wire [31:0] o_17;
  wire [6:0] hex_5; // @(4:7)
  wire [31:0] o_14;
  wire [6:0] hex_4; // @(4:7)
  wire [31:0] o_13;
  wire [6:0] hex_7; // @(4:7)
  wire [31:0] o_34;
  wire [3:0] p_10;
  wire [6:0] hex_6; // @(4:7)
  wire [31:0] o_33;
  wire m_clk; // @(9:21)
  wire [31:0] o_10;
  wire [31:0] o_30;
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
  assign o_6 = ms % 32'h0000000A;
  assign p_7 = $signed(o_6);
  assign o_9 = ms % 32'h00000064;
  assign o_10 = o_9 / 32'h0000000A;
  assign p_9 = $signed(o_10);
  assign o_13 = ms % 32'h000003E8;
  assign o_14 = o_13 / 32'h00000064;
  assign p_0 = $signed(o_14);
  assign o_17 = ms % 32'h00001770;
  assign o_18 = o_17 / 32'h000003E8;
  assign p_5 = $signed(o_18);
  assign o_21 = ms % 32'h0000EA60;
  assign o_22 = o_21 / 32'h00001770;
  assign p_8 = $signed(o_22);
  assign o_25 = ms % 32'h00057E40;
  assign o_26 = o_25 / 32'h0000EA60;
  assign p_10 = $signed(o_26);
  assign o_29 = ms % 32'h0036EE80;
  assign o_30 = o_29 / 32'h00057E40;
  assign p_1 = $signed(o_30);
  assign o_33 = ms % 32'h05265C00;
  assign o_34 = o_33 / 32'h0036EE80;
  assign p_6 = $signed(o_34);
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
  wire [6:0] input_5; // @(65:16)
  wire [6:0] input_6; // @(65:16)
  wire [6:0] input_7; // @(65:16)
  wire [6:0] input_8; // @(65:16)
  wire [6:0] o_29;
  wire [6:0] o_28;
  wire [6:0] o_27;
  wire [6:0] o_26;
  wire [6:0] o_25;
  wire [6:0] o_24;
  wire [6:0] o_23;
  wire [6:0] o_22;
  wire [6:0] o_21;
  wire [6:0] o_20;
  wire [6:0] o_2;
  wire [6:0] o_3;
  wire [6:0] o_19;
  wire [6:0] o_18;
  wire [6:0] o_17;
  wire [6:0] input_14; // @(65:16)
  wire [6:0] o_16;
  wire [6:0] input_15; // @(65:16)
  wire [6:0] o_15;
  wire [6:0] input_12; // @(65:16)
  wire [6:0] o_14;
  wire [6:0] input_13; // @(65:16)
  wire [6:0] o_13;
  wire [6:0] input_10; // @(65:16)
  wire [6:0] o_12;
  wire [6:0] input_11; // @(65:16)
  wire [6:0] o_11;
  wire [6:0] o_10;
  wire [3:0] code; // @(61:9)
  wire [6:0] out; // @(64:10)
  wire [6:0] o_6;
  wire [6:0] o_7;
  wire [6:0] o_4;
  wire [6:0] o_5;
  wire [6:0] o_8;
  wire [6:0] o_9;
  wire [15:0] mask; // @(62:9)
  wire [6:0] input_1; // @(65:16)
  wire [6:0] input_2; // @(65:16)
  wire [6:0] input_3; // @(65:16)
  wire [6:0] input_4; // @(65:16)
  wire [6:0] o_33;
  wire [6:0] o_32;
  wire [6:0] o_31;
  wire [6:0] o_30;
  wire [6:0] input_0; // @(65:16)
  DECODER_2 inst_1(code, mask);
  assign o_2 = $signed(mask[0]);
  assign o_3 = input_0 & o_2;
  assign o_4 = $signed(mask[1]);
  assign o_5 = input_1 & o_4;
  assign o_6 = $signed(mask[2]);
  assign o_7 = input_2 & o_6;
  assign o_8 = $signed(mask[3]);
  assign o_9 = input_3 & o_8;
  assign o_10 = $signed(mask[4]);
  assign o_11 = input_4 & o_10;
  assign o_12 = $signed(mask[5]);
  assign o_13 = input_5 & o_12;
  assign o_14 = $signed(mask[6]);
  assign o_15 = input_6 & o_14;
  assign o_16 = $signed(mask[7]);
  assign o_17 = input_7 & o_16;
  assign o_18 = $signed(mask[8]);
  assign o_19 = input_8 & o_18;
  assign o_20 = $signed(mask[9]);
  assign o_21 = input_9 & o_20;
  assign o_22 = $signed(mask[10]);
  assign o_23 = input_10 & o_22;
  assign o_24 = $signed(mask[11]);
  assign o_25 = input_11 & o_24;
  assign o_26 = $signed(mask[12]);
  assign o_27 = input_12 & o_26;
  assign o_28 = $signed(mask[13]);
  assign o_29 = input_13 & o_28;
  assign o_30 = $signed(mask[14]);
  assign o_31 = input_14 & o_30;
  assign o_32 = $signed(mask[15]);
  assign o_33 = input_15 & o_32;
  assign out = o_3 | o_5 | o_7 | o_9 | o_11 | o_13 | o_15 | o_17 | o_19 | o_21 | o_23 | o_25 | o_27 | o_29 | o_31 | o_33;
endmodule

// @(69:10)
module DECODER_2(
  code, out
);
  input code;
  output out;
  wire o_4;
  wire [3:0] code; // @(71:9)
  wire o_2;
  wire o_3;
  wire [15:0] out; // @(72:9)
  wire o_1;
  assign o_1 = ~code[0];
  assign o_2 = ~code[1];
  assign o_3 = ~code[2];
  assign o_4 = ~code[3];
  assign out[0] = o_1 & o_2 & o_3 & o_4;
  assign out[1] = code[0] & o_2 & o_3 & o_4;
  assign out[2] = o_1 & code[1] & o_3 & o_4;
  assign out[3] = code[0] & code[1] & o_3 & o_4;
  assign out[4] = o_1 & o_2 & code[2] & o_4;
  assign out[5] = code[0] & o_2 & code[2] & o_4;
  assign out[6] = o_1 & code[1] & code[2] & o_4;
  assign out[7] = code[0] & code[1] & code[2] & o_4;
  assign out[8] = o_1 & o_2 & o_3 & code[3];
  assign out[9] = code[0] & o_2 & o_3 & code[3];
  assign out[10] = o_1 & code[1] & o_3 & code[3];
  assign out[11] = code[0] & code[1] & o_3 & code[3];
  assign out[12] = o_1 & o_2 & code[2] & code[3];
  assign out[13] = code[0] & o_2 & code[2] & code[3];
  assign out[14] = o_1 & code[1] & code[2] & code[3];
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
  wire o_4;
  wire code; // @(61:9)
  wire o_5;
  wire input_1; // @(65:16)
  wire o_2;
  wire o_3;
  wire [1:0] mask; // @(62:9)
  wire out; // @(64:10)
  wire input_0; // @(65:16)
  DECODER_1 inst_1(code, mask);
  assign o_2 = $signed(mask[0]);
  assign o_3 = input_0 & o_2;
  assign o_4 = $signed(mask[1]);
  assign o_5 = input_1 & o_4;
  assign out = o_3 | o_5;
endmodule

// @(59:10)
module MUX_1(
  code, input_0, input_1, out
);
  input code;
  input input_0;
  input input_1;
  output out;
  wire [31:0] o_4;
  wire code; // @(61:9)
  wire [31:0] o_5;
  wire [31:0] input_1; // @(65:16)
  wire [31:0] o_2;
  wire [31:0] o_3;
  wire [1:0] mask; // @(62:9)
  wire [31:0] out; // @(64:10)
  wire [31:0] input_0; // @(65:16)
  DECODER_1 inst_1(code, mask);
  assign o_2 = $signed(mask[0]);
  assign o_3 = input_0 & o_2;
  assign o_4 = $signed(mask[1]);
  assign o_5 = input_1 & o_4;
  assign out = o_3 | o_5;
endmodule

// @(69:10)
module DECODER_1(
  code, out
);
  input code;
  output out;
  wire code; // @(71:9)
  wire [1:0] out; // @(72:9)
  wire o_1;
  assign o_1 = ~code[0];
  assign out[0] = o_1;
  assign out[1] = code[0];
endmodule

