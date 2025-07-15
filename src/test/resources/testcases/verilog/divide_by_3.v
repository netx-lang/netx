module Divide_by_3(
  _0, _1
);
/*
  COMPONENT "Divide_by_3"; "divide_by_3.nx:1:10"
  input: clk@0
  output: clk_3@1
*/
  input wire _0; // CLOCK divide_by_3.nx:2:9:clk
  output wire _1; // CLOCK divide_by_3.nx:2:14:clk_3
  wire _2; // divide_by_3.nx:3:9:a
  wire _3; // divide_by_3.nx:3:12:b
  wire _4; // divide_by_3.nx:3:15:c
  wire _5; // divide_by_3.nx:8:28:$o
  assign _5 = ~_2; // divide_by_3.nx:8:28
  wire _6; // divide_by_3.nx:8:38:$o
  assign _6 = ~_3; // divide_by_3.nx:8:38
  wire _7; // divide_by_3.nx:8:20:$o
  assign _7 = _5 & _6; // divide_by_3.nx:8:20
  always @(posedge _7) _2 <= _7; // divide_by_3.nx:5:29
  always @(posedge _2) _3 <= _2; // divide_by_3.nx:5:29
  always @(negedge _3) _4 <= _3; // divide_by_3.nx:5:29
  assign _1 = _3 | _4; // divide_by_3.nx:11:13
endmodule
