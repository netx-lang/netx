// @(12:10)
module top_1(
  d, \$c_0 , \$c_1 , \b.b[0].left , \b.b[0].right , \b.b[1].left , \b.b[1].right , \b.b[2].left , \b.b[2].right , \b.b[3].left , \b.b[3].right , \b.c 
);
  output [2:0] \b.c ; // @(14:11)
  output [1:0] \b.b[0].left , \b.b[0].right , \b.b[1].left , \b.b[1].right , \b.b[2].left , \b.b[2].right , \b.b[3].left , \b.b[3].right ; // @(14:11), @(14:11), @(14:11), @(14:11), @(14:11), @(14:11), @(14:11), @(14:11)
  output [1:0] \$c_0 , \$c_1 ;
  input d; // @(16:12)
  wire c; // @(16:9)
  wire [1:0] \$o_2 , \$o_4 ;
  assign c = ~d;
  assign \$o_2  = $signed(c);
  assign \b.b[0].left  = 2'h00 + \$o_2 ;
  assign \$o_4  = $signed(d);
  assign \b.b[0].right  = 2'h00 + \$o_4 ;
  assign \b.b[1].left  = 2'h01 + \$o_2 ;
  assign \b.b[1].right  = 2'h01 + \$o_4 ;
  assign \b.b[2].left  = 2'h02 + \$o_2 ;
  assign \b.b[2].right  = 2'h02 + \$o_4 ;
  assign \b.b[3].left  = 2'h03 + \$o_2 ;
  assign \b.b[3].right  = 2'h03 + \$o_4 ;
  assign \b.c  = $signed(c);
  assign \$c_1  = 2'h03;
endmodule

