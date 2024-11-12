package pascal.hardscript.compiler.analysis;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.analysis.constraint.*;
import pascal.hardscript.compiler.exception.TypeCheckingException;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
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
        design.forEachScope(scope -> {
            var component = (ComponentScope) scope;
            for (var data : getBoundDataIn(scope)) {
                if (!data.hasImplicitType()) {
                    workList.add(data);
                }
            }
            if (BuiltinComponent.isBuiltin(component.getIdentifier())) {
                collectConstraintsInBuiltin(component);
            }
        });
    }

    private void collectConstraintsInBuiltin(ComponentScope component) {
        switch (BuiltinComponent.of(component.getIdentifier().name())) {
            case OR, NOT, AND, XOR, NOR, NAND -> addAllEqualConstraints(component);
            case REGISTER -> addContainsForRegister(component);
            case SPLIT, IGNORE, FROM -> {
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
        addConstraint(ClockConstraint.of(component.getAttributes().get(Identifier.of("clock"))));
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

        if (lhs instanceof DataAccess && rhs instanceof DataAccess) {
            throwIfConstraintNotHolds(binary);
        } else if (lhs instanceof DataAccess && rhs.hasImplicitType()) {
            rhs.setType(binary.expectedRhs());
            workList.add(rhs);
        } else if (lhs.hasImplicitType() && rhs instanceof DataAccess) {
            lhs.setType(binary.expectedLhs());
            workList.add(lhs);
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
                        lhs.setType(constraint.expectedLhs());
                        workList.add(lhs);
                    } else if (rhs.hasImplicitType()) {
                        rhs.setType(constraint.expectedRhs());
                        workList.add(rhs);
                    }
                }
            }
        }
    }

    private void checkAllTyped() {
        design.forEachScope(scope -> {
            for (var entity : scope.getEntities()) {
                if (!(entity instanceof Instance inst)) {
                    throw new ShallNotReachHereError();
                }
                if (inst.hasImplicitType()) {
                    log.error("{}: Cannot infer type of data: {}", inst.getSourceLoc(), inst);
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

    private static List<Instance> getBoundDataIn(Scope scope) {
        return scope.getEntities().stream().map(Instance.class::cast).toList();
    }
}
