module top(
  _0, _1, _2, _3, _4, _5, _6, _7, _8
);

/*============================== DATA ==============================*/

/* "top"; "simple_timer.nx:1:0" */
  input wire _0; // CLOCK simple_timer.nx:2:6:clk_50
  output wire [6:0] _1; // simple_timer.nx:3:6:hex[0]
  output wire [6:0] _2; // simple_timer.nx:3:6:hex[1]
  output wire [6:0] _3; // simple_timer.nx:3:6:hex[2]
  output wire [6:0] _4; // simple_timer.nx:3:6:hex[3]
  output wire [6:0] _5; // simple_timer.nx:3:6:hex[4]
  output wire [6:0] _6; // simple_timer.nx:3:6:hex[5]
  output wire [6:0] _7; // simple_timer.nx:3:6:hex[6]
  output wire [6:0] _8; // simple_timer.nx:3:6:hex[7]
  wire _9; // CLOCK simple_timer.nx:7:6:ms_clk
  wire _10; // CLOCK simple_timer.nx:7:14:s_clk
  wire _11; // CLOCK simple_timer.nx:7:21:m_clk
  wire _12; // CLOCK simple_timer.nx:7:28:h_clk
  wire [31:0] _13; // simple_timer.nx:8:6:ms
  /* "clock_divider"; "simple_timer.nx:10:11" */
    wire [31:0] _14; // simple_timer.nx:25:6:count
    /* "MUX"; "simple_timer.nx:29:17" */
      wire _15; // simple_timer.nx:59:9:code
      wire [1:0] _18; // simple_timer.nx:60:9:mask
      wire [31:0] _16; // simple_timer.nx:61:9:input[0]
      wire [31:0] _17; // simple_timer.nx:61:19:out
      /* "DECODER"; "simple_timer.nx:62:12" */
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [31:0] _19; // simple_timer.nx:64:26:$o
      wire [31:0] _20; // simple_timer.nx:64:8:$o
      wire [31:0] _21; // simple_timer.nx:64:26:$o
      wire [31:0] _22; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:29:17" */
    /* "MUX"; "simple_timer.nx:30:17" */
      wire [1:0] _25; // simple_timer.nx:60:9:mask
      wire _23; // simple_timer.nx:61:9:input[1]
      wire _24; // simple_timer.nx:61:19:out
      /* "DECODER"; "simple_timer.nx:62:12" */
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire _26; // simple_timer.nx:64:8:$o
      wire _27; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:30:17" */
  /* END "clock_divider"; "simple_timer.nx:10:11" */
  /* "MUX"; "simple_timer.nx:11:22" */
    wire _28; // simple_timer.nx:59:9:code
    wire [1:0] _31; // simple_timer.nx:60:9:mask
    wire [31:0] _29; // simple_timer.nx:61:9:input[0]
    wire [31:0] _30; // simple_timer.nx:61:19:out
    /* "DECODER"; "simple_timer.nx:62:12" */
    /* END "DECODER"; "simple_timer.nx:62:12" */
    wire [31:0] _32; // simple_timer.nx:64:26:$o
    wire [31:0] _33; // simple_timer.nx:64:8:$o
    wire [31:0] _34; // simple_timer.nx:64:26:$o
    wire [31:0] _35; // simple_timer.nx:64:8:$o
  /* END "MUX"; "simple_timer.nx:11:22" */
  wire [31:0] _36; // simple_timer.nx:13:34:$o
  /* "hex_trigger"; "simple_timer.nx:13:11" */
    wire [3:0] _37; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _38; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _39; // simple_timer.nx:75:52:$o
        wire _40; // simple_timer.nx:75:52:$o
        wire _41; // simple_timer.nx:75:52:$o
        wire _42; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _43; // simple_timer.nx:64:26:$o
      wire [6:0] _44; // simple_timer.nx:64:8:$o
      wire [6:0] _45; // simple_timer.nx:64:26:$o
      wire [6:0] _46; // simple_timer.nx:64:8:$o
      wire [6:0] _47; // simple_timer.nx:64:26:$o
      wire [6:0] _48; // simple_timer.nx:64:8:$o
      wire [6:0] _49; // simple_timer.nx:64:26:$o
      wire [6:0] _50; // simple_timer.nx:64:8:$o
      wire [6:0] _51; // simple_timer.nx:64:26:$o
      wire [6:0] _52; // simple_timer.nx:64:8:$o
      wire [6:0] _53; // simple_timer.nx:64:26:$o
      wire [6:0] _54; // simple_timer.nx:64:8:$o
      wire [6:0] _55; // simple_timer.nx:64:26:$o
      wire [6:0] _56; // simple_timer.nx:64:8:$o
      wire [6:0] _57; // simple_timer.nx:64:26:$o
      wire [6:0] _58; // simple_timer.nx:64:8:$o
      wire [6:0] _59; // simple_timer.nx:64:26:$o
      wire [6:0] _60; // simple_timer.nx:64:8:$o
      wire [6:0] _61; // simple_timer.nx:64:26:$o
      wire [6:0] _62; // simple_timer.nx:64:8:$o
      wire [6:0] _63; // simple_timer.nx:64:26:$o
      wire [6:0] _64; // simple_timer.nx:64:8:$o
      wire [6:0] _65; // simple_timer.nx:64:26:$o
      wire [6:0] _66; // simple_timer.nx:64:8:$o
      wire [6:0] _67; // simple_timer.nx:64:26:$o
      wire [6:0] _68; // simple_timer.nx:64:8:$o
      wire [6:0] _69; // simple_timer.nx:64:26:$o
      wire [6:0] _70; // simple_timer.nx:64:8:$o
      wire [6:0] _71; // simple_timer.nx:64:26:$o
      wire [6:0] _72; // simple_timer.nx:64:8:$o
      wire [6:0] _73; // simple_timer.nx:64:26:$o
      wire [6:0] _74; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:13:11" */
  wire [31:0] _75; // simple_timer.nx:14:42:$o
  wire [31:0] _76; // simple_timer.nx:14:34:$o
  /* "hex_trigger"; "simple_timer.nx:14:11" */
    wire [3:0] _77; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _78; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _79; // simple_timer.nx:75:52:$o
        wire _80; // simple_timer.nx:75:52:$o
        wire _81; // simple_timer.nx:75:52:$o
        wire _82; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _83; // simple_timer.nx:64:26:$o
      wire [6:0] _84; // simple_timer.nx:64:8:$o
      wire [6:0] _85; // simple_timer.nx:64:26:$o
      wire [6:0] _86; // simple_timer.nx:64:8:$o
      wire [6:0] _87; // simple_timer.nx:64:26:$o
      wire [6:0] _88; // simple_timer.nx:64:8:$o
      wire [6:0] _89; // simple_timer.nx:64:26:$o
      wire [6:0] _90; // simple_timer.nx:64:8:$o
      wire [6:0] _91; // simple_timer.nx:64:26:$o
      wire [6:0] _92; // simple_timer.nx:64:8:$o
      wire [6:0] _93; // simple_timer.nx:64:26:$o
      wire [6:0] _94; // simple_timer.nx:64:8:$o
      wire [6:0] _95; // simple_timer.nx:64:26:$o
      wire [6:0] _96; // simple_timer.nx:64:8:$o
      wire [6:0] _97; // simple_timer.nx:64:26:$o
      wire [6:0] _98; // simple_timer.nx:64:8:$o
      wire [6:0] _99; // simple_timer.nx:64:26:$o
      wire [6:0] _100; // simple_timer.nx:64:8:$o
      wire [6:0] _101; // simple_timer.nx:64:26:$o
      wire [6:0] _102; // simple_timer.nx:64:8:$o
      wire [6:0] _103; // simple_timer.nx:64:26:$o
      wire [6:0] _104; // simple_timer.nx:64:8:$o
      wire [6:0] _105; // simple_timer.nx:64:26:$o
      wire [6:0] _106; // simple_timer.nx:64:8:$o
      wire [6:0] _107; // simple_timer.nx:64:26:$o
      wire [6:0] _108; // simple_timer.nx:64:8:$o
      wire [6:0] _109; // simple_timer.nx:64:26:$o
      wire [6:0] _110; // simple_timer.nx:64:8:$o
      wire [6:0] _111; // simple_timer.nx:64:26:$o
      wire [6:0] _112; // simple_timer.nx:64:8:$o
      wire [6:0] _113; // simple_timer.nx:64:26:$o
      wire [6:0] _114; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:14:11" */
  wire [31:0] _115; // simple_timer.nx:15:42:$o
  wire [31:0] _116; // simple_timer.nx:15:34:$o
  /* "hex_trigger"; "simple_timer.nx:15:11" */
    wire [3:0] _117; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _118; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _119; // simple_timer.nx:75:52:$o
        wire _120; // simple_timer.nx:75:52:$o
        wire _121; // simple_timer.nx:75:52:$o
        wire _122; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _123; // simple_timer.nx:64:26:$o
      wire [6:0] _124; // simple_timer.nx:64:8:$o
      wire [6:0] _125; // simple_timer.nx:64:26:$o
      wire [6:0] _126; // simple_timer.nx:64:8:$o
      wire [6:0] _127; // simple_timer.nx:64:26:$o
      wire [6:0] _128; // simple_timer.nx:64:8:$o
      wire [6:0] _129; // simple_timer.nx:64:26:$o
      wire [6:0] _130; // simple_timer.nx:64:8:$o
      wire [6:0] _131; // simple_timer.nx:64:26:$o
      wire [6:0] _132; // simple_timer.nx:64:8:$o
      wire [6:0] _133; // simple_timer.nx:64:26:$o
      wire [6:0] _134; // simple_timer.nx:64:8:$o
      wire [6:0] _135; // simple_timer.nx:64:26:$o
      wire [6:0] _136; // simple_timer.nx:64:8:$o
      wire [6:0] _137; // simple_timer.nx:64:26:$o
      wire [6:0] _138; // simple_timer.nx:64:8:$o
      wire [6:0] _139; // simple_timer.nx:64:26:$o
      wire [6:0] _140; // simple_timer.nx:64:8:$o
      wire [6:0] _141; // simple_timer.nx:64:26:$o
      wire [6:0] _142; // simple_timer.nx:64:8:$o
      wire [6:0] _143; // simple_timer.nx:64:26:$o
      wire [6:0] _144; // simple_timer.nx:64:8:$o
      wire [6:0] _145; // simple_timer.nx:64:26:$o
      wire [6:0] _146; // simple_timer.nx:64:8:$o
      wire [6:0] _147; // simple_timer.nx:64:26:$o
      wire [6:0] _148; // simple_timer.nx:64:8:$o
      wire [6:0] _149; // simple_timer.nx:64:26:$o
      wire [6:0] _150; // simple_timer.nx:64:8:$o
      wire [6:0] _151; // simple_timer.nx:64:26:$o
      wire [6:0] _152; // simple_timer.nx:64:8:$o
      wire [6:0] _153; // simple_timer.nx:64:26:$o
      wire [6:0] _154; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:15:11" */
  wire [31:0] _155; // simple_timer.nx:16:42:$o
  wire [31:0] _156; // simple_timer.nx:16:34:$o
  /* "hex_trigger"; "simple_timer.nx:16:11" */
    wire [3:0] _157; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _158; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _159; // simple_timer.nx:75:52:$o
        wire _160; // simple_timer.nx:75:52:$o
        wire _161; // simple_timer.nx:75:52:$o
        wire _162; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _163; // simple_timer.nx:64:26:$o
      wire [6:0] _164; // simple_timer.nx:64:8:$o
      wire [6:0] _165; // simple_timer.nx:64:26:$o
      wire [6:0] _166; // simple_timer.nx:64:8:$o
      wire [6:0] _167; // simple_timer.nx:64:26:$o
      wire [6:0] _168; // simple_timer.nx:64:8:$o
      wire [6:0] _169; // simple_timer.nx:64:26:$o
      wire [6:0] _170; // simple_timer.nx:64:8:$o
      wire [6:0] _171; // simple_timer.nx:64:26:$o
      wire [6:0] _172; // simple_timer.nx:64:8:$o
      wire [6:0] _173; // simple_timer.nx:64:26:$o
      wire [6:0] _174; // simple_timer.nx:64:8:$o
      wire [6:0] _175; // simple_timer.nx:64:26:$o
      wire [6:0] _176; // simple_timer.nx:64:8:$o
      wire [6:0] _177; // simple_timer.nx:64:26:$o
      wire [6:0] _178; // simple_timer.nx:64:8:$o
      wire [6:0] _179; // simple_timer.nx:64:26:$o
      wire [6:0] _180; // simple_timer.nx:64:8:$o
      wire [6:0] _181; // simple_timer.nx:64:26:$o
      wire [6:0] _182; // simple_timer.nx:64:8:$o
      wire [6:0] _183; // simple_timer.nx:64:26:$o
      wire [6:0] _184; // simple_timer.nx:64:8:$o
      wire [6:0] _185; // simple_timer.nx:64:26:$o
      wire [6:0] _186; // simple_timer.nx:64:8:$o
      wire [6:0] _187; // simple_timer.nx:64:26:$o
      wire [6:0] _188; // simple_timer.nx:64:8:$o
      wire [6:0] _189; // simple_timer.nx:64:26:$o
      wire [6:0] _190; // simple_timer.nx:64:8:$o
      wire [6:0] _191; // simple_timer.nx:64:26:$o
      wire [6:0] _192; // simple_timer.nx:64:8:$o
      wire [6:0] _193; // simple_timer.nx:64:26:$o
      wire [6:0] _194; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:16:11" */
  wire [31:0] _195; // simple_timer.nx:17:42:$o
  wire [31:0] _196; // simple_timer.nx:17:34:$o
  /* "hex_trigger"; "simple_timer.nx:17:11" */
    wire [3:0] _197; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _198; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _199; // simple_timer.nx:75:52:$o
        wire _200; // simple_timer.nx:75:52:$o
        wire _201; // simple_timer.nx:75:52:$o
        wire _202; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _203; // simple_timer.nx:64:26:$o
      wire [6:0] _204; // simple_timer.nx:64:8:$o
      wire [6:0] _205; // simple_timer.nx:64:26:$o
      wire [6:0] _206; // simple_timer.nx:64:8:$o
      wire [6:0] _207; // simple_timer.nx:64:26:$o
      wire [6:0] _208; // simple_timer.nx:64:8:$o
      wire [6:0] _209; // simple_timer.nx:64:26:$o
      wire [6:0] _210; // simple_timer.nx:64:8:$o
      wire [6:0] _211; // simple_timer.nx:64:26:$o
      wire [6:0] _212; // simple_timer.nx:64:8:$o
      wire [6:0] _213; // simple_timer.nx:64:26:$o
      wire [6:0] _214; // simple_timer.nx:64:8:$o
      wire [6:0] _215; // simple_timer.nx:64:26:$o
      wire [6:0] _216; // simple_timer.nx:64:8:$o
      wire [6:0] _217; // simple_timer.nx:64:26:$o
      wire [6:0] _218; // simple_timer.nx:64:8:$o
      wire [6:0] _219; // simple_timer.nx:64:26:$o
      wire [6:0] _220; // simple_timer.nx:64:8:$o
      wire [6:0] _221; // simple_timer.nx:64:26:$o
      wire [6:0] _222; // simple_timer.nx:64:8:$o
      wire [6:0] _223; // simple_timer.nx:64:26:$o
      wire [6:0] _224; // simple_timer.nx:64:8:$o
      wire [6:0] _225; // simple_timer.nx:64:26:$o
      wire [6:0] _226; // simple_timer.nx:64:8:$o
      wire [6:0] _227; // simple_timer.nx:64:26:$o
      wire [6:0] _228; // simple_timer.nx:64:8:$o
      wire [6:0] _229; // simple_timer.nx:64:26:$o
      wire [6:0] _230; // simple_timer.nx:64:8:$o
      wire [6:0] _231; // simple_timer.nx:64:26:$o
      wire [6:0] _232; // simple_timer.nx:64:8:$o
      wire [6:0] _233; // simple_timer.nx:64:26:$o
      wire [6:0] _234; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:17:11" */
  wire [31:0] _235; // simple_timer.nx:18:42:$o
  wire [31:0] _236; // simple_timer.nx:18:34:$o
  /* "hex_trigger"; "simple_timer.nx:18:11" */
    wire [3:0] _237; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _238; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _239; // simple_timer.nx:75:52:$o
        wire _240; // simple_timer.nx:75:52:$o
        wire _241; // simple_timer.nx:75:52:$o
        wire _242; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _243; // simple_timer.nx:64:26:$o
      wire [6:0] _244; // simple_timer.nx:64:8:$o
      wire [6:0] _245; // simple_timer.nx:64:26:$o
      wire [6:0] _246; // simple_timer.nx:64:8:$o
      wire [6:0] _247; // simple_timer.nx:64:26:$o
      wire [6:0] _248; // simple_timer.nx:64:8:$o
      wire [6:0] _249; // simple_timer.nx:64:26:$o
      wire [6:0] _250; // simple_timer.nx:64:8:$o
      wire [6:0] _251; // simple_timer.nx:64:26:$o
      wire [6:0] _252; // simple_timer.nx:64:8:$o
      wire [6:0] _253; // simple_timer.nx:64:26:$o
      wire [6:0] _254; // simple_timer.nx:64:8:$o
      wire [6:0] _255; // simple_timer.nx:64:26:$o
      wire [6:0] _256; // simple_timer.nx:64:8:$o
      wire [6:0] _257; // simple_timer.nx:64:26:$o
      wire [6:0] _258; // simple_timer.nx:64:8:$o
      wire [6:0] _259; // simple_timer.nx:64:26:$o
      wire [6:0] _260; // simple_timer.nx:64:8:$o
      wire [6:0] _261; // simple_timer.nx:64:26:$o
      wire [6:0] _262; // simple_timer.nx:64:8:$o
      wire [6:0] _263; // simple_timer.nx:64:26:$o
      wire [6:0] _264; // simple_timer.nx:64:8:$o
      wire [6:0] _265; // simple_timer.nx:64:26:$o
      wire [6:0] _266; // simple_timer.nx:64:8:$o
      wire [6:0] _267; // simple_timer.nx:64:26:$o
      wire [6:0] _268; // simple_timer.nx:64:8:$o
      wire [6:0] _269; // simple_timer.nx:64:26:$o
      wire [6:0] _270; // simple_timer.nx:64:8:$o
      wire [6:0] _271; // simple_timer.nx:64:26:$o
      wire [6:0] _272; // simple_timer.nx:64:8:$o
      wire [6:0] _273; // simple_timer.nx:64:26:$o
      wire [6:0] _274; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:18:11" */
  wire [31:0] _275; // simple_timer.nx:19:42:$o
  wire [31:0] _276; // simple_timer.nx:19:34:$o
  /* "hex_trigger"; "simple_timer.nx:19:11" */
    wire [3:0] _277; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _278; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _279; // simple_timer.nx:75:52:$o
        wire _280; // simple_timer.nx:75:52:$o
        wire _281; // simple_timer.nx:75:52:$o
        wire _282; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _283; // simple_timer.nx:64:26:$o
      wire [6:0] _284; // simple_timer.nx:64:8:$o
      wire [6:0] _285; // simple_timer.nx:64:26:$o
      wire [6:0] _286; // simple_timer.nx:64:8:$o
      wire [6:0] _287; // simple_timer.nx:64:26:$o
      wire [6:0] _288; // simple_timer.nx:64:8:$o
      wire [6:0] _289; // simple_timer.nx:64:26:$o
      wire [6:0] _290; // simple_timer.nx:64:8:$o
      wire [6:0] _291; // simple_timer.nx:64:26:$o
      wire [6:0] _292; // simple_timer.nx:64:8:$o
      wire [6:0] _293; // simple_timer.nx:64:26:$o
      wire [6:0] _294; // simple_timer.nx:64:8:$o
      wire [6:0] _295; // simple_timer.nx:64:26:$o
      wire [6:0] _296; // simple_timer.nx:64:8:$o
      wire [6:0] _297; // simple_timer.nx:64:26:$o
      wire [6:0] _298; // simple_timer.nx:64:8:$o
      wire [6:0] _299; // simple_timer.nx:64:26:$o
      wire [6:0] _300; // simple_timer.nx:64:8:$o
      wire [6:0] _301; // simple_timer.nx:64:26:$o
      wire [6:0] _302; // simple_timer.nx:64:8:$o
      wire [6:0] _303; // simple_timer.nx:64:26:$o
      wire [6:0] _304; // simple_timer.nx:64:8:$o
      wire [6:0] _305; // simple_timer.nx:64:26:$o
      wire [6:0] _306; // simple_timer.nx:64:8:$o
      wire [6:0] _307; // simple_timer.nx:64:26:$o
      wire [6:0] _308; // simple_timer.nx:64:8:$o
      wire [6:0] _309; // simple_timer.nx:64:26:$o
      wire [6:0] _310; // simple_timer.nx:64:8:$o
      wire [6:0] _311; // simple_timer.nx:64:26:$o
      wire [6:0] _312; // simple_timer.nx:64:8:$o
      wire [6:0] _313; // simple_timer.nx:64:26:$o
      wire [6:0] _314; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:19:11" */
  wire [31:0] _315; // simple_timer.nx:20:42:$o
  wire [31:0] _316; // simple_timer.nx:20:34:$o
  /* "hex_trigger"; "simple_timer.nx:20:11" */
    wire [3:0] _317; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _318; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _319; // simple_timer.nx:75:52:$o
        wire _320; // simple_timer.nx:75:52:$o
        wire _321; // simple_timer.nx:75:52:$o
        wire _322; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _323; // simple_timer.nx:64:26:$o
      wire [6:0] _324; // simple_timer.nx:64:8:$o
      wire [6:0] _325; // simple_timer.nx:64:26:$o
      wire [6:0] _326; // simple_timer.nx:64:8:$o
      wire [6:0] _327; // simple_timer.nx:64:26:$o
      wire [6:0] _328; // simple_timer.nx:64:8:$o
      wire [6:0] _329; // simple_timer.nx:64:26:$o
      wire [6:0] _330; // simple_timer.nx:64:8:$o
      wire [6:0] _331; // simple_timer.nx:64:26:$o
      wire [6:0] _332; // simple_timer.nx:64:8:$o
      wire [6:0] _333; // simple_timer.nx:64:26:$o
      wire [6:0] _334; // simple_timer.nx:64:8:$o
      wire [6:0] _335; // simple_timer.nx:64:26:$o
      wire [6:0] _336; // simple_timer.nx:64:8:$o
      wire [6:0] _337; // simple_timer.nx:64:26:$o
      wire [6:0] _338; // simple_timer.nx:64:8:$o
      wire [6:0] _339; // simple_timer.nx:64:26:$o
      wire [6:0] _340; // simple_timer.nx:64:8:$o
      wire [6:0] _341; // simple_timer.nx:64:26:$o
      wire [6:0] _342; // simple_timer.nx:64:8:$o
      wire [6:0] _343; // simple_timer.nx:64:26:$o
      wire [6:0] _344; // simple_timer.nx:64:8:$o
      wire [6:0] _345; // simple_timer.nx:64:26:$o
      wire [6:0] _346; // simple_timer.nx:64:8:$o
      wire [6:0] _347; // simple_timer.nx:64:26:$o
      wire [6:0] _348; // simple_timer.nx:64:8:$o
      wire [6:0] _349; // simple_timer.nx:64:26:$o
      wire [6:0] _350; // simple_timer.nx:64:8:$o
      wire [6:0] _351; // simple_timer.nx:64:26:$o
      wire [6:0] _352; // simple_timer.nx:64:8:$o
      wire [6:0] _353; // simple_timer.nx:64:26:$o
      wire [6:0] _354; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:20:11" */
