package pascal.hardscript.hson.verilog.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.verilog.model.Data;
import pascal.hardscript.hson.verilog.model.VModule;

import java.util.HashMap;
import java.util.Map;

public final class VManager {
    private final Data[] vData;
    private final Map<JsonNode, VModule> modules = new HashMap<>();

    public VManager(int dataCount) {
        vData = new Data[dataCount];
    }

    public void addModule(JsonNode node, VModule module) {
        modules.put(node, module);
    }

    public VModule resolveModule(JsonNode node) {
        return modules.get(node);
    }

    public void addData(int index, Data data) {
        vData[index] = data;
    }

    public Data resolve(int index) {
        return vData[index];
    }
}
