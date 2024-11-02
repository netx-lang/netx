package pascal.hardscript.compiler.analysis;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.analysis.constraint.BinaryConstraint;
import pascal.hardscript.compiler.analysis.constraint.ClockConstraint;
import pascal.hardscript.compiler.analysis.constraint.DataConstraint;
import pascal.hardscript.compiler.analysis.constraint.EqualTypeConstraint;
import pascal.hardscript.compiler.exception.TypeInferenceException;
import pascal.hardscript.compiler.model.data.AutoInferenceType;
import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.data.DataAccess;
import pascal.hardscript.compiler.model.data.DataType;
import pascal.hardscript.compiler.model.scope.ComponentInfo;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

public class DataTypeInference extends ModelAnalysis {
    private static final Logger log = LoggerFactory.getLogger(DataTypeInference.class);
    private final Graph<Data, BinaryConstraint> graph = new SimpleGraph<>(BinaryConstraint.class);
    private final List<DataConstraint> constraints = new LinkedList<>();
    private final Queue<Data> workList = new LinkedList<>();
    private final ComponentInfo componentInfo;

    public DataTypeInference(DesignScope design) {
        super(design);
        this.componentInfo = design.getComponentInfo();
    }

    @Override
    public void analyze() {
        collectTypeConstraints();
        doBinaryTypeInference();
        checkAllTyped();
    }

    public void collectTypeConstraints() {
        design.forEachScope(scope -> {
            for (var data : getBoundDataIn(scope)) {
                if (!(data.getType() instanceof AutoInferenceType)) {
                    workList.add(data);
                }
            }
            if (scope instanceof ComponentScope component) {
                if (componentInfo.containsBuiltin(component.getIdentifier())) {
                    collectConstraintsInBuiltin(component);
                }
            }
        });
    }

    private void collectConstraintsInBuiltin(ComponentScope component) {
        switch (BuiltinComponent.of(component.getIdentifier().string())) {
            case OR, NOT, AND, XOR, NOR, NAND -> addConstrainsForGate(component);
            case REGISTER -> addContainsForRegister(component);
            case TO, SPLIT, IGNORE, SOURCE -> {
                // do nothing
            }
        }
    }

    private void addConstrainsForGate(ComponentScope component) {
        var dataList = Stream.concat(component.getInputs().stream(), component.getOutputs().stream()).toList();
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = i + 1; j < dataList.size(); j++) {
                var constraint = EqualTypeConstraint.of(dataList.get(i), dataList.get(j));
                constraints.add(constraint);
                addBinaryConstraint(constraint);
                addBinaryConstraint(constraint);
            }
        }
    }

    private void addContainsForRegister(ComponentScope component) {
        Data input = component.getInputs().getFirst(), output = component.getOutputs().getFirst();
        var constraint = EqualTypeConstraint.of(input, output);
        addBinaryConstraint(constraint);
        addBinaryConstraint(constraint);
        constraints.add(constraint);

        var clock = (Data) component.getAttributes().get("clock");
        constraints.add(ClockConstraint.of(clock));
    }

    private void addBinaryConstraint(BinaryConstraint constraint) {
        Data lhs = constraint.getLhs(), rhs = constraint.getRhs();
        graph.addVertex(lhs);
        graph.addVertex(rhs);
        graph.addEdge(lhs, rhs, constraint);
    }

    private void doBinaryTypeInference() {
        handleSelectorInference();
        while (!workList.isEmpty()) {
            var data = workList.poll();
            if (graph.containsVertex(data)) {
                for (var constraint : graph.edgesOf(data)) {
                    Data lhs = constraint.getLhs(), rhs = constraint.getRhs();
                    if (lhs.getType() instanceof AutoInferenceType) {
                        lhs.setType(constraint.expectedLhs());
                        workList.add(lhs);
                    } else if (rhs.getType() instanceof AutoInferenceType) {
                        rhs.setType(constraint.expectedRhs());
                        workList.add(rhs);
                    }
                }
            }
        }
    }

    private void handleSelectorInference() {
        for (var constraint : constraints) {
            if (constraint instanceof BinaryConstraint binary) {
                Data lhs = binary.getLhs(), rhs = binary.getRhs();
                DataType lhsType = lhs.getType(), rhsType = rhs.getType();
                if (lhs instanceof DataAccess && rhsType instanceof AutoInferenceType) {
                    rhs.setType(binary.expectedRhs());
                    workList.add(rhs);
                } else if (rhs instanceof DataAccess && lhsType instanceof AutoInferenceType) {
                    lhs.setType(binary.expectedLhs());
                    workList.add(lhs);
                }
            }
        }
    }

    private void checkAllTyped() {
        design.forEachScope(scope -> {
            for (var entity : scope.getEntities()) {
                if (!(entity instanceof Data data)) {
                    throw new ShallNotReachHereError();
                }
                if (data.getType() instanceof AutoInferenceType) {
                    log.error("{}: Cannot infer type of data: {}", data.getSourceLoc(), data);
                    throw new TypeInferenceException();
                }
            }
        });

        for (var constraint : constraints) {
            if (!constraint.holds()) {
                log.error("Type constraint violation: {}", constraint.error());
                throw new TypeInferenceException();
            }
        }
    }

    private static List<Data> getBoundDataIn(Scope scope) {
        return scope.getEntities().stream().map(Data.class::cast).toList();
    }
}
