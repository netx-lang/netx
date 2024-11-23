// @(1:10)
module top_1(
  a, b, c
);
  input a;
  input b;
  output c;
  wire [7:0] a; // @(2:9)
  wire [7:0] b; // @(2:12)
  wire [7:0] c; // @(2:15)
  adder_1 inst_1(a, b, c);
endmodule

// @(6:10)
module adder_1(
  a, b, c
);
  input a;
  input b;
  output c;
  wire p_0;
  wire [7:0] a; // @(7:9)
  wire [7:0] b; // @(7:12)
  wire p_2;
  wire [7:0] c; // @(7:15)
  wire p_1;
  wire p_4;
  wire p_3;
  wire p_6;
  wire p_5;
  wire p_7;
  full_adder_1 inst_1(a[0], b[0], 1'h00, c[0], p_0);
  full_adder_1 inst_2(a[1], b[1], p_0, c[1], p_1);
  full_adder_1 inst_3(a[2], b[2], p_1, c[2], p_2);
  full_adder_1 inst_4(a[3], b[3], p_2, c[3], p_3);
  full_adder_1 inst_5(a[4], b[4], p_3, c[4], p_4);
  full_adder_1 inst_6(a[5], b[5], p_4, c[5], p_5);
  full_adder_1 inst_7(a[6], b[6], p_5, c[6], p_6);
  full_adder_1 inst_8(a[7], b[7], p_6, c[7], p_7);
endmodule

// @(13:10)
module full_adder_1(
  a, b, cin, out, cout
);
  input a;
  input b;
  input cin;
  output out;
  output cout;
  wire p_0;
  wire a; // @(14:9)
  wire b; // @(14:12)
  wire p_2;
  wire p_1;
  wire cout; // @(14:25)
  wire cin; // @(14:15)
  wire out; // @(14:20)
  half_adder_1 inst_1(a, b, p_0, p_1);
  half_adder_1 inst_2(p_1, cin, p_2, out);
  assign cout = p_0 | p_2;
endmodule

// @(18:10)
module half_adder_1(
  a, b, carry, out
);
  input a;
  input b;
  output carry;
  output out;
  wire a; // @(19:9)
  wire b; // @(19:12)
  wire carry; // @(19:20)
  wire out; // @(19:15)
  assign out = a ^ b;
  assign carry = a & b;
endmodule

