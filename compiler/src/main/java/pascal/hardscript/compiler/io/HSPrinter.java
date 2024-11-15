package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.print.NotepadWriter;

import java.util.Map;

import static pascal.hardscript.compiler.Configuration.FULLY_EXPAND;

public final class HSPrinter {
    private final JsonNode json;
    private final NotepadWriter writer;
    private static final int INST_INLINE = FULLY_EXPAND ? 3 : 4;

    public HSPrinter(JsonNode json, NotepadWriter writer) {
        this.json = json;
        this.writer = writer;
    }

    public void print() {
        if (FULLY_EXPAND) {
            printObject(json);
        } else {
            startArray();
            var iterator = json.elements();
            printObject(iterator.next(), INST_INLINE);
            while (iterator.hasNext()) {
                writer.println(",");
                printObject(iterator.next(), INST_INLINE);
            }
            writer.newLine(); endArray();
        }
        writer.flush();
        writer.close();
    }

    private void printObject(JsonNode node) {
        printObject(node, 0);
    }

    private void printObject(JsonNode node, int inline) {
        startObject();
        printAllFields(node, inline); writer.newLine();
        endObject();
    }

    private void printMember(JsonNode node) {
        switch (HsonType.typeOf(node)) {
            case WIRE, CLOCK -> printData(node);
            case REGISTER -> printObject(node);
            case BUILTIN -> printObject(node, 3);
            case INSTANTIATE, COMPONENT -> printObject(node, INST_INLINE);
        }
    }

    private void printData(JsonNode node) {
        writer.print("{");
        var fieldIter = node.fields();
        fieldIter.next();
        printKey("type");
        printStr(node.get("type").textValue());
        fieldIter.forEachRemaining(sub -> {
            writer.print(", ");
            printKey(sub.getKey());
            switch (sub.getValue()) {
                case TextNode text -> printStr(text.textValue());
                case NumericNode num -> printInteger(num.intValue());
                default -> throw new ShallNotReachHereError();
            }
        });
        writer.print("}");
    }

    private void printAllFields(JsonNode node, int inline) {
        int count = 1;
        var fieldIter = node.fields();
        printField(fieldIter.next());
        while (fieldIter.hasNext()) {
            var sub = fieldIter.next();
            if (count < inline) {
                writer.print(", ");
                count++;
            } else {
                writer.println(",");
            }
            printField(sub);
        }
    }

    private void printField(Map.Entry<String, JsonNode> sub) {
        printKey(sub.getKey());
        switch (sub.getValue()) {
            case TextNode text -> printStr(text.textValue());
            case NumericNode num -> printInteger(num.intValue());
            case ObjectNode obj -> printObject(obj);
            case ArrayNode array -> printArrayField(array);
            default -> throw new ShallNotReachHereError();
        }
    }

    private void printArrayField(ArrayNode array) {
        if (array.get(0) instanceof TextNode) {
            writer.print("[");
            var iterator = array.elements();
            printStr(iterator.next().textValue());
            while (iterator.hasNext()) {
                writer.print(", ");
                printStr(iterator.next().textValue());
            }
            writer.print("]");
        } else {
            startArray();
            var iterator = array.elements();
            printMember(iterator.next());
            while (iterator.hasNext()) {
                writer.println(",");
                printMember(iterator.next());
            }
            writer.newLine();
            endArray();
        }
    }

    public enum HsonType {
        COMPONENT, REGISTER, BUILTIN, INSTANTIATE, CLOCK, WIRE;

        public static HsonType typeOf(JsonNode node) {
            return valueOf(node.get("type").textValue().toUpperCase());
        }
    }

    private void startObject() {
        writer.println("{");
        writer.incIndent();
    }

    private void endObject() {
        writer.decIndent();
        writer.print("}");
    }

    private void startArray() {
        writer.println("[");
        writer.incIndent();
    }

    private void endArray() {
        writer.decIndent();
        writer.print("]");
    }

    private void printStr(String str) {
        writer.print("\"");
        writer.print(str);
        writer.print("\"");
    }

    private void printInteger(int integer) {
        writer.print(String.valueOf(integer));
    }

    private void printKey(String str) {
        printStr(str);
        writer.print(": ");
    }
}

