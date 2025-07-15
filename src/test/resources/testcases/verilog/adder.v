module top(
  _0, _1, _2
);
/*
  COMPONENT "top"; "adder.nx:1:10"
  input: a@0, b@1
  output: c@2
*/
  input wire [7:0] _0; // adder.nx:2:27:a
  input wire [7:0] _1; // adder.nx:2:30:b
  output wire [7:0] _2; // adder.nx:2:9:c
  /*
    COMPONENT "adder"; "adder.nx:2:14"
    input: a@0, b@1
    output: c@2
    attribute: { WIDTH: "8" }
  */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[0], b@1[0], 1'h00
      output: c@2[0], cout@3
    */
      wire _3; // adder.nx:13:25:cout
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[0], b@1[0]
        output: carry@4, out@5
      */
        wire _5; // adder.nx:18:9:out
        wire _4; // adder.nx:19:9:carry
        assign _5 = _0[0] ^ _1[0]; // adder.nx:18:18
        assign _4 = _0[0] & _1[0]; // adder.nx:19:18
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@5, 1'h00
        output: carry@6, c@2[0]
      */
        wire _6; // adder.nx:19:9:carry
        assign _2[0] = _5 ^ 1'h00; // adder.nx:18:18
        assign _6 = _5 & 1'h00; // adder.nx:19:18
      assign _3 = _4 | _6; // adder.nx:14:20
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[1], b@1[1], cout@3
      output: c@2[1], cout@7
    */
      wire _7; // adder.nx:13:25:cout
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[1], b@1[1]
        output: carry@8, out@9
      */
        wire _9; // adder.nx:18:9:out
        wire _8; // adder.nx:19:9:carry
        assign _9 = _0[1] ^ _1[1]; // adder.nx:18:18
        assign _8 = _0[1] & _1[1]; // adder.nx:19:18
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@9, cout@3
        output: carry@10, c@2[1]
      */
        wire _10; // adder.nx:19:9:carry
        assign _2[1] = _9 ^ _3; // adder.nx:18:18
        assign _10 = _9 & _3; // adder.nx:19:18
      assign _7 = _8 | _10; // adder.nx:14:20
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[2], b@1[2], cout@7
      output: c@2[2], cout@11
    */
      wire _11; // adder.nx:13:25:cout
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[2], b@1[2]
        output: carry@12, out@13
      */
        wire _13; // adder.nx:18:9:out
        wire _12; // adder.nx:19:9:carry
        assign _13 = _0[2] ^ _1[2]; // adder.nx:18:18
        assign _12 = _0[2] & _1[2]; // adder.nx:19:18
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@13, cout@7
        output: carry@14, c@2[2]
      */
        wire _14; // adder.nx:19:9:carry
        assign _2[2] = _13 ^ _7; // adder.nx:18:18
        assign _14 = _13 & _7; // adder.nx:19:18
      assign _11 = _12 | _14; // adder.nx:14:20
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[3], b@1[3], cout@11
      output: c@2[3], cout@15
    */
      wire _15; // adder.nx:13:25:cout
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[3], b@1[3]
        output: carry@16, out@17
      */
        wire _17; // adder.nx:18:9:out
        wire _16; // adder.nx:19:9:carry
        assign _17 = _0[3] ^ _1[3]; // adder.nx:18:18
        assign _16 = _0[3] & _1[3]; // adder.nx:19:18
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@17, cout@11
        output: carry@18, c@2[3]
      */
        wire _18; // adder.nx:19:9:carry
        assign _2[3] = _17 ^ _11; // adder.nx:18:18
        assign _18 = _17 & _11; // adder.nx:19:18
      assign _15 = _16 | _18; // adder.nx:14:20
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[4], b@1[4], cout@15
      output: c@2[4], cout@19
    */
      wire _19; // adder.nx:13:25:cout
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[4], b@1[4]
        output: carry@20, out@21
      */
        wire _21; // adder.nx:18:9:out
        wire _20; // adder.nx:19:9:carry
        assign _21 = _0[4] ^ _1[4]; // adder.nx:18:18
        assign _20 = _0[4] & _1[4]; // adder.nx:19:18
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@21, cout@15
        output: carry@22, c@2[4]
      */
        wire _22; // adder.nx:19:9:carry
        assign _2[4] = _21 ^ _15; // adder.nx:18:18
        assign _22 = _21 & _15; // adder.nx:19:18
      assign _19 = _20 | _22; // adder.nx:14:20
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[5], b@1[5], cout@19
      output: c@2[5], cout@23
    */
      wire _23; // adder.nx:13:25:cout
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[5], b@1[5]
        output: carry@24, out@25
      */
        wire _25; // adder.nx:18:9:out
        wire _24; // adder.nx:19:9:carry
        assign _25 = _0[5] ^ _1[5]; // adder.nx:18:18
        assign _24 = _0[5] & _1[5]; // adder.nx:19:18
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@25, cout@19
        output: carry@26, c@2[5]
      */
        wire _26; // adder.nx:19:9:carry
        assign _2[5] = _25 ^ _19; // adder.nx:18:18
        assign _26 = _25 & _19; // adder.nx:19:18
      assign _23 = _24 | _26; // adder.nx:14:20
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[6], b@1[6], cout@23
      output: c@2[6], cout@27
    */
      wire _27; // adder.nx:13:25:cout
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[6], b@1[6]
        output: carry@28, out@29
      */
        wire _29; // adder.nx:18:9:out
        wire _28; // adder.nx:19:9:carry
        assign _29 = _0[6] ^ _1[6]; // adder.nx:18:18
        assign _28 = _0[6] & _1[6]; // adder.nx:19:18
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@29, cout@23
        output: carry@30, c@2[6]
      */
        wire _30; // adder.nx:19:9:carry
        assign _2[6] = _29 ^ _23; // adder.nx:18:18
        assign _30 = _29 & _23; // adder.nx:19:18
      assign _27 = _28 | _30; // adder.nx:14:20
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[7], b@1[7], cout@27
      output: c@2[7], cout@31
    */
      wire _31; // adder.nx:13:25:cout
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[7], b@1[7]
        output: carry@32, out@33
      */
        wire _33; // adder.nx:18:9:out
        wire _32; // adder.nx:19:9:carry
        assign _33 = _0[7] ^ _1[7]; // adder.nx:18:18
        assign _32 = _0[7] & _1[7]; // adder.nx:19:18
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@33, cout@27
        output: carry@34, c@2[7]
      */
        wire _34; // adder.nx:19:9:carry
        assign _2[7] = _33 ^ _27; // adder.nx:18:18
        assign _34 = _33 & _27; // adder.nx:19:18
      assign _31 = _32 | _34; // adder.nx:14:20
endmodule
