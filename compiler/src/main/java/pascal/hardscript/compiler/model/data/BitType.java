package pascal.hardscript.compiler.model.data;

public record BitType(int width) implements DataType {

    public static BitType of(int width) {
        return new BitType(width);
    }

    public static BitType forBoolean() {
        return of(1);
    }

    public static BitType forInt32() {
        return of(32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return width == ((BitType) o).width;
    }
}
