// @(1:10)
module full_adder_1(
  a, b, cin, out, cout
);
  input a;
  input b;
  input cin;
  output out;
  output cout;
  wire p_0;
  wire a; // @(2:9)
  wire b; // @(2:12)
  wire p_2;
  wire p_1;
  wire cout; // @(2:25)
  wire cin; // @(2:15)
  wire out; // @(2:20)
  half_adder_1 inst_1(a, b, p_0, p_1);
  half_adder_1 inst_2(p_1, cin, p_2, out);
  assign cout = p_0 | p_2;
endmodule

// @(6:10)
module half_adder_1(
  a, b, carry, out
);
  input a;
  input b;
  output carry;
  output out;
  wire a; // @(7:9)
  wire b; // @(7:12)
  wire carry; // @(7:20)
  wire out; // @(7:15)
  assign out = a ^ b;
  assign carry = a & b;
endmodule

