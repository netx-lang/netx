package pascal.hardscript.compiler.ast;

import java.util.Objects;

public final class Identifier extends Symbol {
    private final String string;

    private Identifier(String string) {
        this.string = string;
    }

    public static Identifier of(String id) {
        return new Identifier(id);
    }

    public String string() {
        return string;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Identifier) obj;
        return Objects.equals(this.string, that.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }

    @Override
    public String toString() {
        return "Identifier[" +
                "id=" + string + ']';
    }
}
