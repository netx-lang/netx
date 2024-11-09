grammar HsonData;

data: identifier | constant;

identifier
    : name=ID '@' '(' line=UNSIGNED_INTEGER ':' column=UNSIGNED_INTEGER ')' '@' index=UNSIGNED_INTEGER selector*
    ;

selector
    : '[' UNSIGNED_INTEGER ']'                      # BitSelector
    | '[' UNSIGNED_INTEGER ':' UNSIGNED_INTEGER ']' # PartSelector
    ;

constant
    : size=UNSIGNED_INTEGER  literal=HEX
    ;

HEX : '\'h' [0-9a-fA-F] [0-9a-fA-F_]* ;
ID : [$a-zA-Z_][a-zA-Z_0-9]*;
UNSIGNED_INTEGER : [0-9] [0-9_]*;
WHITE_SPACE: [ \t\r\n]+ -> skip;