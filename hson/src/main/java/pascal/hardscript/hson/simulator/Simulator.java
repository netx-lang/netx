package pascal.hardscript.hson.simulator;

import pascal.hardscript.hson.model.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.math.BigInteger;
import java.util.*;

public final class Simulator {
    public final static Boolean RANDOM_START = false;
    public final static Boolean DO_VERIFICATION = false;
    private final static Random RANDOM = new Random();

    private final Circuit circuit;
    private final List<ClockNode> clocks = new LinkedList<>();
    private final ValueSupplier supplier = new ValueSupplier();
    private final Map<String, DataNode> ioNodes = new HashMap<>();

    private final ScheduleQueue schedule = new ScheduleQueue();

    public Simulator(Circuit circuit) {
        this.circuit = circuit;
        circuit.getInputs().forEach(node -> ioNodes.put(node.getIdentifier().name(), node));
        circuit.getOutputs().forEach(node -> ioNodes.put(node.getIdentifier().name(), node));
        initialize();
    }

    private void initialize() {
        circuit.topologySort();
        for (var node : circuit.getInputs()) {
            if (node instanceof ClockNode clock) {
                clock.setValue(supplier.nextBoolean());
                clocks.add(clock);
            }
        }
        for (var register : circuit.getRegisters().values()) {
            for (var reg : register) {
                reg.setValue(supplier.next(reg.getWidth()));
            }
        }
    }

    public void setInputValue(String name, BitVector value) {
        setInputValue(Objects.requireNonNull(ioNodes.get(name)), value);
    }

    public void setInputValue(DataNode input, BitVector value) {
        input.setValue(value);
        schedule.add(input);
    }

    public BitVector getOutputValue(DataNode output) {
        return output.getValue();
    }

    public BitVector getOutputValue(String name) {
        return getOutputValue(Objects.requireNonNull(ioNodes.get(name)));
    }

    public void run() {
        boolean hasProgress = true;
        while (hasProgress) {
            while (!schedule.isEmpty()) {
                step();
            }
            hasProgress = clockStep();
        }
    }

    public boolean clockStep() {
        boolean hasProgress = false;
        var clock = pickClock();
        if (clock == null) {
            return false;
        }

        clock.tick();
        for (var reg : circuit.getRegisters().get(clock)) {
            var input = (DataNode) circuit.getPredecessor(reg);
            var output = (DataNode) circuit.getSuccessor(reg);
            if (!input.getValue().equals(reg.getValue())) {
                schedule.add(output);
                hasProgress = true;
            }
        }
        return hasProgress;
    }

    private ClockNode pickClock() {
        // FIXME: deprecated
        if (clocks.isEmpty()) {
            return null;
        }
        if (clocks.size() == 1) {
            return clocks.getFirst();
        }
        return clocks.get(RANDOM.nextInt(clocks.size()));
    }

    public void step() {
        var node = schedule.poll();
        switch (Objects.requireNonNull(node)) {
            case DataNode data -> dataNodeStep(data);
            case ComputationNode computation -> computationStep(computation);
        }
    }

    private void computationStep(ComputationNode node) {
        var id = node.getIdentifier();
        if (BuiltinComponent.isBuiltin(id)) {
            var inputs = circuit.getPredecessors(node).stream()
                    .map(DataNode.class::cast)
                    .map(DataNode::getValue).toList();
            var update = Evaluator.evaluate(BuiltinComponent.of(id), inputs).iterator();
            for (var output : circuit.getSuccessors(node)) {
                // FIXME: check order
                ((DataNode) output).setValue(update.next());
                schedule.add(output);
            }
        } else if (DO_VERIFICATION) {
            // TODO: check constraints, etc.
        }
    }

    private void dataNodeStep(DataNode node) {
        for (var successor : circuit.getSuccessors(node)) {
            switch (successor) {
                case RegisterNode ignore -> {
                    // do nothing since registers are only updated on clock ticks
                }
                case ComputationNode computation -> {
                    var id = computation.getIdentifier();
                    if (DO_VERIFICATION || BuiltinComponent.isBuiltin(id)) {
                        schedule.add(computation);
                    }
                }
                default -> throw new ShallNotReachHereError();
            }
        }
    }

    private static class ScheduleQueue extends PriorityQueue<Node> {
        @Override
        public boolean offer(Node e) {
            if(!super.contains(e)) {
                return super.offer(e);
            }
            return false;
        }
    }

    private static class ValueSupplier {
        public BitVector nextInt() {
            int value = RANDOM_START ? RANDOM.nextInt() : 0;
            return BitVector.fromInt(value);
        }

        public BitVector nextBoolean() {
            boolean value = RANDOM_START && RANDOM.nextBoolean();
            return BitVector.fromBoolean(value);
        }

        public BitVector next(int size) {
            var value = RANDOM_START ? new BigInteger(size, RANDOM) : BigInteger.ZERO;
            return BitVector.fromBigInteger(size, value);
        }
    }
}
