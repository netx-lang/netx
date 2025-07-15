package pascal.netx.compiler.io;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.antlr.NetXParser.*;
import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.compiler.ast.PackageInfoNode;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.*;
import pascal.netx.compiler.ast.expr.*;
import pascal.netx.compiler.ast.expr.qualifier.BindingQualifier;
import pascal.netx.compiler.ast.expr.qualifier.FilterQualifier;
import pascal.netx.compiler.ast.expr.qualifier.GeneratorQualifier;
import pascal.netx.compiler.ast.expr.qualifier.Qualifier;
import pascal.netx.compiler.ast.expr.selector.BitSelector;
import pascal.netx.compiler.ast.expr.selector.PartSelector;
import pascal.netx.compiler.ast.expr.selector.Selector;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;
import pascal.netx.util.type.Pair;

import java.util.ArrayList;
import java.util.List;

public final class TreeParser {

    private final Logger log = LoggerFactory.getLogger(TreeParser.class);

    private final String fileName;

    public TreeParser(String fileName) {
        this.fileName = fileName;
    }

    public PackageNode parseDesign(DesignContext ctx) {
        PackageNode result = PackageNode.of(parsePackageInfo(ctx.header())).setSourceLoc(locationOf(ctx.getStart()));
        for (var child : ctx.children) {
            switch (child) {
                case Import_Context sub -> parseImportInto(sub, result);
                case ComponentContext sub -> parseComponentInto(sub, result);
                case InterfaceContext sub -> parseInterfaceInto(sub, result);
                case EnumContext sub -> parseEnumInto(sub, result);
                case HeaderContext _ -> {
                    // do nothing
                }
                default -> throw new ShallNotReachHereError();
            }
        }
        return result;
    }

    void parseImportInto(Import_Context ctx, PackageNode design) {
        parseImport(ctx).forEach(design::addDefinition);
    }

    void parseComponentInto(ComponentContext ctx, PackageNode design) {
        design.addDefinition(parseComponent(ctx));
    }

     void parseInterfaceInto(InterfaceContext ctx, PackageNode design) {
        design.addDefinition(parseInterface(ctx));
    }

    void parseEnumInto(EnumContext ctx, PackageNode design) {
        design.addDefinition(parseEnum(ctx));
    }

