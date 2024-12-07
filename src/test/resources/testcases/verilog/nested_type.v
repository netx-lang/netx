// @(12:10)
module top_1(
  d, \$c_0 , \$c_1 , \b.b[0].left , \b.b[0].right , \b.b[1].left , \b.b[1].right , \b.b[2].left , \b.b[2].right , \b.b[3].left , \b.b[3].right , \b.c 
);
  output [1:0] \$c_1 ;
  output [1:0] \b.b[2].right ; // @(14:11)
  output [1:0] \b.b[3].left ; // @(14:11)
  output [1:0] \$c_0 ;
  output [1:0] \b.b[1].right ; // @(14:11)
  wire c; // @(16:9)
  input d; // @(16:12)
  output [1:0] \b.b[2].left ; // @(14:11)
  wire [1:0] \$i2_4 ;
  wire [1:0] \$i1_4 ;
  wire [1:0] \$i1_9 ;
  wire [1:0] \$i2_6 ;
  wire [1:0] \$i1_17 ;
  output [2:0] \b.c ; // @(14:11)
  output [1:0] \b.b[0].left ; // @(14:11)
  wire [1:0] \$i1_13 ;
  output [1:0] \b.b[0].right ; // @(14:11)
  output [1:0] \b.b[3].right ; // @(14:11)
  output [1:0] \b.b[1].left ; // @(14:11)
  assign \$c_1  = 2'h03;
  assign c = ~d;
  assign \$i1_4  = $signed(32'h00000000);
  assign \$i2_4  = $signed(c);
  assign \b.b[0].left  = \$i1_4  + \$i2_4 ;
  assign \$i2_6  = $signed(d);
  assign \b.b[0].right  = \$i1_4  + \$i2_6 ;
  assign \$i1_9  = $signed(32'h00000001);
  assign \b.b[1].left  = \$i1_9  + \$i2_4 ;
  assign \b.b[1].right  = \$i1_9  + \$i2_6 ;
  assign \$i1_13  = $signed(32'h00000002);
  assign \b.b[2].left  = \$i1_13  + \$i2_4 ;
  assign \b.b[2].right  = \$i1_13  + \$i2_6 ;
  assign \$i1_17  = $signed(32'h00000003);
  assign \b.b[3].left  = \$i1_17  + \$i2_4 ;
  assign \b.b[3].right  = \$i1_17  + \$i2_6 ;
  assign \b.c  = $signed(c);
endmodule

