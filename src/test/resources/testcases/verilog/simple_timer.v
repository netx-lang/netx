module top(
  _0, _1, _2, _3, _4, _5, _6, _7, _8
);

/*============================== DATA ==============================*/

reg [31:0] __reg__11;
reg __reg__9;
reg [31:0] __reg__10;
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
  wire [31:0] _10; // simple_timer.nx:8:6:ms
  /* "clock_divider"; "simple_timer.nx:10:11" */
    wire [31:0] _11; // simple_timer.nx:25:6:count
    /* "MUX"; "simple_timer.nx:29:17" */
      wire _12; // simple_timer.nx:59:9:code
      wire [1:0] _15; // simple_timer.nx:60:9:mask
      wire [31:0] _13; // simple_timer.nx:61:9:input[0]
      wire [31:0] _14; // simple_timer.nx:61:19:out
      /* "DECODER"; "simple_timer.nx:62:12" */
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [31:0] _16; // simple_timer.nx:64:26:$o
      wire [31:0] _17; // simple_timer.nx:64:8:$o
      wire [31:0] _18; // simple_timer.nx:64:26:$o
      wire [31:0] _19; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:29:17" */
    /* "MUX"; "simple_timer.nx:30:27" */
      wire [1:0] _22; // simple_timer.nx:60:9:mask
      wire _20; // simple_timer.nx:61:9:input[1]
      wire _21; // simple_timer.nx:61:19:out
      /* "DECODER"; "simple_timer.nx:62:12" */
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire _23; // simple_timer.nx:64:8:$o
      wire _24; // simple_timer.nx:64:8:$o
    /* END "MUX"; "simple_timer.nx:30:27" */
  /* END "clock_divider"; "simple_timer.nx:10:11" */
  /* "MUX"; "simple_timer.nx:11:22" */
    wire _25; // simple_timer.nx:59:9:code
    wire [1:0] _28; // simple_timer.nx:60:9:mask
    wire [31:0] _26; // simple_timer.nx:61:9:input[0]
    wire [31:0] _27; // simple_timer.nx:61:19:out
    /* "DECODER"; "simple_timer.nx:62:12" */
    /* END "DECODER"; "simple_timer.nx:62:12" */
    wire [31:0] _29; // simple_timer.nx:64:26:$o
    wire [31:0] _30; // simple_timer.nx:64:8:$o
    wire [31:0] _31; // simple_timer.nx:64:26:$o
    wire [31:0] _32; // simple_timer.nx:64:8:$o
  /* END "MUX"; "simple_timer.nx:11:22" */
  wire [31:0] _33; // simple_timer.nx:13:34:$o
  /* "hex_trigger"; "simple_timer.nx:13:11" */
    wire [3:0] _34; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _35; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _36; // simple_timer.nx:75:52:$o
        wire _37; // simple_timer.nx:75:52:$o
        wire _38; // simple_timer.nx:75:52:$o
        wire _39; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _40; // simple_timer.nx:64:26:$o
      wire [6:0] _41; // simple_timer.nx:64:8:$o
      wire [6:0] _42; // simple_timer.nx:64:26:$o
      wire [6:0] _43; // simple_timer.nx:64:8:$o
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
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:13:11" */
  wire [31:0] _72; // simple_timer.nx:14:42:$o
  wire [31:0] _73; // simple_timer.nx:14:34:$o
  /* "hex_trigger"; "simple_timer.nx:14:11" */
    wire [3:0] _74; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _75; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _76; // simple_timer.nx:75:52:$o
        wire _77; // simple_timer.nx:75:52:$o
        wire _78; // simple_timer.nx:75:52:$o
        wire _79; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _80; // simple_timer.nx:64:26:$o
      wire [6:0] _81; // simple_timer.nx:64:8:$o
      wire [6:0] _82; // simple_timer.nx:64:26:$o
      wire [6:0] _83; // simple_timer.nx:64:8:$o
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
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:14:11" */
  wire [31:0] _112; // simple_timer.nx:15:42:$o
  wire [31:0] _113; // simple_timer.nx:15:34:$o
  /* "hex_trigger"; "simple_timer.nx:15:11" */
    wire [3:0] _114; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _115; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _116; // simple_timer.nx:75:52:$o
        wire _117; // simple_timer.nx:75:52:$o
        wire _118; // simple_timer.nx:75:52:$o
        wire _119; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _120; // simple_timer.nx:64:26:$o
      wire [6:0] _121; // simple_timer.nx:64:8:$o
      wire [6:0] _122; // simple_timer.nx:64:26:$o
      wire [6:0] _123; // simple_timer.nx:64:8:$o
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
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:15:11" */
  wire [31:0] _152; // simple_timer.nx:16:42:$o
  wire [31:0] _153; // simple_timer.nx:16:34:$o
  /* "hex_trigger"; "simple_timer.nx:16:11" */
    wire [3:0] _154; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _155; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _156; // simple_timer.nx:75:52:$o
        wire _157; // simple_timer.nx:75:52:$o
        wire _158; // simple_timer.nx:75:52:$o
        wire _159; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _160; // simple_timer.nx:64:26:$o
      wire [6:0] _161; // simple_timer.nx:64:8:$o
      wire [6:0] _162; // simple_timer.nx:64:26:$o
      wire [6:0] _163; // simple_timer.nx:64:8:$o
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
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:16:11" */
  wire [31:0] _192; // simple_timer.nx:17:42:$o
  wire [31:0] _193; // simple_timer.nx:17:34:$o
  /* "hex_trigger"; "simple_timer.nx:17:11" */
    wire [3:0] _194; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _195; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _196; // simple_timer.nx:75:52:$o
        wire _197; // simple_timer.nx:75:52:$o
        wire _198; // simple_timer.nx:75:52:$o
        wire _199; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _200; // simple_timer.nx:64:26:$o
      wire [6:0] _201; // simple_timer.nx:64:8:$o
      wire [6:0] _202; // simple_timer.nx:64:26:$o
      wire [6:0] _203; // simple_timer.nx:64:8:$o
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
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:17:11" */
  wire [31:0] _232; // simple_timer.nx:18:42:$o
  wire [31:0] _233; // simple_timer.nx:18:34:$o
  /* "hex_trigger"; "simple_timer.nx:18:11" */
    wire [3:0] _234; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _235; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _236; // simple_timer.nx:75:52:$o
        wire _237; // simple_timer.nx:75:52:$o
        wire _238; // simple_timer.nx:75:52:$o
        wire _239; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _240; // simple_timer.nx:64:26:$o
      wire [6:0] _241; // simple_timer.nx:64:8:$o
      wire [6:0] _242; // simple_timer.nx:64:26:$o
      wire [6:0] _243; // simple_timer.nx:64:8:$o
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
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:18:11" */
  wire [31:0] _272; // simple_timer.nx:19:42:$o
  wire [31:0] _273; // simple_timer.nx:19:34:$o
  /* "hex_trigger"; "simple_timer.nx:19:11" */
    wire [3:0] _274; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _275; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _276; // simple_timer.nx:75:52:$o
        wire _277; // simple_timer.nx:75:52:$o
        wire _278; // simple_timer.nx:75:52:$o
        wire _279; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _280; // simple_timer.nx:64:26:$o
      wire [6:0] _281; // simple_timer.nx:64:8:$o
      wire [6:0] _282; // simple_timer.nx:64:26:$o
      wire [6:0] _283; // simple_timer.nx:64:8:$o
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
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:19:11" */
  wire [31:0] _312; // simple_timer.nx:20:42:$o
  wire [31:0] _313; // simple_timer.nx:20:34:$o
  /* "hex_trigger"; "simple_timer.nx:20:11" */
    wire [3:0] _314; // simple_timer.nx:34:6:num
    /* "MUX"; "simple_timer.nx:36:9" */
      wire [15:0] _315; // simple_timer.nx:60:9:mask
      /* "DECODER"; "simple_timer.nx:62:12" */
        wire _316; // simple_timer.nx:75:52:$o
        wire _317; // simple_timer.nx:75:52:$o
        wire _318; // simple_timer.nx:75:52:$o
        wire _319; // simple_timer.nx:75:52:$o
      /* END "DECODER"; "simple_timer.nx:62:12" */
      wire [6:0] _320; // simple_timer.nx:64:26:$o
      wire [6:0] _321; // simple_timer.nx:64:8:$o
      wire [6:0] _322; // simple_timer.nx:64:26:$o
      wire [6:0] _323; // simple_timer.nx:64:8:$o
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
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:20:11" */
/* END "top"; "simple_timer.nx:1:0" */