    EnumNode parseEnum(EnumContext ctx) {
        var iterator = ctx.ID().iterator();
        EnumNode result = EnumNode.of(parseIdentifier(iterator.next()));
        iterator.forEachRemaining(field -> result.addField(parseIdentifier(field)));
        if (ctx.number() != null) {
            for (var number : ctx.number()) {
                if (!(number instanceof BitVectorNumberContext bitCtx)) {
                    log.error("{}: Expected a bitvector number for enum", locationOf(number.getStart()));
                    throw new ParseCancellationException();
                }
                var value = parseBitVectorNumber(bitCtx);
                result.addValue(value.setSourceLoc(locationOf(number.getStart())));
            }
        }
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    PackageInfoNode parsePackageInfo(HeaderContext ctx) {
        if (ctx == null) {
            return PackageInfoNode.of(PackageInfoNode.Kind.APPLICATION, null);
        }
        PackageInfoNode.Kind kind = switch (ctx.kind.getText()) {
            case "library" -> PackageInfoNode.Kind.LIBRARY;
            case "application" -> PackageInfoNode.Kind.APPLICATION;
            default -> throw new ShallNotReachHereError();
        };
        return PackageInfoNode.of(kind, parseIdentifier(ctx.ID()));
    }

    ComponentNode parseComponent(ComponentContext ctx) {
        var identifier = parseIdentifier(ctx.ID());
        var attributes = parseComponentAttributes(ctx);
        var type = parseComponentType(ctx.component_type(), locationOf(ctx.getStart()));
        var result = ComponentNode.of(identifier, type, attributes, parseElements(ctx.component_body().element()));
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    InterfaceNode parseInterface(InterfaceContext ctx) {
        var type = parseExpr(ctx.expression());
        var identifier = parseIdentifier(ctx.ID());
        var attributes = parseInterfaceAttributes(ctx);
        var result = InterfaceNode.of(identifier, type, attributes, parseElements(ctx.component_body().element()));
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    List<ImportNode> parseImport(Import_Context ctx) {
        return switch (ctx.reference()) {
            case CommonRefContext refCtx -> parseCommonImport(ctx, refCtx);
            case WildcardRefContext refCtx -> parseWildcardImport(ctx, refCtx);
            default -> throw new ShallNotReachHereError();
        };
    }

    private List<ImportNode> parseWildcardImport(Import_Context ctx, WildcardRefContext refCtx) {
        return List.of(
                ImportNode.ofWildcard(
                        refCtx.ID().stream().map(this::parseIdentifier).toList()
                ).setSourceLoc(locationOf(ctx.getStart()))
        );
    }

    private List<ImportNode> parseCommonImport(Import_Context ctx, CommonRefContext refCtx) {
        var prefix = refCtx.ID().stream().map(this::parseIdentifier).toList();
        List<ImportNode> results;
        if (refCtx.reference_set() != null) {
            results = refCtx.reference_set().ID().stream()
                    .map(this::parseIdentifier)
                    .map(id -> new ArrayList<>(prefix) {{
                        add(id);
                    }})
                    .map(ImportNode::of)
                    .toList();
        } else {
            results = List.of(ImportNode.of(prefix));
        }
        if (ctx.alias() != null) {
            parseImportAlias(ctx.alias(), results);
        }
        results.forEach(stmt -> stmt.setSourceLoc(locationOf(ctx.getStart())));
        return results;
    }

    private void parseImportAlias(AliasContext ctx, List<ImportNode> results) {
        List<Identifier> aliasID;
        if (ctx.ID() != null) {
            aliasID = List.of(parseIdentifier(ctx.ID()));
        } else {
            aliasID = ctx.reference_set().ID().stream().map(this::parseIdentifier).toList();
        }
        if (aliasID.size() != results.size()) {
            log.error("{}: Unmatched number of import alias", locationOf(ctx.getStart()));
            throw new CompileCancellationException();
        }
        var iter = aliasID.iterator();
        results.forEach(stmt -> stmt.setAlias(iter.next()));
    }

    List<Identifier> parseInterfaceAttributes(InterfaceContext ctx) {
        if (ctx.attributes() == null) {
            return List.of();
        }
        return ctx.attributes().ID().stream()
                .map(this::parseIdentifier).toList();
    }

    List<Identifier> parseComponentAttributes(ComponentContext ctx) {
        if (ctx.attributes() == null) {
            return List.of();
        }
        return ctx.attributes().ID().stream()
                .map(this::parseIdentifier).toList();
    }

    ComponentTypeNode parseComponentType(Component_typeContext ctx, SourceLoc sourceLoc) {
        if (ctx == null) {
            var result = ComponentTypeNode.of(CommonList.ofEmpty(), CommonList.ofEmpty());
            result.walk(new ASTListener() {
                @Override
                public void enterDefault(ASTNode node) {
                    node.setSourceLoc(sourceLoc);
                }
            });
            return result;
        } else {
            return ComponentTypeNode.of(parseExpr(ctx.from), parseExpr(ctx.to))
                    .setSourceLoc(locationOf(ctx.getStart()));
        }
    }

    ComponentElement parseElement(ElementContext ctx) {
        var result = switch (ctx) {
            case BindingStatementContext sub -> parseBinding(sub.binding());
            case MixedInstantiationContext sub -> parseMixedInstantiation(sub);
            case DataDeclarationContext sub -> parseDataInstantiation(sub);
            case ComponentInstantiationContext sub -> parseComponentInstantiation(sub);
            case LoopContext sub -> parseLoop(sub);
            case BranchContext sub -> parseBranch(sub);
            case BlockContext sub -> BlockNode.of(parseElements(sub.element()));
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    ComponentInstantiationNode parseComponentInstantiation(ComponentInstantiationContext ctx) {
        return ComponentInstantiationNode.of(parseExpr(ctx.expression()), false);
    }

    ComponentInstantiationNode parseMixedInstantiation(MixedInstantiationContext ctx) {
        return ComponentInstantiationNode.of(parseExpr(ctx.expression()), true);
    }

    LoopNode parseLoop(LoopContext ctx) {
        return LoopNode.of(parseGeneratorQualifier(ctx.iterator()), parseElement(ctx.element()));
    }

    BranchNode parseBranch(BranchContext ctx) {
        var result = BranchNode.of(parseExpr(ctx.cond), parseElement(ctx.thenBranch), null);
        if (ctx.elseBranch != null) {
            result.setElseBranch(parseElement(ctx.elseBranch));
        }
        return result;
    }

    DataInstantiationNode parseDataInstantiation(DataDeclarationContext ctx) {
        var decl = ctx.data_declaration();
        SymbolExpr type;
        DataInstantiationNode.Kind kind;
        var declarations = parseDeclarationList(decl.declaration_list());
        switch (decl.kind.getText()) {
            case "wire" -> {
                kind = DataInstantiationNode.Kind.WIRE;
                type = parseSymbolExpr(decl.type);
            }
            case "bundle" -> {
                kind = DataInstantiationNode.Kind.BUNDLE;
                type = parseSymbolExpr(decl.type);
            }
            case "auto" -> {
                kind = DataInstantiationNode.Kind.WIRE;
                type = null;
            }
            default -> throw new ShallNotReachHereError();
        }
        return DataInstantiationNode.of(kind, declarations, type);
    }

    List<DataRestNode> parseDeclarationList(Declaration_listContext ctx) {
        List<DataRestNode> result = new ArrayList<>();
        for (var sub : ctx.declaration_symbol()) {
            DataRestNode data = DataRestNode.of(
                    parseIdentifier(sub.ID()),
                    sub.expression().stream().map(this::parseExpr).toList()
            ).setSourceLoc(locationOf(sub.getStart()));
            result.add(data);
        }
        return result;
    }

    ExprNode parseExpr(ExpressionContext ctx) {
        var result = switch (ctx) {
            case ListExprContext sub -> parseListExpr(sub);
            case UnaryExprContext sub -> parseUnaryExpr(sub);
            case FieldExprContext sub -> parseFieldExpr(sub);
            case SymbolExprContext sub -> parseSymbolExpr(sub);
            case NumberExprContext sub -> parseNumberExpr(sub);
            case AccessExprContext sub -> parseAccessExpr(sub);
            case BinaryExprContext sub -> parseBinaryExpr(sub);
            case PortExprContext sub -> parsePortExpr(sub);
            case ConditionalExprContext sub -> parseConditionalExpr(sub);
            case ParenExprContext sub -> parseExpr(sub.expression());
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    SymbolExpr parseSymbolExpr(SymbolExprContext ctx) {
        return parseSymbolExpr(ctx.symbol());
    }

    SymbolExpr parseSymbolExpr(SymbolContext ctx) {
        return switch (ctx) {
            case PrimarySymbolContext sub -> parsePrimarySymbol(sub);
            case CallSymbolContext sub -> parseParamSymbolExpr(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    ListExpr parseListExpr(ListExprContext ctx) {
        return switch (ctx.list()) {
            case CommonListContext sub -> CommonList.of(parseExprList(sub.expression()));
            case RangeListContext sub -> RangeList.of(parseExpr(sub.from), parseExpr(sub.to));
            case ListComprehensionContext sub -> parseListComprehension(sub);
            case EmptyListContext ignored -> CommonList.ofEmpty();
            default -> throw new ShallNotReachHereError();
        };
    }

    ListComprehension parseListComprehension(ListComprehensionContext ctx) {
        return ListComprehension.of(
                parseExpr(ctx.expression()),
                ctx.qualifier().stream().map(this::parserQualifier).toList());
    }

    Qualifier parserQualifier(QualifierContext ctx) {
        var result = switch (ctx) {
            case GeneratorContext sub -> parseGeneratorQualifier(sub.iterator());
            case FilterContext sub -> FilterQualifier.of(parseExpr(sub.expression()));
            case LocalBindingContext sub -> BindingQualifier.of(parseBinding(sub.binding()));
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    GeneratorQualifier parseGeneratorQualifier(IteratorContext ctx) {
        return GeneratorQualifier.of(
                parseIdentifier(ctx.ID()),
                parseExpr(ctx.expression())
        ).setSourceLoc(locationOf(ctx.getStart()));
    }

    BindingNode parseBinding(BindingContext ctx) {
        return BindingNode.of(parseSymbolExpr(ctx.symbol()), parseExpr(ctx.expression()))
                .setSourceLoc(locationOf(ctx.getStart()));
    }

    ExprNode parsePortExpr(PortExprContext ctx) {
        List<PortConnectionExpr.PortAssign> ports = new ArrayList<>();
        for (var assign : ctx.port_assign()) {
            var lhs = parseExpr(assign.lhs);
            var rhs = parseExpr(assign.rhs);
            ports.add(new PortConnectionExpr.PortAssign(lhs, rhs));
        }
        return PortConnectionExpr.of(ports);
    }

    ExprNode parseUnaryExpr(UnaryExprContext ctx) {
        UnaryExpr.Operator op;
        if (ctx.prev != null) {
            op = UnaryExpr.Operator.BLOCK_OUT;
        } else if (ctx.post != null) {
            op = UnaryExpr.Operator.BLOCK_IN;
        } else {
            op = UnaryExpr.Operator.fromString(ctx.op.getText());
        }
        return UnaryExpr.of(op, parseExpr(ctx.expression()));
    }

    ExprNode parseBinaryExpr(BinaryExprContext ctx) {
        var op = BinaryExpr.Operator.fromString(ctx.op.getText());
        return BinaryExpr.of(op, parseExpr(ctx.lhs), parseExpr(ctx.rhs));
    }

    NumberExpr parseNumberExpr(NumberExprContext ctx) {
        return parseNumber(ctx.number());
    }

    private NumberExpr parseNumber(NumberContext n) {
        return switch (n) {
            case IntNumberContext sub -> IntNumber.of(Integer.parseUnsignedInt(sub.UNSIGNED_INTEGER().getText()));
            case BoolNumberContext sub -> BoolNumber.of(Boolean.parseBoolean(sub.BOOLEAN().getText()));
            case BitVectorNumberContext sub -> parseBitVectorNumber(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    BitVectorNumber parseBitVectorNumber(BitVectorNumberContext ctx) {
        return BitVectorNumber.of(BitVector.parse(ctx.getText()));
    }

    ExprNode parseFieldExpr(FieldExprContext ctx) {
        return FieldExpr.of(
                parseExpr(ctx.expression()),
                parseIdentifier(ctx.ID()));
    }

    ExprNode parseAccessExpr(AccessExprContext ctx) {
        var result = parseExpr(ctx.expression());
        for (var selector : ctx.selector()) {
            result = AccessExpr.of(result, parseSelector(selector)).setSourceLoc(result.getSourceLoc());
        }
        return result;
    }

    Selector parseSelector(SelectorContext ctx) {
        var result = switch (ctx) {
            case BitSelectorContext sub -> BitSelector.of(parseExpr(sub.expression()));
            case PartSelectorContext sub -> PartSelector.of(parseExpr(sub.from), parseExpr(sub.to));
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    CallSymbolExpr parseParamSymbolExpr(CallSymbolContext ctx) {
        CallSymbolExpr result;
        var head = parseIdentifier(ctx.ID());
        if (!ctx.expression().isEmpty()) {
            result = CallSymbolExpr.fromOrder(head, parseExprList(ctx.expression()));
        } else if (!ctx.attribute_assign().isEmpty()) {
            result = CallSymbolExpr.fromAssign(head, parseAttrAssigns(ctx.attribute_assign()));
        } else {
            result = CallSymbolExpr.ofEmpty(head);
        }
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    List<Pair<Identifier, ExprNode>> parseAttrAssigns(List<Attribute_assignContext> list) {
        List<Pair<Identifier, ExprNode>> result = new ArrayList<>();
        for (var assign : list) {
            var id = parseIdentifier(assign.ID());
            var expr = parseExpr(assign.expression());
            result.add(Pair.of(id, expr));
        }
        return result;
    }

    ExprNode parseConditionalExpr(ConditionalExprContext ctx) {
        return ConditionalExpr.of(parseExpr(ctx.cond), parseExpr(ctx.thenBranch), parseExpr(ctx.elseBranch));
    }

    Identifier parseIdentifier(TerminalNode ID) {
        return Identifier.of(ID.getText()).setSourceLoc(locationOf(ID.getSymbol()));
    }

    IdentifierExpr parsePrimarySymbol(PrimarySymbolContext ctx) {
        return IdentifierExpr.of(parseIdentifier(ctx.ID())).setSourceLoc(locationOf(ctx.getStart()));
    }

    List<ComponentElement> parseElements(List<ElementContext> list) {
        List<ComponentElement> result = new ArrayList<>();
        for (var element : list) {
            result.add(parseElement(element));
        }
        return result;
    }

    List<ExprNode> parseExprList(List<ExpressionContext> list) {
        List<ExprNode> result = new ArrayList<>();
        for (var expr : list) {
            result.add(parseExpr(expr));
        }
        return result;
    }

    SourceLoc locationOf(Token token) {
        return new SourceLoc(token.getLine(), token.getCharPositionInLine(), fileName);
    }
}
