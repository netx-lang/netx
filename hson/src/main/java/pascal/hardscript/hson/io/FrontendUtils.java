package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import pascal.hardscript.hson.model.Node;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FrontendUtils {
    private static final Pattern ID_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)@([0-9]+):([0-9]+)");

    public static Node.Type typeOf(JsonNode node) {
        String type = node.get("type").textValue();
        return Node.Type.valueOf(type.toUpperCase());
    }

    public static Identifier identifierOf(JsonNode node) {
        return parseIdentifier(node.get("id").textValue());
    }

    public static Identifier parseIdentifier(String string) {
        var matcher = ID_PATTERN.matcher(string);

        var name = matcher.group(1);
        var line = Integer.parseInt(matcher.group(2));
        var column = Integer.parseInt(matcher.group(3));
        return Identifier.of(name).setSourceLoc(SourceLoc.of(line, column));
    }

    public static Iterator<JsonNode> iteratorOf(JsonNode node, String field) {
        var members = arrayNodeOf(node, field);
        return members.elements();
    }

    public static Stream<JsonNode> streamOf(JsonNode node, String field) {
        var array = arrayNodeOf(node, field);
        return StreamSupport.stream(array.spliterator(), false);
    }

    public static void forEachInField(JsonNode node, String field, Consumer<JsonNode> consumer) {
        iteratorOf(node, field).forEachRemaining(consumer);
    }

    public static Iterator<JsonNode> membersIterOf(JsonNode node) {
        return iteratorOf(node, "member");
    }

    public static Stream<JsonNode> membersOf(JsonNode node) {
        return streamOf(node, "member");
    }

    public static void forEachMember(JsonNode node, Consumer<JsonNode> consumer) {
        forEachInField(node, "member", consumer);
    }

    private static ArrayNode arrayNodeOf(JsonNode node, String field) {
        return (ArrayNode) node.get(field);
    }
}
