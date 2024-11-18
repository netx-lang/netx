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
  wire [6:0] o_60;
  wire [3:0] num; // @(36:6)
  wire [6:0] o_28;
  wire [6:0] o_27;
  wire [6:0] o_24;
  wire [6:0] o_23;
  wire [6:0] o_20;
  wire [6:0] o_64;
  wire [6:0] o_63;
  wire p_0;
  wire p_2;
  wire p_1;
  wire p_4;
  wire p_3;
  wire p_6;
  wire p_5;
  wire p_8;
  wire p_7;
  wire p_9;
  wire [6:0] o_3;
  wire [6:0] o_19;
  wire [6:0] o_16;
  wire [6:0] o_15;
  wire [6:0] o_59;
  wire p_30;
  wire p_31;
  wire [6:0] o_12;
  wire [6:0] o_56;
  wire [6:0] o_11;
  wire [6:0] o_55;
  wire [6:0] o_52;
  wire [6:0] o_51;
  wire p_27;
  wire p_28;
  wire p_29;
  wire [6:0] code; // @(37:6)
  wire p_23;
  wire p_24;
  wire p_25;
  wire p_26;
  wire [6:0] o_7;
  wire [6:0] o_4;
  wire [6:0] o_8;
  wire [6:0] o_48;
  wire [6:0] o_47;
  wire p_20;
  wire p_21;
  wire [6:0] o_44;
  wire p_22;
  wire [6:0] o_43;
  wire [6:0] o_40;
  wire p_16;
  wire p_17;
  wire p_18;
  wire p_19;
  wire p_12;
  wire p_13;
  wire p_14;
  wire p_15;
  wire [6:0] o_39;
  wire [6:0] o_36;
  wire [6:0] o_35;
  wire p_10;
  wire p_11;
  wire [6:0] o_32;
  wire [6:0] o_31;
  MUX_2 inst_2(p_0, 1'h00, 1'h01, p_1);
  MUX_2 inst_34(p_2, 1'h00, 1'h01, p_3);
  MUX_2 inst_6(p_4, 1'h00, 1'h01, p_5);
  MUX_2 inst_38(p_6, 1'h00, 1'h01, p_7);
  MUX_2 inst_10(p_8, 1'h00, 1'h01, p_9);
  MUX_2 inst_42(p_10, 1'h00, 1'h01, p_11);
  MUX_2 inst_14(p_12, 1'h00, 1'h01, p_13);
  MUX_2 inst_46(p_14, 1'h00, 1'h01, p_15);
  MUX_2 inst_18(p_16, 1'h00, 1'h01, p_17);
  MUX_2 inst_50(p_18, 1'h00, 1'h01, p_19);
  MUX_2 inst_22(p_20, 1'h00, 1'h01, p_21);
  MUX_2 inst_54(p_22, 1'h00, 1'h01, p_23);
  MUX_2 inst_26(p_24, 1'h00, 1'h01, p_25);
  MUX_2 inst_58(p_26, 1'h00, 1'h01, p_27);
  MUX_2 inst_30(p_28, 1'h00, 1'h01, p_29);
  MUX_2 inst_62(p_30, 1'h00, 1'h01, p_31);
  assign p_0 = 4'h00 == num;
  assign o_3 = $signed(p_1);
  assign o_4 = o_3 & 7'h40;
  assign p_4 = 4'h01 == num;
  assign o_7 = $signed(p_5);
  assign o_8 = o_7 & 7'h79;
  assign p_8 = 4'h02 == num;
  assign o_11 = $signed(p_9);
  assign o_12 = o_11 & 7'h24;
  assign p_12 = 4'h03 == num;
  assign o_15 = $signed(p_13);
  assign o_16 = o_15 & 7'h30;
  assign p_16 = 4'h04 == num;
  assign o_19 = $signed(p_17);
  assign o_20 = o_19 & 7'h19;
  assign p_20 = 4'h05 == num;
  assign o_23 = $signed(p_21);
  assign o_24 = o_23 & 7'h12;
  assign p_24 = 4'h06 == num;
  assign o_27 = $signed(p_25);
  assign o_28 = o_27 & 7'h02;
  assign p_28 = 4'h07 == num;
  assign o_31 = $signed(p_29);
  assign o_32 = o_31 & 7'h78;
  assign p_2 = 4'h08 == num;
  assign o_35 = $signed(p_3);
  assign o_36 = o_35 & 7'h00;
  assign p_6 = 4'h09 == num;
  assign o_39 = $signed(p_7);
  assign o_40 = o_39 & 7'h10;
  assign p_10 = 4'h0A == num;
  assign o_43 = $signed(p_11);
  assign o_44 = o_43 & 7'h08;
  assign p_14 = 4'h0B == num;
  assign o_47 = $signed(p_15);
  assign o_48 = o_47 & 7'h03;
  assign p_18 = 4'h0C == num;
  assign o_51 = $signed(p_19);
  assign o_52 = o_51 & 7'h46;
  assign p_22 = 4'h0D == num;
  assign o_55 = $signed(p_23);
  assign o_56 = o_55 & 7'h21;
  assign p_26 = 4'h0E == num;
  assign o_59 = $signed(p_27);
  assign o_60 = o_59 & 7'h06;
  assign p_30 = 4'h0F == num;
  assign o_63 = $signed(p_31);
  assign o_64 = o_63 & 7'h0E;
  assign code = o_4 | o_8 | o_12 | o_16 | o_20 | o_24 | o_28 | o_32 | o_36 | o_40 | o_44 | o_48 | o_52 | o_56 | o_60 | o_64;
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

// @(60:10)
module MUX_2(
  ctl, i0, i1, out
);
  input ctl;
  input i0;
  input i1;
  output out;
  wire o_4;
  wire o_5;
  wire i0; // @(61:14)
  wire i1; // @(61:18)
  wire ctl; // @(61:9)
  wire o_2;
  wire o_3;
  wire out; // @(61:22)
  wire o_1;
  assign o_1 = ~ctl;
  assign o_2 = $signed(o_1);
  assign o_3 = i0 & o_2;
  assign o_4 = $signed(ctl);
  assign o_5 = i1 & o_4;
  assign out = o_3 | o_5;
endmodule

// @(60:10)
module MUX_1(
  ctl, i0, i1, out
);
  input ctl;
  input i0;
  input i1;
  output out;
  wire [31:0] o_4;
  wire [31:0] o_5;
  wire [31:0] i0; // @(61:14)
  wire [31:0] i1; // @(61:18)
  wire ctl; // @(61:9)
  wire [31:0] o_2;
  wire [31:0] o_3;
  wire [31:0] out; // @(61:22)
  wire o_1;
  assign o_1 = ~ctl;
  assign o_2 = $signed(o_1);
  assign o_3 = i0 & o_2;
  assign o_4 = $signed(ctl);
  assign o_5 = i1 & o_4;
  assign out = o_3 | o_5;
endmodule

