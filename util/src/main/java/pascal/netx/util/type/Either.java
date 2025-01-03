package pascal.netx.util.type;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Either<T, F> permits Left, Right {

    static <T, F> Left<T, F> makeLeft(T value) {
        return new Left<>(value);
    }

    static <T, F> Right<T, F> makeRight(F value) {
        return new Right<>(value);
    }

    Optional<Left<T, F>> asLeft();

    Optional<Right<T, F>> asRight();

    default void match(Consumer<T> left, Consumer<F> right) {
        switch (this) {
            case Left(var value) -> left.accept(value);
            case Right(var value) -> right.accept(value);
        }
    }

    default <R> R map(Function<T, R> left, Function<F, R> right) {
        return switch (this) {
            case Left(var value) -> left.apply(value);
            case Right(var value) -> right.apply(value);
        };
    }
}
