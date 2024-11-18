// @(5:10)
module Divide_by_3_1(
  clk, clk_3
);
  input clk;
  output clk_3;
  wire a; // @(7:9)
  wire b; // @(7:12)
  wire clk; // @(6:9)
  wire clk_3; // @(6:14)
  wire c; // @(7:15)
  wire o_2;
  wire o_3;
  wire o_1;
  reg p_0;
  reg p_1;
  reg p_2;
  assign o_1 = ~a;
  assign o_2 = ~b;
  assign o_3 = o_1 & o_2;
  assign clk_3 = b | c;
  assign c = p_2;
  always @(negedge clk) begin
    p_2 <= b;
  end
  assign a = p_0;
  assign b = p_1;
  always @(posedge clk) begin
    p_0 <= o_3;
    p_1 <= a;
  end
endmodule

