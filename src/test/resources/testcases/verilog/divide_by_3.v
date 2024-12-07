// @(5:10)
module Divide_by_3_1(
  clk, clk_3
);
  wire a; // @(7:9)
  wire b; // @(7:12)
  input clk; // @(6:9)
  output clk_3; // @(6:14)
  wire c; // @(7:15)
  wire \$i2_3 ;
  wire \$i1_3 ;
  wire \$i_4 ;
  reg \$p_0 ;
  reg \$p_1 ;
  reg \$p_2 ;
  assign \$i1_3  = ~a;
  assign \$i2_3  = ~b;
  assign \$i_4  = \$i1_3  & \$i2_3 ;
  assign clk_3 = b | c;
  assign c = \$p_2 ;
  always @(negedge clk) begin
    \$p_2  <= b;
  end
  assign a = \$p_0 ;
  assign b = \$p_1 ;
  always @(posedge clk) begin
    \$p_0  <= \$i_4 ;
    \$p_1  <= a;
  end
endmodule

