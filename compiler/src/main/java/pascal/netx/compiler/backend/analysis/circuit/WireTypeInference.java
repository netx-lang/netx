package pascal.netx.compiler.backend.analysis.circuit;

import pascal.netx.compiler.backend.analysis.circuit.constraint.*;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.model.type.Hole;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.compiler.utils.TypeCheckingException;
import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class performs type inference for data variables in the design.
 * It collects type constraints from the design and infers types for all data variables.
 * If a variable cannot be inferred, it will throw a TypeCheckingException.
 */
public final class WireTypeInference extends CircuitAnalysis {
    private final UnionFind<Signal> equalSignals = new UnionFind<>();
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
        switch (component.asBuiltin()) {
            case OR, NOT, AND, XOR, NOR, NAND, XNOR, ADD, SUB, MUL, DIV, MOD -> addAllEqualConstraints(component);
            case SGE, SGT, SLE, SLT, UGE, UGT, ULE, ULT, EQ, NE -> addInputEqualConstraints(component);
            case CONCAT -> addConstraintForConcat(component);
            case REGISTER -> addContainsForRegister(component);
            case AS_CLK, AS_BIT -> { }
        }
    }

    private void addInputEqualConstraints(ComponentScope component) {
        var inputs = component.getInputs();
        for (int i = 0; i < inputs.size() - 1; i++) {
            addConstraint(new EqualTypeConstraint(inputs.get(i), inputs.get(i + 1)));
        }
    }

    private void addAllEqualConstraints(ComponentScope component) {
        var dataList = Stream.concat(component.getInputs().stream(), component.getOutputs().stream()).toList();
        if (dataList.isEmpty()) {
            return;
        }
        Signal representative = dataList.getFirst();
        for (int i = 1; i < dataList.size(); i++) {
            addConstraint(new EqualTypeConstraint(representative, dataList.get(i)));
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
            equalSignals.union(lhs, rhs);
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
        Map<Signal, List<Signal>> components = new HashMap<>();
        for (var signal : equalSignals.elements()) {
            components.computeIfAbsent(equalSignals.find(signal), ignored -> new LinkedList<>()).add(signal);
        }

        while (!workList.isEmpty()) {
            var data = workList.poll();
            if (equalSignals.contains(data)) {
                for (var signal : components.getOrDefault(equalSignals.find(data), List.of())) {
                    if (signal != data) {
                        setTypeFor(signal, data.getType(), new EqualTypeConstraint(signal, data));
                    }
                }
            }
        }
    }

    private void setTypeFor(Signal signal, Type type) {
        setTypeFor(signal, type, null);
    }

    private void setTypeFor(Signal signal, Type type, WireConstraint sourceConstraint) {
        if (type instanceof Hole) {
            throw new ShallNotReachHereError();
        }
        if (!signal.hasImplicitType()) {
            if (!signal.getType().equals(type)) {
                StringBuilder message = new StringBuilder("Type constraint violation: conflicting types:\n")
                        .append(WireConstraint.describeSignalBlock("signal", signal))
                        .append("\n")
                        .append("  current type: ").append(signal.getType())
                        .append("\n")
                        .append("  inferred type: ").append(type);
                if (sourceConstraint != null) {
                    message.append("\n")
                            .append("  caused by: ")
                            .append(sourceConstraint.error().replace("\n", "\n  "));
                }
                Logger.error(message.toString());
                throw new TypeCheckingException();
            }
            return;
        }
        signal.setType(type);
        workList.add(signal);
    }

    private void checkAllTyped() {
        pack.forEachSubComponent(scope ->
                scope.forEachEntity(entity -> {
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
