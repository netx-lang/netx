package pascal.netx.compiler.model.instance.type;

public record BitType(int width) implements InstanceType {
    private static final BitType BOOLEAN = new BitType(1);
    private static final BitType CLOCK = new BitType(1);
    private static final BitType INT32 = new BitType(32);


    public static BitType of(int width) {
        return new BitType(width);
    }

    public static BitType forBoolean() {
        return BOOLEAN;
    }

    public static BitType forInt32() {
        return INT32;
    }

    public static BitType forClock() {
        return CLOCK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return width == ((BitType) o).width;
    }
}
