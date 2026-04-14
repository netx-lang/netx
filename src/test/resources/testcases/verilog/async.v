module top(
  _0, _1, _2, _3
);

/*============================== DATA ==============================*/

reg [31:0] __reg__3;
/* "top"; "async.nx:1:0" */
  input wire [31:0] _2; // async.nx:2:9:i
  output wire [31:0] _3; // async.nx:2:12:o
  input wire _1; // async.nx:3:9:rst
  input wire _0; // CLOCK async.nx:4:9:clk
/* END "top"; "async.nx:1:0" */

/*============================== CONNECTION ==============================*/

assign _3 = __reg__3;
/*
  COMPONENT "top"; "async.nx:1:0"
  input: clk@0, rst@1, i@2
  output: o@3
*/
  always @(posedge _0 or negedge _1) begin
    if (~_1) __reg__3 <= 0;
    else __reg__3 <= _2;
  end // async.nx:6:9
/* END "top"; "async.nx:1:0" */
endmodule
