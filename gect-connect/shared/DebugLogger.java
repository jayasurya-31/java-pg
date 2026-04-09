package shared;

import java.util.Objects;

/**
 * Debug Logging Profile 2.0: Clean Human-Readable Mode.
 * Pattern: [{level}] {module} ? {message} - {function}() - {file}.java
 */
public class DebugLogger {
    private static String lastLog = "";

    public enum Level {
        ERROR, WARN, INFO, DEBUG
    }

    /**
     * Core logging method following the new format.
     */
    public static void log(Level level, String fullMessage) {
        if (fullMessage == null) return;
        if (Objects.equals(fullMessage, lastLog)) return;
        
        lastLog = fullMessage;
        System.out.println(String.format("[%s] %s", level, fullMessage));
    }

    public static void info(String fullMessage) {
        log(Level.INFO, fullMessage);
    }

    public static void warn(String fullMessage) {
        log(Level.WARN, fullMessage);
    }

    public static void error(String fullMessage) {
        log(Level.ERROR, fullMessage);
    }

    public static void debug(String fullMessage) {
        log(Level.DEBUG, fullMessage);
    }
}
