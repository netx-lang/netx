package pascal.hardscript.hson.verilog.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.hson.antlr.HsonDataParser;
import pascal.hardscript.hson.verilog.model.*;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.List;

import static pascal.hardscript.hson.util.FrontendUtils.*;

public final class RelationParser {
    private final VModule module;
    private final VManager manager;

    public RelationParser(HFrontend frontend) {
        this.module = frontend.getModule();
        this.manager = frontend.getManager();
    }

    public void run(JsonNode root) {
        parse(root, module);
    }
    private void parse(JsonNode node, VModule parent) {
        switch (typeOf(node)) {
            case WIRE, CLOCK -> {
                return;
            }
        }

        var inputs = resolveDataList(node, "input");
        var outputs = resolveDataList(node, "output");
        switch (typeOf(node)) {
            case COMPONENT -> parseComponent(node, parent, inputs, outputs);
            case REGISTER -> parseRegister(node, parent, inputs.getFirst(), outputs.getFirst());
        }
    }

    private void parseComponent(JsonNode node, VModule parent, List<Data> inputs, List<Data> outputs) {
        var name = node.get("id").textValue();
        if (!BuiltinComponent.isBuiltin(name)) {
            var module = manager.resolveModule(node);
            module.setInputs(inputs);
            module.setOutputs(outputs);
            forEachMember(node, n -> parse(n, module));
        } else {
            var builtin = BuiltinComponent.of(name);
            var assign = Assign.of(builtin, outputs, inputs);
            parent.addAssign(assign);
        }
    }

    private void parseRegister(JsonNode node, VModule parent, Data input, Data output) {
        var attribute = node.get("attribute");
        var clock = resolveData(attribute.get("clock"));
        var isPosTrigger = Boolean.parseBoolean(attribute.get("isPosTrigger").textValue());
        var register = Register.of(input, output, clock, isPosTrigger);
        parent.addRegister(register);
    }

    private List<Data> resolveDataList(JsonNode node, String field) {
        return streamOf(node, field).map(this::resolveData).toList();
    }

    private Data resolveData(JsonNode node) {
        var parser = buildParser(node.textValue());
        var context = parser.data();
        if (context.identifier() != null) {
            return resolveData(context.identifier());
        } else if (context.constant() != null) {
            return resolveConstant(context.constant());
        } else {
            return resolveSelect(context.select());
        }
    }

    private Data resolveData(HsonDataParser.IdentifierContext context) {
        var index = Integer.parseUnsignedInt(context.index.getText());
        return manager.resolve(index);
    }

    private Data resolveConstant(HsonDataParser.ConstantContext context) {
        return Constant.of(parseConstantValue(context));
    }

    private Data resolveSelect(HsonDataParser.SelectContext context) {
        var index = Integer.parseUnsignedInt(context.identifier().index.getText());
        Data base = manager.resolve(index), result;
        var selector = parseSelector(context.selector());
        result = DataSelect.of(base, selector);
        return result;
    }
}
