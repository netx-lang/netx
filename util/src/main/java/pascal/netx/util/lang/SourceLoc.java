package pascal.netx.util.lang;

import javax.annotation.Nonnull;

public record SourceLoc(int line, int column, String file) implements Comparable<SourceLoc> {

    static SourceLoc NONE = new SourceLoc(0, 0, "");

    public static SourceLoc none() {
        return NONE;
    }

    public boolean isNone() {
        return this == NONE;
    }

    @Override
    @Nonnull
    public String toString() {
        if (isNone()) {
            return "";
        }
        return file + ":" + line + ":" + column;
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
