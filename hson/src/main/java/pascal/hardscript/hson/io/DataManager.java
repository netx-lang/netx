package pascal.hardscript.hson.io;

import pascal.hardscript.hson.model.DataNode;

import javax.annotation.Nonnull;

public class DataManager {
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
