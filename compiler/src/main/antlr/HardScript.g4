grammar HardScript;

design : module*;

module
    : 'module' ID ':' pattern '{' (instantiation | component | statement)* '}'
    ;

component
    : 'component' ID ':' list_expression
    ;

statement
    : 'for' '(' interator (',' interator)* ')' '{' (instantiation | component)* '}'
    | expression
    ;

instantiation
    : ID ':' pattern
    ;

expression
    : primary_expression
    | access_expression
    | conditional_expression
    ;

primary_expression
    : ID                 # Identifier
    | DECIMAL_LITERAL    # Decimal
    | list_expression    # ListExpr
    | pattern            # PatternExpr
    | '(' expression ')' # ParenExpr
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
    : interator               # Generator
    | 'when' expression       # Filter
    | 'let' ID '=' expression # LocalBinding
    ;

interator
    : ID '<-' list_expression
    ;

ID : [a-zA-Z_][a-zA-Z_0-9]*;
COMMENT: '//' ~('\n')* '\n' -> skip ;
DECIMAL_LITERAL : [0-9]+;
WS: [ \t\r\n]+ -> skip;