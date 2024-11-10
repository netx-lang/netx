package pascal.hardscript.hson.simulator.frontend;

import pascal.hardscript.hson.simulator.model.DataNode;

import javax.annotation.Nonnull;

public final class DataManager {
    private final DataNode[] nodes;

    public DataManager(int dataCount) {
        nodes = new DataNode[dataCount];
    }

    public void addNode(int index, DataNode node) {
        nodes[index] = node;
    }

    @Nonnull
    public DataNode resolve(int index) {
        return nodes[index];
    }
}
