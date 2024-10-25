grammar HardScript;

design : module+;

module
    : 'module' ID module_arguments? ':' type=expression '{' element* '}'
    ;

module_arguments
    : '(' ID (',' ID)* ')'
    ;

element
    : type=expression ':' connection=expression                # Instantiation
    | 'component' symbol (',' symbol)* ':' type=expression     # Component
    | 'for' '(' iterator (',' iterator)* ')' '{' element* '}'  # Loop
    ;

expression
    : symbol                                                        # SymbolExpr
    | number                                                        # NumberExpr
    | symbol '(' expression (',' expression)* ')'                   # ParamSymbolExpr
    | '[' list ']'                                                  # ListExpr
    | op=('+' | '-' | '~' | '!') expression                         # UnaryExpr
    | lhs=expression op='<>' rhs=expression                         # BinaryExpr
    | lhs=expression op='->' rhs=expression                         # BinaryExpr
    | lhs=expression op='**' rhs=expression                         # BinaryExpr
    | lhs=expression op=('*' | '/' | '%') rhs=expression            # BinaryExpr
    | lhs=expression op=('+' | '-') rhs=expression                  # BinaryExpr
    | lhs=expression op=('>>'| '<<' | '>>>' | '<<<') rhs=expression # BinaryExpr
    | lhs=expression op=('<' | '<=' | '>'  | '>=') rhs=expression   # BinaryExpr
    | lhs=expression op=('==' | '!=') rhs=expression                # BinaryExpr
    | lhs=expression op='&' rhs=expression                          # BinaryExpr
    | lhs=expression op=('^~' | '~^' | '^') rhs=expression          # BinaryExpr
    | lhs=expression op='|' rhs=expression                          # BinaryExpr
    | lhs=expression op='&&' rhs=expression                         # BinaryExpr
    | lhs=expression op='||' rhs=expression                         # BinaryExpr
    | expression (selector)+                                        # AccessExpr
    | 'if' cond=expression
      'then' thenBranch=expression
      'else' elseBranch=expression                                  # ConditionalExpr
    | '(' expression ')'                                            # ParenExpr
    ;


selector
    : '[' expression ']'                # BitSelector
    | '[' expression ':' expression ']' # PartSelector
    ;

list
    : expression (',' expression)*               # CommonList
    | from=expression '..' to=expression         # RangeList
    | expression '|' qualifier (',' qualifier)*  # ListComprehension
    |                                            # EmptyList
    ;

qualifier
    : iterator                    # Generator
    | 'when' expression           # Filter
    | 'let' symbol '=' expression # LocalBinding
    ;

iterator
    : symbol '<-' expression
    ;

symbol
    : ID # Identifier
    | ID (interpolation | ID)+ # Interpolated
    ;

interpolation
    : '$' '{' expression '}'
    ;

number
    : INTEGER          # IntNumber
    | BIN_NUMBER       # BitVectorNumber
    | HEX_NUMBER       # BitVectorNumber
    | OCT_NUMBER       # BitVectorNumber
    | DEC_NUMBER       # BitVectorNumber
    ;

BIN_NUMBER : UNSIGNED_INTEGER '\'' [bB] [01] [01_]*;
HEX_NUMBER : UNSIGNED_INTEGER '\'' [hH] [0-9a-fA-F] [0-9a-fA-F_]*;
OCT_NUMBER : UNSIGNED_INTEGER '\'' [oO] [0-7] [0-7_]*;
DEC_NUMBER : UNSIGNED_INTEGER '\'' [dD] [0-9] [0-9_]*;

INTEGER : [+-]? [0-9] [0-9_]*;
UNSIGNED_INTEGER : [0-9] [0-9_]*;

ID : [a-zA-Z_][a-zA-Z_0-9]*;

WHITE_SPACE: [ \t\r\n]+ -> skip;
COMMENT: '//' ~('\n')* '\n' -> skip;