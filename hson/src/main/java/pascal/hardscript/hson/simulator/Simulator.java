package pascal.hardscript.hson.simulator;

import pascal.hardscript.hson.model.Circuit;
import pascal.hardscript.hson.model.ClockNode;
import pascal.hardscript.hson.model.Node;
import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.BitVector;

import java.math.BigInteger;
import java.util.*;

public final class Simulator {
    public final static Boolean RANDOM_START = false;
    private final static Random RANDOM = new Random();

    private final Circuit circuit;
    private final List<ClockNode> clocks = new LinkedList<>();
    private final ValueSupplier supplier = new ValueSupplier();

    private final ScheduleQueue schedule = new ScheduleQueue();

    public Simulator(Circuit circuit) {
        this.circuit = circuit;
        initialize();
    }

    private void initialize() {
        circuit.topologySort();
        for (var entry : circuit.getRegisters().entrySet()) {
            var clock = entry.getKey();
            clock.setValue(supplier.nextBoolean());
            clocks.add(clock);
            for (var reg : entry.getValue()) {
                reg.setValue(supplier.next(reg.getWidth()));
            }
        }
    }

    public void run() {
        boolean hasProgress = true;
        while (hasProgress) {
            while (hasProgress) {
                hasProgress = step();
            }
            clockStep();
        }
    }

    public void clockStep() {
        // FIXME: deprecated
        clocks.getFirst().tick();
    }

    public boolean step() {
        if (schedule.isEmpty()) {
            return false;
        }

        boolean hasProgress = false;
        var node = schedule.poll();
        for (var successor : circuit.getSuccessors(node)) {
            throw new TodoException(); // TODO:
        }
        return hasProgress;
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
