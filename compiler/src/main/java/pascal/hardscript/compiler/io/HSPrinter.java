package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.compiler.analysis.AliasAnalysis;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.data.Clock;
import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.data.DataAccess;
import pascal.hardscript.compiler.model.data.Wire;
import pascal.hardscript.compiler.model.scope.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class HSPrinter {
    private final DesignScope design;
    private final ComponentInfo componentInfo;
    private final AliasAnalysis.Result alias;
    private final Map<Data, Integer> addrMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HSPrinter(DesignScope design) {
        this.design = design;
        this.componentInfo = design.getComponentInfo();
        this.alias = new AliasAnalysis(design).analyze();
    }

    public ObjectNode dump() {
        return dump(design);
    }

    private ObjectNode dump(DesignScope design) {
        var result = createObjectNode();
        setType(result, "design");
        var members = makeMembersIn(result);
        design.getChildren().stream()
                .map(ComponentScope.class::cast)
                .map(this::dump).forEach(members::add);
        return result;
    }

    private ObjectNode dump(ComponentScope module) {
        var result = createObjectNode();
        setType(result, "module");
        setIdentifier(result, module.getIdentifier());

        dumpComponentIO(result, "input", module.getInputs());
        dumpComponentIO(result, "output", module.getOutputs());
        var members = makeMembersIn(result);
        for (var entity : module.getEntities()) {
            if (entity instanceof Data data && needDump(data)) {
                members.add(dump(data));
            }
        }
        for (var sub : extractSubComponent(module)) {
            var instID = sub.getIdentifier();
            if (componentInfo.containsBuiltin(instID)) {
                if (!instID.string().equals("TO")) {
                    dumpBuiltinComponent(sub, instID, members);
                }
            } else {
                members.add(dump(sub));
            }
        }
        return result;
    }

    private boolean needDump(Data data) {
        return (data.isUserDefined()
                && alias.repOf(data) == data
        ) || alias.isBuiltinRep(data);
    }

    private void dumpBuiltinComponent(ComponentScope sub, Identifier instID, ArrayNode members) {
        var builtin = createObjectNode();
        setType(builtin, "builtin");
        setIdentifier(builtin, instID);

        List<Data> needDump = new LinkedList<>();
        for (var entity : sub.getEntities()) {
            if (entity instanceof Data data && needDump(data)) {
                needDump.add(data);
            }
        }
        if (!needDump.isEmpty()) {
            var subarray = makeMembersIn(builtin);
            needDump.forEach(c -> subarray.add(dump(c)));
        }
        dumpComponentIO(builtin, "input", sub.getInputs());
        dumpComponentIO(builtin, "output", sub.getOutputs());
        members.add(builtin);
    }

    private void dumpComponentIO(ObjectNode builtin, String port, List<Data> sub) {
        var outputs = makeArrayIn(builtin, port);
        sub.stream().map(alias::repOf)
                .map(this::dumpIdentifierOf)
                .forEach(outputs::add);
    }

    private List<ComponentScope> extractSubComponent(Scope module) {
        List<ComponentScope> result = new LinkedList<>();
        for (var subScope : module.getChildren()) {
            switch (subScope) {
                case ComponentScope sub -> result.add(sub);
                case BlockScope sub -> result.addAll(extractSubComponent(sub));
                case DesignScope ignored -> throw new ShallNotReachHereError();
            }
        }
        return result;
    }

    private ObjectNode dump(Data data) {
        return switch (data) {
            case Wire wire -> dumpWire(wire);
            case Clock clock -> dumpClock(clock);
            default -> throw new ShallNotReachHereError();
        };
    }

    private ObjectNode dumpWire(Wire wire) {
        var result = createObjectNode();
        setType(result, "wire");
        setIdentifier(result, wire);
        result.put("width", wire.width());
        return result;
    }

    private ObjectNode dumpClock(Clock clock) {
        var result = createObjectNode();
        setType(result, "clock");
        setIdentifier(result, clock);
        return result;
    }

    private void setType(ObjectNode node, String type) {
        node.put("type", type);
    }

    private String dumpIdentifierOf(Data data) {
        if (data instanceof DataAccess access) {
            return data.getIdentifier().string() + "@" +
                    addrMap.computeIfAbsent(access.base(), _ -> addrMap.size() + 1) +
                    access.selector();

        } else {
            return data.getIdentifier().string() + "@" +
                    addrMap.computeIfAbsent(data, _ -> addrMap.size() + 1);
        }
    }

    private void setIdentifier(ObjectNode node, Data data) {
        setIdentifier(node, dumpIdentifierOf(data));
    }

    private void setIdentifier(ObjectNode node, Identifier identifier) {
        setIdentifier(node, identifier.string());
    }

    private void setIdentifier(ObjectNode node, String value) {
        node.put("id", value);
    }

    private ArrayNode makeArrayIn(ObjectNode node, String key) {
        return node.putArray(key);
    }

    private ArrayNode makeMembersIn(ObjectNode node) {
        return makeArrayIn(node, "members");
    }

    private ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }
}
