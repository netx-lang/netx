package pascal.hardscript.simulator;

import pascal.hardscript.simulator.model.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public final class Simulator {
    public final static Boolean DO_VERIFICATION = false;

    private final Circuit circuit;
    private final List<ClockNode> clocks = new LinkedList<>();
    private final Map<String, DataNode> ioNodes = new HashMap<>();

    private final ScheduleQueue schedule = new ScheduleQueue();
    private final ValueSupplier supplier = new ValueSupplier();

    private Consumer<Simulator> onEachTick = null;

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
                clock.setValue(supplier.nextBit());
                clocks.add(clock);
            }
        }
        for (var node : circuit.getAllNodes()) {
            if (node instanceof WireNode wire) {
                wire.setValue(supplier.nextBitVector(wire.getWidth()));
            }
        }
        for (var register : circuit.getRegisters().values()) {
            for (var reg : register) {
                var state = reg.getCurr();
                schedule.add(state);
                state.setValue(supplier.nextBitVector(state.getWidth()));
            }
        }
    }

    public void setInput(String name, BitVector value) {
        setInput(Objects.requireNonNull(ioNodes.get(name)), value);
    }

    public void setInput(DataNode input, BitVector value) {
        input.setValue(value);
        schedule.add(input);
    }

    public BitVector getOutput(DataNode output) {
        return output.getValue();
    }

    public BitVector getOutput(String name) {
        return getOutput(Objects.requireNonNull(ioNodes.get(name)));
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

    @Deprecated
    private ClockNode pickClock() {
        if (clocks.isEmpty()) {
            return null;
        }
        if (clocks.size() == 1) {
            return clocks.getFirst();
        }
        return clocks.get(supplier.nextInt() % clocks.size());
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
                var value = update.next();

                if (data.getWidth() != value.size()) {
                    if (data.getWidth() > value.size()) {
                        value = value.ext(data.getWidth());
                    } else {
                        value = value.truncate(data.getWidth());
                    }
                }

                data.setValue(value);
                if (data instanceof DataSelectNode selectNode) {
                    var base = selectNode.getBase();
                    var baseValue = selectNode.inverseApply();
                    if (!baseValue.equals(base.getValue())) {
                        base.setValue(baseValue);
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
            if (!super.contains(e)) {
                return super.offer(e);
            }
            return false;
        }
    }

    private static class ValueSupplier {
        public final static Boolean RANDOM_START = false;
        private final static Random RANDOM = new Random();

        public int nextInt() {
            return RANDOM.nextInt();
        }

        public BitVector nextBit() {
            boolean value = RANDOM_START && RANDOM.nextBoolean();
            return BitVector.fromBoolean(value);
        }

        public BitVector nextBitVector(int size) {
            var value = RANDOM_START ? new BigInteger(size, RANDOM) : BigInteger.ZERO;
            return BitVector.fromBigInteger(size, value);
        }
    }
}
