package pascal.hardscript.compiler.analysis;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.analysis.constraint.*;
import pascal.hardscript.compiler.exception.TypeCheckingException;
import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

public final class DataTypeInference extends ModelAnalysis {
    private static final Logger log = LoggerFactory.getLogger(DataTypeInference.class);
    private final Graph<Instance, BinaryConstraint> graph = new SimpleGraph<>(BinaryConstraint.class);
    private final List<DataConstraint> constraints = new LinkedList<>();
    private final Queue<Instance> workList = new LinkedList<>();

    public DataTypeInference(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        collectTypeConstraints();
        doBinaryTypeInference();
        checkAllTyped();
    }

    public void collectTypeConstraints() {
        design.forEachSubscope(scope -> {
            for (var alias : scope.getAliases()) {
                addBinaryConstraint(EqualTypeConstraint.of(alias.lhs(), alias.rhs()));
            }
            var component = (ComponentScope) scope;
            if (component.isBuiltin()) {
                collectConstraintsInBuiltin(component);
            }
        });
    }

    private void collectConstraintsInBuiltin(ComponentScope component) {
        switch (BuiltinComponent.of(component.getIdentifier())) {
            case OR, NOT, AND, XOR, NOR, NAND, XNOR, ADD, SUB, MUL, DIV, MOD ->
                    addAllEqualConstraints(component);
            case REGISTER -> addContainsForRegister(component);
            case SPLIT, IGNORE -> {
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
        Instance input = component.getInputs().getFirst(), output = component.getOutputs().getFirst();
        addConstraint(EqualTypeConstraint.of(input, output));
        addConstraint(ClockConstraint.of((Instance) component.getAttributes().get(Identifier.of("clock"))));
    }

    private void addConstraint(DataConstraint constraint) {
        switch (constraint) {
            case UnaryConstraint unary -> addUnaryConstraint(unary);
            case BinaryConstraint binary -> addBinaryConstraint(binary);
        }
    }

    private void addUnaryConstraint(UnaryConstraint unary) {
        constraints.add(unary);
    }

    private void addBinaryConstraint(BinaryConstraint binary) {
        Instance lhs = binary.getLhs(), rhs = binary.getRhs();
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
                    Instance lhs = constraint.getLhs(), rhs = constraint.getRhs();
                    if (lhs.hasImplicitType()) {
                        setTypeFor(lhs, constraint.expectedLhs());
                    } else if (rhs.hasImplicitType()) {
                        setTypeFor(rhs, constraint.expectedRhs());
                    }
                }
            }
        }
    }

    private void setTypeFor(Instance instance, InstanceType type) {
        instance.setType(type);
        workList.add(instance);
    }

    private void checkAllTyped() {
        design.forEachSubscope(scope -> {
            for (var entity : scope.getEntities()) {
                if (!(entity instanceof Instance inst)) {
                    throw new ShallNotReachHereError();
                }
                if (inst.hasImplicitType()) {
                    log.error("{}: Cannot infer type of data: {}", ((Data) inst).getSourceLoc(), inst);
                    throw new TypeCheckingException();
                }
            }
        });
        constraints.forEach(this::throwIfConstraintNotHolds);
    }

    private void throwIfConstraintNotHolds(DataConstraint constraint) {
        if (!constraint.holds()) {
            log.error("Type constraint violation: {}", constraint.error());
            throw new TypeCheckingException();
        }
    }
}
