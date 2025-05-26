// @(1:10) top@(1:10)
module mod_0 (
  t_0, t_1, t_2
);
  // c@(2:9)
  output [7:0] t_2;

  // a@(2:27), b@(2:30)
  input [7:0] t_0, t_1;

  mod_1 inst_0 (t_0, t_1, t_2);
endmodule

// @(5:10) adder@(5:10)
module mod_1 (
  t_0, t_1, t_2
);
  // c@(6:15)
  output [7:0] t_2;

  // a@(6:9), b@(6:12)
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

// @(12:10) full_adder@(12:10)
module mod_2 (
  t_0, t_1, t_2, t_3, t_4
);
  // out@(13:20), cout@(13:25)
  output t_3, t_4;

  // a@(13:9), b@(13:12), cin@(13:15)
  input t_0, t_1, t_2;

  wire t_5, t_6, t_7;
  mod_3 inst_0 (t_0, t_1, t_5, t_6);
  mod_3 inst_1 (t_6, t_2, t_7, t_3);
  assign t_4 = t_5 | t_7;
endmodule

// @(17:10) half_adder@(17:10)
module mod_3 (
  t_0, t_1, t_3, t_2
);
  // out@(18:9), carry@(19:9)
  output t_2, t_3;

  // a@(18:26), b@(18:29)
  input t_0, t_1;

  assign t_2 = t_0 ^ t_1;
  assign t_3 = t_0 & t_1;
endmodule

