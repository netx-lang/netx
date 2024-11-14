package pascal.hardscript.hson.verilog.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.hson.util.selector.BitSelector;
import pascal.hardscript.hson.util.selector.PartSelector;
import pascal.hardscript.hson.util.selector.Selector;
import pascal.hardscript.hson.verilog.model.Constant;
import pascal.hardscript.hson.verilog.model.Instantiation;
import pascal.hardscript.hson.verilog.model.VModule;
import pascal.hardscript.hson.verilog.model.data.Data;
import pascal.hardscript.hson.verilog.model.data.Wire;
import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.hson.verilog.model.ref.DataRefSelect;
import pascal.hardscript.hson.verilog.model.ref.WireRef;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.BitVector;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pascal.hardscript.hson.util.FrontendUtils.*;

class ScopeParser {
    private final HFrontend frontend;
    private final ObjectNode json;
    private final VModule module;

    private ScopeParser(HFrontend frontend, ObjectNode json, VModule module) {
        this.frontend = frontend;
        this.json = json;
        this.module = module;
    }

    public void parse() {
        parseDataMembers();
        module.setInputs(resolveDataList(json, "input"));
        module.setOutputs(resolveDataList(json, "output"));
        parseComponentMembers();
    }

    private List<Data> resolveDataList(JsonNode node, String field) {
        throw new TodoException(); // TODO:
    }

    private void parseDataMembers() {
        forEachMember(json, node -> {
            switch (typeOf(node)) {
                case WIRE -> module.add(
                        Wire.of(identifierOf(node), widthOf(node)));
                case CLOCK -> module.add(
                        Wire.of(identifierOf(node), 1));
            }
        });
    }

    private void parseComponentMembers() {
        forEachMember(json, node -> {
            switch (typeOf(node)) {
                case INSTANTIATE -> parseInstantiation(node);
                case BUILTIN -> {}
                case REGISTER -> {}
            }
        });
    }

    private void parseInstantiation(JsonNode node) {
        var ref = instRefOf(node);
        var module = frontend.resolveModuleDef(ref.moduleIndex());
        var inst = Instantiation.of(module, ref.instIndex());
        module.add(inst);
        inst.setInputs(resolveDataRefList(node, "input"));
        inst.setOutputs(resolveDataRefList(node, "output"));
    }

    private final Map<Pattern, Function<Matcher, DataRef>> IDREF_PATTERN = Map.ofEntries(
            Map.entry(Pattern.compile("(.*)@(\\d+)"), this::resolvePlainDataRef),
            Map.entry(Pattern.compile("(.*)@(\\d+)\\[(\\d+)]"), this::resolveBitSelectDataRef),
            Map.entry(Pattern.compile("(.*)@(\\d+)\\[(\\d+):(\\d+)]"), this::resolvePartSelectDataRef),
            Map.entry(Pattern.compile("(\\d+)'h([0-9a-fA-F]+)"), this::resolveConstant)
    );

    private List<DataRef> resolveDataRefList(JsonNode node, String field) {
        return streamOf(node, field).map(JsonNode::textValue).map(this::resolveDataRef).toList();
    }

    private DataRef resolveDataRef(String literal) {
        for (var entry : IDREF_PATTERN.entrySet()) {
            var matcher = entry.getKey().matcher(literal);
            if (matcher.matches()) {
                return entry.getValue().apply(matcher);
            }
        }
        throw new ShallNotReachHereError();
    }

    private DataRef resolvePlainDataRef(Matcher matcher) {
        int index = Integer.parseUnsignedInt(matcher.group(2));
        Instantiation inst = index == 0 ? null : module.getInstantiation(index);
        return WireRef.of(inst, matcher.group(1));
    }

    private DataRef resolveBitSelectDataRef(Matcher matcher) {
        DataRef base = resolvePlainDataRef(matcher);
        Selector selector = BitSelector.of(Integer.parseUnsignedInt(matcher.group(3)));
        return DataRefSelect.of(base, selector);
    }

    private DataRef resolvePartSelectDataRef(Matcher matcher) {
        DataRef base = resolvePlainDataRef(matcher);
        Selector selector = PartSelector.of(
                Integer.parseUnsignedInt(matcher.group(3)),
                Integer.parseUnsignedInt(matcher.group(4)));
        return DataRefSelect.of(base, selector);
    }

    private DataRef resolveConstant(Matcher matcher) {
        int size = Integer.parseUnsignedInt(matcher.group(1));
        return Constant.of(BitVector.parse(matcher.group(2), size, 16));
    }
}
