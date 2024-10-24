grammar HardScript;

design : module+;

module
    : 'module' ID ':' module_type '{' element* '}'
    ;

module_type
    : list_expression '->' list_expression
    ;

element
    : instantiation # InstantiationWrapper
    | 'component' ID (',' ID)* ':' component_type  # Component
    | 'for' '(' iterator (',' iterator)* ')' '{' element* '}' # Loop
    ;

instantiation
    : instantiation_ref ('<>' instantiation_ref)* ':' list_expression ('->' list_expression)+
    ;

instantiation_ref
    : ID              #AtomInstRef
    | list_expression #CompositeInstRef
    ;

component_type
    : atom_expression # PrimitiveCompType
    | ID '(' component_type (',' component_type)* ')' # CompositeCompType
    ;

expression
    : primary_expression     # PrimaryExprWrapper
    | access_expression      # AccessExprWrapper
    | conditional_expression # ConditionalExprWrapper
    | '(' expression ')'     # ParenExpr
    ;

primary_expression
    : atom_expression    # AtomExprWrapper
    | list_expression    # ListExpr
    | instantiation      # InstExpr
    ;

atom_expression
    : ID                      # Identifier
    | number                  # NumberExpr
    ;

conditional_expression
    : 'if' expression 'then' expression 'else' expression
    ;

access_expression
    : primary_expression (selector)+
    ;

selector
    : '[' expression ']' # BitSelector
    | '[' expression '..' expression ']' # RangeSelector
    ;

list_expression
    : '[' expression '..' expression ']'   # RangeList
    | '[' expression (',' expression)* ']' # CommonList
    | '[' expression '|' qualifier (',' qualifier)* ']' # ListComprehension
    | '[' ']' # EmptyList
    ;

qualifier
    : iterator                # Generator
    | 'when' expression       # Filter
    | 'let' ID '=' expression # LocalBinding
    ;

iterator
    : ID '<-' list_expression
    ;

number
    : INTEGER          # IntNumber
    | bitvector_number # BitVectorNumberWrapper
    ;

bitvector_number
    : UNSIGNED_INTEGER  '\'' SIGN? bin_number
    | UNSIGNED_INTEGER  '\'' SIGN? hex_number
    | UNSIGNED_INTEGER  '\'' SIGN? oct_number
    | UNSIGNED_INTEGER  '\'' SIGN? dec_number
    ;

bin_number : BIN_BASE BIN_REP;
BIN_BASE : [bB];
BIN_REP: [01] [01_]*;

hex_number : HEX_BASE HEX_REP;
HEX_BASE : [hH];
HEX_REP: [0-9a-fA-F] [0-9a-fA-F_]*;

oct_number : OCT_BASE OCT_REP;
OCT_BASE : [oO];
OCT_REP: [0-7] [0-7_]*;

dec_number : DEC_BASE DEC_REP;
SIGN : [sS];
DEC_BASE : [dD];
DEC_REP: [0-9] [0-9_]*;


INTEGER : [+-]? [0-9] [0-9_]*;
UNSIGNED_INTEGER : [0-9] [0-9_]*;

ID : [a-zA-Z_][a-zA-Z_0-9]*;

WHITE_SPACE: [ \t\r\n]+ -> skip;
COMMENT: '//' ~('\n')* '\n' -> skip;
