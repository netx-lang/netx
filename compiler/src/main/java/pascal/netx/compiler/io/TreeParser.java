package pascal.netx.compiler.io;

import org.antlr.v4.runtime.Token;
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

    private static final Logger log = LoggerFactory.getLogger(TreeParser.class);

    public static PackageNode parseDesign(DesignContext ctx) {
        PackageNode result = PackageNode.of(parsePackageInfo(ctx.header())).setSourceLoc(locationOf(ctx.getStart()));
        for (var child : ctx.children) {
            switch (child) {
                case Import_statementContext sub -> parseImportInto(sub, result);
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

    static void parseImportInto(Import_statementContext ctx, PackageNode design) {
        parseImport(ctx).forEach(design::addDefinition);
    }

    static void parseComponentInto(ComponentContext ctx, PackageNode design) {
        design.addDefinition(parseComponent(ctx));
    }

    static  void parseInterfaceInto(InterfaceContext ctx, PackageNode design) {
        design.addDefinition(parseInterface(ctx));
    }

    static void parseEnumInto(EnumContext ctx, PackageNode design) {
        design.addDefinition(parseEnum(ctx));
    }

    static EnumNode parseEnum(EnumContext ctx) {
        var iterator = ctx.ID().iterator();
        EnumNode result = EnumNode.of(parseIdentifier(iterator.next()));
        iterator.forEachRemaining(field -> result.addField(parseIdentifier(field)));
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static PackageInfoNode parsePackageInfo(HeaderContext ctx) {
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

    static ComponentNode parseComponent(ComponentContext ctx) {
        var identifier = parseIdentifier(ctx.ID());
        var attributes = parseComponentAttributes(ctx);
        var type = parseComponentType(ctx.component_type(), locationOf(ctx.getStart()));
        var result = switch (ctx.component_body()) {
            case ImplementedContext body ->
                    ComponentNode.of(identifier, type, attributes, parseElements(body.element()));
            case PassedContext _ ->
                    ComponentNode.ofPassed(identifier, type, attributes);
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static InterfaceNode parseInterface(InterfaceContext ctx) {
        if (!(ctx.component_body() instanceof ImplementedContext body)) {
            log.error("{}: Expected an implemented interface", locationOf(ctx.getStart()));
            throw new CompileCancellationException();
        }
        var type = parseExpr(ctx.expression());
        var identifier = parseIdentifier(ctx.ID());
        var attributes = parseInterfaceAttributes(ctx);
        var result = InterfaceNode.of(identifier, type, attributes, parseElements(body.element()));
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static List<ImportNode> parseImport(Import_statementContext ctx) {
        return switch (ctx.lib_reference()) {
            case CommonLibRefContext refCtx -> parseCommonImport(ctx, refCtx);
            case WildcardLibRefContext refCtx -> parseWildcardImport(ctx, refCtx);
            default -> throw new ShallNotReachHereError();
        };
    }

    private static List<ImportNode> parseWildcardImport(Import_statementContext ctx, WildcardLibRefContext refCtx) {
        return List.of(
                ImportNode.ofWildcard(
                        refCtx.ID().stream().map(TreeParser::parseIdentifier).toList()
                ).setSourceLoc(locationOf(ctx.getStart()))
        );
    }

    private static List<ImportNode> parseCommonImport(Import_statementContext ctx, CommonLibRefContext refCtx) {
        var prefix = refCtx.ID().stream().map(TreeParser::parseIdentifier).toList();
        List<ImportNode> results;
        if (refCtx.lib_reference_set() != null) {
            results = refCtx.lib_reference_set().ID().stream()
                    .map(TreeParser::parseIdentifier)
                    .map(id -> new ArrayList<>(prefix) {{
                        add(id);
                    }})
                    .map(ImportNode::of)
                    .toList();
        } else {
            results = List.of(ImportNode.of(prefix));
        }
        if (ctx.lib_alias() != null) {
            parseImportAlias(ctx.lib_alias(), results);
        }
        results.forEach(stmt -> stmt.setSourceLoc(locationOf(ctx.getStart())));
        return results;
    }

    private static void parseImportAlias(Lib_aliasContext ctx, List<ImportNode> results) {
        List<Identifier> aliasID;
        if (ctx.ID() != null) {
            aliasID = List.of(parseIdentifier(ctx.ID()));
        } else {
            aliasID = ctx.lib_reference_set().ID().stream().map(TreeParser::parseIdentifier).toList();
        }
        if (aliasID.size() != results.size()) {
            log.error("{}: Unmatched number of import alias", locationOf(ctx.getStart()));
            throw new CompileCancellationException();
        }
        var iter = aliasID.iterator();
        results.forEach(stmt -> stmt.setAlias(iter.next()));
    }

    static List<Identifier> parseInterfaceAttributes(InterfaceContext ctx) {
        if (ctx.attributes() == null) {
            return List.of();
        }
        return ctx.attributes().ID().stream()
                .map(TreeParser::parseIdentifier).toList();
    }

    static List<Identifier> parseComponentAttributes(ComponentContext ctx) {
        if (ctx.attributes() == null) {
            return List.of();
        }
        return ctx.attributes().ID().stream()
                .map(TreeParser::parseIdentifier).toList();
    }

    static ComponentTypeNode parseComponentType(Component_typeContext ctx, SourceLoc sourceLoc) {
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

    static ComponentElement parseElement(ElementContext ctx) {
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

    static ComponentInstantiationNode parseComponentInstantiation(ComponentInstantiationContext ctx) {
        return ComponentInstantiationNode.of(parseExpr(ctx.expression()), false);
    }

    static ComponentInstantiationNode parseMixedInstantiation(MixedInstantiationContext ctx) {
        return ComponentInstantiationNode.of(parseExpr(ctx.expression()), true);
    }

    static LoopNode parseLoop(LoopContext ctx) {
        return LoopNode.of(parseGeneratorQualifier(ctx.iterator()), parseElement(ctx.element()));
    }

    static BranchNode parseBranch(BranchContext ctx) {
        var result = BranchNode.of(parseExpr(ctx.cond), parseElement(ctx.thenBranch), null);
        if (ctx.elseBranch != null) {
            result.setElseBranch(parseElement(ctx.elseBranch));
        }
        return result;
    }

    static DataInstantiationNode parseDataInstantiation(DataDeclarationContext ctx) {
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

    static List<DataRestNode> parseDeclarationList(Declaration_listContext ctx) {
        List<DataRestNode> result = new ArrayList<>();
        for (var sub : ctx.declaration_symbol()) {
            DataRestNode data = DataRestNode.of(
                    parseIdentifier(sub.ID()),
                    sub.expression().stream().map(TreeParser::parseExpr).toList()
            ).setSourceLoc(locationOf(sub.getStart()));
            result.add(data);
        }
        return result;
    }

    static ExprNode parseExpr(ExpressionContext ctx) {
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

    static SymbolExpr parseSymbolExpr(SymbolExprContext ctx) {
        return parseSymbolExpr(ctx.symbol());
    }

    static SymbolExpr parseSymbolExpr(SymbolContext ctx) {
        return switch (ctx) {
            case PrimarySymbolContext sub -> parsePrimarySymbol(sub);
            case CallSymbolContext sub -> parseParamSymbolExpr(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static ListExpr parseListExpr(ListExprContext ctx) {
        return switch (ctx.list()) {
            case CommonListContext sub -> CommonList.of(parseExprList(sub.expression()));
            case RangeListContext sub -> RangeList.of(parseExpr(sub.from), parseExpr(sub.to));
            case ListComprehensionContext sub -> parseListComprehension(sub);
            case EmptyListContext ignored -> CommonList.ofEmpty();
            default -> throw new ShallNotReachHereError();
        };
    }

    static ListComprehension parseListComprehension(ListComprehensionContext ctx) {
        return ListComprehension.of(
                parseExpr(ctx.expression()),
                ctx.qualifier().stream().map(TreeParser::parserQualifier).toList());
    }

    static Qualifier parserQualifier(QualifierContext ctx) {
        var result = switch (ctx) {
            case GeneratorContext sub -> parseGeneratorQualifier(sub.iterator());
            case FilterContext sub -> FilterQualifier.of(parseExpr(sub.expression()));
            case LocalBindingContext sub -> BindingQualifier.of(parseBinding(sub.binding()));
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static GeneratorQualifier parseGeneratorQualifier(IteratorContext ctx) {
        return GeneratorQualifier.of(
                parseIdentifier(ctx.ID()),
                parseExpr(ctx.expression())
        ).setSourceLoc(locationOf(ctx.getStart()));
    }

    static BindingNode parseBinding(BindingContext ctx) {
        return BindingNode.of(parseSymbolExpr(ctx.symbol()), parseExpr(ctx.expression()))
                .setSourceLoc(locationOf(ctx.getStart()));
    }

    static ExprNode parsePortExpr(PortExprContext ctx) {
        List<PortConnectionExpr.PortAssign> ports = new ArrayList<>();
        for (var assign : ctx.port_assign()) {
            var lhs = parseExpr(assign.lhs);
            var rhs = parseExpr(assign.rhs);
            ports.add(new PortConnectionExpr.PortAssign(lhs, rhs));
        }
        return PortConnectionExpr.of(ports);
    }

    static ExprNode parseUnaryExpr(UnaryExprContext ctx) {
        UnaryExpr.Operator op;
        if (ctx.prev != null) {
            op = UnaryExpr.Operator.SINK;
        } else if (ctx.post != null) {
            op = UnaryExpr.Operator.SOURCE;
        } else {
            op = UnaryExpr.Operator.fromString(ctx.op.getText());
        }
        return UnaryExpr.of(op, parseExpr(ctx.expression()));
    }

    static ExprNode parseBinaryExpr(BinaryExprContext ctx) {
        var op = BinaryExpr.Operator.fromString(ctx.op.getText());
        return BinaryExpr.of(op, parseExpr(ctx.lhs), parseExpr(ctx.rhs));
    }

    static NumberExpr parseNumberExpr(NumberExprContext ctx) {
        return switch (ctx.number()) {
            case IntNumberContext sub -> IntNumber.of(Integer.parseUnsignedInt(sub.UNSIGNED_INTEGER().getText()));
            case BoolNumberContext sub -> BoolNumber.of(Boolean.parseBoolean(sub.BOOLEAN().getText()));
            case BitVectorNumberContext sub -> parseBitVectorNumber(sub);
            default -> throw new ShallNotReachHereError();
        };
    }

    static BitVectorNumber parseBitVectorNumber(BitVectorNumberContext ctx) {
        return BitVectorNumber.of(BitVector.parse(ctx.getText()));
    }

    static ExprNode parseFieldExpr(FieldExprContext ctx) {
        return FieldExpr.of(
                parseExpr(ctx.expression()),
                parseIdentifier(ctx.ID()));
    }

    static ExprNode parseAccessExpr(AccessExprContext ctx) {
        var result = parseExpr(ctx.expression());
        for (var selector : ctx.selector()) {
            result = AccessExpr.of(result, parseSelector(selector)).setSourceLoc(result.getSourceLoc());
        }
        return result;
    }

    static Selector parseSelector(SelectorContext ctx) {
        var result = switch (ctx) {
            case BitSelectorContext sub -> BitSelector.of(parseExpr(sub.expression()));
            case PartSelectorContext sub -> PartSelector.of(parseExpr(sub.from), parseExpr(sub.to));
            default -> throw new ShallNotReachHereError();
        };
        return result.setSourceLoc(locationOf(ctx.getStart()));
    }

    static CallSymbolExpr parseParamSymbolExpr(CallSymbolContext ctx) {
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

    static List<Pair<Identifier, ExprNode>> parseAttrAssigns(List<Attribute_assignContext> list) {
        List<Pair<Identifier, ExprNode>> result = new ArrayList<>();
        for (var assign : list) {
            var id = parseIdentifier(assign.ID());
            var expr = parseExpr(assign.expression());
            result.add(Pair.of(id, expr));
        }
        return result;
    }

    static ExprNode parseConditionalExpr(ConditionalExprContext ctx) {
        return ConditionalExpr.of(parseExpr(ctx.cond), parseExpr(ctx.thenBranch), parseExpr(ctx.elseBranch));
    }

    static Identifier parseIdentifier(TerminalNode ID) {
        return Identifier.of(ID.getText()).setSourceLoc(locationOf(ID.getSymbol()));
    }

    static IdentifierExpr parsePrimarySymbol(PrimarySymbolContext ctx) {
        return IdentifierExpr.of(parseIdentifier(ctx.ID())).setSourceLoc(locationOf(ctx.getStart()));
    }

    static List<ComponentElement> parseElements(List<ElementContext> list) {
        List<ComponentElement> result = new ArrayList<>();
        for (var element : list) {
            result.add(parseElement(element));
        }
        return result;
    }

    static List<ExprNode> parseExprList(List<ExpressionContext> list) {
        List<ExprNode> result = new ArrayList<>();
        for (var expr : list) {
            result.add(parseExpr(expr));
        }
        return result;
    }

    static SourceLoc locationOf(Token token) {
        return new SourceLoc(token.getLine(), token.getCharPositionInLine());
    }
}
