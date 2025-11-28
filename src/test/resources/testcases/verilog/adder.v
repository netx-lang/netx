module top(
  _0, _1, _2
);

/*============================== DATA ==============================*/

/* "top"; "adder.nx:1:10" */
  input wire [7:0] _0; // adder.nx:2:27:a
  input wire [7:0] _1; // adder.nx:2:30:b
  output wire [7:0] _2; // adder.nx:2:9:c
  /* "adder"; "adder.nx:2:14" */
    /* "full_adder"; "adder.nx:7:34" */
      wire _3; // adder.nx:13:25:cout
      /* "half_adder"; "adder.nx:14:52" */
        wire _4; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:52" */
      /* "half_adder"; "adder.nx:14:36" */
        wire _5; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:36" */
    /* END "full_adder"; "adder.nx:7:34" */
    /* "full_adder"; "adder.nx:7:34" */
      wire _6; // adder.nx:13:25:cout
      /* "half_adder"; "adder.nx:14:52" */
        wire _8; // adder.nx:18:9:out
        wire _7; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:52" */
      /* "half_adder"; "adder.nx:14:36" */
        wire _9; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:36" */
    /* END "full_adder"; "adder.nx:7:34" */
    /* "full_adder"; "adder.nx:7:34" */
      wire _10; // adder.nx:13:25:cout
      /* "half_adder"; "adder.nx:14:52" */
        wire _12; // adder.nx:18:9:out
        wire _11; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:52" */
      /* "half_adder"; "adder.nx:14:36" */
        wire _13; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:36" */
    /* END "full_adder"; "adder.nx:7:34" */
    /* "full_adder"; "adder.nx:7:34" */
      wire _14; // adder.nx:13:25:cout
      /* "half_adder"; "adder.nx:14:52" */
        wire _16; // adder.nx:18:9:out
        wire _15; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:52" */
      /* "half_adder"; "adder.nx:14:36" */
        wire _17; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:36" */
    /* END "full_adder"; "adder.nx:7:34" */
    /* "full_adder"; "adder.nx:7:34" */
      wire _18; // adder.nx:13:25:cout
      /* "half_adder"; "adder.nx:14:52" */
        wire _20; // adder.nx:18:9:out
        wire _19; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:52" */
      /* "half_adder"; "adder.nx:14:36" */
        wire _21; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:36" */
    /* END "full_adder"; "adder.nx:7:34" */
    /* "full_adder"; "adder.nx:7:34" */
      wire _22; // adder.nx:13:25:cout
      /* "half_adder"; "adder.nx:14:52" */
        wire _24; // adder.nx:18:9:out
        wire _23; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:52" */
      /* "half_adder"; "adder.nx:14:36" */
        wire _25; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:36" */
    /* END "full_adder"; "adder.nx:7:34" */
    /* "full_adder"; "adder.nx:7:34" */
      wire _26; // adder.nx:13:25:cout
      /* "half_adder"; "adder.nx:14:52" */
        wire _28; // adder.nx:18:9:out
        wire _27; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:52" */
      /* "half_adder"; "adder.nx:14:36" */
        wire _29; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:36" */
    /* END "full_adder"; "adder.nx:7:34" */
    /* "full_adder"; "adder.nx:7:34" */
      wire _30; // adder.nx:13:25:cout
      /* "half_adder"; "adder.nx:14:52" */
        wire _32; // adder.nx:18:9:out
        wire _31; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:52" */
      /* "half_adder"; "adder.nx:14:36" */
        wire _33; // adder.nx:19:9:carry
      /* END "half_adder"; "adder.nx:14:36" */
    /* END "full_adder"; "adder.nx:7:34" */
  /* END "adder"; "adder.nx:2:14" */
/* END "top"; "adder.nx:1:10" */

/*============================== CONNECTION ==============================*/

