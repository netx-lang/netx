package pascal.hardscript.hson.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FrontendUtils {

    public static HsonType typeOf(JsonNode node) {
        String type = node.get("type").textValue();
        return HsonType.valueOf(type.toUpperCase());
    }

    public static Identifier identifierOf(JsonNode node) {
        return Identifier.of(node.get("id").textValue()).setSourceLoc(sourceLocOf(node));
    }

    public static ComponentRef instRefOf(JsonNode node) {
        var literal = node.get("id").textValue().split("@");
        return ComponentRef.of(literal[0], defIndexOf(node), Integer.parseUnsignedInt(literal[1]));
    }

    public static BuiltinRef builtinRefOf(JsonNode node) {
        var literal = node.get("id").textValue().split("@");
        return BuiltinRef.of(BuiltinComponent.of(literal[0]), Integer.parseUnsignedInt(literal[1]));
    }

    public static SourceLoc sourceLocOf(JsonNode node) {
        var literal = node.get("loc").textValue().split(":");
        return SourceLoc.of(Integer.parseUnsignedInt(literal[0]), Integer.parseUnsignedInt(literal[1]));
    }

    public static int defIndexOf(JsonNode node) {
        return node.get("defIndex").intValue();
    }

    public static int widthOf(JsonNode node) {
        return node.get("width").intValue();
    }

    public static Iterator<JsonNode> iteratorOf(JsonNode node, String field) {
        if (node.has(field)) {
            var members = arrayNodeOf(node, field);
            return members.elements();
        }
        return Collections.emptyIterator();
    }

    public static Stream<JsonNode> streamOf(JsonNode node, String field) {
        var array = arrayNodeOf(node, field);
        return StreamSupport.stream(array.spliterator(), false);
    }

    public static void forEachInField(JsonNode node, String field, Consumer<JsonNode> consumer) {
        iteratorOf(node, field).forEachRemaining(consumer);
    }

    public static void forEachMember(JsonNode node, Consumer<JsonNode> consumer) {
        forEachInField(node, "members", consumer);
    }

    private static ArrayNode arrayNodeOf(JsonNode node, String field) {
        return (ArrayNode) node.get(field);
    }

    public record ModuleIndex(String name, int index) {
        public static ModuleIndex of(String name, int index) {
            return new ModuleIndex(name, index);
        }
    }

    public record BuiltinRef(BuiltinComponent builtin, int index) {
        public static BuiltinRef of(BuiltinComponent builtin, int index) {
            return new BuiltinRef(builtin, index);
        }
    }

    public record ComponentRef(ModuleIndex moduleIndex, int instIndex) {
        public static ComponentRef of(String name, int defIndex, int instIndex) {
            return new ComponentRef(ModuleIndex.of(name, defIndex), instIndex);
        }
    }
}
