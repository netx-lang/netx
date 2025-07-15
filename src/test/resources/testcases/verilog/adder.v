// @(adder.nx:1:10) top@(adder.nx:1:10)
module mod_0 (
  t_0, t_1, t_2
);
  // c@(adder.nx:2:9)
  output [7:0] t_2;

  // a@(adder.nx:2:27), b@(adder.nx:2:30)
  input [7:0] t_0, t_1;

  mod_1 inst_0 (t_0, t_1, t_2);
endmodule

// @(adder.nx:5:10) adder@(adder.nx:5:10)(WIDTH=8)
module mod_1 (
  t_0, t_1, t_2
);
  // c@(adder.nx:6:15)
  output [7:0] t_2;

  // a@(adder.nx:6:9), b@(adder.nx:6:12)
  input [7:0] t_0, t_1;

  wire t_3, t_4, t_5, t_6, t_7, t_8, t_9, t_10;
  mod_2 inst_0 (t_0[0], t_1[0], 1'h00, t_2[0], t_3);
  mod_2 inst_1 (t_0[1], t_1[1], t_3, t_2[1], t_4);
  mod_2 inst_2 (t_0[2], t_1[2], t_4, t_2[2], t_5);
  mod_2 inst_3 (t_0[3], t_1[3], t_5, t_2[3], t_6);
  mod_2 inst_4 (t_0[4], t_1[4], t_6, t_2[4], t_7);
  mod_2 inst_5 (t_0[5], t_1[5], t_7, t_2[5], t_8);
  mod_2 inst_6 (t_0[6], t_1[6], t_8, t_2[6], t_9);
  mod_2 inst_7 (t_0[7], t_1[7], t_9, t_2[7], t_10);
endmodule

// @(adder.nx:12:10) full_adder@(adder.nx:12:10)
module mod_2 (
  t_0, t_1, t_2, t_3, t_4
);
  // out@(adder.nx:13:20), cout@(adder.nx:13:25)
  output t_3, t_4;

  // a@(adder.nx:13:9), b@(adder.nx:13:12), cin@(adder.nx:13:15)
  input t_0, t_1, t_2;

  wire t_5, t_6, t_7;
  mod_3 inst_0 (t_0, t_1, t_5, t_6);
  mod_3 inst_1 (t_6, t_2, t_7, t_3);
  assign t_4 = t_5 | t_7;
endmodule

// @(adder.nx:17:10) half_adder@(adder.nx:17:10)
module mod_3 (
  t_0, t_1, t_3, t_2
);
  // out@(adder.nx:18:9), carry@(adder.nx:19:9)
  output t_2, t_3;

  // a@(adder.nx:18:26), b@(adder.nx:18:29)
  input t_0, t_1;

  assign t_2 = t_0 ^ t_1;
  assign t_3 = t_0 & t_1;
endmodule

