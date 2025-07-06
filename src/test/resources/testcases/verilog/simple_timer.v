// @(1:10) top@(1:10)
module mod_0 (
  t_0, t_1, t_2, t_3, t_4, t_5, t_6, t_7, t_8
);
  // hex[0]@(3:6), hex[1]@(3:6), hex[2]@(3:6), hex[3]@(3:6), hex[4]@(3:6), hex[5]@(3:6), hex[6]@(3:6), hex[7]@(3:6)
  output [6:0] t_1, t_2, t_3, t_4, t_5, t_6, t_7, t_8;

  // clk_50@(2:6)
  input t_0;

  // ms_clk@(7:6), s_clk@(7:14), m_clk@(7:21), h_clk@(7:28)
  wire t_9, t_10, t_11, t_12;

  wire t_29;
  wire [3:0] t_32, t_33, t_34, t_35, t_36, t_37, t_38, t_39;
  // ms@(8:6)
  wire [31:0] t_13;

  wire [31:0] t_14, t_15, t_16, t_17, t_18, t_19, t_20, t_21, t_22, t_23, t_24, t_25, t_26, t_27, t_28, t_30, t_31;
  reg [31:0]  r_0; // $p_11@(0:0)
  mod_4 inst_0 (t_0, t_9);
  mod_6 inst_1 (t_29, t_30, 32'h00000000, t_31);
  mod_1 inst_2 (t_32, t_1);
  mod_1 inst_3 (t_33, t_2);
  mod_1 inst_4 (t_34, t_3);
  mod_1 inst_5 (t_35, t_4);
  mod_1 inst_6 (t_36, t_5);
  mod_1 inst_7 (t_37, t_6);
  mod_1 inst_8 (t_38, t_7);
  mod_1 inst_9 (t_39, t_8);
  assign t_29 = t_13 == 32'h0083D5FF;
  assign t_30 = t_13 + 32'h00000001;
  assign t_14 = t_13 % 32'h0000000A;
  assign t_32 = $signed(t_14);
  assign t_15 = t_13 % 32'h00000064;
  assign t_16 = t_15 / 32'h0000000A;
  assign t_33 = $signed(t_16);
  assign t_17 = t_13 % 32'h000003E8;
  assign t_18 = t_17 / 32'h00000064;
  assign t_34 = $signed(t_18);
  assign t_19 = t_13 % 32'h00001770;
  assign t_20 = t_19 / 32'h000003E8;
  assign t_35 = $signed(t_20);
  assign t_21 = t_13 % 32'h0000EA60;
  assign t_22 = t_21 / 32'h00001770;
  assign t_36 = $signed(t_22);
  assign t_23 = t_13 % 32'h00057E40;
  assign t_24 = t_23 / 32'h0000EA60;
  assign t_37 = $signed(t_24);
  assign t_25 = t_13 % 32'h0036EE80;
  assign t_26 = t_25 / 32'h00057E40;
  assign t_38 = $signed(t_26);
  assign t_27 = t_13 % 32'h05265C00;
  assign t_28 = t_27 / 32'h0036EE80;
  assign t_39 = $signed(t_28);
  assign t_13 = r_0;
  always @(posedge t_9) begin
    r_0 <= t_31;
  end
endmodule

// @(33:10) hex_trigger@(33:10)
module mod_1 (
  t_0, t_1
);
  // code@(35:6)
  output [6:0] t_1;

  // num@(34:6)
  input [3:0] t_0;

  mod_2 inst_0 (t_0, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E, t_1);
endmodule

// @(57:10) MUX@(57:10)(width=4)
module mod_2 (
  t_0, t_2, t_3, t_4, t_5, t_6, t_7, t_8, t_9, t_10, t_11, t_12, t_13, t_14, t_15, t_16, t_17, t_18
);
  // out@(61:19)
  output [6:0] t_18;

  // code@(59:9)
  input [3:0] t_0;

  // input[0]@(61:9), input[1]@(61:9), input[2]@(61:9), input[3]@(61:9), input[4]@(61:9), input[5]@(61:9), input[6]@(61:9), input[7]@(61:9)
  input [6:0] t_2, t_3, t_4, t_5, t_6, t_7, t_8, t_9;

  // input[8]@(61:9), input[9]@(61:9), input[10]@(61:9), input[11]@(61:9), input[12]@(61:9), input[13]@(61:9), input[14]@(61:9), input[15]@(61:9)
  input [6:0] t_10, t_11, t_12, t_13, t_14, t_15, t_16, t_17;

  // mask@(60:9)
  wire [15:0] t_1;

  wire [6:0] t_19, t_20, t_21, t_22, t_23, t_24, t_25, t_26, t_27, t_28, t_29, t_30, t_31, t_32, t_33, t_34, t_35, t_36, t_37, t_38, t_39, t_40, t_41, t_42, t_43, t_44, t_45, t_46, t_47, t_48, t_49, t_50;
  mod_3 inst_0 (t_0, t_1);
  assign t_19 = $signed(t_1[0]);
  assign t_20 = t_2 & t_19;
  assign t_21 = $signed(t_1[1]);
  assign t_22 = t_3 & t_21;
  assign t_23 = $signed(t_1[2]);
  assign t_24 = t_4 & t_23;
  assign t_25 = $signed(t_1[3]);
  assign t_26 = t_5 & t_25;
  assign t_27 = $signed(t_1[4]);
  assign t_28 = t_6 & t_27;
  assign t_29 = $signed(t_1[5]);
  assign t_30 = t_7 & t_29;
  assign t_31 = $signed(t_1[6]);
  assign t_32 = t_8 & t_31;
  assign t_33 = $signed(t_1[7]);
  assign t_34 = t_9 & t_33;
  assign t_35 = $signed(t_1[8]);
  assign t_36 = t_10 & t_35;
  assign t_37 = $signed(t_1[9]);
  assign t_38 = t_11 & t_37;
  assign t_39 = $signed(t_1[10]);
  assign t_40 = t_12 & t_39;
  assign t_41 = $signed(t_1[11]);
  assign t_42 = t_13 & t_41;
  assign t_43 = $signed(t_1[12]);
  assign t_44 = t_14 & t_43;
  assign t_45 = $signed(t_1[13]);
  assign t_46 = t_15 & t_45;
  assign t_47 = $signed(t_1[14]);
  assign t_48 = t_16 & t_47;
  assign t_49 = $signed(t_1[15]);
  assign t_50 = t_17 & t_49;
  assign t_18 = t_20 | t_22 | t_24 | t_26 | t_28 | t_30 | t_32 | t_34 | t_36 | t_38 | t_40 | t_42 | t_44 | t_46 | t_48 | t_50;
endmodule

// @(68:10) DECODER@(68:10)(width=4)
module mod_3 (
  t_0, t_1
);
  // out@(71:9)
  output [15:0] t_1;

  // code@(70:9)
  input [3:0] t_0;

  wire t_2, t_3, t_4, t_5;
  assign t_2 = ~t_0[0];
  assign t_3 = ~t_0[1];
  assign t_4 = ~t_0[2];
  assign t_5 = ~t_0[3];
  assign t_1[0] = t_2 & t_3 & t_4 & t_5;
  assign t_1[1] = t_0[0] & t_3 & t_4 & t_5;
  assign t_1[2] = t_2 & t_0[1] & t_4 & t_5;
  assign t_1[3] = t_0[0] & t_0[1] & t_4 & t_5;
  assign t_1[4] = t_2 & t_3 & t_0[2] & t_5;
  assign t_1[5] = t_0[0] & t_3 & t_0[2] & t_5;
  assign t_1[6] = t_2 & t_0[1] & t_0[2] & t_5;
  assign t_1[7] = t_0[0] & t_0[1] & t_0[2] & t_5;
  assign t_1[8] = t_2 & t_3 & t_4 & t_0[3];
  assign t_1[9] = t_0[0] & t_3 & t_4 & t_0[3];
  assign t_1[10] = t_2 & t_0[1] & t_4 & t_0[3];
  assign t_1[11] = t_0[0] & t_0[1] & t_4 & t_0[3];
  assign t_1[12] = t_2 & t_3 & t_0[2] & t_0[3];
  assign t_1[13] = t_0[0] & t_3 & t_0[2] & t_0[3];
  assign t_1[14] = t_2 & t_0[1] & t_0[2] & t_0[3];
  assign t_1[15] = t_0[0] & t_0[1] & t_0[2] & t_0[3];
endmodule

// @(23:10) clock_divider@(23:10)(max=500000)
module mod_4 (
  t_0, t_1
);
  // clk@(24:14)
  output t_1;

  // clk_50@(24:6)
  input t_0;

  wire t_3, t_6, t_7;
  // count@(25:6)
  wire [31:0] t_2;

  wire [31:0] t_4, t_5;
  reg [31:0]  r_0; // $p_5@(0:0)
  reg  r_1; // $p_6@(0:0)
  mod_6 inst_0 (t_3, t_4, 32'h00000000, t_5);
  mod_5 inst_1 (t_3, t_1, t_6, t_7);
  assign t_3 = t_2 == 32'h0003D08F;
  assign t_4 = t_2 + 32'h00000001;
  assign t_6 = ~t_1;
  assign t_2 = r_0;
  assign t_1 = r_1;
  always @(posedge t_0) begin
    r_0 <= t_5;
    r_1 <= t_7;
  end
endmodule

// @(57:10) MUX@(57:10)(width=1)
module mod_5 (
  t_0, t_2, t_3, t_4
);
  // out@(61:19)
  output t_4;

  // code@(59:9), input[0]@(61:9), input[1]@(61:9)
  input t_0, t_2, t_3;

  wire t_5, t_6;
  // mask@(60:9)
  wire [1:0] t_1;

  mod_7 inst_0 (t_0, t_1);
  assign t_5 = t_2 & t_1[0];
  assign t_6 = t_3 & t_1[1];
  assign t_4 = t_5 | t_6;
endmodule

// @(57:10) MUX@(57:10)(width=1)
module mod_6 (
  t_0, t_2, t_3, t_4
);
  // out@(61:19)
  output [31:0] t_4;

  // code@(59:9)
  input t_0;

  // input[0]@(61:9), input[1]@(61:9)
  input [31:0] t_2, t_3;

  wire [31:0] t_5, t_6, t_7, t_8;
  // mask@(60:9)
  wire [1:0] t_1;

  mod_7 inst_0 (t_0, t_1);
  assign t_5 = $signed(t_1[0]);
  assign t_6 = t_2 & t_5;
  assign t_7 = $signed(t_1[1]);
  assign t_8 = t_3 & t_7;
  assign t_4 = t_6 | t_8;
endmodule

// @(68:10) DECODER@(68:10)(width=1)
module mod_7 (
  t_0, t_1
);
  // out@(71:9)
  output [1:0] t_1;

  // code@(70:9)
  input t_0;

  assign t_1[0] = ~t_0;
  assign t_1[1] = t_0;
  assign t_1[1] = t_0[0];
endmodule

