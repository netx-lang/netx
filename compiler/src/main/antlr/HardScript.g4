grammar HardScript;

design : component+;

component
    : 'component' ID component_arguments? (':' type=expression)? '{' element* '}'
    ;

component_arguments
    : '(' ID (',' ID)* ')'
    ;

element
    : type=expression ':' connection=expression           # ComponentInstantiation
    | binding                                             # BindingStatement
    | 'for' '(' iterator ')' '{' element* '}'             # Loop
    | 'inst' primary_symbol (',' primary_symbol)* 'of' type=type_expression    # DataInstantiation
    ;

expression
    : symbol                                                        # SymbolExpr
    | number                                                        # NumberExpr
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
      'else' elseBranch=expression                                       # ConditionalExpr
    | '(' expression ')'                                                 # ParenExpr
    | 'component' (primary_symbol (',' primary_symbol)*)? ':' expression # LambdaExpr
    ;

type_expression
    : primary_symbol '(' (type_argument (',' type_argument)*)? ')'
    ;

type_argument
    : 'auto'
    | expression
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
    | binding                     # LocalBinding
    ;

iterator
    : primary_symbol 'in' expression
    ;

binding
    : 'let' primary_symbol '=' expression
    ;

symbol
    : primary_symbol                                      # PrimarySymbolExpr
    | primary_symbol '(' expression (',' expression)* ')' # ParamSymbolExpr
    | primary_symbol '(' ')'                              # ParamSymbolExpr
    ;

primary_symbol
    : ID # Identifier
    | ID (interpolation | ID)+ # Interpolated
    ;

interpolation
    : '$' '{' expression '}'
    ;

number
    : INTEGER          # IntNumber
    | BOOLEAN          # BoolNumber
    | BIN_NUMBER       # BitVectorNumber
    | HEX_NUMBER       # BitVectorNumber
    | OCT_NUMBER       # BitVectorNumber
    | DEC_NUMBER       # BitVectorNumber
    ;

BOOLEAN : 'true' | 'false';

BIN_NUMBER : UNSIGNED_INTEGER '\'' [bB] [01] [01_]*;
HEX_NUMBER : UNSIGNED_INTEGER '\'' [hH] [0-9a-fA-F] [0-9a-fA-F_]*;
OCT_NUMBER : UNSIGNED_INTEGER '\'' [oO] [0-7] [0-7_]*;
DEC_NUMBER : UNSIGNED_INTEGER '\'' [dD] [0-9] [0-9_]*;

INTEGER : [+-]? [0-9] [0-9_]*;
UNSIGNED_INTEGER : [0-9] [0-9_]*;

ID : [a-zA-Z_][a-zA-Z_0-9]*;

WHITE_SPACE: [ \t\r\n]+ -> skip;
COMMENT: '//' ~('\n')* '\n' -> skip;