package pascal.hardscript.hson.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import pascal.hardscript.hson.antlr.HsonDataLexer;
import pascal.hardscript.hson.antlr.HsonDataParser;
import pascal.hardscript.hson.model.Node;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FrontendUtils {

    public static Node.Type typeOf(JsonNode node) {
        String type = node.get("type").textValue();
        return Node.Type.valueOf(type.toUpperCase());
    }

    public static HsonDataParser buildParser(String string) {
        var lexer = new HsonDataLexer(CharStreams.fromString(string));
        var stream = new CommonTokenStream(lexer);
        return new HsonDataParser(stream);
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

    public static Iterator<JsonNode> membersIterOf(JsonNode node) {
        return iteratorOf(node, "members");
    }

    public static Stream<JsonNode> membersOf(JsonNode node) {
        return streamOf(node, "members");
    }

    public static void forEachMember(JsonNode node, Consumer<JsonNode> consumer) {
        forEachInField(node, "members", consumer);
    }

    private static ArrayNode arrayNodeOf(JsonNode node, String field) {
        return (ArrayNode) node.get(field);
    }
}
