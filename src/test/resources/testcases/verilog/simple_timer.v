module top(
  _0, _1, _2, _3, _4, _5, _6, _7, _8
);

/*============================== DATA ==============================*/

/* "top"; "simple_timer.nx:1:10" */
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
      wire _27; // simple_timer.nx:64:8:$o
      wire _28; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:30:17" */
  /* END "clock_divider"; "simple_timer.nx:10:11" */
  /* "MUX"; "simple_timer.nx:11:22" */
    wire _29; // simple_timer.nx:59:9:code
    wire [1:0] _32; // simple_timer.nx:60:9:mask
    wire [31:0] _30; // simple_timer.nx:61:9:input[0]
    wire [31:0] _31; // simple_timer.nx:61:19:out
    /* "DECODER"; "simple_timer.nx:62:12" */
    /* END "DECODER"; "simple_timer.nx:62:12" */
    wire [31:0] _33; // simple_timer.nx:64:26:$o
    wire [31:0] _34; // simple_timer.nx:64:8:$o
    wire [31:0] _35; // simple_timer.nx:64:26:$o
    wire [31:0] _36; // simple_timer.nx:64:8:$o
  /* END "MUX"; "simple_timer.nx:11:22" */
  wire [31:0] _37; // simple_timer.nx:13:34:$o
  /* "hex_trigger"; "simple_timer.nx:13:11" */
    wire [3:0] _38; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _39; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _40; // simple_timer.nx:75:52:$o
        wire _41; // simple_timer.nx:75:52:$o
        wire _42; // simple_timer.nx:75:52:$o
        wire _43; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _44; // simple_timer.nx:64:26:$o
      wire [6:0] _45; // simple_timer.nx:64:8:$o
      wire [6:0] _46; // simple_timer.nx:64:26:$o
      wire [6:0] _47; // simple_timer.nx:64:8:$o
      wire [6:0] _48; // simple_timer.nx:64:26:$o
      wire [6:0] _49; // simple_timer.nx:64:8:$o
      wire [6:0] _50; // simple_timer.nx:64:26:$o
      wire [6:0] _51; // simple_timer.nx:64:8:$o
      wire [6:0] _52; // simple_timer.nx:64:26:$o
      wire [6:0] _53; // simple_timer.nx:64:8:$o
      wire [6:0] _54; // simple_timer.nx:64:26:$o
      wire [6:0] _55; // simple_timer.nx:64:8:$o
      wire [6:0] _56; // simple_timer.nx:64:26:$o
      wire [6:0] _57; // simple_timer.nx:64:8:$o
      wire [6:0] _58; // simple_timer.nx:64:26:$o
      wire [6:0] _59; // simple_timer.nx:64:8:$o
      wire [6:0] _60; // simple_timer.nx:64:26:$o
      wire [6:0] _61; // simple_timer.nx:64:8:$o
      wire [6:0] _62; // simple_timer.nx:64:26:$o
      wire [6:0] _63; // simple_timer.nx:64:8:$o
      wire [6:0] _64; // simple_timer.nx:64:26:$o
      wire [6:0] _65; // simple_timer.nx:64:8:$o
      wire [6:0] _66; // simple_timer.nx:64:26:$o
      wire [6:0] _67; // simple_timer.nx:64:8:$o
      wire [6:0] _68; // simple_timer.nx:64:26:$o
      wire [6:0] _69; // simple_timer.nx:64:8:$o
      wire [6:0] _70; // simple_timer.nx:64:26:$o
      wire [6:0] _71; // simple_timer.nx:64:8:$o
      wire [6:0] _72; // simple_timer.nx:64:26:$o
      wire [6:0] _73; // simple_timer.nx:64:8:$o
      wire [6:0] _74; // simple_timer.nx:64:26:$o
      wire [6:0] _75; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:13:11" */
  wire [31:0] _76; // simple_timer.nx:14:42:$o
  wire [31:0] _77; // simple_timer.nx:14:34:$o
  /* "hex_trigger"; "simple_timer.nx:14:11" */
    wire [3:0] _78; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _79; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _80; // simple_timer.nx:75:52:$o
        wire _81; // simple_timer.nx:75:52:$o
        wire _82; // simple_timer.nx:75:52:$o
        wire _83; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _84; // simple_timer.nx:64:26:$o
      wire [6:0] _85; // simple_timer.nx:64:8:$o
      wire [6:0] _86; // simple_timer.nx:64:26:$o
      wire [6:0] _87; // simple_timer.nx:64:8:$o
      wire [6:0] _88; // simple_timer.nx:64:26:$o
      wire [6:0] _89; // simple_timer.nx:64:8:$o
      wire [6:0] _90; // simple_timer.nx:64:26:$o
      wire [6:0] _91; // simple_timer.nx:64:8:$o
      wire [6:0] _92; // simple_timer.nx:64:26:$o
      wire [6:0] _93; // simple_timer.nx:64:8:$o
      wire [6:0] _94; // simple_timer.nx:64:26:$o
      wire [6:0] _95; // simple_timer.nx:64:8:$o
      wire [6:0] _96; // simple_timer.nx:64:26:$o
      wire [6:0] _97; // simple_timer.nx:64:8:$o
      wire [6:0] _98; // simple_timer.nx:64:26:$o
      wire [6:0] _99; // simple_timer.nx:64:8:$o
      wire [6:0] _100; // simple_timer.nx:64:26:$o
      wire [6:0] _101; // simple_timer.nx:64:8:$o
      wire [6:0] _102; // simple_timer.nx:64:26:$o
      wire [6:0] _103; // simple_timer.nx:64:8:$o
      wire [6:0] _104; // simple_timer.nx:64:26:$o
      wire [6:0] _105; // simple_timer.nx:64:8:$o
      wire [6:0] _106; // simple_timer.nx:64:26:$o
      wire [6:0] _107; // simple_timer.nx:64:8:$o
      wire [6:0] _108; // simple_timer.nx:64:26:$o
      wire [6:0] _109; // simple_timer.nx:64:8:$o
      wire [6:0] _110; // simple_timer.nx:64:26:$o
      wire [6:0] _111; // simple_timer.nx:64:8:$o
      wire [6:0] _112; // simple_timer.nx:64:26:$o
      wire [6:0] _113; // simple_timer.nx:64:8:$o
      wire [6:0] _114; // simple_timer.nx:64:26:$o
      wire [6:0] _115; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:14:11" */
  wire [31:0] _116; // simple_timer.nx:15:42:$o
  wire [31:0] _117; // simple_timer.nx:15:34:$o
  /* "hex_trigger"; "simple_timer.nx:15:11" */
    wire [3:0] _118; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _119; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _120; // simple_timer.nx:75:52:$o
        wire _121; // simple_timer.nx:75:52:$o
        wire _122; // simple_timer.nx:75:52:$o
        wire _123; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _124; // simple_timer.nx:64:26:$o
      wire [6:0] _125; // simple_timer.nx:64:8:$o
      wire [6:0] _126; // simple_timer.nx:64:26:$o
      wire [6:0] _127; // simple_timer.nx:64:8:$o
      wire [6:0] _128; // simple_timer.nx:64:26:$o
      wire [6:0] _129; // simple_timer.nx:64:8:$o
      wire [6:0] _130; // simple_timer.nx:64:26:$o
      wire [6:0] _131; // simple_timer.nx:64:8:$o
      wire [6:0] _132; // simple_timer.nx:64:26:$o
      wire [6:0] _133; // simple_timer.nx:64:8:$o
      wire [6:0] _134; // simple_timer.nx:64:26:$o
      wire [6:0] _135; // simple_timer.nx:64:8:$o
      wire [6:0] _136; // simple_timer.nx:64:26:$o
      wire [6:0] _137; // simple_timer.nx:64:8:$o
      wire [6:0] _138; // simple_timer.nx:64:26:$o
      wire [6:0] _139; // simple_timer.nx:64:8:$o
      wire [6:0] _140; // simple_timer.nx:64:26:$o
      wire [6:0] _141; // simple_timer.nx:64:8:$o
      wire [6:0] _142; // simple_timer.nx:64:26:$o
      wire [6:0] _143; // simple_timer.nx:64:8:$o
      wire [6:0] _144; // simple_timer.nx:64:26:$o
      wire [6:0] _145; // simple_timer.nx:64:8:$o
      wire [6:0] _146; // simple_timer.nx:64:26:$o
      wire [6:0] _147; // simple_timer.nx:64:8:$o
      wire [6:0] _148; // simple_timer.nx:64:26:$o
      wire [6:0] _149; // simple_timer.nx:64:8:$o
      wire [6:0] _150; // simple_timer.nx:64:26:$o
      wire [6:0] _151; // simple_timer.nx:64:8:$o
      wire [6:0] _152; // simple_timer.nx:64:26:$o
      wire [6:0] _153; // simple_timer.nx:64:8:$o
      wire [6:0] _154; // simple_timer.nx:64:26:$o
      wire [6:0] _155; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:15:11" */
  wire [31:0] _156; // simple_timer.nx:16:42:$o
  wire [31:0] _157; // simple_timer.nx:16:34:$o
  /* "hex_trigger"; "simple_timer.nx:16:11" */
    wire [3:0] _158; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _159; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _160; // simple_timer.nx:75:52:$o
        wire _161; // simple_timer.nx:75:52:$o
        wire _162; // simple_timer.nx:75:52:$o
        wire _163; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _164; // simple_timer.nx:64:26:$o
      wire [6:0] _165; // simple_timer.nx:64:8:$o
      wire [6:0] _166; // simple_timer.nx:64:26:$o
      wire [6:0] _167; // simple_timer.nx:64:8:$o
      wire [6:0] _168; // simple_timer.nx:64:26:$o
      wire [6:0] _169; // simple_timer.nx:64:8:$o
      wire [6:0] _170; // simple_timer.nx:64:26:$o
      wire [6:0] _171; // simple_timer.nx:64:8:$o
      wire [6:0] _172; // simple_timer.nx:64:26:$o
      wire [6:0] _173; // simple_timer.nx:64:8:$o
      wire [6:0] _174; // simple_timer.nx:64:26:$o
      wire [6:0] _175; // simple_timer.nx:64:8:$o
      wire [6:0] _176; // simple_timer.nx:64:26:$o
      wire [6:0] _177; // simple_timer.nx:64:8:$o
      wire [6:0] _178; // simple_timer.nx:64:26:$o
      wire [6:0] _179; // simple_timer.nx:64:8:$o
      wire [6:0] _180; // simple_timer.nx:64:26:$o
      wire [6:0] _181; // simple_timer.nx:64:8:$o
      wire [6:0] _182; // simple_timer.nx:64:26:$o
      wire [6:0] _183; // simple_timer.nx:64:8:$o
      wire [6:0] _184; // simple_timer.nx:64:26:$o
      wire [6:0] _185; // simple_timer.nx:64:8:$o
      wire [6:0] _186; // simple_timer.nx:64:26:$o
      wire [6:0] _187; // simple_timer.nx:64:8:$o
      wire [6:0] _188; // simple_timer.nx:64:26:$o
      wire [6:0] _189; // simple_timer.nx:64:8:$o
      wire [6:0] _190; // simple_timer.nx:64:26:$o
      wire [6:0] _191; // simple_timer.nx:64:8:$o
      wire [6:0] _192; // simple_timer.nx:64:26:$o
      wire [6:0] _193; // simple_timer.nx:64:8:$o
      wire [6:0] _194; // simple_timer.nx:64:26:$o
      wire [6:0] _195; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:16:11" */
  wire [31:0] _196; // simple_timer.nx:17:42:$o
  wire [31:0] _197; // simple_timer.nx:17:34:$o
  /* "hex_trigger"; "simple_timer.nx:17:11" */
    wire [3:0] _198; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _199; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _200; // simple_timer.nx:75:52:$o
        wire _201; // simple_timer.nx:75:52:$o
        wire _202; // simple_timer.nx:75:52:$o
        wire _203; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _204; // simple_timer.nx:64:26:$o
      wire [6:0] _205; // simple_timer.nx:64:8:$o
      wire [6:0] _206; // simple_timer.nx:64:26:$o
      wire [6:0] _207; // simple_timer.nx:64:8:$o
      wire [6:0] _208; // simple_timer.nx:64:26:$o
      wire [6:0] _209; // simple_timer.nx:64:8:$o
      wire [6:0] _210; // simple_timer.nx:64:26:$o
      wire [6:0] _211; // simple_timer.nx:64:8:$o
      wire [6:0] _212; // simple_timer.nx:64:26:$o
      wire [6:0] _213; // simple_timer.nx:64:8:$o
      wire [6:0] _214; // simple_timer.nx:64:26:$o
      wire [6:0] _215; // simple_timer.nx:64:8:$o
      wire [6:0] _216; // simple_timer.nx:64:26:$o
      wire [6:0] _217; // simple_timer.nx:64:8:$o
      wire [6:0] _218; // simple_timer.nx:64:26:$o
      wire [6:0] _219; // simple_timer.nx:64:8:$o
      wire [6:0] _220; // simple_timer.nx:64:26:$o
      wire [6:0] _221; // simple_timer.nx:64:8:$o
      wire [6:0] _222; // simple_timer.nx:64:26:$o
      wire [6:0] _223; // simple_timer.nx:64:8:$o
      wire [6:0] _224; // simple_timer.nx:64:26:$o
      wire [6:0] _225; // simple_timer.nx:64:8:$o
      wire [6:0] _226; // simple_timer.nx:64:26:$o
      wire [6:0] _227; // simple_timer.nx:64:8:$o
      wire [6:0] _228; // simple_timer.nx:64:26:$o
      wire [6:0] _229; // simple_timer.nx:64:8:$o
      wire [6:0] _230; // simple_timer.nx:64:26:$o
      wire [6:0] _231; // simple_timer.nx:64:8:$o
      wire [6:0] _232; // simple_timer.nx:64:26:$o
      wire [6:0] _233; // simple_timer.nx:64:8:$o
      wire [6:0] _234; // simple_timer.nx:64:26:$o
      wire [6:0] _235; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:17:11" */
  wire [31:0] _236; // simple_timer.nx:18:42:$o
  wire [31:0] _237; // simple_timer.nx:18:34:$o
  /* "hex_trigger"; "simple_timer.nx:18:11" */
    wire [3:0] _238; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _239; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _240; // simple_timer.nx:75:52:$o
        wire _241; // simple_timer.nx:75:52:$o
        wire _242; // simple_timer.nx:75:52:$o
        wire _243; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _244; // simple_timer.nx:64:26:$o
      wire [6:0] _245; // simple_timer.nx:64:8:$o
      wire [6:0] _246; // simple_timer.nx:64:26:$o
      wire [6:0] _247; // simple_timer.nx:64:8:$o
      wire [6:0] _248; // simple_timer.nx:64:26:$o
      wire [6:0] _249; // simple_timer.nx:64:8:$o
      wire [6:0] _250; // simple_timer.nx:64:26:$o
      wire [6:0] _251; // simple_timer.nx:64:8:$o
      wire [6:0] _252; // simple_timer.nx:64:26:$o
      wire [6:0] _253; // simple_timer.nx:64:8:$o
      wire [6:0] _254; // simple_timer.nx:64:26:$o
      wire [6:0] _255; // simple_timer.nx:64:8:$o
      wire [6:0] _256; // simple_timer.nx:64:26:$o
      wire [6:0] _257; // simple_timer.nx:64:8:$o
      wire [6:0] _258; // simple_timer.nx:64:26:$o
      wire [6:0] _259; // simple_timer.nx:64:8:$o
      wire [6:0] _260; // simple_timer.nx:64:26:$o
      wire [6:0] _261; // simple_timer.nx:64:8:$o
      wire [6:0] _262; // simple_timer.nx:64:26:$o
      wire [6:0] _263; // simple_timer.nx:64:8:$o
      wire [6:0] _264; // simple_timer.nx:64:26:$o
      wire [6:0] _265; // simple_timer.nx:64:8:$o
      wire [6:0] _266; // simple_timer.nx:64:26:$o
      wire [6:0] _267; // simple_timer.nx:64:8:$o
      wire [6:0] _268; // simple_timer.nx:64:26:$o
      wire [6:0] _269; // simple_timer.nx:64:8:$o
      wire [6:0] _270; // simple_timer.nx:64:26:$o
      wire [6:0] _271; // simple_timer.nx:64:8:$o
      wire [6:0] _272; // simple_timer.nx:64:26:$o
      wire [6:0] _273; // simple_timer.nx:64:8:$o
      wire [6:0] _274; // simple_timer.nx:64:26:$o
      wire [6:0] _275; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:18:11" */
  wire [31:0] _276; // simple_timer.nx:19:42:$o
  wire [31:0] _277; // simple_timer.nx:19:34:$o
  /* "hex_trigger"; "simple_timer.nx:19:11" */
    wire [3:0] _278; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _279; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _280; // simple_timer.nx:75:52:$o
        wire _281; // simple_timer.nx:75:52:$o
        wire _282; // simple_timer.nx:75:52:$o
        wire _283; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _284; // simple_timer.nx:64:26:$o
      wire [6:0] _285; // simple_timer.nx:64:8:$o
      wire [6:0] _286; // simple_timer.nx:64:26:$o
      wire [6:0] _287; // simple_timer.nx:64:8:$o
      wire [6:0] _288; // simple_timer.nx:64:26:$o
      wire [6:0] _289; // simple_timer.nx:64:8:$o
      wire [6:0] _290; // simple_timer.nx:64:26:$o
      wire [6:0] _291; // simple_timer.nx:64:8:$o
      wire [6:0] _292; // simple_timer.nx:64:26:$o
      wire [6:0] _293; // simple_timer.nx:64:8:$o
      wire [6:0] _294; // simple_timer.nx:64:26:$o
      wire [6:0] _295; // simple_timer.nx:64:8:$o
      wire [6:0] _296; // simple_timer.nx:64:26:$o
      wire [6:0] _297; // simple_timer.nx:64:8:$o
      wire [6:0] _298; // simple_timer.nx:64:26:$o
      wire [6:0] _299; // simple_timer.nx:64:8:$o
      wire [6:0] _300; // simple_timer.nx:64:26:$o
      wire [6:0] _301; // simple_timer.nx:64:8:$o
      wire [6:0] _302; // simple_timer.nx:64:26:$o
      wire [6:0] _303; // simple_timer.nx:64:8:$o
      wire [6:0] _304; // simple_timer.nx:64:26:$o
      wire [6:0] _305; // simple_timer.nx:64:8:$o
      wire [6:0] _306; // simple_timer.nx:64:26:$o
      wire [6:0] _307; // simple_timer.nx:64:8:$o
      wire [6:0] _308; // simple_timer.nx:64:26:$o
      wire [6:0] _309; // simple_timer.nx:64:8:$o
      wire [6:0] _310; // simple_timer.nx:64:26:$o
      wire [6:0] _311; // simple_timer.nx:64:8:$o
      wire [6:0] _312; // simple_timer.nx:64:26:$o
      wire [6:0] _313; // simple_timer.nx:64:8:$o
      wire [6:0] _314; // simple_timer.nx:64:26:$o
      wire [6:0] _315; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:19:11" */
  wire [31:0] _316; // simple_timer.nx:20:42:$o
  wire [31:0] _317; // simple_timer.nx:20:34:$o
  /* "hex_trigger"; "simple_timer.nx:20:11" */
    wire [3:0] _318; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _319; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _320; // simple_timer.nx:75:52:$o
        wire _321; // simple_timer.nx:75:52:$o
        wire _322; // simple_timer.nx:75:52:$o
        wire _323; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _324; // simple_timer.nx:64:26:$o
      wire [6:0] _325; // simple_timer.nx:64:8:$o
      wire [6:0] _326; // simple_timer.nx:64:26:$o
      wire [6:0] _327; // simple_timer.nx:64:8:$o
      wire [6:0] _328; // simple_timer.nx:64:26:$o
      wire [6:0] _329; // simple_timer.nx:64:8:$o
      wire [6:0] _330; // simple_timer.nx:64:26:$o
      wire [6:0] _331; // simple_timer.nx:64:8:$o
      wire [6:0] _332; // simple_timer.nx:64:26:$o
      wire [6:0] _333; // simple_timer.nx:64:8:$o
      wire [6:0] _334; // simple_timer.nx:64:26:$o
      wire [6:0] _335; // simple_timer.nx:64:8:$o
      wire [6:0] _336; // simple_timer.nx:64:26:$o
      wire [6:0] _337; // simple_timer.nx:64:8:$o
      wire [6:0] _338; // simple_timer.nx:64:26:$o
      wire [6:0] _339; // simple_timer.nx:64:8:$o
      wire [6:0] _340; // simple_timer.nx:64:26:$o
      wire [6:0] _341; // simple_timer.nx:64:8:$o
      wire [6:0] _342; // simple_timer.nx:64:26:$o
      wire [6:0] _343; // simple_timer.nx:64:8:$o
      wire [6:0] _344; // simple_timer.nx:64:26:$o
      wire [6:0] _345; // simple_timer.nx:64:8:$o
      wire [6:0] _346; // simple_timer.nx:64:26:$o
      wire [6:0] _347; // simple_timer.nx:64:8:$o
      wire [6:0] _348; // simple_timer.nx:64:26:$o
      wire [6:0] _349; // simple_timer.nx:64:8:$o
      wire [6:0] _350; // simple_timer.nx:64:26:$o
      wire [6:0] _351; // simple_timer.nx:64:8:$o
      wire [6:0] _352; // simple_timer.nx:64:26:$o
      wire [6:0] _353; // simple_timer.nx:64:8:$o
      wire [6:0] _354; // simple_timer.nx:64:26:$o
      wire [6:0] _355; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:20:11" */
