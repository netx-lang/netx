grammar HardScript;

design : (component | binding)+;

component
    : 'component' ID attributes? component_type? '{' element* '}'
    ;

component_type
    : ':' from=expression '->' to=expression
    ;

attributes
    : '(' ID (',' ID)* ')'
    ;

element
    : lhs=expression '<-' rhs=expression                                    # ComponentInstantiation
    | binding                                                               # BindingStatement
    | 'for' '(' iterator ')' '{' element* '}'                               # Loop
    | 'inst' primary_symbol (',' primary_symbol)* 'of' type=param_symbol    # DataInstantiation
    | 'inst' primary_symbol (',' primary_symbol)*                           # DataInstantiation
    ;

expression
    : symbol                                                        # SymbolExpr
    | number                                                        # NumberExpr
    | '[' list ']'                                                  # ListExpr
    | op=('+' | '-' | '~' | '!') expression                         # UnaryExpr
    | expression (selector)+                                        # AccessExpr
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
    | lhs=expression op='::' rhs=expression                         # BinaryExpr
    | lhs=expression op='<>' rhs=expression                         # BinaryExpr
    | 'if' cond=expression
      'then' thenBranch=expression
      'else' elseBranch=expression                                  # ConditionalExpr
    | '(' expression ')'                                            # ParenExpr
    ;

selector
    : '[' expression ']'                        # BitSelector
    | '[' from=expression ':' to=expression ']' # PartSelector
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
    : 'let' symbol '=' expression
    ;

symbol
    : param_symbol   # ParamSymbol
    | primary_symbol # PrimarySymbol
    ;

primary_symbol
    : ID # Identifier
    | ID (interpolation | ID)+ # Interpolated
    ;

param_symbol
    : primary_symbol '(' expression (',' expression)* ')'
    | primary_symbol '(' ')'
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