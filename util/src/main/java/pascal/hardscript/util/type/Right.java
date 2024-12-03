package pascal.hardscript.util.type;

import java.util.Optional;

public record Right<T, F>(F value) implements Either<T, F> {
    @Override
    public Optional<Left<T, F>> asLeft() {
        return Optional.empty();
    }

    @Override
    public Optional<Right<T, F>> asRight() {
        return Optional.of(this);
    }
}
