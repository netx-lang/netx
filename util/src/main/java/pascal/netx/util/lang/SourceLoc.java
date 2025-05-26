package pascal.netx.util.lang;

import javax.annotation.Nonnull;

public record SourceLoc(int line, int column) implements Comparable<SourceLoc> {

    static SourceLoc NONE = new SourceLoc(0, 0);

    public static SourceLoc none() {
        return NONE;
    }

    public static SourceLoc of(int line, int column) {
        return new SourceLoc(line, column);
    }

    public boolean isNone() {
        return this == NONE;
    }

    @Override
    @Nonnull
    public String toString() {
        return line + ":" + column;
    }

    @Override
    public int compareTo(SourceLoc o) {
        if (line == o.line) {
            return Integer.compare(column, o.column);
        } else {
            return Integer.compare(line, o.line);
        }
    }
}
