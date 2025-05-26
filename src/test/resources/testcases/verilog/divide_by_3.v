// @(1:10) Divide_by_3@(1:10)
module mod_0 (
  t_0, t_1
);
  // clk_3@(2:14)
  output t_1;

  // clk@(2:9)
  input t_0;

  // a@(3:9), b@(3:12), c@(3:15)
  wire t_2, t_3, t_4;

  wire t_5, t_6, t_7;
  reg  r_0; // $p_0@(0:0)
  reg  r_1; // $p_1@(0:0)
  reg  r_2; // $p_2@(0:0)
  assign t_5 = ~t_2;
  assign t_6 = ~t_3;
  assign t_7 = t_5 & t_6;
  assign t_1 = t_3 | t_4;
  assign t_4 = r_2;
  always @(negedge t_0) begin
    r_2 <= t_3;
  end
  assign t_2 = r_0;
  assign t_3 = r_1;
  always @(posedge t_0) begin
    r_0 <= t_7;
    r_1 <= t_2;
  end
endmodule

