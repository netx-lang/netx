grammar HardScript;

design : module*;

module : 'module' ID ':' pattern module_body;

module_body : '{' instantiation* '}';

instantiation : ID ':' pattern;

pattern : list '->' list;

expression
    : ID              # Identifier
    | list            # ListTerm
    | DECIMAL_LITERAL # Decimal
    ;

list
    : '[' expression '..' expression ']'    # RangeList
    | '[' expression (',' expression)* ']'  # CommonList
    | '[' ']'                               # EmptyList
    | '[' expression '|' qualifier (',' qualifier)* ']' # ComprehensionList
    ;

qualifier
    : ID '<-' list      # Generator
    | 'when' expression       # Filter
    | 'let' ID '=' expression # LocalBinding
    ;

ID : [a-zA-Z_][a-zA-Z_0-9]*;
DECIMAL_LITERAL : [0-9]+;