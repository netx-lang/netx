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
        var matcher = ID_PATTERN.matcher(node.get("id").textValue());

        var name = matcher.group(1);
        var line = Integer.parseInt(matcher.group(2));
        var column = Integer.parseInt(matcher.group(3));
        return Identifier.of(name).setSourceLoc(SourceLoc.of(line, column));
    }

    public static Iterator<JsonNode> membersIterOf(JsonNode node) {
        var members = membersNodeOf(node);
        return members.elements();
    }

    public static void forEachMember(JsonNode node, Consumer<JsonNode> consumer) {
        membersIterOf(node).forEachRemaining(consumer);
    }

    public static Stream<JsonNode> membersOf(JsonNode node) {
        var members = membersNodeOf(node);
        return StreamSupport.stream(members.spliterator(), false);

    }

    private static ArrayNode membersNodeOf(JsonNode node) {
        return (ArrayNode) node.get("members");
    }
}
