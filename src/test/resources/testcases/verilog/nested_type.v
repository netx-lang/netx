// @(12:10)
module top_1(
  d, \$c_0 , \$c_1 , \b.b[0].left , \b.b[0].right , \b.b[1].left , \b.b[1].right , \b.b[2].left , \b.b[2].right , \b.b[3].left , \b.b[3].right , \b.c 
);
  output [2:0] \b.c ; // @(14:11)
  output [1:0] \b.b[0].left , \b.b[0].right , \b.b[1].left , \b.b[1].right , \b.b[2].left , \b.b[2].right , \b.b[3].left , \b.b[3].right ; // @(14:11), @(14:11), @(14:11), @(14:11), @(14:11), @(14:11), @(14:11), @(14:11)
  output [1:0] \$c_0 , \$c_1 ; // @(0:0), @(0:0)
  input d; // @(16:12)
  wire c; // @(16:9)
  wire [1:0] \$o_2 , \$o_3 , \$o_5 , \$o_8 , \$o_12 , \$o_16 ; // @(0:0), @(0:0), @(0:0), @(0:0), @(0:0), @(0:0)
  assign c = ~d;
  assign \$o_2  = $signed(32'h00000000);
  assign \$o_3  = $signed(c);
  assign \b.b[0].left  = \$o_2  + \$o_3 ;
  assign \$o_5  = $signed(d);
  assign \b.b[0].right  = \$o_2  + \$o_5 ;
  assign \$o_8  = $signed(32'h00000001);
  assign \b.b[1].left  = \$o_8  + \$o_3 ;
  assign \b.b[1].right  = \$o_8  + \$o_5 ;
  assign \$o_12  = $signed(32'h00000002);
  assign \b.b[2].left  = \$o_12  + \$o_3 ;
  assign \b.b[2].right  = \$o_12  + \$o_5 ;
  assign \$o_16  = $signed(32'h00000003);
  assign \b.b[3].left  = \$o_16  + \$o_3 ;
  assign \b.b[3].right  = \$o_16  + \$o_5 ;
  assign \b.c  = $signed(c);
  assign \$c_1  = 2'h03;
endmodule

