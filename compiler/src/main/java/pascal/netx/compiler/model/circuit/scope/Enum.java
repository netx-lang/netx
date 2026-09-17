package pascal.netx.compiler.model.circuit.scope;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.ast.element.EnumNode;
import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.compiler.model.circuit.signal.VectorValue;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public record Enum(Identifier name, int width, Map<Identifier, VectorValue> fields) implements Entity {
    public static Enum from(EnumNode node) {
        checkUniqueEnumFields(node);
        Map<Identifier, VectorValue> enumMap = new HashMap<>();
        int width = 1;
        if (node.getValues().isEmpty()) {
            width = BigInteger.valueOf(node.getFields().size() - 1).bitLength();
            for (int i = 0; i < node.getFields().size(); i++) {
                enumMap.put(node.getFields().get(i), VectorValue.of(BitVector.fromInt(width, i)));
            }
        } else {
            for (var number : node.getValues()) {
                width = Math.max(width, number.getRepresentation().size());
            }

            for (int i = 0; i < node.getFields().size(); i++) {
                var field = node.getFields().get(i);
                var number = node.getValues().get(i).getRepresentation();
                if (number.size() != width) {
                    number = number.uext(width);
                }
                enumMap.put(field, VectorValue.of(number));
            }
            checkUniqueEnumValues(node, enumMap.values());
        }
        return new Enum(node.getName(), width, enumMap);
    }

    private static void checkUniqueEnumFields(EnumNode node) {
        if (node.getFields().size() != new HashSet<>(node.getFields()).size()) {
            Logger.error(node.getSourceLoc(), "Enum fields must be unique, but found duplicates in '{}'",
                    node.getName());
            throw new CompileCancellationException();
        }
    }

    private static void checkUniqueEnumValues(EnumNode node, Collection<VectorValue> values) {
        if (values.size() != new HashSet<>(values).size()) {
            Logger.error(node.getSourceLoc(), "Enum values must be unique, but found duplicates in '{}'",
                    node.getName());
            throw new CompileCancellationException();
        }
    }
}
