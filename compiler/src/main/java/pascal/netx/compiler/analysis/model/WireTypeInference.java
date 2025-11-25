package pascal.netx.compiler.analysis.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import pascal.netx.compiler.analysis.model.constraint.*;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.signal.Signal;
import pascal.netx.compiler.circuit.signal.type.BitType;
import pascal.netx.compiler.circuit.signal.type.PolyType;
import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.compiler.utils.TypeCheckingException;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * This class performs type inference for data variables in the design.
 * It collects type constraints from the design and infers types for all data variables.
 * If a variable cannot be inferred, it will throw a TypeCheckingException.
 */
public final class WireTypeInference extends ModelAnalysis {
    private final Graph<Signal, BinaryConstraint> graph = new SimpleGraph<>(BinaryConstraint.class);
    private final List<WireConstraint> constraints = new LinkedList<>();
    private final Queue<Signal> workList = new LinkedList<>();

    public WireTypeInference(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        collectTypeConstraints();
        doTypeInference();
        checkAllTyped();
    }

    public void collectTypeConstraints() {
        pack.forEachSubComponent(scope -> {
            for (var alias : scope.getAliases()) {
                addBinaryConstraint(new EqualTypeConstraint(alias.lhs(), alias.rhs()));
            }
            if (scope.isBuiltin()) {
                collectConstraintsInBuiltin(scope);
            }
        });
    }

    private void collectConstraintsInBuiltin(ComponentScope component) {
        switch (BuiltinComponent.of(component.getIdentifier())) {
            case OR, NOT, AND, XOR, NOR, NAND, XNOR, ADD, SUB, MUL, DIV, MOD -> addAllEqualConstraints(component);
            case CONCAT -> addConstraintForConcat(component);
            case REGISTER -> addContainsForRegister(component);
        }
    }

    private void addAllEqualConstraints(ComponentScope component) {
        var dataList = Stream.concat(component.getInputs().stream(), component.getOutputs().stream()).toList();
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = i + 1; j < dataList.size(); j++) {
                addConstraint(new EqualTypeConstraint(dataList.get(i), dataList.get(j)));
            }
        }
    }

    private void addContainsForRegister(ComponentScope component) {
        addConstraint(new ClockConstraint(component.getInputs().get(0)));
        addConstraint(new EqualTypeConstraint(
                component.getInputs().get(1),
                component.getOutputs().getFirst()));
    }

    private void addConstraintForConcat(ComponentScope component) {
        var input = component.getInputs();
        var output = component.getOutputs().getFirst();
        addConstraint(new ConcatConstraint(output, input));
    }

    private void addConstraint(WireConstraint constraint) {
        switch (constraint) {
            case UnaryConstraint unary -> constraints.add(unary);
            case ConcatConstraint concat -> addConcatConstraint(concat);
            case BinaryConstraint binary -> addBinaryConstraint(binary);
        }
    }

    private void addBinaryConstraint(BinaryConstraint binary) {
        Signal lhs = binary.getLhs(), rhs = binary.getRhs();
        if (lhs == rhs) {
            return;
        }

        boolean lhsHasType = !(lhs.hasImplicitType());
        boolean rhsHasType = !(rhs.hasImplicitType());
        if (lhsHasType && rhsHasType) {
            throwIfConstraintNotHolds(binary);
        } else if (lhsHasType) {
            setTypeFor(rhs, binary.expectedRhs());
        } else if (rhsHasType) {
            setTypeFor(lhs, binary.expectedLhs());
        } else {
            constraints.add(binary);
            graph.addVertex(lhs);
            graph.addVertex(rhs);
            graph.addEdge(lhs, rhs, binary);
        }
    }

    private void addConcatConstraint(ConcatConstraint concat) {
        boolean lhsHasType = !(concat.lhs().hasImplicitType());
        boolean rhsHasType = concat.rhs().stream().noneMatch(Signal::hasImplicitType);
        if (lhsHasType && rhsHasType) {
            throwIfConstraintNotHolds(concat);
        } else if (rhsHasType) {
            setTypeFor(concat.lhs(), BitType.of(
                    concat.rhs().stream()
                            .map(Signal::getType)
                            .map(BitType.class::cast)
                            .map(BitType::width)
                            .reduce(0, Integer::sum)));
        } else {
            constraints.add(concat);
        }
    }

    private void doTypeInference() {
        while (!workList.isEmpty()) {
            var data = workList.poll();
            if (graph.containsVertex(data)) {
                for (var constraint : graph.edgesOf(data)) {
                    Signal lhs = constraint.getLhs(), rhs = constraint.getRhs();
                    if (lhs.hasImplicitType()) {
                        setTypeFor(lhs, constraint.expectedLhs());
                    } else if (rhs.hasImplicitType()) {
                        setTypeFor(rhs, constraint.expectedRhs());
                    }
                }
            }
        }
    }

    private void setTypeFor(Signal signal, Type type) {
        if (type instanceof PolyType) {
            throw new ShallNotReachHereError();
        }
        signal.setType(type);
        workList.add(signal);
    }

    private void checkAllTyped() {
        pack.forEachSubComponent(scope ->
                scope.getBinding().forEachEntity(entity -> {
                    if (!(entity instanceof Signal inst)) {
                        throw new ShallNotReachHereError();
                    }
                    if (inst.hasImplicitType()) {
                        Logger.error("Cannot infer type of wire: {}", inst);
                        throw new TypeCheckingException();
                    }
                }
        ));
        constraints.forEach(this::throwIfConstraintNotHolds);
    }

    private void throwIfConstraintNotHolds(WireConstraint constraint) {
        if (!constraint.holds()) {
            Logger.error("Type constraint violation: {}", constraint.error());
            throw new TypeCheckingException();
        }
    }
}