/*
  COMPONENT "top"; "adder.nx:1:10"
  input: a@0, b@1
  output: c@2
*/
  /*
    COMPONENT "adder"; "adder.nx:2:14"
    input: a@0, b@1
    output: c@2
    attribute: { WIDTH: "8" }
  */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[0], b@1[0], 1'h0
      output: c@2[0], cout@3
    */
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[0], b@1[0]
        output: carry@4, c@2[0]
      */
        assign _2[0] = _0[0] ^ _1[0]; // adder.nx:18:18
        assign _4 = _0[0] & _1[0]; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:52" */
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: c@2[0], 1'h0
        output: carry@5, c@2[0]
      */
        assign _5 = _2[0] & 1'h0; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:36" */
      assign _3 = _4 | _5; // adder.nx:14:20
    /* END "full_adder"; "adder.nx:7:34" */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[1], b@1[1], cout@3
      output: c@2[1], cout@6
    */
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[1], b@1[1]
        output: carry@7, out@8
      */
        assign _8 = _0[1] ^ _1[1]; // adder.nx:18:18
        assign _7 = _0[1] & _1[1]; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:52" */
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@8, cout@3
        output: carry@9, c@2[1]
      */
        assign _2[1] = _8 ^ _3; // adder.nx:18:18
        assign _9 = _8 & _3; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:36" */
      assign _6 = _7 | _9; // adder.nx:14:20
    /* END "full_adder"; "adder.nx:7:34" */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[2], b@1[2], cout@6
      output: c@2[2], cout@10
    */
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[2], b@1[2]
        output: carry@11, out@12
      */
        assign _12 = _0[2] ^ _1[2]; // adder.nx:18:18
        assign _11 = _0[2] & _1[2]; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:52" */
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@12, cout@6
        output: carry@13, c@2[2]
      */
        assign _2[2] = _12 ^ _6; // adder.nx:18:18
        assign _13 = _12 & _6; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:36" */
      assign _10 = _11 | _13; // adder.nx:14:20
    /* END "full_adder"; "adder.nx:7:34" */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[3], b@1[3], cout@10
      output: c@2[3], cout@14
    */
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[3], b@1[3]
        output: carry@15, out@16
      */
        assign _16 = _0[3] ^ _1[3]; // adder.nx:18:18
        assign _15 = _0[3] & _1[3]; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:52" */
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@16, cout@10
        output: carry@17, c@2[3]
      */
        assign _2[3] = _16 ^ _10; // adder.nx:18:18
        assign _17 = _16 & _10; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:36" */
      assign _14 = _15 | _17; // adder.nx:14:20
    /* END "full_adder"; "adder.nx:7:34" */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[4], b@1[4], cout@14
      output: c@2[4], cout@18
    */
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[4], b@1[4]
        output: carry@19, out@20
      */
        assign _20 = _0[4] ^ _1[4]; // adder.nx:18:18
        assign _19 = _0[4] & _1[4]; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:52" */
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@20, cout@14
        output: carry@21, c@2[4]
      */
        assign _2[4] = _20 ^ _14; // adder.nx:18:18
        assign _21 = _20 & _14; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:36" */
      assign _18 = _19 | _21; // adder.nx:14:20
    /* END "full_adder"; "adder.nx:7:34" */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[5], b@1[5], cout@18
      output: c@2[5], cout@22
    */
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[5], b@1[5]
        output: carry@23, out@24
      */
        assign _24 = _0[5] ^ _1[5]; // adder.nx:18:18
        assign _23 = _0[5] & _1[5]; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:52" */
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@24, cout@18
        output: carry@25, c@2[5]
      */
        assign _2[5] = _24 ^ _18; // adder.nx:18:18
        assign _25 = _24 & _18; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:36" */
      assign _22 = _23 | _25; // adder.nx:14:20
    /* END "full_adder"; "adder.nx:7:34" */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[6], b@1[6], cout@22
      output: c@2[6], cout@26
    */
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[6], b@1[6]
        output: carry@27, out@28
      */
        assign _28 = _0[6] ^ _1[6]; // adder.nx:18:18
        assign _27 = _0[6] & _1[6]; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:52" */
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@28, cout@22
        output: carry@29, c@2[6]
      */
        assign _2[6] = _28 ^ _22; // adder.nx:18:18
        assign _29 = _28 & _22; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:36" */
      assign _26 = _27 | _29; // adder.nx:14:20
    /* END "full_adder"; "adder.nx:7:34" */
    /*
      COMPONENT "full_adder"; "adder.nx:7:34"
      input: a@0[7], b@1[7], cout@26
      output: c@2[7], cout@30
    */
      /*
        COMPONENT "half_adder"; "adder.nx:14:52"
        input: a@0[7], b@1[7]
        output: carry@31, out@32
      */
        assign _32 = _0[7] ^ _1[7]; // adder.nx:18:18
        assign _31 = _0[7] & _1[7]; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:52" */
      /*
        COMPONENT "half_adder"; "adder.nx:14:36"
        input: out@32, cout@26
        output: carry@33, c@2[7]
      */
        assign _2[7] = _32 ^ _26; // adder.nx:18:18
        assign _33 = _32 & _26; // adder.nx:19:18
      /* END "half_adder"; "adder.nx:14:36" */
      assign _30 = _31 | _33; // adder.nx:14:20
    /* END "full_adder"; "adder.nx:7:34" */
  /* END "adder"; "adder.nx:2:14" */
/* END "top"; "adder.nx:1:10" */
endmodule
