package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.element.*;
import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.exception.TypeCheckingException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.EntityList;
import pascal.netx.compiler.model.Signature;
import pascal.netx.compiler.model.expr.*;
import pascal.netx.compiler.model.scope.Attributes;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.binding.MonoBinding;
import pascal.netx.compiler.model.variable.Array;
import pascal.netx.compiler.model.variable.Bundle;
import pascal.netx.compiler.model.variable.Composition;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.type.ArrayType;
import pascal.netx.compiler.model.variable.type.InterfaceType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.*;

import static pascal.netx.compiler.model.Entities.requiresInstantiateExpr;
import static pascal.netx.compiler.model.Utils.*;

final class ComponentElaborator {
    private static final Logger log = LoggerFactory.getLogger(ComponentElaborator.class);
    private final ComponentScope scope;
    private final ComponentEvaluator evaluator;

    private final Configuration config;
    private final BuiltInComponentFactory factory;

    ComponentElaborator(Configuration config, ComponentScope scope) {
        this.config = config;
        this.scope = scope;
        this.factory = compilerOf(scope).getBuiltinFactory();
        this.evaluator = scope.getEvaluator();
    }

    public void build(ComponentNode node) {
        buildComponentElements(node.getElements());
        buildComponentIO(node);
    }

    private void buildComponentIO(ComponentNode node) {
        var sourceLoc = node.getSourceLoc();
        var type = node.getType();
        extractIOList(sourceLoc, type.getInput()).forEach(scope::addInput);
        extractIOList(sourceLoc, type.getOutput()).forEach(scope::addOutput);
    }

    private List<Variable> extractIOList(SourceLoc sourceLoc, ExprNode node) {
        var entity = evaluator.evaluate(node);
        if (entity instanceof EntityList(var list)) {
            List<Variable> result = new ArrayList<>();
            for (var member : list) {
                if (member instanceof Variable variable) {
                    result.add(variable);
                } else {
                    log.error("{}: Expected list of instances but got {}", sourceLoc, list);
                    throw new CompileCancellationException();
                }
            }
            return result;
        } else if (entity instanceof Variable variable) {
            return List.of(variable);
        }
        log.error("{}: Expected list of instances but got {}", sourceLoc, entity);
        throw new CompileCancellationException();
    }

    private void buildComponentElements(List<ComponentElement> list) {
        for (var element : list) {
            switch (element) {
                case LoopNode loop -> buildLoop(loop);
                case BindingNode binding -> buildBinding(binding);
                case DataInstantiationNode data -> buildData(data);
                case ComponentInstantiationNode instantiation -> buildSubComponent(instantiation);
            }
        }
    }

    private void buildLoop(LoopNode loop) {
        var generator = loop.getGenerator();
        var id = generator.getHead();
        for (var entity : evaluator.evaluateToList(generator.getFrom()).entities()) {
            scope.overrideBindings(MonoBinding.of(id, entity));
            buildComponentElements(loop.getElements());
            scope.restoreBindings();
        }
    }

    private void buildBinding(BindingNode binding) {
        scope.bind(evaluator.evaluateBinding(binding));
    }

    private void buildData(DataInstantiationNode node) {
        var baseFactory = evaluator.evaluateToMutableFactory(node.getType());
        switch (node.getKind()) {
            case BUNDLE -> checkFactoryLegality(node.getSourceLoc(), baseFactory.getType(), true);
            case WIRE -> checkFactoryLegality(node.getSourceLoc(), baseFactory.getType(), false);
        }
        for (var rest : node.getDataRests()) {
            var factory = baseFactory;
            if (!rest.getDimensions().isEmpty()) {
                for (ExprNode dimension : rest.getDimensions()) {
                    var size = evaluator.evaluateToInt(dimension);
                    factory = new Array.Factory(compilerOf(scope), size, factory);
                }
            }
            var id = rest.getIdentifier();
            scope.bind(id, factory.create(scope, id));
        }
    }