/* END "top"; "simple_timer.nx:1:0" */

/*============================== CONNECTION ==============================*/

/*
  COMPONENT "top"; "simple_timer.nx:1:0"
  input: clk_50@0
  output: hex[0]@1, hex[1]@2, hex[2]@3, hex[3]@4, hex[4]@5, hex[5]@6, hex[6]@7, hex[7]@8
*/
  /*
    COMPONENT "clock_divider"; "simple_timer.nx:10:11"
    input: clk_50@0
    output: ms_clk@9
    attribute: { max: "500000" }
  */
    assign _15 = _14 == 249999; // simple_timer.nx:28:11
    assign _16 = _14 + 1; // simple_timer.nx:29:33
    /*
      COMPONENT "MUX"; "simple_timer.nx:29:17"
      input: code@15, input[0]@16, 0
      output: out@17
      attribute: { width: "1" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: code@15
        output: mask@18
        attribute: { width: "1" }
      */
        assign _18[0] = ~_15; // simple_timer.nx:75:52
        assign _18[1] = _15; // simple_timer.nx:62:12
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _19 = $signed(_18[0]); // simple_timer.nx:64:26
      assign _20 = _16 & _19; // simple_timer.nx:64:8
      assign _21 = $signed(_18[1]); // simple_timer.nx:64:26
      assign _22 = 0 & _21; // simple_timer.nx:64:8
      assign _17 = _20 | _22; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:29:17" */
    always @(posedge _0) _14 <= _17; // simple_timer.nx:27:11
    assign _23 = ~_9; // simple_timer.nx:30:38
    /*
      COMPONENT "MUX"; "simple_timer.nx:30:17"
      input: code@15, ms_clk@9, input[1]@23
      output: out@24
      attribute: { width: "1" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: code@15
        output: mask@25
        attribute: { width: "1" }
      */
        assign _25[0] = ~_15; // simple_timer.nx:75:52
        assign _25[1] = _15; // simple_timer.nx:62:12
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _26 = _9 & _25[0]; // simple_timer.nx:64:8
      assign _27 = _23 & _25[1]; // simple_timer.nx:64:8
      assign _24 = _26 | _27; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:30:17" */
    always @(posedge _0) _9 <= _24; // simple_timer.nx:27:11
  /* END "clock_divider"; "simple_timer.nx:10:11" */
  assign _28 = _13 == 8639999; // simple_timer.nx:11:33
  assign _29 = _13 + 1; // simple_timer.nx:11:66
  /*
    COMPONENT "MUX"; "simple_timer.nx:11:22"
    input: code@28, input[0]@29, 0
    output: out@30
    attribute: { width: "1" }
  */
    /*
      COMPONENT "DECODER"; "simple_timer.nx:62:12"
      input: code@28
      output: mask@31
      attribute: { width: "1" }
    */
      assign _31[0] = ~_28; // simple_timer.nx:75:52
      assign _31[1] = _28; // simple_timer.nx:62:12
    /* END "DECODER"; "simple_timer.nx:62:12" */
    assign _32 = $signed(_31[0]); // simple_timer.nx:64:26
    assign _33 = _29 & _32; // simple_timer.nx:64:8
    assign _34 = $signed(_31[1]); // simple_timer.nx:64:26
    assign _35 = 0 & _34; // simple_timer.nx:64:8
    assign _30 = _33 | _35; // simple_timer.nx:63:11
  /* END "MUX"; "simple_timer.nx:11:22" */
  always @(posedge _9) _13 <= _30; // simple_timer.nx:5:16
  assign _36 = _13 % 10; // simple_timer.nx:13:34
  assign _37 = $signed(_36); // simple_timer.nx:13:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:13:11"
    input: num@37
    output: hex[0]@1
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@37, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[0]@1
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@37
        output: mask@38
        attribute: { width: "4" }
      */
        assign _39 = ~_37[0]; // simple_timer.nx:75:52
        assign _40 = ~_37[1]; // simple_timer.nx:75:52
        assign _41 = ~_37[2]; // simple_timer.nx:75:52
        assign _42 = ~_37[3]; // simple_timer.nx:75:52
        assign _38[0] = _39 & _40 & _41 & _42; // simple_timer.nx:73:18
        assign _38[1] = _37[0] & _40 & _41 & _42; // simple_timer.nx:73:18
        assign _38[2] = _39 & _37[1] & _41 & _42; // simple_timer.nx:73:18
        assign _38[3] = _37[0] & _37[1] & _41 & _42; // simple_timer.nx:73:18
        assign _38[4] = _39 & _40 & _37[2] & _42; // simple_timer.nx:73:18
        assign _38[5] = _37[0] & _40 & _37[2] & _42; // simple_timer.nx:73:18
        assign _38[6] = _39 & _37[1] & _37[2] & _42; // simple_timer.nx:73:18
        assign _38[7] = _37[0] & _37[1] & _37[2] & _42; // simple_timer.nx:73:18
        assign _38[8] = _39 & _40 & _41 & _37[3]; // simple_timer.nx:73:18
        assign _38[9] = _37[0] & _40 & _41 & _37[3]; // simple_timer.nx:73:18
        assign _38[10] = _39 & _37[1] & _41 & _37[3]; // simple_timer.nx:73:18
        assign _38[11] = _37[0] & _37[1] & _41 & _37[3]; // simple_timer.nx:73:18
        assign _38[12] = _39 & _40 & _37[2] & _37[3]; // simple_timer.nx:73:18
        assign _38[13] = _37[0] & _40 & _37[2] & _37[3]; // simple_timer.nx:73:18
        assign _38[14] = _39 & _37[1] & _37[2] & _37[3]; // simple_timer.nx:73:18
        assign _38[15] = _37[0] & _37[1] & _37[2] & _37[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _43 = $signed(_38[0]); // simple_timer.nx:64:26
      assign _44 = 7'h40 & _43; // simple_timer.nx:64:8
      assign _45 = $signed(_38[1]); // simple_timer.nx:64:26
      assign _46 = 7'h79 & _45; // simple_timer.nx:64:8
      assign _47 = $signed(_38[2]); // simple_timer.nx:64:26
      assign _48 = 7'h24 & _47; // simple_timer.nx:64:8
      assign _49 = $signed(_38[3]); // simple_timer.nx:64:26
      assign _50 = 7'h30 & _49; // simple_timer.nx:64:8
      assign _51 = $signed(_38[4]); // simple_timer.nx:64:26
      assign _52 = 7'h19 & _51; // simple_timer.nx:64:8
      assign _53 = $signed(_38[5]); // simple_timer.nx:64:26
      assign _54 = 7'h12 & _53; // simple_timer.nx:64:8
      assign _55 = $signed(_38[6]); // simple_timer.nx:64:26
      assign _56 = 7'h2 & _55; // simple_timer.nx:64:8
      assign _57 = $signed(_38[7]); // simple_timer.nx:64:26
      assign _58 = 7'h78 & _57; // simple_timer.nx:64:8
      assign _59 = $signed(_38[8]); // simple_timer.nx:64:26
      assign _60 = 7'h0 & _59; // simple_timer.nx:64:8
      assign _61 = $signed(_38[9]); // simple_timer.nx:64:26
      assign _62 = 7'h10 & _61; // simple_timer.nx:64:8
      assign _63 = $signed(_38[10]); // simple_timer.nx:64:26
      assign _64 = 7'h8 & _63; // simple_timer.nx:64:8
      assign _65 = $signed(_38[11]); // simple_timer.nx:64:26
      assign _66 = 7'h3 & _65; // simple_timer.nx:64:8
      assign _67 = $signed(_38[12]); // simple_timer.nx:64:26
      assign _68 = 7'h46 & _67; // simple_timer.nx:64:8
      assign _69 = $signed(_38[13]); // simple_timer.nx:64:26
      assign _70 = 7'h21 & _69; // simple_timer.nx:64:8
      assign _71 = $signed(_38[14]); // simple_timer.nx:64:26
      assign _72 = 7'h6 & _71; // simple_timer.nx:64:8
      assign _73 = $signed(_38[15]); // simple_timer.nx:64:26
      assign _74 = 7'hE & _73; // simple_timer.nx:64:8
      assign _1 = _44 | _46 | _48 | _50 | _52 | _54 | _56 | _58 | _60 | _62 | _64 | _66 | _68 | _70 | _72 | _74; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:13:11" */
  assign _75 = _13 % 100; // simple_timer.nx:14:42
  assign _76 = _75 / 10; // simple_timer.nx:14:34
  assign _77 = $signed(_76); // simple_timer.nx:14:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:14:11"
    input: num@77
    output: hex[1]@2
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@77, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[1]@2
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@77
        output: mask@78
        attribute: { width: "4" }
      */
        assign _79 = ~_77[0]; // simple_timer.nx:75:52
        assign _80 = ~_77[1]; // simple_timer.nx:75:52
        assign _81 = ~_77[2]; // simple_timer.nx:75:52
        assign _82 = ~_77[3]; // simple_timer.nx:75:52
        assign _78[0] = _79 & _80 & _81 & _82; // simple_timer.nx:73:18
        assign _78[1] = _77[0] & _80 & _81 & _82; // simple_timer.nx:73:18
        assign _78[2] = _79 & _77[1] & _81 & _82; // simple_timer.nx:73:18
        assign _78[3] = _77[0] & _77[1] & _81 & _82; // simple_timer.nx:73:18
        assign _78[4] = _79 & _80 & _77[2] & _82; // simple_timer.nx:73:18
        assign _78[5] = _77[0] & _80 & _77[2] & _82; // simple_timer.nx:73:18
        assign _78[6] = _79 & _77[1] & _77[2] & _82; // simple_timer.nx:73:18
        assign _78[7] = _77[0] & _77[1] & _77[2] & _82; // simple_timer.nx:73:18
        assign _78[8] = _79 & _80 & _81 & _77[3]; // simple_timer.nx:73:18
        assign _78[9] = _77[0] & _80 & _81 & _77[3]; // simple_timer.nx:73:18
        assign _78[10] = _79 & _77[1] & _81 & _77[3]; // simple_timer.nx:73:18
        assign _78[11] = _77[0] & _77[1] & _81 & _77[3]; // simple_timer.nx:73:18
        assign _78[12] = _79 & _80 & _77[2] & _77[3]; // simple_timer.nx:73:18
        assign _78[13] = _77[0] & _80 & _77[2] & _77[3]; // simple_timer.nx:73:18
        assign _78[14] = _79 & _77[1] & _77[2] & _77[3]; // simple_timer.nx:73:18
        assign _78[15] = _77[0] & _77[1] & _77[2] & _77[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _83 = $signed(_78[0]); // simple_timer.nx:64:26
      assign _84 = 7'h40 & _83; // simple_timer.nx:64:8
      assign _85 = $signed(_78[1]); // simple_timer.nx:64:26
      assign _86 = 7'h79 & _85; // simple_timer.nx:64:8
      assign _87 = $signed(_78[2]); // simple_timer.nx:64:26
      assign _88 = 7'h24 & _87; // simple_timer.nx:64:8
      assign _89 = $signed(_78[3]); // simple_timer.nx:64:26
      assign _90 = 7'h30 & _89; // simple_timer.nx:64:8
      assign _91 = $signed(_78[4]); // simple_timer.nx:64:26
      assign _92 = 7'h19 & _91; // simple_timer.nx:64:8
      assign _93 = $signed(_78[5]); // simple_timer.nx:64:26
      assign _94 = 7'h12 & _93; // simple_timer.nx:64:8
      assign _95 = $signed(_78[6]); // simple_timer.nx:64:26
      assign _96 = 7'h2 & _95; // simple_timer.nx:64:8
      assign _97 = $signed(_78[7]); // simple_timer.nx:64:26
      assign _98 = 7'h78 & _97; // simple_timer.nx:64:8
      assign _99 = $signed(_78[8]); // simple_timer.nx:64:26
      assign _100 = 7'h0 & _99; // simple_timer.nx:64:8
      assign _101 = $signed(_78[9]); // simple_timer.nx:64:26
      assign _102 = 7'h10 & _101; // simple_timer.nx:64:8
      assign _103 = $signed(_78[10]); // simple_timer.nx:64:26
      assign _104 = 7'h8 & _103; // simple_timer.nx:64:8
      assign _105 = $signed(_78[11]); // simple_timer.nx:64:26
      assign _106 = 7'h3 & _105; // simple_timer.nx:64:8
      assign _107 = $signed(_78[12]); // simple_timer.nx:64:26
      assign _108 = 7'h46 & _107; // simple_timer.nx:64:8
      assign _109 = $signed(_78[13]); // simple_timer.nx:64:26
      assign _110 = 7'h21 & _109; // simple_timer.nx:64:8
      assign _111 = $signed(_78[14]); // simple_timer.nx:64:26
      assign _112 = 7'h6 & _111; // simple_timer.nx:64:8
      assign _113 = $signed(_78[15]); // simple_timer.nx:64:26
      assign _114 = 7'hE & _113; // simple_timer.nx:64:8
      assign _2 = _84 | _86 | _88 | _90 | _92 | _94 | _96 | _98 | _100 | _102 | _104 | _106 | _108 | _110 | _112 | _114; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:14:11" */
  assign _115 = _13 % 1000; // simple_timer.nx:15:42
  assign _116 = _115 / 100; // simple_timer.nx:15:34
  assign _117 = $signed(_116); // simple_timer.nx:15:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:15:11"
    input: num@117
    output: hex[2]@3
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@117, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[2]@3
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@117
        output: mask@118
        attribute: { width: "4" }
      */
        assign _119 = ~_117[0]; // simple_timer.nx:75:52
        assign _120 = ~_117[1]; // simple_timer.nx:75:52
        assign _121 = ~_117[2]; // simple_timer.nx:75:52
        assign _122 = ~_117[3]; // simple_timer.nx:75:52
        assign _118[0] = _119 & _120 & _121 & _122; // simple_timer.nx:73:18
        assign _118[1] = _117[0] & _120 & _121 & _122; // simple_timer.nx:73:18
        assign _118[2] = _119 & _117[1] & _121 & _122; // simple_timer.nx:73:18
        assign _118[3] = _117[0] & _117[1] & _121 & _122; // simple_timer.nx:73:18
        assign _118[4] = _119 & _120 & _117[2] & _122; // simple_timer.nx:73:18
        assign _118[5] = _117[0] & _120 & _117[2] & _122; // simple_timer.nx:73:18
        assign _118[6] = _119 & _117[1] & _117[2] & _122; // simple_timer.nx:73:18
        assign _118[7] = _117[0] & _117[1] & _117[2] & _122; // simple_timer.nx:73:18
        assign _118[8] = _119 & _120 & _121 & _117[3]; // simple_timer.nx:73:18
        assign _118[9] = _117[0] & _120 & _121 & _117[3]; // simple_timer.nx:73:18
        assign _118[10] = _119 & _117[1] & _121 & _117[3]; // simple_timer.nx:73:18
        assign _118[11] = _117[0] & _117[1] & _121 & _117[3]; // simple_timer.nx:73:18
        assign _118[12] = _119 & _120 & _117[2] & _117[3]; // simple_timer.nx:73:18
        assign _118[13] = _117[0] & _120 & _117[2] & _117[3]; // simple_timer.nx:73:18
        assign _118[14] = _119 & _117[1] & _117[2] & _117[3]; // simple_timer.nx:73:18
        assign _118[15] = _117[0] & _117[1] & _117[2] & _117[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _123 = $signed(_118[0]); // simple_timer.nx:64:26
      assign _124 = 7'h40 & _123; // simple_timer.nx:64:8
      assign _125 = $signed(_118[1]); // simple_timer.nx:64:26
      assign _126 = 7'h79 & _125; // simple_timer.nx:64:8
      assign _127 = $signed(_118[2]); // simple_timer.nx:64:26
      assign _128 = 7'h24 & _127; // simple_timer.nx:64:8
      assign _129 = $signed(_118[3]); // simple_timer.nx:64:26
      assign _130 = 7'h30 & _129; // simple_timer.nx:64:8
      assign _131 = $signed(_118[4]); // simple_timer.nx:64:26
      assign _132 = 7'h19 & _131; // simple_timer.nx:64:8
      assign _133 = $signed(_118[5]); // simple_timer.nx:64:26
      assign _134 = 7'h12 & _133; // simple_timer.nx:64:8
      assign _135 = $signed(_118[6]); // simple_timer.nx:64:26
      assign _136 = 7'h2 & _135; // simple_timer.nx:64:8
      assign _137 = $signed(_118[7]); // simple_timer.nx:64:26
      assign _138 = 7'h78 & _137; // simple_timer.nx:64:8
      assign _139 = $signed(_118[8]); // simple_timer.nx:64:26
      assign _140 = 7'h0 & _139; // simple_timer.nx:64:8
      assign _141 = $signed(_118[9]); // simple_timer.nx:64:26
      assign _142 = 7'h10 & _141; // simple_timer.nx:64:8
      assign _143 = $signed(_118[10]); // simple_timer.nx:64:26
      assign _144 = 7'h8 & _143; // simple_timer.nx:64:8
      assign _145 = $signed(_118[11]); // simple_timer.nx:64:26
      assign _146 = 7'h3 & _145; // simple_timer.nx:64:8
      assign _147 = $signed(_118[12]); // simple_timer.nx:64:26
      assign _148 = 7'h46 & _147; // simple_timer.nx:64:8
      assign _149 = $signed(_118[13]); // simple_timer.nx:64:26
      assign _150 = 7'h21 & _149; // simple_timer.nx:64:8
      assign _151 = $signed(_118[14]); // simple_timer.nx:64:26
      assign _152 = 7'h6 & _151; // simple_timer.nx:64:8
      assign _153 = $signed(_118[15]); // simple_timer.nx:64:26
      assign _154 = 7'hE & _153; // simple_timer.nx:64:8
      assign _3 = _124 | _126 | _128 | _130 | _132 | _134 | _136 | _138 | _140 | _142 | _144 | _146 | _148 | _150 | _152 | _154; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:15:11" */
  assign _155 = _13 % 6000; // simple_timer.nx:16:42
  assign _156 = _155 / 1000; // simple_timer.nx:16:34
  assign _157 = $signed(_156); // simple_timer.nx:16:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:16:11"
    input: num@157
    output: hex[3]@4
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@157, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[3]@4
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@157
        output: mask@158
        attribute: { width: "4" }
      */
        assign _159 = ~_157[0]; // simple_timer.nx:75:52
        assign _160 = ~_157[1]; // simple_timer.nx:75:52
        assign _161 = ~_157[2]; // simple_timer.nx:75:52
        assign _162 = ~_157[3]; // simple_timer.nx:75:52
        assign _158[0] = _159 & _160 & _161 & _162; // simple_timer.nx:73:18
        assign _158[1] = _157[0] & _160 & _161 & _162; // simple_timer.nx:73:18
        assign _158[2] = _159 & _157[1] & _161 & _162; // simple_timer.nx:73:18
        assign _158[3] = _157[0] & _157[1] & _161 & _162; // simple_timer.nx:73:18
        assign _158[4] = _159 & _160 & _157[2] & _162; // simple_timer.nx:73:18
        assign _158[5] = _157[0] & _160 & _157[2] & _162; // simple_timer.nx:73:18
        assign _158[6] = _159 & _157[1] & _157[2] & _162; // simple_timer.nx:73:18
        assign _158[7] = _157[0] & _157[1] & _157[2] & _162; // simple_timer.nx:73:18
        assign _158[8] = _159 & _160 & _161 & _157[3]; // simple_timer.nx:73:18
        assign _158[9] = _157[0] & _160 & _161 & _157[3]; // simple_timer.nx:73:18
        assign _158[10] = _159 & _157[1] & _161 & _157[3]; // simple_timer.nx:73:18
        assign _158[11] = _157[0] & _157[1] & _161 & _157[3]; // simple_timer.nx:73:18
        assign _158[12] = _159 & _160 & _157[2] & _157[3]; // simple_timer.nx:73:18
        assign _158[13] = _157[0] & _160 & _157[2] & _157[3]; // simple_timer.nx:73:18
        assign _158[14] = _159 & _157[1] & _157[2] & _157[3]; // simple_timer.nx:73:18
        assign _158[15] = _157[0] & _157[1] & _157[2] & _157[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _163 = $signed(_158[0]); // simple_timer.nx:64:26
      assign _164 = 7'h40 & _163; // simple_timer.nx:64:8
      assign _165 = $signed(_158[1]); // simple_timer.nx:64:26
      assign _166 = 7'h79 & _165; // simple_timer.nx:64:8
      assign _167 = $signed(_158[2]); // simple_timer.nx:64:26
      assign _168 = 7'h24 & _167; // simple_timer.nx:64:8
      assign _169 = $signed(_158[3]); // simple_timer.nx:64:26
      assign _170 = 7'h30 & _169; // simple_timer.nx:64:8
      assign _171 = $signed(_158[4]); // simple_timer.nx:64:26
      assign _172 = 7'h19 & _171; // simple_timer.nx:64:8
      assign _173 = $signed(_158[5]); // simple_timer.nx:64:26
      assign _174 = 7'h12 & _173; // simple_timer.nx:64:8
      assign _175 = $signed(_158[6]); // simple_timer.nx:64:26
      assign _176 = 7'h2 & _175; // simple_timer.nx:64:8
      assign _177 = $signed(_158[7]); // simple_timer.nx:64:26
      assign _178 = 7'h78 & _177; // simple_timer.nx:64:8
      assign _179 = $signed(_158[8]); // simple_timer.nx:64:26
      assign _180 = 7'h0 & _179; // simple_timer.nx:64:8
      assign _181 = $signed(_158[9]); // simple_timer.nx:64:26
      assign _182 = 7'h10 & _181; // simple_timer.nx:64:8
      assign _183 = $signed(_158[10]); // simple_timer.nx:64:26
      assign _184 = 7'h8 & _183; // simple_timer.nx:64:8
      assign _185 = $signed(_158[11]); // simple_timer.nx:64:26
      assign _186 = 7'h3 & _185; // simple_timer.nx:64:8
      assign _187 = $signed(_158[12]); // simple_timer.nx:64:26
      assign _188 = 7'h46 & _187; // simple_timer.nx:64:8
      assign _189 = $signed(_158[13]); // simple_timer.nx:64:26
      assign _190 = 7'h21 & _189; // simple_timer.nx:64:8
      assign _191 = $signed(_158[14]); // simple_timer.nx:64:26
      assign _192 = 7'h6 & _191; // simple_timer.nx:64:8
      assign _193 = $signed(_158[15]); // simple_timer.nx:64:26
      assign _194 = 7'hE & _193; // simple_timer.nx:64:8
      assign _4 = _164 | _166 | _168 | _170 | _172 | _174 | _176 | _178 | _180 | _182 | _184 | _186 | _188 | _190 | _192 | _194; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:16:11" */
  assign _195 = _13 % 60000; // simple_timer.nx:17:42
  assign _196 = _195 / 6000; // simple_timer.nx:17:34
  assign _197 = $signed(_196); // simple_timer.nx:17:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:17:11"
    input: num@197
    output: hex[4]@5
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@197, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[4]@5
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@197
        output: mask@198
        attribute: { width: "4" }
      */
        assign _199 = ~_197[0]; // simple_timer.nx:75:52
        assign _200 = ~_197[1]; // simple_timer.nx:75:52
        assign _201 = ~_197[2]; // simple_timer.nx:75:52
        assign _202 = ~_197[3]; // simple_timer.nx:75:52
        assign _198[0] = _199 & _200 & _201 & _202; // simple_timer.nx:73:18
        assign _198[1] = _197[0] & _200 & _201 & _202; // simple_timer.nx:73:18
        assign _198[2] = _199 & _197[1] & _201 & _202; // simple_timer.nx:73:18
        assign _198[3] = _197[0] & _197[1] & _201 & _202; // simple_timer.nx:73:18
        assign _198[4] = _199 & _200 & _197[2] & _202; // simple_timer.nx:73:18
        assign _198[5] = _197[0] & _200 & _197[2] & _202; // simple_timer.nx:73:18
        assign _198[6] = _199 & _197[1] & _197[2] & _202; // simple_timer.nx:73:18
        assign _198[7] = _197[0] & _197[1] & _197[2] & _202; // simple_timer.nx:73:18
        assign _198[8] = _199 & _200 & _201 & _197[3]; // simple_timer.nx:73:18
        assign _198[9] = _197[0] & _200 & _201 & _197[3]; // simple_timer.nx:73:18
        assign _198[10] = _199 & _197[1] & _201 & _197[3]; // simple_timer.nx:73:18
        assign _198[11] = _197[0] & _197[1] & _201 & _197[3]; // simple_timer.nx:73:18
        assign _198[12] = _199 & _200 & _197[2] & _197[3]; // simple_timer.nx:73:18
        assign _198[13] = _197[0] & _200 & _197[2] & _197[3]; // simple_timer.nx:73:18
        assign _198[14] = _199 & _197[1] & _197[2] & _197[3]; // simple_timer.nx:73:18
        assign _198[15] = _197[0] & _197[1] & _197[2] & _197[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _203 = $signed(_198[0]); // simple_timer.nx:64:26
      assign _204 = 7'h40 & _203; // simple_timer.nx:64:8
      assign _205 = $signed(_198[1]); // simple_timer.nx:64:26
      assign _206 = 7'h79 & _205; // simple_timer.nx:64:8
      assign _207 = $signed(_198[2]); // simple_timer.nx:64:26
      assign _208 = 7'h24 & _207; // simple_timer.nx:64:8
      assign _209 = $signed(_198[3]); // simple_timer.nx:64:26
      assign _210 = 7'h30 & _209; // simple_timer.nx:64:8
      assign _211 = $signed(_198[4]); // simple_timer.nx:64:26
      assign _212 = 7'h19 & _211; // simple_timer.nx:64:8
      assign _213 = $signed(_198[5]); // simple_timer.nx:64:26
      assign _214 = 7'h12 & _213; // simple_timer.nx:64:8
      assign _215 = $signed(_198[6]); // simple_timer.nx:64:26
      assign _216 = 7'h2 & _215; // simple_timer.nx:64:8
      assign _217 = $signed(_198[7]); // simple_timer.nx:64:26
      assign _218 = 7'h78 & _217; // simple_timer.nx:64:8
      assign _219 = $signed(_198[8]); // simple_timer.nx:64:26
      assign _220 = 7'h0 & _219; // simple_timer.nx:64:8
      assign _221 = $signed(_198[9]); // simple_timer.nx:64:26
      assign _222 = 7'h10 & _221; // simple_timer.nx:64:8
      assign _223 = $signed(_198[10]); // simple_timer.nx:64:26
      assign _224 = 7'h8 & _223; // simple_timer.nx:64:8
      assign _225 = $signed(_198[11]); // simple_timer.nx:64:26
      assign _226 = 7'h3 & _225; // simple_timer.nx:64:8
      assign _227 = $signed(_198[12]); // simple_timer.nx:64:26
      assign _228 = 7'h46 & _227; // simple_timer.nx:64:8
      assign _229 = $signed(_198[13]); // simple_timer.nx:64:26
      assign _230 = 7'h21 & _229; // simple_timer.nx:64:8
      assign _231 = $signed(_198[14]); // simple_timer.nx:64:26
      assign _232 = 7'h6 & _231; // simple_timer.nx:64:8
      assign _233 = $signed(_198[15]); // simple_timer.nx:64:26
      assign _234 = 7'hE & _233; // simple_timer.nx:64:8
      assign _5 = _204 | _206 | _208 | _210 | _212 | _214 | _216 | _218 | _220 | _222 | _224 | _226 | _228 | _230 | _232 | _234; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:17:11" */
  assign _235 = _13 % 360000; // simple_timer.nx:18:42
  assign _236 = _235 / 60000; // simple_timer.nx:18:34
  assign _237 = $signed(_236); // simple_timer.nx:18:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:18:11"
    input: num@237
    output: hex[5]@6
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@237, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[5]@6
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@237
        output: mask@238
        attribute: { width: "4" }
      */
        assign _239 = ~_237[0]; // simple_timer.nx:75:52
        assign _240 = ~_237[1]; // simple_timer.nx:75:52
        assign _241 = ~_237[2]; // simple_timer.nx:75:52
        assign _242 = ~_237[3]; // simple_timer.nx:75:52
        assign _238[0] = _239 & _240 & _241 & _242; // simple_timer.nx:73:18
        assign _238[1] = _237[0] & _240 & _241 & _242; // simple_timer.nx:73:18
        assign _238[2] = _239 & _237[1] & _241 & _242; // simple_timer.nx:73:18
        assign _238[3] = _237[0] & _237[1] & _241 & _242; // simple_timer.nx:73:18
        assign _238[4] = _239 & _240 & _237[2] & _242; // simple_timer.nx:73:18
        assign _238[5] = _237[0] & _240 & _237[2] & _242; // simple_timer.nx:73:18
        assign _238[6] = _239 & _237[1] & _237[2] & _242; // simple_timer.nx:73:18
        assign _238[7] = _237[0] & _237[1] & _237[2] & _242; // simple_timer.nx:73:18
        assign _238[8] = _239 & _240 & _241 & _237[3]; // simple_timer.nx:73:18
        assign _238[9] = _237[0] & _240 & _241 & _237[3]; // simple_timer.nx:73:18
        assign _238[10] = _239 & _237[1] & _241 & _237[3]; // simple_timer.nx:73:18
        assign _238[11] = _237[0] & _237[1] & _241 & _237[3]; // simple_timer.nx:73:18
        assign _238[12] = _239 & _240 & _237[2] & _237[3]; // simple_timer.nx:73:18
        assign _238[13] = _237[0] & _240 & _237[2] & _237[3]; // simple_timer.nx:73:18
        assign _238[14] = _239 & _237[1] & _237[2] & _237[3]; // simple_timer.nx:73:18
        assign _238[15] = _237[0] & _237[1] & _237[2] & _237[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _243 = $signed(_238[0]); // simple_timer.nx:64:26
      assign _244 = 7'h40 & _243; // simple_timer.nx:64:8
      assign _245 = $signed(_238[1]); // simple_timer.nx:64:26
      assign _246 = 7'h79 & _245; // simple_timer.nx:64:8
      assign _247 = $signed(_238[2]); // simple_timer.nx:64:26
      assign _248 = 7'h24 & _247; // simple_timer.nx:64:8
      assign _249 = $signed(_238[3]); // simple_timer.nx:64:26
      assign _250 = 7'h30 & _249; // simple_timer.nx:64:8
      assign _251 = $signed(_238[4]); // simple_timer.nx:64:26
      assign _252 = 7'h19 & _251; // simple_timer.nx:64:8
      assign _253 = $signed(_238[5]); // simple_timer.nx:64:26
      assign _254 = 7'h12 & _253; // simple_timer.nx:64:8
      assign _255 = $signed(_238[6]); // simple_timer.nx:64:26
      assign _256 = 7'h2 & _255; // simple_timer.nx:64:8
      assign _257 = $signed(_238[7]); // simple_timer.nx:64:26
      assign _258 = 7'h78 & _257; // simple_timer.nx:64:8
      assign _259 = $signed(_238[8]); // simple_timer.nx:64:26
      assign _260 = 7'h0 & _259; // simple_timer.nx:64:8
      assign _261 = $signed(_238[9]); // simple_timer.nx:64:26
      assign _262 = 7'h10 & _261; // simple_timer.nx:64:8
      assign _263 = $signed(_238[10]); // simple_timer.nx:64:26
      assign _264 = 7'h8 & _263; // simple_timer.nx:64:8
      assign _265 = $signed(_238[11]); // simple_timer.nx:64:26
      assign _266 = 7'h3 & _265; // simple_timer.nx:64:8
      assign _267 = $signed(_238[12]); // simple_timer.nx:64:26
      assign _268 = 7'h46 & _267; // simple_timer.nx:64:8
      assign _269 = $signed(_238[13]); // simple_timer.nx:64:26
      assign _270 = 7'h21 & _269; // simple_timer.nx:64:8
      assign _271 = $signed(_238[14]); // simple_timer.nx:64:26
      assign _272 = 7'h6 & _271; // simple_timer.nx:64:8
      assign _273 = $signed(_238[15]); // simple_timer.nx:64:26
      assign _274 = 7'hE & _273; // simple_timer.nx:64:8
      assign _6 = _244 | _246 | _248 | _250 | _252 | _254 | _256 | _258 | _260 | _262 | _264 | _266 | _268 | _270 | _272 | _274; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:18:11" */
  assign _275 = _13 % 3600000; // simple_timer.nx:19:42
  assign _276 = _275 / 360000; // simple_timer.nx:19:34
  assign _277 = $signed(_276); // simple_timer.nx:19:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:19:11"
    input: num@277
    output: hex[6]@7
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@277, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[6]@7
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@277
        output: mask@278
        attribute: { width: "4" }
      */
        assign _279 = ~_277[0]; // simple_timer.nx:75:52
        assign _280 = ~_277[1]; // simple_timer.nx:75:52
        assign _281 = ~_277[2]; // simple_timer.nx:75:52
        assign _282 = ~_277[3]; // simple_timer.nx:75:52
        assign _278[0] = _279 & _280 & _281 & _282; // simple_timer.nx:73:18
        assign _278[1] = _277[0] & _280 & _281 & _282; // simple_timer.nx:73:18
        assign _278[2] = _279 & _277[1] & _281 & _282; // simple_timer.nx:73:18
        assign _278[3] = _277[0] & _277[1] & _281 & _282; // simple_timer.nx:73:18
        assign _278[4] = _279 & _280 & _277[2] & _282; // simple_timer.nx:73:18
        assign _278[5] = _277[0] & _280 & _277[2] & _282; // simple_timer.nx:73:18
        assign _278[6] = _279 & _277[1] & _277[2] & _282; // simple_timer.nx:73:18
        assign _278[7] = _277[0] & _277[1] & _277[2] & _282; // simple_timer.nx:73:18
        assign _278[8] = _279 & _280 & _281 & _277[3]; // simple_timer.nx:73:18
        assign _278[9] = _277[0] & _280 & _281 & _277[3]; // simple_timer.nx:73:18
        assign _278[10] = _279 & _277[1] & _281 & _277[3]; // simple_timer.nx:73:18
        assign _278[11] = _277[0] & _277[1] & _281 & _277[3]; // simple_timer.nx:73:18
        assign _278[12] = _279 & _280 & _277[2] & _277[3]; // simple_timer.nx:73:18
        assign _278[13] = _277[0] & _280 & _277[2] & _277[3]; // simple_timer.nx:73:18
        assign _278[14] = _279 & _277[1] & _277[2] & _277[3]; // simple_timer.nx:73:18
        assign _278[15] = _277[0] & _277[1] & _277[2] & _277[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _283 = $signed(_278[0]); // simple_timer.nx:64:26
      assign _284 = 7'h40 & _283; // simple_timer.nx:64:8
      assign _285 = $signed(_278[1]); // simple_timer.nx:64:26
      assign _286 = 7'h79 & _285; // simple_timer.nx:64:8
      assign _287 = $signed(_278[2]); // simple_timer.nx:64:26
      assign _288 = 7'h24 & _287; // simple_timer.nx:64:8
      assign _289 = $signed(_278[3]); // simple_timer.nx:64:26
      assign _290 = 7'h30 & _289; // simple_timer.nx:64:8
      assign _291 = $signed(_278[4]); // simple_timer.nx:64:26
      assign _292 = 7'h19 & _291; // simple_timer.nx:64:8
      assign _293 = $signed(_278[5]); // simple_timer.nx:64:26
      assign _294 = 7'h12 & _293; // simple_timer.nx:64:8
      assign _295 = $signed(_278[6]); // simple_timer.nx:64:26
      assign _296 = 7'h2 & _295; // simple_timer.nx:64:8
      assign _297 = $signed(_278[7]); // simple_timer.nx:64:26
      assign _298 = 7'h78 & _297; // simple_timer.nx:64:8
      assign _299 = $signed(_278[8]); // simple_timer.nx:64:26
      assign _300 = 7'h0 & _299; // simple_timer.nx:64:8
      assign _301 = $signed(_278[9]); // simple_timer.nx:64:26
      assign _302 = 7'h10 & _301; // simple_timer.nx:64:8
      assign _303 = $signed(_278[10]); // simple_timer.nx:64:26
      assign _304 = 7'h8 & _303; // simple_timer.nx:64:8
      assign _305 = $signed(_278[11]); // simple_timer.nx:64:26
      assign _306 = 7'h3 & _305; // simple_timer.nx:64:8
      assign _307 = $signed(_278[12]); // simple_timer.nx:64:26
      assign _308 = 7'h46 & _307; // simple_timer.nx:64:8
      assign _309 = $signed(_278[13]); // simple_timer.nx:64:26
      assign _310 = 7'h21 & _309; // simple_timer.nx:64:8
      assign _311 = $signed(_278[14]); // simple_timer.nx:64:26
      assign _312 = 7'h6 & _311; // simple_timer.nx:64:8
      assign _313 = $signed(_278[15]); // simple_timer.nx:64:26
      assign _314 = 7'hE & _313; // simple_timer.nx:64:8
      assign _7 = _284 | _286 | _288 | _290 | _292 | _294 | _296 | _298 | _300 | _302 | _304 | _306 | _308 | _310 | _312 | _314; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:19:11" */
  assign _315 = _13 % 86400000; // simple_timer.nx:20:42
  assign _316 = _315 / 3600000; // simple_timer.nx:20:34
  assign _317 = $signed(_316); // simple_timer.nx:20:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:20:11"
    input: num@317
    output: hex[7]@8
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@317, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[7]@8
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@317
        output: mask@318
        attribute: { width: "4" }
      */
        assign _319 = ~_317[0]; // simple_timer.nx:75:52
        assign _320 = ~_317[1]; // simple_timer.nx:75:52
        assign _321 = ~_317[2]; // simple_timer.nx:75:52
        assign _322 = ~_317[3]; // simple_timer.nx:75:52
        assign _318[0] = _319 & _320 & _321 & _322; // simple_timer.nx:73:18
        assign _318[1] = _317[0] & _320 & _321 & _322; // simple_timer.nx:73:18
        assign _318[2] = _319 & _317[1] & _321 & _322; // simple_timer.nx:73:18
        assign _318[3] = _317[0] & _317[1] & _321 & _322; // simple_timer.nx:73:18
        assign _318[4] = _319 & _320 & _317[2] & _322; // simple_timer.nx:73:18
        assign _318[5] = _317[0] & _320 & _317[2] & _322; // simple_timer.nx:73:18
        assign _318[6] = _319 & _317[1] & _317[2] & _322; // simple_timer.nx:73:18
        assign _318[7] = _317[0] & _317[1] & _317[2] & _322; // simple_timer.nx:73:18
        assign _318[8] = _319 & _320 & _321 & _317[3]; // simple_timer.nx:73:18
        assign _318[9] = _317[0] & _320 & _321 & _317[3]; // simple_timer.nx:73:18
        assign _318[10] = _319 & _317[1] & _321 & _317[3]; // simple_timer.nx:73:18
        assign _318[11] = _317[0] & _317[1] & _321 & _317[3]; // simple_timer.nx:73:18
        assign _318[12] = _319 & _320 & _317[2] & _317[3]; // simple_timer.nx:73:18
        assign _318[13] = _317[0] & _320 & _317[2] & _317[3]; // simple_timer.nx:73:18
        assign _318[14] = _319 & _317[1] & _317[2] & _317[3]; // simple_timer.nx:73:18
        assign _318[15] = _317[0] & _317[1] & _317[2] & _317[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _323 = $signed(_318[0]); // simple_timer.nx:64:26
      assign _324 = 7'h40 & _323; // simple_timer.nx:64:8
      assign _325 = $signed(_318[1]); // simple_timer.nx:64:26
      assign _326 = 7'h79 & _325; // simple_timer.nx:64:8
      assign _327 = $signed(_318[2]); // simple_timer.nx:64:26
      assign _328 = 7'h24 & _327; // simple_timer.nx:64:8
      assign _329 = $signed(_318[3]); // simple_timer.nx:64:26
      assign _330 = 7'h30 & _329; // simple_timer.nx:64:8
      assign _331 = $signed(_318[4]); // simple_timer.nx:64:26
      assign _332 = 7'h19 & _331; // simple_timer.nx:64:8
      assign _333 = $signed(_318[5]); // simple_timer.nx:64:26
      assign _334 = 7'h12 & _333; // simple_timer.nx:64:8
      assign _335 = $signed(_318[6]); // simple_timer.nx:64:26
      assign _336 = 7'h2 & _335; // simple_timer.nx:64:8
      assign _337 = $signed(_318[7]); // simple_timer.nx:64:26
      assign _338 = 7'h78 & _337; // simple_timer.nx:64:8
      assign _339 = $signed(_318[8]); // simple_timer.nx:64:26
      assign _340 = 7'h0 & _339; // simple_timer.nx:64:8
      assign _341 = $signed(_318[9]); // simple_timer.nx:64:26
      assign _342 = 7'h10 & _341; // simple_timer.nx:64:8
      assign _343 = $signed(_318[10]); // simple_timer.nx:64:26
      assign _344 = 7'h8 & _343; // simple_timer.nx:64:8
      assign _345 = $signed(_318[11]); // simple_timer.nx:64:26
      assign _346 = 7'h3 & _345; // simple_timer.nx:64:8
      assign _347 = $signed(_318[12]); // simple_timer.nx:64:26
      assign _348 = 7'h46 & _347; // simple_timer.nx:64:8
      assign _349 = $signed(_318[13]); // simple_timer.nx:64:26
      assign _350 = 7'h21 & _349; // simple_timer.nx:64:8
      assign _351 = $signed(_318[14]); // simple_timer.nx:64:26
      assign _352 = 7'h6 & _351; // simple_timer.nx:64:8
      assign _353 = $signed(_318[15]); // simple_timer.nx:64:26
      assign _354 = 7'hE & _353; // simple_timer.nx:64:8
      assign _8 = _324 | _326 | _328 | _330 | _332 | _334 | _336 | _338 | _340 | _342 | _344 | _346 | _348 | _350 | _352 | _354; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:20:11" */
/* END "top"; "simple_timer.nx:1:0" */
endmodule
