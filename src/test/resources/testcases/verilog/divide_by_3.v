module Divide_by_3(
  _0, _1
);

/*============================== DATA ==============================*/

reg __reg__2;
reg __reg__3;
reg __reg__4;
/* "Divide_by_3"; "divide_by_3.nx:1:0" */
  input wire _0; // CLOCK divide_by_3.nx:2:9:clk
  output wire _1; // CLOCK divide_by_3.nx:2:14:clk_3
  wire _2; // divide_by_3.nx:3:9:a
  wire _3; // divide_by_3.nx:3:12:b
  wire _4; // divide_by_3.nx:3:15:c
  wire _5; // divide_by_3.nx:8:28:$o
  wire _6; // divide_by_3.nx:8:38:$o
  wire _7; // divide_by_3.nx:8:20:$o
/* END "Divide_by_3"; "divide_by_3.nx:1:0" */

/*============================== CONNECTION ==============================*/

assign _2 = __reg__2;
assign _3 = __reg__3;
assign _4 = __reg__4;
/*
  COMPONENT "Divide_by_3"; "divide_by_3.nx:1:0"
  input: clk@0
  output: clk_3@1
*/
  assign _5 = ~_2; // divide_by_3.nx:8:28
  assign _6 = ~_3; // divide_by_3.nx:8:38
  assign _7 = _5 & _6; // divide_by_3.nx:8:20
  always @(posedge _0) __reg__2 <= _7; // divide_by_3.nx:5:29
  always @(posedge _0) __reg__3 <= _2; // divide_by_3.nx:5:29
  always @(negedge _0) __reg__4 <= _3; // divide_by_3.nx:5:29
  assign _1 = _3 | _4; // divide_by_3.nx:11:23
/* END "Divide_by_3"; "divide_by_3.nx:1:0" */
endmodule
