package pascal.netx.util.type;

import java.util.Optional;

public record Left<T, F>(T value) implements Either<T, F> {

    @Override
    public Optional<Left<T, F>> asLeft() {
        return Optional.of(this);
    }

    @Override
    public Optional<Right<T, F>> asRight() {
        return Optional.empty();
    }
}
