package pascal.hardscript.hson.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import pascal.hardscript.hson.antlr.HsonDataLexer;
import pascal.hardscript.hson.antlr.HsonDataParser;
import pascal.hardscript.hson.util.selector.BitSelector;
import pascal.hardscript.hson.util.selector.PartSelector;
import pascal.hardscript.hson.util.selector.Selector;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;
import pascal.hardscript.util.type.Pair;

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

    public static HsonDataParser buildParser(String string) {
        var lexer = new HsonDataLexer(CharStreams.fromString(string));
        var stream = new CommonTokenStream(lexer);
        return new HsonDataParser(stream);
    }

    public static int widthOf(JsonNode node) {
        return node.get("width").intValue();
    }

    public static Pair<Identifier, Integer> parseIdentifier(JsonNode node) {
        return parseIdentifier(node.get("id").textValue());
    }

    public static Pair<Identifier, Integer> parseIdentifier(String string) {
        var parser = buildParser(string);
        var context = parser.identifier();

        var id = Identifier.of(context.name.getText()).setSourceLoc(
                SourceLoc.of(
                        Integer.parseUnsignedInt(context.line.getText()),
                        Integer.parseUnsignedInt(context.column.getText())
                )
        );
        var index = Integer.parseUnsignedInt(context.index.getText());
        return Pair.of(id, index);
    }

    public static Selector parseSelector(HsonDataParser.SelectorContext ctx) {
        return switch (ctx) {
            case HsonDataParser.BitSelectorContext sub -> BitSelector.of(
                    Integer.parseUnsignedInt(sub.index.getText()));
            case HsonDataParser.PartSelectorContext sub -> PartSelector.of(
                    Integer.parseUnsignedInt(sub.from.getText()),
                    Integer.parseUnsignedInt(sub.to.getText()));
            default -> throw new ShallNotReachHereError();
        };
    }

    public static BitVector parseConstantValue(HsonDataParser.ConstantContext ctx) {
        BitVector value;
        if (ctx.HEX() != null) {
            var literal = ctx.getText().toLowerCase().split("'h");
            value = BitVector.parse(literal[1], Integer.parseUnsignedInt(literal[0]), 16);
        } else {
            value = BitVector.fromInt(Integer.parseInt(ctx.getText()));
        }
        return value;
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
}
