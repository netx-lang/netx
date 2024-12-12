package pascal.hardscript.verilog.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.hson.HsonType;
import pascal.hardscript.util.hson.selector.BitSelector;
import pascal.hardscript.util.hson.selector.PartSelector;
import pascal.hardscript.util.hson.selector.Selector;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.verilog.model.*;
import pascal.hardscript.verilog.model.ref.Constant;
import pascal.hardscript.verilog.model.ref.DataRef;
import pascal.hardscript.verilog.model.ref.WireRef;
import pascal.hardscript.verilog.model.ref.WireRefSelect;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pascal.hardscript.util.hson.FrontendUtils.*;

public final class ScopeParser {
    private final HFrontend frontend;
    private final JsonNode json;
    private final VModule module;

    private final DataRefParser refParser = new DataRefParser();

    public ScopeParser(HFrontend frontend, JsonNode json, VModule module) {
        this.frontend = frontend;
        this.json = json;
        this.module = module;
    }

    public void parse() {
        parseDataMembers();
        module.setInputs(refParser.resolveDataRefList(json, "input"));
        module.setOutputs(refParser.resolveDataRefList(json, "output"));
        parseComponentMembers();
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
            if (Objects.requireNonNull(typeOf(node)) == HsonType.INSTANTIATE) {
                prepareInstantiation(node);
            }
        });
        forEachMember(json, node -> {
            switch (typeOf(node)) {
                case INSTANTIATE -> parseInstantiation(node);
                case BUILTIN -> parseBuiltin(node);
            }
        });
    }

    private void prepareInstantiation(JsonNode node) {
        var ref = instRefOf(node);
        var def = frontend.resolveModuleDef(ref.moduleIndex());
        var inst = Instantiation.of(def, ref.instIndex());
        module.add(inst);
    }

    private void parseInstantiation(JsonNode node) {
        var ref = instRefOf(node);
        var inst = module.getInstantiation(ref.instIndex());
        inst.setInputs(refParser.resolveDataRefList(node, "input"));
        inst.setOutputs(refParser.resolveDataRefList(node, "output"));
    }

    private void parseBuiltin(JsonNode node) {
        var ref = builtinRefOf(node);
        if (node.has("members")) {
            forEachMember(node, sub -> {
                var rawID = identifierOf(sub);
                var newID = Identifier.of(rawID.name() + "_" + ref.index());
                module.add(Wire.of(newID, widthOf(sub)));
            });
        }
        if (ref.builtin() == BuiltinComponent.REGISTER) {
            var attrNode = node.get("attribute");
            var register = Register.of(Boolean.parseBoolean(attrNode.get("isPosTrigger").textValue()));
            module.add(register);
            register.setIn(refParser.resolveDataRefList(node, "input").getFirst());
            register.setOut(refParser.resolveDataRefList(node, "output").getFirst());
            register.setClock(refParser.resolveDataRef(attrNode.get("clock").textValue()));
        } else {
            var assign = Assign.of(ref.builtin());
            module.add(ref.index(), assign);
            assign.setInputs(refParser.resolveDataRefList(node, "input"));
            assign.setOutputs(refParser.resolveDataRefList(node, "output"));
        }
    }

    private class DataRefParser {
        private final Map<Pattern, Function<Matcher, DataRef>> IDREF_PATTERN = Map.ofEntries(
                Map.entry(Pattern.compile("(.*)@(\\d+)"), this::resolvePlainDataRef),
                Map.entry(Pattern.compile("(.*)@(\\d+)\\[(\\d+)]"), this::resolveBitSelectDataRef),
                Map.entry(Pattern.compile("(.*)@(\\d+)\\[(\\d+):(\\d+)]"), this::resolvePartSelectDataRef),
                Map.entry(Pattern.compile("(\\d+)'h([0-9a-fA-F]+)"), this::resolveBitVectorConstant),
                Map.entry(Pattern.compile("(\\d+)"), this::resolveIntConstant)
        );

        public List<DataRef> resolveDataRefList(JsonNode node, String field) {
            return streamOf(node, field).map(JsonNode::textValue).map(this::resolveDataRef).toList();
        }

        public DataRef resolveDataRef(String literal) {
            for (var entry : IDREF_PATTERN.entrySet()) {
                var matcher = entry.getKey().matcher(literal);
                if (matcher.matches()) {
                    return entry.getValue().apply(matcher);
                }
            }
            throw new ShallNotReachHereError();
        }

        private DataRef resolvePlainDataRef(Matcher matcher) {
            String name = matcher.group(1);
            int index = Integer.parseUnsignedInt(matcher.group(2));
            if (index == 0) {
                return WireRef.of(null, name);
            } else {
                var instantiation = module.getInstantiation(index);
                if (instantiation != null) {
                    return WireRef.of(instantiation, name);
                } else {
                    return WireRef.of(null, name + "_" + index);
                }
            }
        }

        private DataRef resolveBitSelectDataRef(Matcher matcher) {
            DataRef base = resolvePlainDataRef(matcher);
            Selector selector = BitSelector.of(Integer.parseUnsignedInt(matcher.group(3)));
            return WireRefSelect.of((WireRef) base, selector);
        }

        private DataRef resolvePartSelectDataRef(Matcher matcher) {
            DataRef base = resolvePlainDataRef(matcher);
            Selector selector = PartSelector.of(
                    Integer.parseUnsignedInt(matcher.group(3)),
                    Integer.parseUnsignedInt(matcher.group(4)));
            return WireRefSelect.of((WireRef) base, selector);
        }

        private DataRef resolveBitVectorConstant(Matcher matcher) {
            int size = Integer.parseUnsignedInt(matcher.group(1));
            return Constant.of(BitVector.parse(matcher.group(2), size, 16));
        }

        private DataRef resolveIntConstant(Matcher matcher) {
            int value = Integer.parseUnsignedInt(matcher.group(1));
            return Constant.of(BitVector.fromInt(value));
        }
    }
}
