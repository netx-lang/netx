// @(nested_type.nx:12:10) top@(nested_type.nx:12:10)
module mod_0 (
  t_10, t_13, t_14, t_0, t_1, t_2, t_3, t_4, t_5, t_6, t_7, t_8
);
  // b.c@(nested_type.nx:14:11)
  output [2:0] t_8;

  // b.b[0].left@(nested_type.nx:14:11), b.b[0].right@(nested_type.nx:14:11), b.b[1].left@(nested_type.nx:14:11), b.b[1].right@(nested_type.nx:14:11), b.b[2].left@(nested_type.nx:14:11), b.b[2].right@(nested_type.nx:14:11), b.b[3].left@(nested_type.nx:14:11), b.b[3].right@(nested_type.nx:14:11)
  output [1:0] t_0, t_1, t_2, t_3, t_4, t_5, t_6, t_7;

  output [1:0] t_13, t_14;
  // d@(nested_type.nx:16:12)
  input t_10;

  // c@(nested_type.nx:16:9)
  wire t_9;

  wire [1:0] t_11, t_12;
  assign t_9 = ~t_10;
  assign t_11 = $signed(t_9);
  assign t_0 = 2'h00 + t_11;
  assign t_12 = $signed(t_10);
  assign t_1 = 2'h00 + t_12;
  assign t_2 = 2'h01 + t_11;
  assign t_3 = 2'h01 + t_12;
  assign t_4 = 2'h02 + t_11;
  assign t_5 = 2'h02 + t_12;
  assign t_6 = 2'h03 + t_11;
  assign t_7 = 2'h03 + t_12;
  assign t_8 = $signed(t_9);
  assign t_14 = 2'h03;
endmodule

