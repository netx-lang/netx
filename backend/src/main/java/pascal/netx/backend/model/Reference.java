package pascal.netx.backend.model;

public sealed interface Reference permits DataReference, ValueReference {
    static Reference from(String literal) {
        if (literal.contains("@")) {
            var parts = literal.split("@");
            if (parts[1].contains("[")) {
                int index = parts[1].indexOf('[');
                int id = Integer.parseUnsignedInt(parts[1].substring(0, index));
                return new DataReference(parts[0], id, parts[1].substring(index));
            } else {
                int id = Integer.parseUnsignedInt(parts[1]);
                return new DataReference(parts[0], id, "");
            }
        } else {
            return new ValueReference(literal);
        }
    }

    String dump();
}
