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
  wire [31:0] \$o_6 , \$o_9 , \$o_10 , \$o_13 , \$o_14 , \$o_17 , \$o_18 , \$o_21 , \$o_22 , \$o_25 , \$o_26 , \$o_29 , \$o_30 , \$o_33 , \$o_34 , \$p_1 , \$p_2 ;
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
  assign \$o_6  = ms % 32'h0000000A;
  assign \$p_3  = $signed(\$o_6 );
  assign \$o_9  = ms % 32'h00000064;
  assign \$o_10  = \$o_9  / 32'h0000000A;
  assign \$p_4  = $signed(\$o_10 );
  assign \$o_13  = ms % 32'h000003E8;
  assign \$o_14  = \$o_13  / 32'h00000064;
  assign \$p_5  = $signed(\$o_14 );
  assign \$o_17  = ms % 32'h00001770;
  assign \$o_18  = \$o_17  / 32'h000003E8;
  assign \$p_6  = $signed(\$o_18 );
  assign \$o_21  = ms % 32'h0000EA60;
  assign \$o_22  = \$o_21  / 32'h00001770;
  assign \$p_7  = $signed(\$o_22 );
  assign \$o_25  = ms % 32'h00057E40;
  assign \$o_26  = \$o_25  / 32'h0000EA60;
  assign \$p_8  = $signed(\$o_26 );
  assign \$o_29  = ms % 32'h0036EE80;
  assign \$o_30  = \$o_29  / 32'h00057E40;
  assign \$p_9  = $signed(\$o_30 );
  assign \$o_33  = ms % 32'h05265C00;
  assign \$o_34  = \$o_33  / 32'h0036EE80;
  assign \$p_10  = $signed(\$o_34 );
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
  wire [15:0] mask; // @(60:9)
  wire [6:0] \$o_2 , \$o_3 , \$o_4 , \$o_5 , \$o_6 , \$o_7 , \$o_8 , \$o_9 , \$o_10 , \$o_11 , \$o_12 , \$o_13 , \$o_14 , \$o_15 , \$o_16 , \$o_17 , \$o_18 , \$o_19 , \$o_20 , \$o_21 , \$o_22 , \$o_23 , \$o_24 , \$o_25 , \$o_26 , \$o_27 , \$o_28 , \$o_29 , \$o_30 , \$o_31 , \$o_32 , \$o_33 ;
  DECODER_2 \$inst_1 (code, mask);
  assign \$o_2  = $signed(mask[0]);
  assign \$o_3  = \input[0]  & \$o_2 ;
  assign \$o_4  = $signed(mask[1]);
  assign \$o_5  = \input[1]  & \$o_4 ;
  assign \$o_6  = $signed(mask[2]);
  assign \$o_7  = \input[2]  & \$o_6 ;
  assign \$o_8  = $signed(mask[3]);
  assign \$o_9  = \input[3]  & \$o_8 ;
  assign \$o_10  = $signed(mask[4]);
  assign \$o_11  = \input[4]  & \$o_10 ;
  assign \$o_12  = $signed(mask[5]);
  assign \$o_13  = \input[5]  & \$o_12 ;
  assign \$o_14  = $signed(mask[6]);
  assign \$o_15  = \input[6]  & \$o_14 ;
  assign \$o_16  = $signed(mask[7]);
  assign \$o_17  = \input[7]  & \$o_16 ;
  assign \$o_18  = $signed(mask[8]);
  assign \$o_19  = \input[8]  & \$o_18 ;
  assign \$o_20  = $signed(mask[9]);
  assign \$o_21  = \input[9]  & \$o_20 ;
  assign \$o_22  = $signed(mask[10]);
  assign \$o_23  = \input[10]  & \$o_22 ;
  assign \$o_24  = $signed(mask[11]);
  assign \$o_25  = \input[11]  & \$o_24 ;
  assign \$o_26  = $signed(mask[12]);
  assign \$o_27  = \input[12]  & \$o_26 ;
  assign \$o_28  = $signed(mask[13]);
  assign \$o_29  = \input[13]  & \$o_28 ;
  assign \$o_30  = $signed(mask[14]);
  assign \$o_31  = \input[14]  & \$o_30 ;
  assign \$o_32  = $signed(mask[15]);
  assign \$o_33  = \input[15]  & \$o_32 ;
  assign out = \$o_3  | \$o_5  | \$o_7  | \$o_9  | \$o_11  | \$o_13  | \$o_15  | \$o_17  | \$o_19  | \$o_21  | \$o_23  | \$o_25  | \$o_27  | \$o_29  | \$o_31  | \$o_33 ;
