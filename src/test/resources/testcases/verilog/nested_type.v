module top(
  _0, 2'h00, 2'h03, _1, _2, _3, _4, _5, _6, _7, _8, _9
);

/*============================== DATA ==============================*/

/* "top"; "nested_type.nx:12:10" */
  output wire [1:0] _1; // nested_type.nx:14:11:b.b[0].left
  output wire [1:0] _2; // nested_type.nx:14:11:b.b[0].right
  output wire [1:0] _3; // nested_type.nx:14:11:b.b[1].left
  output wire [1:0] _4; // nested_type.nx:14:11:b.b[1].right
  output wire [1:0] _5; // nested_type.nx:14:11:b.b[2].left
  output wire [1:0] _6; // nested_type.nx:14:11:b.b[2].right
  output wire [1:0] _7; // nested_type.nx:14:11:b.b[3].left
  output wire [1:0] _8; // nested_type.nx:14:11:b.b[3].right
  output wire [2:0] _9; // nested_type.nx:14:11:b.c
  wire _10; // nested_type.nx:16:9:c
  input wire _0; // nested_type.nx:16:12:d
  wire [1:0] _11; // nested_type.nx:20:31:$o
  wire [1:0] _12; // nested_type.nx:21:31:$o
/* END "top"; "nested_type.nx:12:10" */

/*============================== CONNECTION ==============================*/

/*
  COMPONENT "top"; "nested_type.nx:12:10"
  input: d@0
  output: 2'h00, 2'h03, b.b[0].left@1, b.b[0].right@2, b.b[1].left@3, b.b[1].right@4, b.b[2].left@5, b.b[2].right@6, b.b[3].left@7, b.b[3].right@8, b.c@9
*/
  assign _10 = ~_0; // nested_type.nx:17:9
  assign _11 = $signed(_10); // nested_type.nx:20:31
  assign _1 = 2'h00 + _11; // nested_type.nx:20:12
  assign _12 = $signed(_0); // nested_type.nx:21:31
  assign _2 = 2'h00 + _12; // nested_type.nx:21:12
  assign _3 = 2'h01 + _11; // nested_type.nx:20:12
  assign _4 = 2'h01 + _12; // nested_type.nx:21:12
  assign _5 = 2'h02 + _11; // nested_type.nx:20:12
  assign _6 = 2'h02 + _12; // nested_type.nx:21:12
  assign _7 = 2'h03 + _11; // nested_type.nx:20:12
  assign _8 = 2'h03 + _12; // nested_type.nx:21:12
  assign _9 = $signed(_10); // nested_type.nx:24:22
/* END "top"; "nested_type.nx:12:10" */
endmodule
