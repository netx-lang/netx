grammar NetX;

design
    : header? (import_ | component | interface | enum)+
    ;

header
    : kind='application' ID ';'
    | kind='library' ID ';'
    ;

import_
    : 'import' reference ('as' alias)? ';'
    ;

reference
    : ID ('.' ID)* ('.' reference_set)? # CommonRef
    | ID ('.' ID)* '.' '*'              # WildcardRef
    ;

reference_set
    : '{' ID (',' ID)* '}'
    ;

alias
    : ID
    | reference_set
    ;

component
    : 'component' ID attributes? (':' component_sig)? component_body
    ;

interface
    : 'interface' ID attributes? ':' type=expression component_body
    ;

enum
    : 'enum' ID '{' ID (',' ID)* '}'
    | 'enum' ID '{' ID '=' number ( ',' ID '=' number )* '}'
    ;

component_sig
    : from=expression '->' to=expression
    ;

component_body
    : '{' element* '}'
    ;

attributes
    : '(' ID (',' ID)* ')'
    ;

element
    : binding ';'                             # BindingStatement
    | data_declaration ';'                    # DataDeclaration
    | 'auto' expression ';'                   # MixedInstantiation
    | expression ';'                          # ComponentInstantiation
    | 'for' iterator element                  # Loop
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

declaration_list
    : declaration_symbol (',' declaration_symbol)*
    ;

declaration_symbol
    : ID ('[' expression ']')*
    ;

expression
    : symbol                                                          # SymbolExpr
    | STRING                                                          # StringExpr
    | number                                                          # NumberExpr
    | '[' list ']'                                                    # ListExpr
    | expression (selector)+                                          # AccessExpr
    | expression '.' ID                                               # FieldExpr
    | <assoc=right> prev='#' expression                               # UnaryExpr
    | <assoc=left> expression post='#'                                # UnaryExpr
    | <assoc=right> op=('+' | '-' | '~' | 'not') expression           # UnaryExpr
    | <assoc=right> lhs=expression op='**' rhs=expression             # BinaryExpr
    | lhs=expression op=('*' | '/' | '%') rhs=expression              # BinaryExpr
    | lhs=expression op=('+' | '-') rhs=expression                    # BinaryExpr
    | lhs=expression op=('>>'| '<<' | '>>>' | '<<<') rhs=expression   # BinaryExpr
    | lhs=expression op=('<' | '<=' | '>'  | '>=') rhs=expression     # BinaryExpr
    | lhs=expression op=('<*' | '<=*' | '>*'  | '>=*') rhs=expression # BinaryExpr
    | lhs=expression op=('==' | '!=') rhs=expression                  # BinaryExpr
    | lhs=expression op='&' rhs=expression                            # BinaryExpr
    | lhs=expression op=('^~' | '~^' | '^') rhs=expression            # BinaryExpr
    | lhs=expression op='|' rhs=expression                            # BinaryExpr
    | lhs=expression op='and' rhs=expression                          # BinaryExpr
    | lhs=expression op='or' rhs=expression                           # BinaryExpr
    | lhs=expression op='++' rhs=expression                           # BinaryExpr
    | lhs=expression op='<>' rhs=expression                           # BinaryExpr
    | lhs=expression op='||' rhs=expression                           # BinaryExpr
    | '{' port_assign (',' port_assign)* '}'                          # PortExpr
    | 'if' cond=expression
      'then' thenBranch=expression
      'else' elseBranch=expression                                    # ConditionalExpr
    | binding 'in' body=expression                                    # LetExpr
    | '(' expression ')'                                              # ParenExpr
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
    : iterator          # Generator
    | 'when' expression # Filter
    | binding           # LocalBinding
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

port_assign
    : DEFAULT ':' rhs=expression
    | lhs=expression ':' rhs=expression
    ;

number
    : UNSIGNED_INTEGER # IntNumber
    | BOOLEAN          # BoolNumber
    | FLOAT            # FloatNumber
    | SIZED_FLOAT      # FloatNumber
    | BIN_NUMBER       # BitVectorNumber
    | HEX_NUMBER       # BitVectorNumber
    | OCT_NUMBER       # BitVectorNumber
    | DEC_NUMBER       # BitVectorNumber
    ;

DEFAULT : 'default';
BOOLEAN : 'true' | 'false';
STRING : '"' (~["\\] | '\\' .)* '"';

BIN_NUMBER : UNSIGNED_INTEGER '\'' [bB] [01] [01_]*;
HEX_NUMBER : UNSIGNED_INTEGER '\'' [hH] [0-9a-fA-F] [0-9a-fA-F_]*;
OCT_NUMBER : UNSIGNED_INTEGER '\'' [oO] [0-7] [0-7_]*;
DEC_NUMBER : UNSIGNED_INTEGER '\'' [dD] [0-9] [0-9_]*;

UNSIGNED_INTEGER : [0-9] [0-9_]*;

FLOAT : UNSIGNED_INTEGER ('.' UNSIGNED_INTEGER)?;
SIZED_FLOAT : UNSIGNED_INTEGER '\'' [fF] FLOAT;

ID : [a-zA-Z_][a-zA-Z_0-9]*;

WHITE_SPACE: [ \t\r\n]+ -> skip;
COMMENT: '//' ~('\n')* '\n' -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;