    private void checkFactoryLegality(SourceLoc sourceLoc, Type type, boolean isBundle) {
        if (type instanceof ArrayType arrayType) {
            checkFactoryLegality(sourceLoc, arrayType.baseType(), isBundle);
        } else {
            if (isBundle) {
                if (!(type instanceof InterfaceType)) {
                    log.error("{}: Expected bundle type got {}", sourceLoc, type);
                    throw new CompileCancellationException();
                }
            } else {
                if (type instanceof InterfaceType) {
                    log.error("{}: Expected wire type but got {}", sourceLoc, type);
                    throw new CompileCancellationException();
                }
            }
        }
    }

    private void buildSubComponent(ComponentInstantiationNode instantiation) {
        var sourceLoc = instantiation.getSourceLoc();
        boolean inPlace = instantiation.needDeclareInPlace();
        var compType = evaluator.evaluateToComponentExpr(instantiation.getExpr(), inPlace);
        new Instantiator(sourceLoc).instantiate(compType);
    }

    private class Instantiator {
        private final SourceLoc sourceLoc;

        private Instantiator(SourceLoc sourceLoc) {
            this.sourceLoc = sourceLoc;
        }

        private static List<PrimaryExpr> leftMostAtomsOf(Expr expr) {
            List<PrimaryExpr> result = new ArrayList<>();
            switch (expr) {
                case PrimaryExpr e -> result.add(e);
                case SequentialExpr e -> result.addAll(leftMostAtomsOf(e.lhs()));
                case ParallelExpr e -> e.exprs().forEach(sub -> result.addAll(leftMostAtomsOf(sub)));
                case DecoratedExpr e -> {
                    if (Objects.requireNonNull(e.decorator()) != DecoratedExpr.Decorator.SINK) {
                        result.addAll(leftMostAtomsOf(e.operand()));
                    }
                }
            }
            return result;
        }

        /**
         * Check whether the type of e_1 and e_2 matches in e_1 <+> e_2
         */
        private static void checkTiedType(SourceLoc sourceLoc, List<Bundle> to, List<PrimaryExpr> from) {
            int size = requiresMatchedSize(sourceLoc, from, to);
            for (int i = 0; i < size; i++) {
                if (from.get(i) instanceof ComponentRef fi
                        && fi.getDefinition().getKind() == ComponentNode.Kind.INTERFACE) {
                    var lhsID = to.get(i).getType().getIdentifier();
                    var rhsID = fi.getDefinition().getIdentifier();
                    if (lhsID.equals(rhsID)) {
                        continue;
                    }
                    log.error("{}: Unmatched type in isTied expression {} and {}", sourceLoc, lhsID, rhsID);
                } else {
                    log.error("{}: Expected interface but got {}", sourceLoc, from.get(i));
                }
                throw new TypeCheckingException();
            }
        }

        public void instantiate(Expr type) {
            doInstantiate(type);
        }

        ComponentPort doInstantiate(Expr expr) {
            return switch (expr) {
                case SequentialExpr sequential -> instantiateSequentialExpr(sequential);
                case ParallelExpr parallel -> instantiateParallelExpr(parallel);
                case PrimaryExpr primary -> instantiatePrimaryExpr(primary);
                case DecoratedExpr decorated -> instantiateDecoratedExpr(decorated);
            };
        }

        private ComponentPort instantiateDecoratedExpr(DecoratedExpr expr) {
            var port = doInstantiate(expr.operand());
            return switch (expr.decorator()) {
                case SOURCE -> ComponentPort.of(port.target, List.of());
                case SINK -> ComponentPort.of(List.of(), port.source);
                case BIDI -> port;
            };
        }

        private ComponentPort instantiatePrimaryExpr(PrimaryExpr expr) {
            return switch (expr) {
                case ComponentRef ref -> instantiateComponentRef(ref);
                case VariableRef ref -> instantiateInstanceRef(ref);
                case BuiltinRef ref -> instantiateBuiltinRef(ref);
                case ImportRef ref -> instantiateImportRef(ref);
            };
        }