/* END "top"; "simple_timer.nx:1:10" */

/*============================== CONNECTION ==============================*/

/*
  COMPONENT "top"; "simple_timer.nx:1:10"
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
        assign _18[1] = _15; // :0:0
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _19 = $signed(_18[0]); // simple_timer.nx:64:26
      assign _20 = _16 & _19; // simple_timer.nx:64:8
      assign _21 = $signed(_18[1]); // simple_timer.nx:64:26
      assign _22 = 0 & _21; // simple_timer.nx:64:8
      assign _17 = _20 | _22; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:29:17" */
    always @(posedge _17) _14 <= _17; // simple_timer.nx:27:11
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
        assign _25[1] = _26; // :0:0
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _27 = _9 & _25[0]; // simple_timer.nx:64:8
      assign _28 = _23 & _25[1]; // simple_timer.nx:64:8
      assign _24 = _27 | _28; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:30:17" */
    always @(posedge _24) _9 <= _24; // simple_timer.nx:27:11
  /* END "clock_divider"; "simple_timer.nx:10:11" */
  assign _29 = _13 == 8639999; // simple_timer.nx:11:33
  assign _30 = _13 + 1; // simple_timer.nx:11:66
  /*
    COMPONENT "MUX"; "simple_timer.nx:11:22"
    input: code@29, input[0]@30, 0
    output: out@31
    attribute: { width: "1" }
  */
    /*
      COMPONENT "DECODER"; "simple_timer.nx:62:12"
      input: code@29
      output: mask@32
      attribute: { width: "1" }
    */
      assign _32[0] = ~_29; // simple_timer.nx:75:52
      assign _32[1] = _29; // :0:0
    /* END "DECODER"; "simple_timer.nx:62:12" */
    assign _33 = $signed(_32[0]); // simple_timer.nx:64:26
    assign _34 = _30 & _33; // simple_timer.nx:64:8
    assign _35 = $signed(_32[1]); // simple_timer.nx:64:26
    assign _36 = 0 & _35; // simple_timer.nx:64:8
    assign _31 = _34 | _36; // simple_timer.nx:63:11
  /* END "MUX"; "simple_timer.nx:11:22" */
  always @(posedge _31) _13 <= _31; // simple_timer.nx:5:16
  assign _37 = _13 % 10; // simple_timer.nx:13:34
  assign _38 = $signed(_37); // simple_timer.nx:13:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:13:11"
    input: num@38
    output: hex[0]@1
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@38, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E
      output: hex[0]@1
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@38
        output: mask@39
        attribute: { width: "4" }
      */
        assign _40 = ~_38[0]; // simple_timer.nx:75:52
        assign _41 = ~_38[1]; // simple_timer.nx:75:52
        assign _42 = ~_38[2]; // simple_timer.nx:75:52
        assign _43 = ~_38[3]; // simple_timer.nx:75:52
        assign _39[0] = _40 & _41 & _42 & _43; // simple_timer.nx:73:18
        assign _39[1] = _38[0] & _41 & _42 & _43; // simple_timer.nx:73:18
        assign _39[2] = _40 & _38[1] & _42 & _43; // simple_timer.nx:73:18
        assign _39[3] = _38[0] & _38[1] & _42 & _43; // simple_timer.nx:73:18
        assign _39[4] = _40 & _41 & _38[2] & _43; // simple_timer.nx:73:18
        assign _39[5] = _38[0] & _41 & _38[2] & _43; // simple_timer.nx:73:18
        assign _39[6] = _40 & _38[1] & _38[2] & _43; // simple_timer.nx:73:18
        assign _39[7] = _38[0] & _38[1] & _38[2] & _43; // simple_timer.nx:73:18
        assign _39[8] = _40 & _41 & _42 & _38[3]; // simple_timer.nx:73:18
        assign _39[9] = _38[0] & _41 & _42 & _38[3]; // simple_timer.nx:73:18
        assign _39[10] = _40 & _38[1] & _42 & _38[3]; // simple_timer.nx:73:18
        assign _39[11] = _38[0] & _38[1] & _42 & _38[3]; // simple_timer.nx:73:18
        assign _39[12] = _40 & _41 & _38[2] & _38[3]; // simple_timer.nx:73:18
        assign _39[13] = _38[0] & _41 & _38[2] & _38[3]; // simple_timer.nx:73:18
        assign _39[14] = _40 & _38[1] & _38[2] & _38[3]; // simple_timer.nx:73:18
        assign _39[15] = _38[0] & _38[1] & _38[2] & _38[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _44 = $signed(_39[0]); // simple_timer.nx:64:26
      assign _45 = 7'h40 & _44; // simple_timer.nx:64:8
      assign _46 = $signed(_39[1]); // simple_timer.nx:64:26
      assign _47 = 7'h79 & _46; // simple_timer.nx:64:8
      assign _48 = $signed(_39[2]); // simple_timer.nx:64:26
      assign _49 = 7'h24 & _48; // simple_timer.nx:64:8
      assign _50 = $signed(_39[3]); // simple_timer.nx:64:26
      assign _51 = 7'h30 & _50; // simple_timer.nx:64:8
      assign _52 = $signed(_39[4]); // simple_timer.nx:64:26
      assign _53 = 7'h19 & _52; // simple_timer.nx:64:8
      assign _54 = $signed(_39[5]); // simple_timer.nx:64:26
      assign _55 = 7'h12 & _54; // simple_timer.nx:64:8
      assign _56 = $signed(_39[6]); // simple_timer.nx:64:26
      assign _57 = 7'h02 & _56; // simple_timer.nx:64:8
      assign _58 = $signed(_39[7]); // simple_timer.nx:64:26
      assign _59 = 7'h78 & _58; // simple_timer.nx:64:8
      assign _60 = $signed(_39[8]); // simple_timer.nx:64:26
      assign _61 = 7'h00 & _60; // simple_timer.nx:64:8
      assign _62 = $signed(_39[9]); // simple_timer.nx:64:26
      assign _63 = 7'h10 & _62; // simple_timer.nx:64:8
      assign _64 = $signed(_39[10]); // simple_timer.nx:64:26
      assign _65 = 7'h08 & _64; // simple_timer.nx:64:8
      assign _66 = $signed(_39[11]); // simple_timer.nx:64:26
      assign _67 = 7'h03 & _66; // simple_timer.nx:64:8
      assign _68 = $signed(_39[12]); // simple_timer.nx:64:26
      assign _69 = 7'h46 & _68; // simple_timer.nx:64:8
      assign _70 = $signed(_39[13]); // simple_timer.nx:64:26
      assign _71 = 7'h21 & _70; // simple_timer.nx:64:8
      assign _72 = $signed(_39[14]); // simple_timer.nx:64:26
      assign _73 = 7'h06 & _72; // simple_timer.nx:64:8
      assign _74 = $signed(_39[15]); // simple_timer.nx:64:26
      assign _75 = 7'h0E & _74; // simple_timer.nx:64:8
      assign _1 = _45 | _47 | _49 | _51 | _53 | _55 | _57 | _59 | _61 | _63 | _65 | _67 | _69 | _71 | _73 | _75; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:13:11" */
  assign _76 = _13 % 100; // simple_timer.nx:14:42
  assign _77 = _76 / 10; // simple_timer.nx:14:34
  assign _78 = $signed(_77); // simple_timer.nx:14:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:14:11"
    input: num@78
    output: hex[1]@2
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@78, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E
      output: hex[1]@2
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@78
        output: mask@79
        attribute: { width: "4" }
      */
        assign _80 = ~_78[0]; // simple_timer.nx:75:52
        assign _81 = ~_78[1]; // simple_timer.nx:75:52
        assign _82 = ~_78[2]; // simple_timer.nx:75:52
        assign _83 = ~_78[3]; // simple_timer.nx:75:52
        assign _79[0] = _80 & _81 & _82 & _83; // simple_timer.nx:73:18
        assign _79[1] = _78[0] & _81 & _82 & _83; // simple_timer.nx:73:18
        assign _79[2] = _80 & _78[1] & _82 & _83; // simple_timer.nx:73:18
        assign _79[3] = _78[0] & _78[1] & _82 & _83; // simple_timer.nx:73:18
        assign _79[4] = _80 & _81 & _78[2] & _83; // simple_timer.nx:73:18
        assign _79[5] = _78[0] & _81 & _78[2] & _83; // simple_timer.nx:73:18
        assign _79[6] = _80 & _78[1] & _78[2] & _83; // simple_timer.nx:73:18
        assign _79[7] = _78[0] & _78[1] & _78[2] & _83; // simple_timer.nx:73:18
        assign _79[8] = _80 & _81 & _82 & _78[3]; // simple_timer.nx:73:18
        assign _79[9] = _78[0] & _81 & _82 & _78[3]; // simple_timer.nx:73:18
        assign _79[10] = _80 & _78[1] & _82 & _78[3]; // simple_timer.nx:73:18
        assign _79[11] = _78[0] & _78[1] & _82 & _78[3]; // simple_timer.nx:73:18
        assign _79[12] = _80 & _81 & _78[2] & _78[3]; // simple_timer.nx:73:18
        assign _79[13] = _78[0] & _81 & _78[2] & _78[3]; // simple_timer.nx:73:18
        assign _79[14] = _80 & _78[1] & _78[2] & _78[3]; // simple_timer.nx:73:18
        assign _79[15] = _78[0] & _78[1] & _78[2] & _78[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _84 = $signed(_79[0]); // simple_timer.nx:64:26
      assign _85 = 7'h40 & _84; // simple_timer.nx:64:8
      assign _86 = $signed(_79[1]); // simple_timer.nx:64:26
      assign _87 = 7'h79 & _86; // simple_timer.nx:64:8
      assign _88 = $signed(_79[2]); // simple_timer.nx:64:26
      assign _89 = 7'h24 & _88; // simple_timer.nx:64:8
      assign _90 = $signed(_79[3]); // simple_timer.nx:64:26
      assign _91 = 7'h30 & _90; // simple_timer.nx:64:8
      assign _92 = $signed(_79[4]); // simple_timer.nx:64:26
      assign _93 = 7'h19 & _92; // simple_timer.nx:64:8
      assign _94 = $signed(_79[5]); // simple_timer.nx:64:26
      assign _95 = 7'h12 & _94; // simple_timer.nx:64:8
      assign _96 = $signed(_79[6]); // simple_timer.nx:64:26
      assign _97 = 7'h02 & _96; // simple_timer.nx:64:8
      assign _98 = $signed(_79[7]); // simple_timer.nx:64:26
      assign _99 = 7'h78 & _98; // simple_timer.nx:64:8
      assign _100 = $signed(_79[8]); // simple_timer.nx:64:26
      assign _101 = 7'h00 & _100; // simple_timer.nx:64:8
      assign _102 = $signed(_79[9]); // simple_timer.nx:64:26
      assign _103 = 7'h10 & _102; // simple_timer.nx:64:8
      assign _104 = $signed(_79[10]); // simple_timer.nx:64:26
      assign _105 = 7'h08 & _104; // simple_timer.nx:64:8
      assign _106 = $signed(_79[11]); // simple_timer.nx:64:26
      assign _107 = 7'h03 & _106; // simple_timer.nx:64:8
      assign _108 = $signed(_79[12]); // simple_timer.nx:64:26
      assign _109 = 7'h46 & _108; // simple_timer.nx:64:8
      assign _110 = $signed(_79[13]); // simple_timer.nx:64:26
      assign _111 = 7'h21 & _110; // simple_timer.nx:64:8
      assign _112 = $signed(_79[14]); // simple_timer.nx:64:26
      assign _113 = 7'h06 & _112; // simple_timer.nx:64:8
      assign _114 = $signed(_79[15]); // simple_timer.nx:64:26
      assign _115 = 7'h0E & _114; // simple_timer.nx:64:8
      assign _2 = _85 | _87 | _89 | _91 | _93 | _95 | _97 | _99 | _101 | _103 | _105 | _107 | _109 | _111 | _113 | _115; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:14:11" */
  assign _116 = _13 % 1000; // simple_timer.nx:15:42
  assign _117 = _116 / 100; // simple_timer.nx:15:34
  assign _118 = $signed(_117); // simple_timer.nx:15:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:15:11"
    input: num@118
    output: hex[2]@3
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@118, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E
      output: hex[2]@3
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@118
        output: mask@119
        attribute: { width: "4" }
      */
        assign _120 = ~_118[0]; // simple_timer.nx:75:52
        assign _121 = ~_118[1]; // simple_timer.nx:75:52
        assign _122 = ~_118[2]; // simple_timer.nx:75:52
        assign _123 = ~_118[3]; // simple_timer.nx:75:52
        assign _119[0] = _120 & _121 & _122 & _123; // simple_timer.nx:73:18
        assign _119[1] = _118[0] & _121 & _122 & _123; // simple_timer.nx:73:18
        assign _119[2] = _120 & _118[1] & _122 & _123; // simple_timer.nx:73:18
        assign _119[3] = _118[0] & _118[1] & _122 & _123; // simple_timer.nx:73:18
        assign _119[4] = _120 & _121 & _118[2] & _123; // simple_timer.nx:73:18
        assign _119[5] = _118[0] & _121 & _118[2] & _123; // simple_timer.nx:73:18
        assign _119[6] = _120 & _118[1] & _118[2] & _123; // simple_timer.nx:73:18
        assign _119[7] = _118[0] & _118[1] & _118[2] & _123; // simple_timer.nx:73:18
        assign _119[8] = _120 & _121 & _122 & _118[3]; // simple_timer.nx:73:18
        assign _119[9] = _118[0] & _121 & _122 & _118[3]; // simple_timer.nx:73:18
        assign _119[10] = _120 & _118[1] & _122 & _118[3]; // simple_timer.nx:73:18
        assign _119[11] = _118[0] & _118[1] & _122 & _118[3]; // simple_timer.nx:73:18
        assign _119[12] = _120 & _121 & _118[2] & _118[3]; // simple_timer.nx:73:18
        assign _119[13] = _118[0] & _121 & _118[2] & _118[3]; // simple_timer.nx:73:18
        assign _119[14] = _120 & _118[1] & _118[2] & _118[3]; // simple_timer.nx:73:18
        assign _119[15] = _118[0] & _118[1] & _118[2] & _118[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _124 = $signed(_119[0]); // simple_timer.nx:64:26
      assign _125 = 7'h40 & _124; // simple_timer.nx:64:8
      assign _126 = $signed(_119[1]); // simple_timer.nx:64:26
      assign _127 = 7'h79 & _126; // simple_timer.nx:64:8
      assign _128 = $signed(_119[2]); // simple_timer.nx:64:26
      assign _129 = 7'h24 & _128; // simple_timer.nx:64:8
      assign _130 = $signed(_119[3]); // simple_timer.nx:64:26
      assign _131 = 7'h30 & _130; // simple_timer.nx:64:8
      assign _132 = $signed(_119[4]); // simple_timer.nx:64:26
      assign _133 = 7'h19 & _132; // simple_timer.nx:64:8
      assign _134 = $signed(_119[5]); // simple_timer.nx:64:26
      assign _135 = 7'h12 & _134; // simple_timer.nx:64:8
      assign _136 = $signed(_119[6]); // simple_timer.nx:64:26
      assign _137 = 7'h02 & _136; // simple_timer.nx:64:8
      assign _138 = $signed(_119[7]); // simple_timer.nx:64:26
      assign _139 = 7'h78 & _138; // simple_timer.nx:64:8
      assign _140 = $signed(_119[8]); // simple_timer.nx:64:26
      assign _141 = 7'h00 & _140; // simple_timer.nx:64:8
      assign _142 = $signed(_119[9]); // simple_timer.nx:64:26
      assign _143 = 7'h10 & _142; // simple_timer.nx:64:8
      assign _144 = $signed(_119[10]); // simple_timer.nx:64:26
      assign _145 = 7'h08 & _144; // simple_timer.nx:64:8
      assign _146 = $signed(_119[11]); // simple_timer.nx:64:26
      assign _147 = 7'h03 & _146; // simple_timer.nx:64:8
      assign _148 = $signed(_119[12]); // simple_timer.nx:64:26
      assign _149 = 7'h46 & _148; // simple_timer.nx:64:8
      assign _150 = $signed(_119[13]); // simple_timer.nx:64:26
      assign _151 = 7'h21 & _150; // simple_timer.nx:64:8
      assign _152 = $signed(_119[14]); // simple_timer.nx:64:26
      assign _153 = 7'h06 & _152; // simple_timer.nx:64:8
      assign _154 = $signed(_119[15]); // simple_timer.nx:64:26
      assign _155 = 7'h0E & _154; // simple_timer.nx:64:8
      assign _3 = _125 | _127 | _129 | _131 | _133 | _135 | _137 | _139 | _141 | _143 | _145 | _147 | _149 | _151 | _153 | _155; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:15:11" */
  assign _156 = _13 % 6000; // simple_timer.nx:16:42
  assign _157 = _156 / 1000; // simple_timer.nx:16:34
  assign _158 = $signed(_157); // simple_timer.nx:16:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:16:11"
    input: num@158
    output: hex[3]@4
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@158, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E
      output: hex[3]@4
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@158
        output: mask@159
        attribute: { width: "4" }
      */
        assign _160 = ~_158[0]; // simple_timer.nx:75:52
        assign _161 = ~_158[1]; // simple_timer.nx:75:52
        assign _162 = ~_158[2]; // simple_timer.nx:75:52
        assign _163 = ~_158[3]; // simple_timer.nx:75:52
        assign _159[0] = _160 & _161 & _162 & _163; // simple_timer.nx:73:18
        assign _159[1] = _158[0] & _161 & _162 & _163; // simple_timer.nx:73:18
        assign _159[2] = _160 & _158[1] & _162 & _163; // simple_timer.nx:73:18
        assign _159[3] = _158[0] & _158[1] & _162 & _163; // simple_timer.nx:73:18
        assign _159[4] = _160 & _161 & _158[2] & _163; // simple_timer.nx:73:18
        assign _159[5] = _158[0] & _161 & _158[2] & _163; // simple_timer.nx:73:18
        assign _159[6] = _160 & _158[1] & _158[2] & _163; // simple_timer.nx:73:18
        assign _159[7] = _158[0] & _158[1] & _158[2] & _163; // simple_timer.nx:73:18
        assign _159[8] = _160 & _161 & _162 & _158[3]; // simple_timer.nx:73:18
        assign _159[9] = _158[0] & _161 & _162 & _158[3]; // simple_timer.nx:73:18
        assign _159[10] = _160 & _158[1] & _162 & _158[3]; // simple_timer.nx:73:18
        assign _159[11] = _158[0] & _158[1] & _162 & _158[3]; // simple_timer.nx:73:18
        assign _159[12] = _160 & _161 & _158[2] & _158[3]; // simple_timer.nx:73:18
        assign _159[13] = _158[0] & _161 & _158[2] & _158[3]; // simple_timer.nx:73:18
        assign _159[14] = _160 & _158[1] & _158[2] & _158[3]; // simple_timer.nx:73:18
        assign _159[15] = _158[0] & _158[1] & _158[2] & _158[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _164 = $signed(_159[0]); // simple_timer.nx:64:26
      assign _165 = 7'h40 & _164; // simple_timer.nx:64:8
      assign _166 = $signed(_159[1]); // simple_timer.nx:64:26
      assign _167 = 7'h79 & _166; // simple_timer.nx:64:8
      assign _168 = $signed(_159[2]); // simple_timer.nx:64:26
      assign _169 = 7'h24 & _168; // simple_timer.nx:64:8
      assign _170 = $signed(_159[3]); // simple_timer.nx:64:26
      assign _171 = 7'h30 & _170; // simple_timer.nx:64:8
      assign _172 = $signed(_159[4]); // simple_timer.nx:64:26
      assign _173 = 7'h19 & _172; // simple_timer.nx:64:8
      assign _174 = $signed(_159[5]); // simple_timer.nx:64:26
      assign _175 = 7'h12 & _174; // simple_timer.nx:64:8
      assign _176 = $signed(_159[6]); // simple_timer.nx:64:26
      assign _177 = 7'h02 & _176; // simple_timer.nx:64:8
      assign _178 = $signed(_159[7]); // simple_timer.nx:64:26
      assign _179 = 7'h78 & _178; // simple_timer.nx:64:8
      assign _180 = $signed(_159[8]); // simple_timer.nx:64:26
      assign _181 = 7'h00 & _180; // simple_timer.nx:64:8
      assign _182 = $signed(_159[9]); // simple_timer.nx:64:26
      assign _183 = 7'h10 & _182; // simple_timer.nx:64:8
      assign _184 = $signed(_159[10]); // simple_timer.nx:64:26
      assign _185 = 7'h08 & _184; // simple_timer.nx:64:8
      assign _186 = $signed(_159[11]); // simple_timer.nx:64:26
      assign _187 = 7'h03 & _186; // simple_timer.nx:64:8
      assign _188 = $signed(_159[12]); // simple_timer.nx:64:26
      assign _189 = 7'h46 & _188; // simple_timer.nx:64:8
      assign _190 = $signed(_159[13]); // simple_timer.nx:64:26
      assign _191 = 7'h21 & _190; // simple_timer.nx:64:8
      assign _192 = $signed(_159[14]); // simple_timer.nx:64:26
      assign _193 = 7'h06 & _192; // simple_timer.nx:64:8
      assign _194 = $signed(_159[15]); // simple_timer.nx:64:26
      assign _195 = 7'h0E & _194; // simple_timer.nx:64:8
      assign _4 = _165 | _167 | _169 | _171 | _173 | _175 | _177 | _179 | _181 | _183 | _185 | _187 | _189 | _191 | _193 | _195; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:16:11" */
  assign _196 = _13 % 60000; // simple_timer.nx:17:42
  assign _197 = _196 / 6000; // simple_timer.nx:17:34
  assign _198 = $signed(_197); // simple_timer.nx:17:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:17:11"
    input: num@198
    output: hex[4]@5
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@198, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E
      output: hex[4]@5
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@198
        output: mask@199
        attribute: { width: "4" }
      */
        assign _200 = ~_198[0]; // simple_timer.nx:75:52
        assign _201 = ~_198[1]; // simple_timer.nx:75:52
        assign _202 = ~_198[2]; // simple_timer.nx:75:52
        assign _203 = ~_198[3]; // simple_timer.nx:75:52
        assign _199[0] = _200 & _201 & _202 & _203; // simple_timer.nx:73:18
        assign _199[1] = _198[0] & _201 & _202 & _203; // simple_timer.nx:73:18
        assign _199[2] = _200 & _198[1] & _202 & _203; // simple_timer.nx:73:18
        assign _199[3] = _198[0] & _198[1] & _202 & _203; // simple_timer.nx:73:18
        assign _199[4] = _200 & _201 & _198[2] & _203; // simple_timer.nx:73:18
        assign _199[5] = _198[0] & _201 & _198[2] & _203; // simple_timer.nx:73:18
        assign _199[6] = _200 & _198[1] & _198[2] & _203; // simple_timer.nx:73:18
        assign _199[7] = _198[0] & _198[1] & _198[2] & _203; // simple_timer.nx:73:18
        assign _199[8] = _200 & _201 & _202 & _198[3]; // simple_timer.nx:73:18
        assign _199[9] = _198[0] & _201 & _202 & _198[3]; // simple_timer.nx:73:18
        assign _199[10] = _200 & _198[1] & _202 & _198[3]; // simple_timer.nx:73:18
        assign _199[11] = _198[0] & _198[1] & _202 & _198[3]; // simple_timer.nx:73:18
        assign _199[12] = _200 & _201 & _198[2] & _198[3]; // simple_timer.nx:73:18
        assign _199[13] = _198[0] & _201 & _198[2] & _198[3]; // simple_timer.nx:73:18
        assign _199[14] = _200 & _198[1] & _198[2] & _198[3]; // simple_timer.nx:73:18
        assign _199[15] = _198[0] & _198[1] & _198[2] & _198[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _204 = $signed(_199[0]); // simple_timer.nx:64:26
      assign _205 = 7'h40 & _204; // simple_timer.nx:64:8
      assign _206 = $signed(_199[1]); // simple_timer.nx:64:26
      assign _207 = 7'h79 & _206; // simple_timer.nx:64:8
      assign _208 = $signed(_199[2]); // simple_timer.nx:64:26
      assign _209 = 7'h24 & _208; // simple_timer.nx:64:8
      assign _210 = $signed(_199[3]); // simple_timer.nx:64:26
      assign _211 = 7'h30 & _210; // simple_timer.nx:64:8
      assign _212 = $signed(_199[4]); // simple_timer.nx:64:26
      assign _213 = 7'h19 & _212; // simple_timer.nx:64:8
      assign _214 = $signed(_199[5]); // simple_timer.nx:64:26
      assign _215 = 7'h12 & _214; // simple_timer.nx:64:8
      assign _216 = $signed(_199[6]); // simple_timer.nx:64:26
      assign _217 = 7'h02 & _216; // simple_timer.nx:64:8
      assign _218 = $signed(_199[7]); // simple_timer.nx:64:26
      assign _219 = 7'h78 & _218; // simple_timer.nx:64:8
      assign _220 = $signed(_199[8]); // simple_timer.nx:64:26
      assign _221 = 7'h00 & _220; // simple_timer.nx:64:8
      assign _222 = $signed(_199[9]); // simple_timer.nx:64:26
      assign _223 = 7'h10 & _222; // simple_timer.nx:64:8
      assign _224 = $signed(_199[10]); // simple_timer.nx:64:26
      assign _225 = 7'h08 & _224; // simple_timer.nx:64:8
      assign _226 = $signed(_199[11]); // simple_timer.nx:64:26
      assign _227 = 7'h03 & _226; // simple_timer.nx:64:8
      assign _228 = $signed(_199[12]); // simple_timer.nx:64:26
      assign _229 = 7'h46 & _228; // simple_timer.nx:64:8
      assign _230 = $signed(_199[13]); // simple_timer.nx:64:26
      assign _231 = 7'h21 & _230; // simple_timer.nx:64:8
      assign _232 = $signed(_199[14]); // simple_timer.nx:64:26
      assign _233 = 7'h06 & _232; // simple_timer.nx:64:8
      assign _234 = $signed(_199[15]); // simple_timer.nx:64:26
      assign _235 = 7'h0E & _234; // simple_timer.nx:64:8
      assign _5 = _205 | _207 | _209 | _211 | _213 | _215 | _217 | _219 | _221 | _223 | _225 | _227 | _229 | _231 | _233 | _235; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:17:11" */
  assign _236 = _13 % 360000; // simple_timer.nx:18:42
  assign _237 = _236 / 60000; // simple_timer.nx:18:34
  assign _238 = $signed(_237); // simple_timer.nx:18:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:18:11"
    input: num@238
    output: hex[5]@6
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@238, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E
      output: hex[5]@6
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@238
        output: mask@239
        attribute: { width: "4" }
      */
        assign _240 = ~_238[0]; // simple_timer.nx:75:52
        assign _241 = ~_238[1]; // simple_timer.nx:75:52
        assign _242 = ~_238[2]; // simple_timer.nx:75:52
        assign _243 = ~_238[3]; // simple_timer.nx:75:52
        assign _239[0] = _240 & _241 & _242 & _243; // simple_timer.nx:73:18
        assign _239[1] = _238[0] & _241 & _242 & _243; // simple_timer.nx:73:18
        assign _239[2] = _240 & _238[1] & _242 & _243; // simple_timer.nx:73:18
        assign _239[3] = _238[0] & _238[1] & _242 & _243; // simple_timer.nx:73:18
        assign _239[4] = _240 & _241 & _238[2] & _243; // simple_timer.nx:73:18
        assign _239[5] = _238[0] & _241 & _238[2] & _243; // simple_timer.nx:73:18
        assign _239[6] = _240 & _238[1] & _238[2] & _243; // simple_timer.nx:73:18
        assign _239[7] = _238[0] & _238[1] & _238[2] & _243; // simple_timer.nx:73:18
        assign _239[8] = _240 & _241 & _242 & _238[3]; // simple_timer.nx:73:18
        assign _239[9] = _238[0] & _241 & _242 & _238[3]; // simple_timer.nx:73:18
        assign _239[10] = _240 & _238[1] & _242 & _238[3]; // simple_timer.nx:73:18
        assign _239[11] = _238[0] & _238[1] & _242 & _238[3]; // simple_timer.nx:73:18
        assign _239[12] = _240 & _241 & _238[2] & _238[3]; // simple_timer.nx:73:18
        assign _239[13] = _238[0] & _241 & _238[2] & _238[3]; // simple_timer.nx:73:18
        assign _239[14] = _240 & _238[1] & _238[2] & _238[3]; // simple_timer.nx:73:18
        assign _239[15] = _238[0] & _238[1] & _238[2] & _238[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _244 = $signed(_239[0]); // simple_timer.nx:64:26
      assign _245 = 7'h40 & _244; // simple_timer.nx:64:8
      assign _246 = $signed(_239[1]); // simple_timer.nx:64:26
      assign _247 = 7'h79 & _246; // simple_timer.nx:64:8
      assign _248 = $signed(_239[2]); // simple_timer.nx:64:26
      assign _249 = 7'h24 & _248; // simple_timer.nx:64:8
      assign _250 = $signed(_239[3]); // simple_timer.nx:64:26
      assign _251 = 7'h30 & _250; // simple_timer.nx:64:8
      assign _252 = $signed(_239[4]); // simple_timer.nx:64:26
      assign _253 = 7'h19 & _252; // simple_timer.nx:64:8
      assign _254 = $signed(_239[5]); // simple_timer.nx:64:26
      assign _255 = 7'h12 & _254; // simple_timer.nx:64:8
      assign _256 = $signed(_239[6]); // simple_timer.nx:64:26
      assign _257 = 7'h02 & _256; // simple_timer.nx:64:8
      assign _258 = $signed(_239[7]); // simple_timer.nx:64:26
      assign _259 = 7'h78 & _258; // simple_timer.nx:64:8
      assign _260 = $signed(_239[8]); // simple_timer.nx:64:26
      assign _261 = 7'h00 & _260; // simple_timer.nx:64:8
      assign _262 = $signed(_239[9]); // simple_timer.nx:64:26
      assign _263 = 7'h10 & _262; // simple_timer.nx:64:8
      assign _264 = $signed(_239[10]); // simple_timer.nx:64:26
      assign _265 = 7'h08 & _264; // simple_timer.nx:64:8
      assign _266 = $signed(_239[11]); // simple_timer.nx:64:26
      assign _267 = 7'h03 & _266; // simple_timer.nx:64:8
      assign _268 = $signed(_239[12]); // simple_timer.nx:64:26
      assign _269 = 7'h46 & _268; // simple_timer.nx:64:8
      assign _270 = $signed(_239[13]); // simple_timer.nx:64:26
      assign _271 = 7'h21 & _270; // simple_timer.nx:64:8
      assign _272 = $signed(_239[14]); // simple_timer.nx:64:26
      assign _273 = 7'h06 & _272; // simple_timer.nx:64:8
      assign _274 = $signed(_239[15]); // simple_timer.nx:64:26
      assign _275 = 7'h0E & _274; // simple_timer.nx:64:8
      assign _6 = _245 | _247 | _249 | _251 | _253 | _255 | _257 | _259 | _261 | _263 | _265 | _267 | _269 | _271 | _273 | _275; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:18:11" */
  assign _276 = _13 % 3600000; // simple_timer.nx:19:42
  assign _277 = _276 / 360000; // simple_timer.nx:19:34
  assign _278 = $signed(_277); // simple_timer.nx:19:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:19:11"
    input: num@278
    output: hex[6]@7
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@278, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E
      output: hex[6]@7
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@278
        output: mask@279
        attribute: { width: "4" }
      */
        assign _280 = ~_278[0]; // simple_timer.nx:75:52
        assign _281 = ~_278[1]; // simple_timer.nx:75:52
        assign _282 = ~_278[2]; // simple_timer.nx:75:52
        assign _283 = ~_278[3]; // simple_timer.nx:75:52
        assign _279[0] = _280 & _281 & _282 & _283; // simple_timer.nx:73:18
        assign _279[1] = _278[0] & _281 & _282 & _283; // simple_timer.nx:73:18
        assign _279[2] = _280 & _278[1] & _282 & _283; // simple_timer.nx:73:18
        assign _279[3] = _278[0] & _278[1] & _282 & _283; // simple_timer.nx:73:18
        assign _279[4] = _280 & _281 & _278[2] & _283; // simple_timer.nx:73:18
        assign _279[5] = _278[0] & _281 & _278[2] & _283; // simple_timer.nx:73:18
        assign _279[6] = _280 & _278[1] & _278[2] & _283; // simple_timer.nx:73:18
        assign _279[7] = _278[0] & _278[1] & _278[2] & _283; // simple_timer.nx:73:18
        assign _279[8] = _280 & _281 & _282 & _278[3]; // simple_timer.nx:73:18
        assign _279[9] = _278[0] & _281 & _282 & _278[3]; // simple_timer.nx:73:18
        assign _279[10] = _280 & _278[1] & _282 & _278[3]; // simple_timer.nx:73:18
        assign _279[11] = _278[0] & _278[1] & _282 & _278[3]; // simple_timer.nx:73:18
        assign _279[12] = _280 & _281 & _278[2] & _278[3]; // simple_timer.nx:73:18
        assign _279[13] = _278[0] & _281 & _278[2] & _278[3]; // simple_timer.nx:73:18
        assign _279[14] = _280 & _278[1] & _278[2] & _278[3]; // simple_timer.nx:73:18
        assign _279[15] = _278[0] & _278[1] & _278[2] & _278[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _284 = $signed(_279[0]); // simple_timer.nx:64:26
      assign _285 = 7'h40 & _284; // simple_timer.nx:64:8
      assign _286 = $signed(_279[1]); // simple_timer.nx:64:26
      assign _287 = 7'h79 & _286; // simple_timer.nx:64:8
      assign _288 = $signed(_279[2]); // simple_timer.nx:64:26
      assign _289 = 7'h24 & _288; // simple_timer.nx:64:8
      assign _290 = $signed(_279[3]); // simple_timer.nx:64:26
      assign _291 = 7'h30 & _290; // simple_timer.nx:64:8
      assign _292 = $signed(_279[4]); // simple_timer.nx:64:26
      assign _293 = 7'h19 & _292; // simple_timer.nx:64:8
      assign _294 = $signed(_279[5]); // simple_timer.nx:64:26
      assign _295 = 7'h12 & _294; // simple_timer.nx:64:8
      assign _296 = $signed(_279[6]); // simple_timer.nx:64:26
      assign _297 = 7'h02 & _296; // simple_timer.nx:64:8
      assign _298 = $signed(_279[7]); // simple_timer.nx:64:26
      assign _299 = 7'h78 & _298; // simple_timer.nx:64:8
      assign _300 = $signed(_279[8]); // simple_timer.nx:64:26
      assign _301 = 7'h00 & _300; // simple_timer.nx:64:8
      assign _302 = $signed(_279[9]); // simple_timer.nx:64:26
      assign _303 = 7'h10 & _302; // simple_timer.nx:64:8
      assign _304 = $signed(_279[10]); // simple_timer.nx:64:26
      assign _305 = 7'h08 & _304; // simple_timer.nx:64:8
      assign _306 = $signed(_279[11]); // simple_timer.nx:64:26
      assign _307 = 7'h03 & _306; // simple_timer.nx:64:8
      assign _308 = $signed(_279[12]); // simple_timer.nx:64:26
      assign _309 = 7'h46 & _308; // simple_timer.nx:64:8
      assign _310 = $signed(_279[13]); // simple_timer.nx:64:26
      assign _311 = 7'h21 & _310; // simple_timer.nx:64:8
      assign _312 = $signed(_279[14]); // simple_timer.nx:64:26
      assign _313 = 7'h06 & _312; // simple_timer.nx:64:8
      assign _314 = $signed(_279[15]); // simple_timer.nx:64:26
      assign _315 = 7'h0E & _314; // simple_timer.nx:64:8
      assign _7 = _285 | _287 | _289 | _291 | _293 | _295 | _297 | _299 | _301 | _303 | _305 | _307 | _309 | _311 | _313 | _315; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:19:11" */
  assign _316 = _13 % 86400000; // simple_timer.nx:20:42
  assign _317 = _316 / 3600000; // simple_timer.nx:20:34
  assign _318 = $signed(_317); // simple_timer.nx:20:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:20:11"
    input: num@318
    output: hex[7]@8
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@318, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h02, 7'h78, 7'h00, 7'h10, 7'h08, 7'h03, 7'h46, 7'h21, 7'h06, 7'h0E
      output: hex[7]@8
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@318
        output: mask@319
        attribute: { width: "4" }
      */
        assign _320 = ~_318[0]; // simple_timer.nx:75:52
        assign _321 = ~_318[1]; // simple_timer.nx:75:52
        assign _322 = ~_318[2]; // simple_timer.nx:75:52
        assign _323 = ~_318[3]; // simple_timer.nx:75:52
        assign _319[0] = _320 & _321 & _322 & _323; // simple_timer.nx:73:18
        assign _319[1] = _318[0] & _321 & _322 & _323; // simple_timer.nx:73:18
        assign _319[2] = _320 & _318[1] & _322 & _323; // simple_timer.nx:73:18
        assign _319[3] = _318[0] & _318[1] & _322 & _323; // simple_timer.nx:73:18
        assign _319[4] = _320 & _321 & _318[2] & _323; // simple_timer.nx:73:18
        assign _319[5] = _318[0] & _321 & _318[2] & _323; // simple_timer.nx:73:18
        assign _319[6] = _320 & _318[1] & _318[2] & _323; // simple_timer.nx:73:18
        assign _319[7] = _318[0] & _318[1] & _318[2] & _323; // simple_timer.nx:73:18
        assign _319[8] = _320 & _321 & _322 & _318[3]; // simple_timer.nx:73:18
        assign _319[9] = _318[0] & _321 & _322 & _318[3]; // simple_timer.nx:73:18
        assign _319[10] = _320 & _318[1] & _322 & _318[3]; // simple_timer.nx:73:18
        assign _319[11] = _318[0] & _318[1] & _322 & _318[3]; // simple_timer.nx:73:18
        assign _319[12] = _320 & _321 & _318[2] & _318[3]; // simple_timer.nx:73:18
        assign _319[13] = _318[0] & _321 & _318[2] & _318[3]; // simple_timer.nx:73:18
        assign _319[14] = _320 & _318[1] & _318[2] & _318[3]; // simple_timer.nx:73:18
        assign _319[15] = _318[0] & _318[1] & _318[2] & _318[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _324 = $signed(_319[0]); // simple_timer.nx:64:26
      assign _325 = 7'h40 & _324; // simple_timer.nx:64:8
      assign _326 = $signed(_319[1]); // simple_timer.nx:64:26
      assign _327 = 7'h79 & _326; // simple_timer.nx:64:8
      assign _328 = $signed(_319[2]); // simple_timer.nx:64:26
      assign _329 = 7'h24 & _328; // simple_timer.nx:64:8
      assign _330 = $signed(_319[3]); // simple_timer.nx:64:26
      assign _331 = 7'h30 & _330; // simple_timer.nx:64:8
      assign _332 = $signed(_319[4]); // simple_timer.nx:64:26
      assign _333 = 7'h19 & _332; // simple_timer.nx:64:8
      assign _334 = $signed(_319[5]); // simple_timer.nx:64:26
      assign _335 = 7'h12 & _334; // simple_timer.nx:64:8
      assign _336 = $signed(_319[6]); // simple_timer.nx:64:26
      assign _337 = 7'h02 & _336; // simple_timer.nx:64:8
      assign _338 = $signed(_319[7]); // simple_timer.nx:64:26
      assign _339 = 7'h78 & _338; // simple_timer.nx:64:8
      assign _340 = $signed(_319[8]); // simple_timer.nx:64:26
      assign _341 = 7'h00 & _340; // simple_timer.nx:64:8
      assign _342 = $signed(_319[9]); // simple_timer.nx:64:26
      assign _343 = 7'h10 & _342; // simple_timer.nx:64:8
      assign _344 = $signed(_319[10]); // simple_timer.nx:64:26
      assign _345 = 7'h08 & _344; // simple_timer.nx:64:8
      assign _346 = $signed(_319[11]); // simple_timer.nx:64:26
      assign _347 = 7'h03 & _346; // simple_timer.nx:64:8
      assign _348 = $signed(_319[12]); // simple_timer.nx:64:26
      assign _349 = 7'h46 & _348; // simple_timer.nx:64:8
      assign _350 = $signed(_319[13]); // simple_timer.nx:64:26
      assign _351 = 7'h21 & _350; // simple_timer.nx:64:8
      assign _352 = $signed(_319[14]); // simple_timer.nx:64:26
      assign _353 = 7'h06 & _352; // simple_timer.nx:64:8
      assign _354 = $signed(_319[15]); // simple_timer.nx:64:26
      assign _355 = 7'h0E & _354; // simple_timer.nx:64:8
      assign _8 = _325 | _327 | _329 | _331 | _333 | _335 | _337 | _339 | _341 | _343 | _345 | _347 | _349 | _351 | _353 | _355; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:20:11" */
/* END "top"; "simple_timer.nx:1:10" */
endmodule
