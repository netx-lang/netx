grammar NetX;

design
    : header? (import_statement | component | enum)+
    ;

header
    : kind='application' ID ';'
    | kind='library' ID ';'
    ;

import_statement
    : 'import' lib_reference ('as' lib_alias)? ';'
    ;

lib_reference
    : ID ('.' ID)* ('.' lib_reference_set)? # CommonLibRef
    | ID ('.' ID)* '.' '*'                  # WildcardLibRef
    ;

lib_reference_set
    : '{' ID (',' ID)* '}'
    ;

lib_alias
    : ID
    | lib_reference_set
    ;

component
    : kind='component' ID attributes? (':' component_type)? component_body
    | kind='interface' ID attributes? ':' type=expression component_body
    ;

enum
    : 'enum' ID '{' ID (',' ID)* '}'
    ;

component_type
    : from=expression '->' to=expression
    ;

component_body
    : '{' element* '}'  # Implemented
    | 'pass' ';'        # Passed
    ;

attributes
    : '(' ID (',' ID)* ')'
    ;

element
    : binding ';'                             # BindingStatement
    | data_declaration ';'                    # DataDeclaration
    | 'auto' expression ';'                   # MixedInstantiation
    | expression ';'                          # ComponentInstantiation
    | 'for' '(' iterator ')' element          # Loop
    | 'if' cond=expression
      'then' thenBranch=element
      ('else' elseBranch=element)?            # Branch
    | '{' element* '}'                        # Block
    ;

data_declaration
    : kind='wire'   declaration_list 'of' type=symbol
    | kind='bundle' declaration_list 'of' type=symbol
    | kind='auto'   declaration_list
    ;

declaration_symbol
    : ID ('[' expression ']')*
    ;

declaration_list
    : declaration_symbol (',' declaration_symbol)*
    ;

expression
    : symbol                                                        # SymbolExpr
    | number                                                        # NumberExpr
    | '[' list ']'                                                  # ListExpr
    | expression (selector)+                                        # AccessExpr
    | expression '.' ID                                             # FieldExpr
    | prev='#' expression                                           # UnaryExpr
    | expression post='#'                                           # UnaryExpr
    | op=('+' | '-' | '~' | '!') expression                         # UnaryExpr
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
    | lhs=expression op='<+>' rhs=expression                        # BinaryExpr
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
    : ID 'in' expression
    ;

binding
    : 'let' symbol '=' expression
    ;

symbol
    : ID '(' expression (',' expression)* ')'             # CallSymbol
    | ID '(' attribute_assign (',' attribute_assign)* ')' # CallSymbol
    | ID '(' ')'                                          # CallSymbol
    | ID                                                  # PrimarySymbol
    ;

attribute_assign
    : ID '=' expression
    ;

number
    : UNSIGNED_INTEGER # IntNumber
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

UNSIGNED_INTEGER : [0-9] [0-9_]*;

ID : [a-zA-Z_][a-zA-Z_0-9]*;

WHITE_SPACE: [ \t\r\n]+ -> skip;
COMMENT: '//' ~('\n')* '\n' -> skip;