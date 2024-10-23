grammar HardScript;

design : module*;

module
    : 'module' ID ':' pattern module_body
    ;

module_body
    : '{' (instantiation | component)* '}'
    ;

component
    : 'component' ID ':' list_expression
    ;

instantiation
    : ID ':' pattern
    ;

expression
    : primary_expression
    | access_expression
    ;

primary_expression
    : ID                # Identifier
    | list_expression   # ListExpr
    | pattern           # PatternExpr
    | DECIMAL_LITERAL   # Decimal
    ;

access_expression
    : primary_expression ('[' expression ']')+
    ;

pattern
    : list_expression '->' list_expression
    ;

list_expression
    : '[' expression '..' expression ']' # RangeList
    | '[' expression (',' expression)* ']' # CommonList
    | '[' expression '|' qualifier (',' qualifier)* ']' # ListComprehension
    | '[' ']' # EmptyList
    ;

qualifier
    : ID '<-' list_expression # Generator
    | 'when' expression       # Filter
    | 'let' ID '=' expression # LocalBinding
    ;

ID : [a-zA-Z_][a-zA-Z_0-9]*;
COMMENT: '//' ~('\n')* '\n' -> skip ;
DECIMAL_LITERAL : [0-9]+;
WS: [ \t\r\n]+ -> skip;