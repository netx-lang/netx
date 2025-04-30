// @(1:10)
module top_1(
  a, b, c
);
  output [7:0] c; // @(2:9)
  input [7:0] a, b; // @(2:27), @(2:30)
  adder_1 \$inst_1 (a, b, c);
endmodule

// @(5:10)
module adder_1(
  a, b, c
);
  output [7:0] c; // @(6:15)
  input [7:0] a, b; // @(6:9), @(6:12)
  wire \$p_0 , \$p_1 , \$p_2 , \$p_3 , \$p_4 , \$p_5 , \$p_6 , \$p_7 , \$p_8 , \$p_9 , \$p_10 , \$p_11 , \$p_12 , \$p_13 , \$p_14 , \$p_15 , \$p_16 , \$p_17 , \$p_18 , \$p_19 , \$p_20 , \$p_21 , \$p_22 , \$p_23 ;
  full_adder_1 \$inst_1 (\$p_0 , \$p_1 , 1'h00, c[0], \$p_2 );
  full_adder_1 \$inst_4 (\$p_3 , \$p_4 , \$p_2 , c[1], \$p_5 );
  full_adder_1 \$inst_7 (\$p_6 , \$p_7 , \$p_5 , c[2], \$p_8 );
  full_adder_1 \$inst_10 (\$p_9 , \$p_10 , \$p_8 , c[3], \$p_11 );
  full_adder_1 \$inst_13 (\$p_12 , \$p_13 , \$p_11 , c[4], \$p_14 );
  full_adder_1 \$inst_16 (\$p_15 , \$p_16 , \$p_14 , c[5], \$p_17 );
  full_adder_1 \$inst_19 (\$p_18 , \$p_19 , \$p_17 , c[6], \$p_20 );
  full_adder_1 \$inst_22 (\$p_21 , \$p_22 , \$p_20 , c[7], \$p_23 );
  assign \$p_0  = a[0];
  assign \$p_1  = b[0];
  assign \$p_3  = a[1];
  assign \$p_4  = b[1];
  assign \$p_6  = a[2];
  assign \$p_7  = b[2];
  assign \$p_9  = a[3];
  assign \$p_10  = b[3];
  assign \$p_12  = a[4];
  assign \$p_13  = b[4];
  assign \$p_15  = a[5];
  assign \$p_16  = b[5];
  assign \$p_18  = a[6];
  assign \$p_19  = b[6];
  assign \$p_21  = a[7];
  assign \$p_22  = b[7];
endmodule

// @(12:10)
module full_adder_1(
  a, b, cin, out, cout
);
  output out, cout; // @(13:20), @(13:25)
  input a, b, cin; // @(13:9), @(13:12), @(13:15)
  wire \$p_0 , \$p_1 , \$p_2 ;
  half_adder_1 \$inst_1 (a, b, \$p_0 , \$p_1 );
  half_adder_1 \$inst_2 (\$p_1 , cin, \$p_2 , out);
  assign cout = \$p_0  | \$p_2 ;
endmodule

// @(17:10)
module half_adder_1(
  a, b, carry, out
);
  output out, carry; // @(18:9), @(19:9)
  input a, b; // @(18:26), @(18:29)
  assign out = a ^ b;
  assign carry = a & b;
endmodule

