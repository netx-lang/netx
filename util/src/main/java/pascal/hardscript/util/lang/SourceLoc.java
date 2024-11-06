package pascal.hardscript.util.lang;

public record SourceLoc(int line, int column) implements Comparable<SourceLoc> {

    public static SourceLoc of(int line, int column) {
        return new SourceLoc(line, column);
    }

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
