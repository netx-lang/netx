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
  wire \$p_0 , \$p_1 , \$p_2 , \$p_3 , \$p_4 , \$p_5 , \$p_6 , \$p_7 ;
  full_adder_1 \$inst_1 (a[0], b[0], 1'h00, c[0], \$p_0 );
  full_adder_1 \$inst_4 (a[1], b[1], \$p_0 , c[1], \$p_1 );
  full_adder_1 \$inst_7 (a[2], b[2], \$p_1 , c[2], \$p_2 );
  full_adder_1 \$inst_10 (a[3], b[3], \$p_2 , c[3], \$p_3 );
  full_adder_1 \$inst_13 (a[4], b[4], \$p_3 , c[4], \$p_4 );
  full_adder_1 \$inst_16 (a[5], b[5], \$p_4 , c[5], \$p_5 );
  full_adder_1 \$inst_19 (a[6], b[6], \$p_5 , c[6], \$p_6 );
  full_adder_1 \$inst_22 (a[7], b[7], \$p_6 , c[7], \$p_7 );
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

