package pascal.netx.util.type;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record Pair<T, F>(T first, F second) {
    public static <T, F> Pair<T, F> of(T first, F second) {
        return new Pair<>(first, second);
    }

    public <R> R map(BiFunction<T, F, R> mapper) {
        return mapper.apply(first, second);
    }

    public void accept(BiConsumer<T, F> consumer) {
        consumer.accept(first, second);
    }
}
