package pascal.netx.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public final class TestUtils {

    public static List<Path> collectAllFilesUnder(Path dir) throws IOException {
        return collectFilesUnder(dir, _ -> true);
    }

    public static List<Path> collectFilesUnder(Path dir, Predicate<? super Path> predicate) throws IOException {
        try (var paths = Files.list(dir)) {
            return paths.filter(Files::isRegularFile).filter(predicate).toList();
        }
    }
}
