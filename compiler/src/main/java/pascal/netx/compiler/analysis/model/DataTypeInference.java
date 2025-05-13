package pascal.netx.compiler.analysis.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.analysis.model.constraint.*;
import pascal.netx.compiler.exception.TypeCheckingException;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Data;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.type.PolyType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

public final class DataTypeInference extends ModelAnalysis {
    private static final Logger log = LoggerFactory.getLogger(DataTypeInference.class);
    private final Graph<Variable, BinaryConstraint> graph = new SimpleGraph<>(BinaryConstraint.class);
    private final List<DataConstraint> constraints = new LinkedList<>();
    private final Queue<Variable> workList = new LinkedList<>();

    public DataTypeInference(PackageScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        collectTypeConstraints();
        doBinaryTypeInference();
        checkAllTyped();
    }

    public void collectTypeConstraints() {
        pack.forEachSubComponent(scope -> {
            for (var alias : scope.getAliases()) {
                addBinaryConstraint(EqualTypeConstraint.of(alias.lhs(), alias.rhs()));
            }
            if (scope.isBuiltin()) {
                collectConstraintsInBuiltin(scope);
            }
        });
    }

    private void collectConstraintsInBuiltin(ComponentScope component) {
        switch (BuiltinComponent.of(component.getIdentifier())) {
            case OR, NOT, AND, XOR, NOR, NAND, XNOR, ADD, SUB, MUL, DIV, MOD -> addAllEqualConstraints(component);
            case REGISTER -> addContainsForRegister(component);
            case SPLIT -> {
                // do nothing
            }
        }
    }

    private void addAllEqualConstraints(ComponentScope component) {
        var dataList = Stream.concat(component.getInputs().stream(), component.getOutputs().stream()).toList();
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = i + 1; j < dataList.size(); j++) {
                addConstraint(EqualTypeConstraint.of(dataList.get(i), dataList.get(j)));
            }
        }
    }

    private void addContainsForRegister(ComponentScope component) {
        Variable input = component.getInputs().getFirst(), output = component.getOutputs().getFirst();
        addConstraint(EqualTypeConstraint.of(input, output));
        addConstraint(ClockConstraint.of(component.getInputs().get(1)));
    }

    private void addConstraint(DataConstraint constraint) {
        switch (constraint) {
            case UnaryConstraint unary -> constraints.add(unary);
            case BinaryConstraint binary -> addBinaryConstraint(binary);
        }
    }

    private void addBinaryConstraint(BinaryConstraint binary) {
        Variable lhs = binary.getLhs(), rhs = binary.getRhs();
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

    private void doBinaryTypeInference() {
        while (!workList.isEmpty()) {
            var data = workList.poll();
            if (graph.containsVertex(data)) {
                for (var constraint : graph.edgesOf(data)) {
                    Variable lhs = constraint.getLhs(), rhs = constraint.getRhs();
                    if (lhs.hasImplicitType()) {
                        setTypeFor(lhs, constraint.expectedLhs());
                    } else if (rhs.hasImplicitType()) {
                        setTypeFor(rhs, constraint.expectedRhs());
                    }
                }
            }
        }
    }

    private void setTypeFor(Variable variable, Type type) {
        if (type instanceof PolyType) {
            throw new ShallNotReachHereError();
        }
        variable.setType(type);
        workList.add(variable);
    }

    private void checkAllTyped() {
        pack.forEachSubComponent(scope ->
                scope.getBinding().forEachEntity(entity -> {
                    if (!(entity instanceof Variable inst)) {
                        throw new ShallNotReachHereError();
                    }
                    if (inst.hasImplicitType()) {
                        log.error("{}: Cannot infer type of data: {}", ((Data) inst).getSourceLoc(), inst);
                        throw new TypeCheckingException();
                    }
                }
        ));
        constraints.forEach(this::throwIfConstraintNotHolds);
    }

    private void throwIfConstraintNotHolds(DataConstraint constraint) {
        if (!constraint.holds()) {
            log.error("Type constraint violation: {}", constraint.error());
            throw new TypeCheckingException();
        }
    }
}
