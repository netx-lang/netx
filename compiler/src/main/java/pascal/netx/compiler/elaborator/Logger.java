package pascal.netx.compiler.elaborator;

import org.slf4j.LoggerFactory;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks source code locations and names during package and component elaboration.
 * Maintains a stack of currently elaborating packages and components with their source locations.
 * This class follows the Singleton pattern to ensure a single instance is used throughout the elaboration process.
 */
public final class Logger {

    private static final Logger INSTANCE = new Logger();
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);
    private final Deque<Entry> stack = new ArrayDeque<>();

    private Logger() {
    }

    /**
     * Gets the singleton instance of SourceTracker
     *
     * @return the singleton SourceTracker instance
     */
    public static Logger getInstance() {
        return INSTANCE;
    }

    public static void error(String message, Object... args) {
        error(SourceLoc.none(), message, args);
    }

    public static void error(SourceLoc loc, String message, Object... args) {
        logWithContext(log::error, true, loc, message, args);
    }

    public static void warn(String message, Object... args) {
        warn(SourceLoc.none(), message, args);
    }

    public static void warn(SourceLoc loc, String message, Object... args) {
        logWithContext(log::warn, false, loc, message, args);
    }

    public static void info(String message, Object... args) {
        info(SourceLoc.none(), message, args);
    }

    public static void info(SourceLoc loc, String message, Object... args) {
        logWithContext(log::info, false, loc, message, args);
    }

    public static void print(String message, Object... args) {
        log.info(message, args);
    }

    private static void logWithContext(LogFunction logFunc, boolean clear, SourceLoc loc, String message, Object... args) {
        var sb = new StringBuilder();
        if (!loc.isNone()) {
            sb.append(loc).append(": ");
        }
        sb.append(message);
        for (var entry : INSTANCE.stack) {
            sb.append("\n\t\t\t\t\tin ").append(entry);
        }
        logFunc.log(sb.toString(), args);
        if (clear) {
            INSTANCE.stack.clear();
        }
    }

    public static void startPackage(Identifier name, SourceLoc sourceLoc) {
        getInstance().stack.push(new Entry(Type.PACKAGE, name, sourceLoc));
    }

    public static void startComponent(Identifier name, SourceLoc sourceLoc) {
        getInstance().stack.push(new Entry(Type.COMPONENT, name, sourceLoc));
    }

    public static void finishPackage() {
        var stack = getInstance().stack;
        if (stack.isEmpty() || stack.peek().getType() != Type.PACKAGE) {
            throw new ShallNotReachHereError("No package currently being elaborated");
        }
        stack.pop();
    }

    public static void finishComponent() {
        var stack = getInstance().stack;
        if (stack.isEmpty() || stack.peek().getType() != Type.COMPONENT) {
            throw new ShallNotReachHereError("No component currently being elaborated");
        }
        stack.pop();
    }

    public enum Type {
        PACKAGE,
        COMPONENT;

        @Override
        public String toString() {
            return switch (this) {
                case PACKAGE -> "package";
                case COMPONENT -> "component";
            };
        }
    }

    @FunctionalInterface
    private interface LogFunction {
        void log(String message, Object... args);
    }

    public static final class Entry {
        private final Type type;
        private final Identifier name;
        private final SourceLoc sourceLoc;

        private Entry(Type type, Identifier name, SourceLoc sourceLoc) {
            this.type = type;
            this.name = name;
            this.sourceLoc = sourceLoc;
        }

        public Type getType() {
            return type;
        }

        public Identifier getName() {
            return name;
        }

        public SourceLoc getSourceLoc() {
            return sourceLoc;
        }

        @Override
        public String toString() {
            return sourceLoc + ": " + type + " " + name.name();
        }
    }
}