/*============================== CONNECTION ==============================*/

assign _11 = __reg__11;
assign _9 = __reg__9;
assign _10 = __reg__10;
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
    assign _12 = _11 == 249999; // simple_timer.nx:28:11
    assign _13 = _11 + 1; // simple_timer.nx:29:33
    /*
      COMPONENT "MUX"; "simple_timer.nx:29:17"
      input: code@12, input[0]@13, 0
      output: out@14
      attribute: { width: "1" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: code@12
        output: mask@15
        attribute: { width: "1" }
      */
        assign _15[0] = ~_12; // simple_timer.nx:75:52
        assign _15[1] = _12; // simple_timer.nx:62:12
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _16 = $signed(_15[0]); // simple_timer.nx:64:26
      assign _17 = _13 & _16; // simple_timer.nx:64:8
      assign _18 = $signed(_15[1]); // simple_timer.nx:64:26
      assign _19 = 0 & _18; // simple_timer.nx:64:8
      assign _14 = _17 | _19; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:29:17" */
    always @(posedge _0) __reg__11 <= _14; // simple_timer.nx:27:11
    assign _20 = ~_9; // simple_timer.nx:30:58
    /*
      COMPONENT "MUX"; "simple_timer.nx:30:27"
      input: code@12, ms_clk@9, input[1]@20
      output: out@21
      attribute: { width: "1" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: code@12
        output: mask@22
        attribute: { width: "1" }
      */
        assign _22[0] = ~_12; // simple_timer.nx:75:52
        assign _22[1] = _12; // simple_timer.nx:62:12
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _23 = _9 & _22[0]; // simple_timer.nx:64:8
      assign _24 = _20 & _22[1]; // simple_timer.nx:64:8
      assign _21 = _23 | _24; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:30:27" */
    always @(posedge _0) __reg__9 <= _21; // simple_timer.nx:27:11
  /* END "clock_divider"; "simple_timer.nx:10:11" */
  assign _25 = _10 == 8639999; // simple_timer.nx:11:33
  assign _26 = _10 + 1; // simple_timer.nx:11:66
  /*
    COMPONENT "MUX"; "simple_timer.nx:11:22"
    input: code@25, input[0]@26, 0
    output: out@27
    attribute: { width: "1" }
  */
    /*
      COMPONENT "DECODER"; "simple_timer.nx:62:12"
      input: code@25
      output: mask@28
      attribute: { width: "1" }
    */
      assign _28[0] = ~_25; // simple_timer.nx:75:52
      assign _28[1] = _25; // simple_timer.nx:62:12
    /* END "DECODER"; "simple_timer.nx:62:12" */
    assign _29 = $signed(_28[0]); // simple_timer.nx:64:26
    assign _30 = _26 & _29; // simple_timer.nx:64:8
    assign _31 = $signed(_28[1]); // simple_timer.nx:64:26
    assign _32 = 0 & _31; // simple_timer.nx:64:8
    assign _27 = _30 | _32; // simple_timer.nx:63:11
  /* END "MUX"; "simple_timer.nx:11:22" */
  always @(posedge _9) __reg__10 <= _27; // simple_timer.nx:5:16
  assign _33 = _10 % 10; // simple_timer.nx:13:34
  assign _34 = $signed(_33); // simple_timer.nx:13:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:13:11"
    input: num@34
    output: hex[0]@1
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@34, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[0]@1
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@34
        output: mask@35
        attribute: { width: "4" }
      */
        assign _36 = ~_34[0]; // simple_timer.nx:75:52
        assign _37 = ~_34[1]; // simple_timer.nx:75:52
        assign _38 = ~_34[2]; // simple_timer.nx:75:52
        assign _39 = ~_34[3]; // simple_timer.nx:75:52
        assign _35[0] = _36 & _37 & _38 & _39; // simple_timer.nx:73:18
        assign _35[1] = _34[0] & _37 & _38 & _39; // simple_timer.nx:73:18
        assign _35[2] = _36 & _34[1] & _38 & _39; // simple_timer.nx:73:18
        assign _35[3] = _34[0] & _34[1] & _38 & _39; // simple_timer.nx:73:18
        assign _35[4] = _36 & _37 & _34[2] & _39; // simple_timer.nx:73:18
        assign _35[5] = _34[0] & _37 & _34[2] & _39; // simple_timer.nx:73:18
        assign _35[6] = _36 & _34[1] & _34[2] & _39; // simple_timer.nx:73:18
        assign _35[7] = _34[0] & _34[1] & _34[2] & _39; // simple_timer.nx:73:18
        assign _35[8] = _36 & _37 & _38 & _34[3]; // simple_timer.nx:73:18
        assign _35[9] = _34[0] & _37 & _38 & _34[3]; // simple_timer.nx:73:18
        assign _35[10] = _36 & _34[1] & _38 & _34[3]; // simple_timer.nx:73:18
        assign _35[11] = _34[0] & _34[1] & _38 & _34[3]; // simple_timer.nx:73:18
        assign _35[12] = _36 & _37 & _34[2] & _34[3]; // simple_timer.nx:73:18
        assign _35[13] = _34[0] & _37 & _34[2] & _34[3]; // simple_timer.nx:73:18
        assign _35[14] = _36 & _34[1] & _34[2] & _34[3]; // simple_timer.nx:73:18
        assign _35[15] = _34[0] & _34[1] & _34[2] & _34[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _40 = $signed(_35[0]); // simple_timer.nx:64:26
      assign _41 = 7'h40 & _40; // simple_timer.nx:64:8
      assign _42 = $signed(_35[1]); // simple_timer.nx:64:26
      assign _43 = 7'h79 & _42; // simple_timer.nx:64:8
      assign _44 = $signed(_35[2]); // simple_timer.nx:64:26
      assign _45 = 7'h24 & _44; // simple_timer.nx:64:8
      assign _46 = $signed(_35[3]); // simple_timer.nx:64:26
      assign _47 = 7'h30 & _46; // simple_timer.nx:64:8
      assign _48 = $signed(_35[4]); // simple_timer.nx:64:26
      assign _49 = 7'h19 & _48; // simple_timer.nx:64:8
      assign _50 = $signed(_35[5]); // simple_timer.nx:64:26
      assign _51 = 7'h12 & _50; // simple_timer.nx:64:8
      assign _52 = $signed(_35[6]); // simple_timer.nx:64:26
      assign _53 = 7'h2 & _52; // simple_timer.nx:64:8
      assign _54 = $signed(_35[7]); // simple_timer.nx:64:26
      assign _55 = 7'h78 & _54; // simple_timer.nx:64:8
      assign _56 = $signed(_35[8]); // simple_timer.nx:64:26
      assign _57 = 7'h0 & _56; // simple_timer.nx:64:8
      assign _58 = $signed(_35[9]); // simple_timer.nx:64:26
      assign _59 = 7'h10 & _58; // simple_timer.nx:64:8
      assign _60 = $signed(_35[10]); // simple_timer.nx:64:26
      assign _61 = 7'h8 & _60; // simple_timer.nx:64:8
      assign _62 = $signed(_35[11]); // simple_timer.nx:64:26
      assign _63 = 7'h3 & _62; // simple_timer.nx:64:8
      assign _64 = $signed(_35[12]); // simple_timer.nx:64:26
      assign _65 = 7'h46 & _64; // simple_timer.nx:64:8
      assign _66 = $signed(_35[13]); // simple_timer.nx:64:26
      assign _67 = 7'h21 & _66; // simple_timer.nx:64:8
      assign _68 = $signed(_35[14]); // simple_timer.nx:64:26
      assign _69 = 7'h6 & _68; // simple_timer.nx:64:8
      assign _70 = $signed(_35[15]); // simple_timer.nx:64:26
      assign _71 = 7'hE & _70; // simple_timer.nx:64:8
      assign _1 = _41 | _43 | _45 | _47 | _49 | _51 | _53 | _55 | _57 | _59 | _61 | _63 | _65 | _67 | _69 | _71; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:13:11" */
  assign _72 = _10 % 100; // simple_timer.nx:14:42
  assign _73 = _72 / 10; // simple_timer.nx:14:34
  assign _74 = $signed(_73); // simple_timer.nx:14:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:14:11"
    input: num@74
    output: hex[1]@2
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@74, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[1]@2
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@74
        output: mask@75
        attribute: { width: "4" }
      */
        assign _76 = ~_74[0]; // simple_timer.nx:75:52
        assign _77 = ~_74[1]; // simple_timer.nx:75:52
        assign _78 = ~_74[2]; // simple_timer.nx:75:52
        assign _79 = ~_74[3]; // simple_timer.nx:75:52
        assign _75[0] = _76 & _77 & _78 & _79; // simple_timer.nx:73:18
        assign _75[1] = _74[0] & _77 & _78 & _79; // simple_timer.nx:73:18
        assign _75[2] = _76 & _74[1] & _78 & _79; // simple_timer.nx:73:18
        assign _75[3] = _74[0] & _74[1] & _78 & _79; // simple_timer.nx:73:18
        assign _75[4] = _76 & _77 & _74[2] & _79; // simple_timer.nx:73:18
        assign _75[5] = _74[0] & _77 & _74[2] & _79; // simple_timer.nx:73:18
        assign _75[6] = _76 & _74[1] & _74[2] & _79; // simple_timer.nx:73:18
        assign _75[7] = _74[0] & _74[1] & _74[2] & _79; // simple_timer.nx:73:18
        assign _75[8] = _76 & _77 & _78 & _74[3]; // simple_timer.nx:73:18
        assign _75[9] = _74[0] & _77 & _78 & _74[3]; // simple_timer.nx:73:18
        assign _75[10] = _76 & _74[1] & _78 & _74[3]; // simple_timer.nx:73:18
        assign _75[11] = _74[0] & _74[1] & _78 & _74[3]; // simple_timer.nx:73:18
        assign _75[12] = _76 & _77 & _74[2] & _74[3]; // simple_timer.nx:73:18
        assign _75[13] = _74[0] & _77 & _74[2] & _74[3]; // simple_timer.nx:73:18
        assign _75[14] = _76 & _74[1] & _74[2] & _74[3]; // simple_timer.nx:73:18
        assign _75[15] = _74[0] & _74[1] & _74[2] & _74[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _80 = $signed(_75[0]); // simple_timer.nx:64:26
      assign _81 = 7'h40 & _80; // simple_timer.nx:64:8
      assign _82 = $signed(_75[1]); // simple_timer.nx:64:26
      assign _83 = 7'h79 & _82; // simple_timer.nx:64:8
      assign _84 = $signed(_75[2]); // simple_timer.nx:64:26
      assign _85 = 7'h24 & _84; // simple_timer.nx:64:8
      assign _86 = $signed(_75[3]); // simple_timer.nx:64:26
      assign _87 = 7'h30 & _86; // simple_timer.nx:64:8
      assign _88 = $signed(_75[4]); // simple_timer.nx:64:26
      assign _89 = 7'h19 & _88; // simple_timer.nx:64:8
      assign _90 = $signed(_75[5]); // simple_timer.nx:64:26
      assign _91 = 7'h12 & _90; // simple_timer.nx:64:8
      assign _92 = $signed(_75[6]); // simple_timer.nx:64:26
      assign _93 = 7'h2 & _92; // simple_timer.nx:64:8
      assign _94 = $signed(_75[7]); // simple_timer.nx:64:26
      assign _95 = 7'h78 & _94; // simple_timer.nx:64:8
      assign _96 = $signed(_75[8]); // simple_timer.nx:64:26
      assign _97 = 7'h0 & _96; // simple_timer.nx:64:8
      assign _98 = $signed(_75[9]); // simple_timer.nx:64:26
      assign _99 = 7'h10 & _98; // simple_timer.nx:64:8
      assign _100 = $signed(_75[10]); // simple_timer.nx:64:26
      assign _101 = 7'h8 & _100; // simple_timer.nx:64:8
      assign _102 = $signed(_75[11]); // simple_timer.nx:64:26
      assign _103 = 7'h3 & _102; // simple_timer.nx:64:8
      assign _104 = $signed(_75[12]); // simple_timer.nx:64:26
      assign _105 = 7'h46 & _104; // simple_timer.nx:64:8
      assign _106 = $signed(_75[13]); // simple_timer.nx:64:26
      assign _107 = 7'h21 & _106; // simple_timer.nx:64:8
      assign _108 = $signed(_75[14]); // simple_timer.nx:64:26
      assign _109 = 7'h6 & _108; // simple_timer.nx:64:8
      assign _110 = $signed(_75[15]); // simple_timer.nx:64:26
      assign _111 = 7'hE & _110; // simple_timer.nx:64:8
      assign _2 = _81 | _83 | _85 | _87 | _89 | _91 | _93 | _95 | _97 | _99 | _101 | _103 | _105 | _107 | _109 | _111; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:14:11" */
  assign _112 = _10 % 1000; // simple_timer.nx:15:42
  assign _113 = _112 / 100; // simple_timer.nx:15:34
  assign _114 = $signed(_113); // simple_timer.nx:15:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:15:11"
    input: num@114
    output: hex[2]@3
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@114, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[2]@3
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@114
        output: mask@115
        attribute: { width: "4" }
      */
        assign _116 = ~_114[0]; // simple_timer.nx:75:52
        assign _117 = ~_114[1]; // simple_timer.nx:75:52
        assign _118 = ~_114[2]; // simple_timer.nx:75:52
        assign _119 = ~_114[3]; // simple_timer.nx:75:52
        assign _115[0] = _116 & _117 & _118 & _119; // simple_timer.nx:73:18
        assign _115[1] = _114[0] & _117 & _118 & _119; // simple_timer.nx:73:18
        assign _115[2] = _116 & _114[1] & _118 & _119; // simple_timer.nx:73:18
        assign _115[3] = _114[0] & _114[1] & _118 & _119; // simple_timer.nx:73:18
        assign _115[4] = _116 & _117 & _114[2] & _119; // simple_timer.nx:73:18
        assign _115[5] = _114[0] & _117 & _114[2] & _119; // simple_timer.nx:73:18
        assign _115[6] = _116 & _114[1] & _114[2] & _119; // simple_timer.nx:73:18
        assign _115[7] = _114[0] & _114[1] & _114[2] & _119; // simple_timer.nx:73:18
        assign _115[8] = _116 & _117 & _118 & _114[3]; // simple_timer.nx:73:18
        assign _115[9] = _114[0] & _117 & _118 & _114[3]; // simple_timer.nx:73:18
        assign _115[10] = _116 & _114[1] & _118 & _114[3]; // simple_timer.nx:73:18
        assign _115[11] = _114[0] & _114[1] & _118 & _114[3]; // simple_timer.nx:73:18
        assign _115[12] = _116 & _117 & _114[2] & _114[3]; // simple_timer.nx:73:18
        assign _115[13] = _114[0] & _117 & _114[2] & _114[3]; // simple_timer.nx:73:18
        assign _115[14] = _116 & _114[1] & _114[2] & _114[3]; // simple_timer.nx:73:18
        assign _115[15] = _114[0] & _114[1] & _114[2] & _114[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _120 = $signed(_115[0]); // simple_timer.nx:64:26
      assign _121 = 7'h40 & _120; // simple_timer.nx:64:8
      assign _122 = $signed(_115[1]); // simple_timer.nx:64:26
      assign _123 = 7'h79 & _122; // simple_timer.nx:64:8
      assign _124 = $signed(_115[2]); // simple_timer.nx:64:26
      assign _125 = 7'h24 & _124; // simple_timer.nx:64:8
      assign _126 = $signed(_115[3]); // simple_timer.nx:64:26
      assign _127 = 7'h30 & _126; // simple_timer.nx:64:8
      assign _128 = $signed(_115[4]); // simple_timer.nx:64:26
      assign _129 = 7'h19 & _128; // simple_timer.nx:64:8
      assign _130 = $signed(_115[5]); // simple_timer.nx:64:26
      assign _131 = 7'h12 & _130; // simple_timer.nx:64:8
      assign _132 = $signed(_115[6]); // simple_timer.nx:64:26
      assign _133 = 7'h2 & _132; // simple_timer.nx:64:8
      assign _134 = $signed(_115[7]); // simple_timer.nx:64:26
      assign _135 = 7'h78 & _134; // simple_timer.nx:64:8
      assign _136 = $signed(_115[8]); // simple_timer.nx:64:26
      assign _137 = 7'h0 & _136; // simple_timer.nx:64:8
      assign _138 = $signed(_115[9]); // simple_timer.nx:64:26
      assign _139 = 7'h10 & _138; // simple_timer.nx:64:8
      assign _140 = $signed(_115[10]); // simple_timer.nx:64:26
      assign _141 = 7'h8 & _140; // simple_timer.nx:64:8
      assign _142 = $signed(_115[11]); // simple_timer.nx:64:26
      assign _143 = 7'h3 & _142; // simple_timer.nx:64:8
      assign _144 = $signed(_115[12]); // simple_timer.nx:64:26
      assign _145 = 7'h46 & _144; // simple_timer.nx:64:8
      assign _146 = $signed(_115[13]); // simple_timer.nx:64:26
      assign _147 = 7'h21 & _146; // simple_timer.nx:64:8
      assign _148 = $signed(_115[14]); // simple_timer.nx:64:26
      assign _149 = 7'h6 & _148; // simple_timer.nx:64:8
      assign _150 = $signed(_115[15]); // simple_timer.nx:64:26
      assign _151 = 7'hE & _150; // simple_timer.nx:64:8
      assign _3 = _121 | _123 | _125 | _127 | _129 | _131 | _133 | _135 | _137 | _139 | _141 | _143 | _145 | _147 | _149 | _151; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:15:11" */
  assign _152 = _10 % 6000; // simple_timer.nx:16:42
  assign _153 = _152 / 1000; // simple_timer.nx:16:34
  assign _154 = $signed(_153); // simple_timer.nx:16:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:16:11"
    input: num@154
    output: hex[3]@4
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@154, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[3]@4
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@154
        output: mask@155
        attribute: { width: "4" }
      */
        assign _156 = ~_154[0]; // simple_timer.nx:75:52
        assign _157 = ~_154[1]; // simple_timer.nx:75:52
        assign _158 = ~_154[2]; // simple_timer.nx:75:52
        assign _159 = ~_154[3]; // simple_timer.nx:75:52
        assign _155[0] = _156 & _157 & _158 & _159; // simple_timer.nx:73:18
        assign _155[1] = _154[0] & _157 & _158 & _159; // simple_timer.nx:73:18
        assign _155[2] = _156 & _154[1] & _158 & _159; // simple_timer.nx:73:18
        assign _155[3] = _154[0] & _154[1] & _158 & _159; // simple_timer.nx:73:18
        assign _155[4] = _156 & _157 & _154[2] & _159; // simple_timer.nx:73:18
        assign _155[5] = _154[0] & _157 & _154[2] & _159; // simple_timer.nx:73:18
        assign _155[6] = _156 & _154[1] & _154[2] & _159; // simple_timer.nx:73:18
        assign _155[7] = _154[0] & _154[1] & _154[2] & _159; // simple_timer.nx:73:18
        assign _155[8] = _156 & _157 & _158 & _154[3]; // simple_timer.nx:73:18
        assign _155[9] = _154[0] & _157 & _158 & _154[3]; // simple_timer.nx:73:18
        assign _155[10] = _156 & _154[1] & _158 & _154[3]; // simple_timer.nx:73:18
        assign _155[11] = _154[0] & _154[1] & _158 & _154[3]; // simple_timer.nx:73:18
        assign _155[12] = _156 & _157 & _154[2] & _154[3]; // simple_timer.nx:73:18
        assign _155[13] = _154[0] & _157 & _154[2] & _154[3]; // simple_timer.nx:73:18
        assign _155[14] = _156 & _154[1] & _154[2] & _154[3]; // simple_timer.nx:73:18
        assign _155[15] = _154[0] & _154[1] & _154[2] & _154[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _160 = $signed(_155[0]); // simple_timer.nx:64:26
      assign _161 = 7'h40 & _160; // simple_timer.nx:64:8
      assign _162 = $signed(_155[1]); // simple_timer.nx:64:26
      assign _163 = 7'h79 & _162; // simple_timer.nx:64:8
      assign _164 = $signed(_155[2]); // simple_timer.nx:64:26
      assign _165 = 7'h24 & _164; // simple_timer.nx:64:8
      assign _166 = $signed(_155[3]); // simple_timer.nx:64:26
      assign _167 = 7'h30 & _166; // simple_timer.nx:64:8
      assign _168 = $signed(_155[4]); // simple_timer.nx:64:26
      assign _169 = 7'h19 & _168; // simple_timer.nx:64:8
      assign _170 = $signed(_155[5]); // simple_timer.nx:64:26
      assign _171 = 7'h12 & _170; // simple_timer.nx:64:8
      assign _172 = $signed(_155[6]); // simple_timer.nx:64:26
      assign _173 = 7'h2 & _172; // simple_timer.nx:64:8
      assign _174 = $signed(_155[7]); // simple_timer.nx:64:26
      assign _175 = 7'h78 & _174; // simple_timer.nx:64:8
      assign _176 = $signed(_155[8]); // simple_timer.nx:64:26
      assign _177 = 7'h0 & _176; // simple_timer.nx:64:8
      assign _178 = $signed(_155[9]); // simple_timer.nx:64:26
      assign _179 = 7'h10 & _178; // simple_timer.nx:64:8
      assign _180 = $signed(_155[10]); // simple_timer.nx:64:26
      assign _181 = 7'h8 & _180; // simple_timer.nx:64:8
      assign _182 = $signed(_155[11]); // simple_timer.nx:64:26
      assign _183 = 7'h3 & _182; // simple_timer.nx:64:8
      assign _184 = $signed(_155[12]); // simple_timer.nx:64:26
      assign _185 = 7'h46 & _184; // simple_timer.nx:64:8
      assign _186 = $signed(_155[13]); // simple_timer.nx:64:26
      assign _187 = 7'h21 & _186; // simple_timer.nx:64:8
      assign _188 = $signed(_155[14]); // simple_timer.nx:64:26
      assign _189 = 7'h6 & _188; // simple_timer.nx:64:8
      assign _190 = $signed(_155[15]); // simple_timer.nx:64:26
      assign _191 = 7'hE & _190; // simple_timer.nx:64:8
      assign _4 = _161 | _163 | _165 | _167 | _169 | _171 | _173 | _175 | _177 | _179 | _181 | _183 | _185 | _187 | _189 | _191; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:16:11" */
  assign _192 = _10 % 60000; // simple_timer.nx:17:42
  assign _193 = _192 / 6000; // simple_timer.nx:17:34
  assign _194 = $signed(_193); // simple_timer.nx:17:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:17:11"
    input: num@194
    output: hex[4]@5
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@194, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[4]@5
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@194
        output: mask@195
        attribute: { width: "4" }
      */
        assign _196 = ~_194[0]; // simple_timer.nx:75:52
        assign _197 = ~_194[1]; // simple_timer.nx:75:52
        assign _198 = ~_194[2]; // simple_timer.nx:75:52
        assign _199 = ~_194[3]; // simple_timer.nx:75:52
        assign _195[0] = _196 & _197 & _198 & _199; // simple_timer.nx:73:18
        assign _195[1] = _194[0] & _197 & _198 & _199; // simple_timer.nx:73:18
        assign _195[2] = _196 & _194[1] & _198 & _199; // simple_timer.nx:73:18
        assign _195[3] = _194[0] & _194[1] & _198 & _199; // simple_timer.nx:73:18
        assign _195[4] = _196 & _197 & _194[2] & _199; // simple_timer.nx:73:18
        assign _195[5] = _194[0] & _197 & _194[2] & _199; // simple_timer.nx:73:18
        assign _195[6] = _196 & _194[1] & _194[2] & _199; // simple_timer.nx:73:18
        assign _195[7] = _194[0] & _194[1] & _194[2] & _199; // simple_timer.nx:73:18
        assign _195[8] = _196 & _197 & _198 & _194[3]; // simple_timer.nx:73:18
        assign _195[9] = _194[0] & _197 & _198 & _194[3]; // simple_timer.nx:73:18
        assign _195[10] = _196 & _194[1] & _198 & _194[3]; // simple_timer.nx:73:18
        assign _195[11] = _194[0] & _194[1] & _198 & _194[3]; // simple_timer.nx:73:18
        assign _195[12] = _196 & _197 & _194[2] & _194[3]; // simple_timer.nx:73:18
        assign _195[13] = _194[0] & _197 & _194[2] & _194[3]; // simple_timer.nx:73:18
        assign _195[14] = _196 & _194[1] & _194[2] & _194[3]; // simple_timer.nx:73:18
        assign _195[15] = _194[0] & _194[1] & _194[2] & _194[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _200 = $signed(_195[0]); // simple_timer.nx:64:26
      assign _201 = 7'h40 & _200; // simple_timer.nx:64:8
      assign _202 = $signed(_195[1]); // simple_timer.nx:64:26
      assign _203 = 7'h79 & _202; // simple_timer.nx:64:8
      assign _204 = $signed(_195[2]); // simple_timer.nx:64:26
      assign _205 = 7'h24 & _204; // simple_timer.nx:64:8
      assign _206 = $signed(_195[3]); // simple_timer.nx:64:26
      assign _207 = 7'h30 & _206; // simple_timer.nx:64:8
      assign _208 = $signed(_195[4]); // simple_timer.nx:64:26
      assign _209 = 7'h19 & _208; // simple_timer.nx:64:8
      assign _210 = $signed(_195[5]); // simple_timer.nx:64:26
      assign _211 = 7'h12 & _210; // simple_timer.nx:64:8
      assign _212 = $signed(_195[6]); // simple_timer.nx:64:26
      assign _213 = 7'h2 & _212; // simple_timer.nx:64:8
      assign _214 = $signed(_195[7]); // simple_timer.nx:64:26
      assign _215 = 7'h78 & _214; // simple_timer.nx:64:8
      assign _216 = $signed(_195[8]); // simple_timer.nx:64:26
      assign _217 = 7'h0 & _216; // simple_timer.nx:64:8
      assign _218 = $signed(_195[9]); // simple_timer.nx:64:26
      assign _219 = 7'h10 & _218; // simple_timer.nx:64:8
      assign _220 = $signed(_195[10]); // simple_timer.nx:64:26
      assign _221 = 7'h8 & _220; // simple_timer.nx:64:8
      assign _222 = $signed(_195[11]); // simple_timer.nx:64:26
      assign _223 = 7'h3 & _222; // simple_timer.nx:64:8
      assign _224 = $signed(_195[12]); // simple_timer.nx:64:26
      assign _225 = 7'h46 & _224; // simple_timer.nx:64:8
      assign _226 = $signed(_195[13]); // simple_timer.nx:64:26
      assign _227 = 7'h21 & _226; // simple_timer.nx:64:8
      assign _228 = $signed(_195[14]); // simple_timer.nx:64:26
      assign _229 = 7'h6 & _228; // simple_timer.nx:64:8
      assign _230 = $signed(_195[15]); // simple_timer.nx:64:26
      assign _231 = 7'hE & _230; // simple_timer.nx:64:8
      assign _5 = _201 | _203 | _205 | _207 | _209 | _211 | _213 | _215 | _217 | _219 | _221 | _223 | _225 | _227 | _229 | _231; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:17:11" */
  assign _232 = _10 % 360000; // simple_timer.nx:18:42
  assign _233 = _232 / 60000; // simple_timer.nx:18:34
  assign _234 = $signed(_233); // simple_timer.nx:18:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:18:11"
    input: num@234
    output: hex[5]@6
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@234, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[5]@6
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@234
        output: mask@235
        attribute: { width: "4" }
      */
        assign _236 = ~_234[0]; // simple_timer.nx:75:52
        assign _237 = ~_234[1]; // simple_timer.nx:75:52
        assign _238 = ~_234[2]; // simple_timer.nx:75:52
        assign _239 = ~_234[3]; // simple_timer.nx:75:52
        assign _235[0] = _236 & _237 & _238 & _239; // simple_timer.nx:73:18
        assign _235[1] = _234[0] & _237 & _238 & _239; // simple_timer.nx:73:18
        assign _235[2] = _236 & _234[1] & _238 & _239; // simple_timer.nx:73:18
        assign _235[3] = _234[0] & _234[1] & _238 & _239; // simple_timer.nx:73:18
        assign _235[4] = _236 & _237 & _234[2] & _239; // simple_timer.nx:73:18
        assign _235[5] = _234[0] & _237 & _234[2] & _239; // simple_timer.nx:73:18
        assign _235[6] = _236 & _234[1] & _234[2] & _239; // simple_timer.nx:73:18
        assign _235[7] = _234[0] & _234[1] & _234[2] & _239; // simple_timer.nx:73:18
        assign _235[8] = _236 & _237 & _238 & _234[3]; // simple_timer.nx:73:18
        assign _235[9] = _234[0] & _237 & _238 & _234[3]; // simple_timer.nx:73:18
        assign _235[10] = _236 & _234[1] & _238 & _234[3]; // simple_timer.nx:73:18
        assign _235[11] = _234[0] & _234[1] & _238 & _234[3]; // simple_timer.nx:73:18
        assign _235[12] = _236 & _237 & _234[2] & _234[3]; // simple_timer.nx:73:18
        assign _235[13] = _234[0] & _237 & _234[2] & _234[3]; // simple_timer.nx:73:18
        assign _235[14] = _236 & _234[1] & _234[2] & _234[3]; // simple_timer.nx:73:18
        assign _235[15] = _234[0] & _234[1] & _234[2] & _234[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _240 = $signed(_235[0]); // simple_timer.nx:64:26
      assign _241 = 7'h40 & _240; // simple_timer.nx:64:8
      assign _242 = $signed(_235[1]); // simple_timer.nx:64:26
      assign _243 = 7'h79 & _242; // simple_timer.nx:64:8
      assign _244 = $signed(_235[2]); // simple_timer.nx:64:26
      assign _245 = 7'h24 & _244; // simple_timer.nx:64:8
      assign _246 = $signed(_235[3]); // simple_timer.nx:64:26
      assign _247 = 7'h30 & _246; // simple_timer.nx:64:8
      assign _248 = $signed(_235[4]); // simple_timer.nx:64:26
      assign _249 = 7'h19 & _248; // simple_timer.nx:64:8
      assign _250 = $signed(_235[5]); // simple_timer.nx:64:26
      assign _251 = 7'h12 & _250; // simple_timer.nx:64:8
      assign _252 = $signed(_235[6]); // simple_timer.nx:64:26
      assign _253 = 7'h2 & _252; // simple_timer.nx:64:8
      assign _254 = $signed(_235[7]); // simple_timer.nx:64:26
      assign _255 = 7'h78 & _254; // simple_timer.nx:64:8
      assign _256 = $signed(_235[8]); // simple_timer.nx:64:26
      assign _257 = 7'h0 & _256; // simple_timer.nx:64:8
      assign _258 = $signed(_235[9]); // simple_timer.nx:64:26
      assign _259 = 7'h10 & _258; // simple_timer.nx:64:8
      assign _260 = $signed(_235[10]); // simple_timer.nx:64:26
      assign _261 = 7'h8 & _260; // simple_timer.nx:64:8
      assign _262 = $signed(_235[11]); // simple_timer.nx:64:26
      assign _263 = 7'h3 & _262; // simple_timer.nx:64:8
      assign _264 = $signed(_235[12]); // simple_timer.nx:64:26
      assign _265 = 7'h46 & _264; // simple_timer.nx:64:8
      assign _266 = $signed(_235[13]); // simple_timer.nx:64:26
      assign _267 = 7'h21 & _266; // simple_timer.nx:64:8
      assign _268 = $signed(_235[14]); // simple_timer.nx:64:26
      assign _269 = 7'h6 & _268; // simple_timer.nx:64:8
      assign _270 = $signed(_235[15]); // simple_timer.nx:64:26
      assign _271 = 7'hE & _270; // simple_timer.nx:64:8
      assign _6 = _241 | _243 | _245 | _247 | _249 | _251 | _253 | _255 | _257 | _259 | _261 | _263 | _265 | _267 | _269 | _271; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:18:11" */
  assign _272 = _10 % 3600000; // simple_timer.nx:19:42
  assign _273 = _272 / 360000; // simple_timer.nx:19:34
  assign _274 = $signed(_273); // simple_timer.nx:19:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:19:11"
    input: num@274
    output: hex[6]@7
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@274, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[6]@7
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@274
        output: mask@275
        attribute: { width: "4" }
      */
        assign _276 = ~_274[0]; // simple_timer.nx:75:52
        assign _277 = ~_274[1]; // simple_timer.nx:75:52
        assign _278 = ~_274[2]; // simple_timer.nx:75:52
        assign _279 = ~_274[3]; // simple_timer.nx:75:52
        assign _275[0] = _276 & _277 & _278 & _279; // simple_timer.nx:73:18
        assign _275[1] = _274[0] & _277 & _278 & _279; // simple_timer.nx:73:18
        assign _275[2] = _276 & _274[1] & _278 & _279; // simple_timer.nx:73:18
        assign _275[3] = _274[0] & _274[1] & _278 & _279; // simple_timer.nx:73:18
        assign _275[4] = _276 & _277 & _274[2] & _279; // simple_timer.nx:73:18
        assign _275[5] = _274[0] & _277 & _274[2] & _279; // simple_timer.nx:73:18
        assign _275[6] = _276 & _274[1] & _274[2] & _279; // simple_timer.nx:73:18
        assign _275[7] = _274[0] & _274[1] & _274[2] & _279; // simple_timer.nx:73:18
        assign _275[8] = _276 & _277 & _278 & _274[3]; // simple_timer.nx:73:18
        assign _275[9] = _274[0] & _277 & _278 & _274[3]; // simple_timer.nx:73:18
        assign _275[10] = _276 & _274[1] & _278 & _274[3]; // simple_timer.nx:73:18
        assign _275[11] = _274[0] & _274[1] & _278 & _274[3]; // simple_timer.nx:73:18
        assign _275[12] = _276 & _277 & _274[2] & _274[3]; // simple_timer.nx:73:18
        assign _275[13] = _274[0] & _277 & _274[2] & _274[3]; // simple_timer.nx:73:18
        assign _275[14] = _276 & _274[1] & _274[2] & _274[3]; // simple_timer.nx:73:18
        assign _275[15] = _274[0] & _274[1] & _274[2] & _274[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _280 = $signed(_275[0]); // simple_timer.nx:64:26
      assign _281 = 7'h40 & _280; // simple_timer.nx:64:8
      assign _282 = $signed(_275[1]); // simple_timer.nx:64:26
      assign _283 = 7'h79 & _282; // simple_timer.nx:64:8
      assign _284 = $signed(_275[2]); // simple_timer.nx:64:26
      assign _285 = 7'h24 & _284; // simple_timer.nx:64:8
      assign _286 = $signed(_275[3]); // simple_timer.nx:64:26
      assign _287 = 7'h30 & _286; // simple_timer.nx:64:8
      assign _288 = $signed(_275[4]); // simple_timer.nx:64:26
      assign _289 = 7'h19 & _288; // simple_timer.nx:64:8
      assign _290 = $signed(_275[5]); // simple_timer.nx:64:26
      assign _291 = 7'h12 & _290; // simple_timer.nx:64:8
      assign _292 = $signed(_275[6]); // simple_timer.nx:64:26
      assign _293 = 7'h2 & _292; // simple_timer.nx:64:8
      assign _294 = $signed(_275[7]); // simple_timer.nx:64:26
      assign _295 = 7'h78 & _294; // simple_timer.nx:64:8
      assign _296 = $signed(_275[8]); // simple_timer.nx:64:26
      assign _297 = 7'h0 & _296; // simple_timer.nx:64:8
      assign _298 = $signed(_275[9]); // simple_timer.nx:64:26
      assign _299 = 7'h10 & _298; // simple_timer.nx:64:8
      assign _300 = $signed(_275[10]); // simple_timer.nx:64:26
      assign _301 = 7'h8 & _300; // simple_timer.nx:64:8
      assign _302 = $signed(_275[11]); // simple_timer.nx:64:26
      assign _303 = 7'h3 & _302; // simple_timer.nx:64:8
      assign _304 = $signed(_275[12]); // simple_timer.nx:64:26
      assign _305 = 7'h46 & _304; // simple_timer.nx:64:8
      assign _306 = $signed(_275[13]); // simple_timer.nx:64:26
      assign _307 = 7'h21 & _306; // simple_timer.nx:64:8
      assign _308 = $signed(_275[14]); // simple_timer.nx:64:26
      assign _309 = 7'h6 & _308; // simple_timer.nx:64:8
      assign _310 = $signed(_275[15]); // simple_timer.nx:64:26
      assign _311 = 7'hE & _310; // simple_timer.nx:64:8
      assign _7 = _281 | _283 | _285 | _287 | _289 | _291 | _293 | _295 | _297 | _299 | _301 | _303 | _305 | _307 | _309 | _311; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:19:11" */
  assign _312 = _10 % 86400000; // simple_timer.nx:20:42
  assign _313 = _312 / 3600000; // simple_timer.nx:20:34
  assign _314 = $signed(_313); // simple_timer.nx:20:26
  /*
    COMPONENT "hex_trigger"; "simple_timer.nx:20:11"
    input: num@314
    output: hex[7]@8
  */
    /*
      COMPONENT "MUX"; "simple_timer.nx:36:9"
      input: num@314, 7'h40, 7'h79, 7'h24, 7'h30, 7'h19, 7'h12, 7'h2, 7'h78, 7'h0, 7'h10, 7'h8, 7'h3, 7'h46, 7'h21, 7'h6, 7'hE
      output: hex[7]@8
      attribute: { width: "4" }
    */
      /*
        COMPONENT "DECODER"; "simple_timer.nx:62:12"
        input: num@314
        output: mask@315
        attribute: { width: "4" }
      */
        assign _316 = ~_314[0]; // simple_timer.nx:75:52
        assign _317 = ~_314[1]; // simple_timer.nx:75:52
        assign _318 = ~_314[2]; // simple_timer.nx:75:52
        assign _319 = ~_314[3]; // simple_timer.nx:75:52
        assign _315[0] = _316 & _317 & _318 & _319; // simple_timer.nx:73:18
        assign _315[1] = _314[0] & _317 & _318 & _319; // simple_timer.nx:73:18
        assign _315[2] = _316 & _314[1] & _318 & _319; // simple_timer.nx:73:18
        assign _315[3] = _314[0] & _314[1] & _318 & _319; // simple_timer.nx:73:18
        assign _315[4] = _316 & _317 & _314[2] & _319; // simple_timer.nx:73:18
        assign _315[5] = _314[0] & _317 & _314[2] & _319; // simple_timer.nx:73:18
        assign _315[6] = _316 & _314[1] & _314[2] & _319; // simple_timer.nx:73:18
        assign _315[7] = _314[0] & _314[1] & _314[2] & _319; // simple_timer.nx:73:18
        assign _315[8] = _316 & _317 & _318 & _314[3]; // simple_timer.nx:73:18
        assign _315[9] = _314[0] & _317 & _318 & _314[3]; // simple_timer.nx:73:18
        assign _315[10] = _316 & _314[1] & _318 & _314[3]; // simple_timer.nx:73:18
        assign _315[11] = _314[0] & _314[1] & _318 & _314[3]; // simple_timer.nx:73:18
        assign _315[12] = _316 & _317 & _314[2] & _314[3]; // simple_timer.nx:73:18
        assign _315[13] = _314[0] & _317 & _314[2] & _314[3]; // simple_timer.nx:73:18
        assign _315[14] = _316 & _314[1] & _314[2] & _314[3]; // simple_timer.nx:73:18
        assign _315[15] = _314[0] & _314[1] & _314[2] & _314[3]; // simple_timer.nx:73:18
      /* END "DECODER"; "simple_timer.nx:62:12" */
      assign _320 = $signed(_315[0]); // simple_timer.nx:64:26
      assign _321 = 7'h40 & _320; // simple_timer.nx:64:8
      assign _322 = $signed(_315[1]); // simple_timer.nx:64:26
      assign _323 = 7'h79 & _322; // simple_timer.nx:64:8
      assign _324 = $signed(_315[2]); // simple_timer.nx:64:26
      assign _325 = 7'h24 & _324; // simple_timer.nx:64:8
      assign _326 = $signed(_315[3]); // simple_timer.nx:64:26
      assign _327 = 7'h30 & _326; // simple_timer.nx:64:8
      assign _328 = $signed(_315[4]); // simple_timer.nx:64:26
      assign _329 = 7'h19 & _328; // simple_timer.nx:64:8
      assign _330 = $signed(_315[5]); // simple_timer.nx:64:26
      assign _331 = 7'h12 & _330; // simple_timer.nx:64:8
      assign _332 = $signed(_315[6]); // simple_timer.nx:64:26
      assign _333 = 7'h2 & _332; // simple_timer.nx:64:8
      assign _334 = $signed(_315[7]); // simple_timer.nx:64:26
      assign _335 = 7'h78 & _334; // simple_timer.nx:64:8
      assign _336 = $signed(_315[8]); // simple_timer.nx:64:26
      assign _337 = 7'h0 & _336; // simple_timer.nx:64:8
      assign _338 = $signed(_315[9]); // simple_timer.nx:64:26
      assign _339 = 7'h10 & _338; // simple_timer.nx:64:8
      assign _340 = $signed(_315[10]); // simple_timer.nx:64:26
      assign _341 = 7'h8 & _340; // simple_timer.nx:64:8
      assign _342 = $signed(_315[11]); // simple_timer.nx:64:26
      assign _343 = 7'h3 & _342; // simple_timer.nx:64:8
      assign _344 = $signed(_315[12]); // simple_timer.nx:64:26
      assign _345 = 7'h46 & _344; // simple_timer.nx:64:8
      assign _346 = $signed(_315[13]); // simple_timer.nx:64:26
      assign _347 = 7'h21 & _346; // simple_timer.nx:64:8
      assign _348 = $signed(_315[14]); // simple_timer.nx:64:26
      assign _349 = 7'h6 & _348; // simple_timer.nx:64:8
      assign _350 = $signed(_315[15]); // simple_timer.nx:64:26
      assign _351 = 7'hE & _350; // simple_timer.nx:64:8
      assign _8 = _321 | _323 | _325 | _327 | _329 | _331 | _333 | _335 | _337 | _339 | _341 | _343 | _345 | _347 | _349 | _351; // simple_timer.nx:63:11
    /* END "MUX"; "simple_timer.nx:36:9" */
  /* END "hex_trigger"; "simple_timer.nx:20:11" */
/* END "top"; "simple_timer.nx:1:0" */
endmodule
