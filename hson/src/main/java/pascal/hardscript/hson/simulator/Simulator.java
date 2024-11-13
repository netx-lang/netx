package pascal.hardscript.hson.simulator;

import pascal.hardscript.hson.simulator.model.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public final class Simulator {
    public final static Boolean RANDOM_START = false;
    public final static Boolean DO_VERIFICATION = false;
    private final static Random RANDOM = new Random();

    private final Circuit circuit;
    private final List<ClockNode> clocks = new LinkedList<>();
    private final ValueSupplier supplier = new ValueSupplier();
    private final Map<String, DataNode> ioNodes = new HashMap<>();

    private Consumer<Simulator> onEachTick = null;
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
        for (var node : circuit.getAllNodes()) {
            if (node instanceof DataNode data && !(data instanceof DataSelectNode)) {
                data.setValue(supplier.next(data.getWidth()));
            }
        }
        for (var register : circuit.getRegisters().values()) {
            for (var reg : register) {
                var state = reg.getCurr();
                schedule.add(state);
                state.setValue(supplier.next(state.getWidth()));
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

    public void setOnEachTick(Consumer<Simulator> onEachTick) {
        this.onEachTick = onEachTick;
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

    public void runUntil(int steps) {
        for (int i = 0; i < steps; i++) {
            while (!schedule.isEmpty()) {
                step();
            }
            clockStep();
        }
    }

    public boolean clockStep() {
        if (onEachTick != null) {
            onEachTick.accept(this);
        }

        var clock = pickClock();
        if (clock == null) {
            return false;
        }

        var isPosEdge = clock.tick();
        List<Register> toUpdate = new LinkedList<>();
        for (var reg : circuit.getRegisters().get(clock)) {
            if (reg.isPosTrigger() == isPosEdge) {
                DataNode curr = reg.getCurr(), next = reg.getNext();
                if (!next.getValue().equals(curr.getValue())) {
                    toUpdate.add(reg);

                }
            }
        }
        var incomingValue = toUpdate.stream()
                .map(Register::getNext)
                .map(DataNode::getValue).toList().iterator();
        for (var reg : toUpdate) {
            reg.getCurr().setValue(incomingValue.next());
            schedule.add(reg.getCurr());
        }
        return !toUpdate.isEmpty();
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
                var data = (DataNode) output;
                data.setValue(update.next());
                if (data instanceof DataSelectNode selectNode) {
                    var base = selectNode.getBase();
                    var value = selectNode.inverseApply();
                    if (!value.equals(base.getValue())) {
                        base.setValue(value);
                        schedule.add(base);
                    }
                }
                schedule.add(data);
            }
        } else if (DO_VERIFICATION) {
            // TODO: check constraints, etc.
            throw new TodoException();
        }
    }

    private void dataNodeStep(DataNode node) {
        for (var successor : circuit.getSuccessors(node)) {
            switch (successor) {
                case ComputationNode computation -> {
                    var id = computation.getIdentifier();
                    if (DO_VERIFICATION || BuiltinComponent.isBuiltin(id)) {
                        schedule.add(computation);
                    }
                }
                case DataSelectNode select -> {
                    var value = select.apply();
                    if (!value.equals(select.getValue())) {
                        select.setValue(value);
                        schedule.add(select);
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
