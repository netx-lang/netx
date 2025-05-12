package pascal.netx.util.lang;

import java.util.Objects;

public final class Identifier {
    private final String name;
    private SourceLoc sourceLoc;

    private Identifier(String name) {
        this.name = name;
    }

    public static Identifier of(String id) {
        return new Identifier(id).setSourceLoc(SourceLoc.none());
    }

    public String name() {
        return name;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    public Identifier setSourceLoc(SourceLoc sourceLoc) {
        this.sourceLoc = sourceLoc;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Identifier) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String toStringWithLoc() {
        return String.format("%s@(%s)", name, sourceLoc);
    }

    @Override
    public String toString() {
        return toStringWithLoc();
    }
}
