grammar HsonData;

data
    : identifier | constant | select;

select
    : identifier selector
    ;

identifier
    : name=ID '@' '(' line=UNSIGNED_INTEGER ':' column=UNSIGNED_INTEGER ')' '@' index=UNSIGNED_INTEGER
    ;

selector
    : '[' index=UNSIGNED_INTEGER ']'                        # BitSelector
    | '[' from=UNSIGNED_INTEGER ':' to=UNSIGNED_INTEGER ']' # PartSelector
    ;

constant
    : HEX
    | UNSIGNED_INTEGER
    ;

HEX : UNSIGNED_INTEGER '\'h' [0-9a-fA-F] [0-9a-fA-F_]* ;
ID : [$a-zA-Z_][a-zA-Z_0-9]*;
UNSIGNED_INTEGER : [0-9] [0-9_]*;
WHITE_SPACE: [ \t\r\n]+ -> skip;