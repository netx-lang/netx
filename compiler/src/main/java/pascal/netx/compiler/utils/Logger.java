package pascal.netx.compiler.utils;

import org.slf4j.LoggerFactory;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import java.util.Stack;

/**
 * Tracks source code locations and names during package and component elaboration.
 * Maintains a stack of currently elaborating packages and components with their source locations.
 * This class follows the Singleton pattern to ensure a single instance is used throughout the elaboration process.
 */
public final class Logger {

    private static final Logger INSTANCE = new Logger();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);
    private final Stack<Entry> stack = new Stack<>();
    private final Stack<SourceLoc> sourceLocStack = new Stack<>();

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

    public static void enterLoc(SourceLoc loc) {
        getInstance().sourceLocStack.push(loc);
    }

    public static void exitLoc() {
        getInstance().sourceLocStack.pop();
    }

    public static SourceLoc getCurrentLoc() {
        var stack = getInstance().sourceLocStack;
        if (stack.isEmpty()) {
            return SourceLoc.none();
        }
        return stack.peek();
    }

    public static void error(String message, Object... args) {
        error(getCurrentLoc(), message, args);
    }

    public static void error(SourceLoc loc, String message, Object... args) {
        log(logger::error, loc, message, args);
    }

    public static void warn(String message, Object... args) {
        warn(getCurrentLoc(), message, args);
    }

    public static void warn(SourceLoc loc, String message, Object... args) {
        log(logger::warn, loc, message, args);
    }

    public static void info(String message, Object... args) {
        info(getCurrentLoc(), message, args);
    }

    public static void info(SourceLoc loc, String message, Object... args) {
        log(logger::info, loc, message, args);
    }

    public static void print(String message, Object... args) {
        logger.info(message, args);
    }

    private static void log(LogFunction logFunc, SourceLoc loc, String message, Object... args) {
        var sb = new StringBuilder();
        if (!loc.isNone()) {
            sb.append(loc).append(": ");
        }
        sb.append(message);
        for (var entry : INSTANCE.stack) {
            sb.append("\n\t\t\t\t\tin ").append(entry);
        }
        logFunc.log(sb.toString(), args);
    }

    public static void startPackage(Identifier name, SourceLoc sourceLoc) {
        getInstance().stack.push(new Entry(Type.PACKAGE, name, sourceLoc));
        enterLoc(sourceLoc);
    }

    public static void startComponent(Identifier name, SourceLoc sourceLoc) {
        getInstance().stack.push(new Entry(Type.COMPONENT, name, sourceLoc));
        enterLoc(sourceLoc);
    }

    public static void finishPackage() {
        var stack = getInstance().stack;
        if (stack.isEmpty() || stack.peek().type() != Type.PACKAGE) {
            throw new ShallNotReachHereError("No package currently being elaborated");
        }
        stack.pop();
        exitLoc();
    }

    public static void finishComponent() {
        var stack = getInstance().stack;
        if (stack.isEmpty() || stack.peek().type() != Type.COMPONENT) {
            throw new ShallNotReachHereError("No component currently being elaborated");
        }
        stack.pop();
        exitLoc();
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

    public record Entry(Type type, Identifier name, SourceLoc sourceLoc) {
        @Override
        @Nonnull
        public String toString() {
                return sourceLoc + ": " + type + " " + name.name();
        }
    }
}
