grammar HardScript;

design : module+;

module
    : 'module' symbol ':' module_type '{' element* '}'
    ;

module_type
    : list_expression '->' list_expression
    ;

element
    : instantiation                                            # InstantiationWrapper
    | 'component' symbol (',' symbol)* ':' component_type      # Component
    | 'for' '(' iterator (',' iterator)* ')' '{' element* '}'  # Loop
    ;

instantiation
    : instantiation_ref ('<>' instantiation_ref)* ':' list_expression ('->' list_expression)+
    ;

instantiation_ref
    : symbol          # AtomInstRef
    | list_expression # CompositeInstRef
    ;

component_type
    : atom_expression                                     # PrimitiveCompType
    | symbol '(' component_type (',' component_type)* ')' # CompositeCompType
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
    : symbol # SymbolWrapper
    | number # NumberExpr
    ;

conditional_expression
    : 'if' atom_expression 'then' expression 'else' expression
    ;

access_expression
    : primary_expression (selector)+
    ;

selector
    : '[' expression ']'                # BitSelector
    | '[' expression ':' expression ']' # PartSelector
    ;

list_expression
    : '[' expression (',' expression)* ']'              # CommonList
    | '[' atom_expression '..' atom_expression ']'      # RangeList
    | '[' expression '|' qualifier (',' qualifier)* ']' # ListComprehension
    | '[' ']'                                           # EmptyList
    ;

qualifier
    : iterator                    # Generator
    | 'when' expression           # Filter
    | 'let' symbol '=' expression # LocalBinding
    ;

iterator
    : symbol '<-' list_expression
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
    | bitvector_number # BitVectorNumberWrapper
    ;

bitvector_number
    : BIN_NUMBER
    | HEX_NUMBER
    | OCT_NUMBER
    | DEC_NUMBER
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