        private ComponentPort instantiateSequentialExpr(SequentialExpr sequentialExpr) {
            // right-hand-side goes first
            var rhs = doInstantiate(sequentialExpr.rhs());
            var lhs = doInstantiate(sequentialExpr.lhs());

            List<? extends Variable> receiver;
            if (sequentialExpr.isTied()) {
                var bundles = lhs.source().stream().map(Bundle.class::cast).toList();
                checkTiedType(sourceLoc, bundles, leftMostAtomsOf(sequentialExpr.rhs()));
                receiver = bundles.stream().map(Composition::getElements).flatMap(Collection::stream).toList();
            } else {
                receiver = lhs.source();
            }

            int size = requiresMatchedSize(sourceLoc, receiver, rhs.target());
            for (int i = 0; i < size; ++i) {
                scope.getManager().assign(sourceLoc, receiver.get(i), rhs.target().get(i));
            }
            return ComponentPort.of(lhs.target(), rhs.source());
        }

        private ComponentPort instantiateParallelExpr(ParallelExpr parallelExpr) {
            List<Variable> input = new LinkedList<>(), output = new LinkedList<>();
            for (var sub : parallelExpr.exprs()) {
                var port = doInstantiate(sub);
                input.addAll(port.target());
                output.addAll(port.source());
            }
            return ComponentPort.of(input, output);
        }

        private ComponentPort instantiateInstanceRef(VariableRef ref) {
            var inst = List.of(ref.getInstance());
            return ComponentPort.of(inst, inst);
        }

        private ComponentPort instantiateImportRef(ImportRef ref) {
            SourceLoc sourceLoc = ref.getSourceLoc();
            List<Identifier> reference = ref.getReferences();

            var compiler = projectOf(scope).getManager().dispatchCompiler(sourceLoc, reference);
            var component = switch (reference.size()) {
                case 2 -> {
                    var pack = compiler.getPackManager().resolvePackage(sourceLoc, reference.get(0));
                    yield compiler.elaborate(pack, reference.get(1), ref.getParameters());
                }
                case 3 -> {
                    var pack = compiler.getPackManager().resolvePackage(sourceLoc, reference.get(1));
                    yield compiler.elaborate(pack, reference.get(2), ref.getParameters());
                }
                default -> throw new ShallNotReachHereError();
            };

            component.setParent(scope);
            component.setPackage(scope.getPackage());

            return ComponentPort.of(component.getOutputs(), component.getInputs());
        }

        private ComponentPort instantiateBuiltinRef(BuiltinRef ref) {
            var component = factory.createBuiltInComponent(scope, ref);
            if (ref.getBuiltin() == BuiltinComponent.REGISTER) {
                var clock = doInstantiate(requiresInstantiateExpr(sourceLoc, ref.getParameters().getFirst()));
                requiresMatchedSize(sourceLoc, 1, clock.target);
                component.getAttributes().addAttribute(Identifier.of("clock"), clock.target.getFirst());
            }
            return ComponentPort.of(component.getOutputs(), component.getInputs());
        }

        private ComponentPort instantiateComponentRef(ComponentRef ref) {
            var node = ref.getDefinition();
            ComponentScope component = switch (ref.getDefinition().getKind()) {
                case COMPONENT -> ComponentScope.newComponent(scope, ref.getIdentifier());
                case INTERFACE -> ComponentScope.newInterface(scope, ref.getIdentifier());
            };

            var attributes = extractAttributes(ref, node);
            for (var entry : attributes.entrySet()) {
                component.bind(entry.getKey(), entry.getValue());
            }
            component.setAttributes(attributes);
            new ComponentElaborator(config, component).build(node);
            if (!config.fullyExpand()) {
                setSignatureFor(ref, component);
            }
            return ComponentPort.of(component.getOutputs(), component.getInputs());
        }

        private void setSignatureFor(ComponentRef ref, ComponentScope scope) {
            var signature = Signature.of(ref, scope.getInputs(), scope.getOutputs());
            scope.setSignature(signature);
        }

        private Attributes extractAttributes(ComponentRef ref, ComponentNode node) {
            Attributes attributes = new Attributes();
            int size = requiresMatchedSize(sourceLoc, node.getAttributes(), ref.getParameters());
            for (int i = 0; i < size; i++) {
                var id = node.getAttributes().get(i);
                var entity = ref.getParameters().get(i);
                attributes.addAttribute(id, entity);
            }
            return attributes;
        }

        private record ComponentPort(List<Variable> target, List<Variable> source) {
            public static ComponentPort of(List<Variable> target, List<Variable> source) {
                return new ComponentPort(target, source);
            }
        }
    }

}