endmodule

// @(68:10)
module DECODER_2(
  code, out
);
  output [15:0] out; // @(71:9)
  input [3:0] code; // @(70:9)
  wire \$o_1 , \$o_2 , \$o_3 , \$o_4 ;
  assign \$o_1  = ~code[0];
  assign \$o_2  = ~code[1];
  assign \$o_3  = ~code[2];
  assign \$o_4  = ~code[3];
  assign out[0] = \$o_1  & \$o_2  & \$o_3  & \$o_4 ;
  assign out[1] = code[0] & \$o_2  & \$o_3  & \$o_4 ;
  assign out[2] = \$o_1  & code[1] & \$o_3  & \$o_4 ;
  assign out[3] = code[0] & code[1] & \$o_3  & \$o_4 ;
  assign out[4] = \$o_1  & \$o_2  & code[2] & \$o_4 ;
  assign out[5] = code[0] & \$o_2  & code[2] & \$o_4 ;
  assign out[6] = \$o_1  & code[1] & code[2] & \$o_4 ;
  assign out[7] = code[0] & code[1] & code[2] & \$o_4 ;
  assign out[8] = \$o_1  & \$o_2  & \$o_3  & code[3];
  assign out[9] = code[0] & \$o_2  & \$o_3  & code[3];
  assign out[10] = \$o_1  & code[1] & \$o_3  & code[3];
  assign out[11] = code[0] & code[1] & \$o_3  & code[3];
  assign out[12] = \$o_1  & \$o_2  & code[2] & code[3];
  assign out[13] = code[0] & \$o_2  & code[2] & code[3];
  assign out[14] = \$o_1  & code[1] & code[2] & code[3];
  assign out[15] = code[0] & code[1] & code[2] & code[3];
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
  wire \$o_2 , \$o_3 , \$o_4 , \$o_5 ;
  wire [1:0] mask; // @(60:9)
  DECODER_1 \$inst_1 (code, mask);
  assign \$o_2  = $signed(mask[0]);
  assign \$o_3  = \input[0]  & \$o_2 ;
  assign \$o_4  = $signed(mask[1]);
  assign \$o_5  = \input[1]  & \$o_4 ;
  assign out = \$o_3  | \$o_5 ;
endmodule

// @(57:10)
module MUX_1(
  code, \input[0] , \input[1] , out
);
  output [31:0] out; // @(61:19)
  input code; // @(59:9)
  input [31:0] \input[0] , \input[1] ; // @(61:9), @(61:9)
  wire [31:0] \$o_2 , \$o_3 , \$o_4 , \$o_5 ;
  wire [1:0] mask; // @(60:9)
  DECODER_1 \$inst_1 (code, mask);
  assign \$o_2  = $signed(mask[0]);
  assign \$o_3  = \input[0]  & \$o_2 ;
  assign \$o_4  = $signed(mask[1]);
  assign \$o_5  = \input[1]  & \$o_4 ;
  assign out = \$o_3  | \$o_5 ;
endmodule

// @(68:10)
module DECODER_1(
  code, out
);
  output [1:0] out; // @(71:9)
  input code; // @(70:9)
  wire \$o_1 ;
  assign \$o_1  = ~code;
  assign out[0] = \$o_1 ;
  assign out[1] = code;
endmodule

