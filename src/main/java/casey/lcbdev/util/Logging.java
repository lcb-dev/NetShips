package casey.lcbdev.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public final class Logging {
    private static final String LOG_DIR = "logs";
    private static final String LOG_PREFIX = "NetShips_";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
    private static volatile boolean initialized = false;
    private static Handler sharedFileHandler = null;

    private Logging() {}

    public static synchronized void init() {
        if (initialized) return;

        try {
            Path logDir = Path.of(LOG_DIR);
            if (!Files.exists(logDir)) Files.createDirectories(logDir);

            String ts = LocalDateTime.now().format(TS_FMT);
            String filename = LOG_DIR + "/" + LOG_PREFIX + ts + ".log";

            sharedFileHandler = new FileHandler(filename, true);
            sharedFileHandler.setFormatter(new CustomFormatter());
            sharedFileHandler.setLevel(Level.ALL);

            String[] allowedPrefixes = new String[] { "casey.lcbdev" };
            sharedFileHandler.setFilter(record -> {
                String loggerName = record.getLoggerName();
                if(loggerName == null) return false;
                for(String p : allowedPrefixes) {
                    if(loggerName.startsWith(p)) return true;
                }
                return false;
            });

            Logger root = Logger.getLogger("");
            for (Handler h : root.getHandlers()) {
                root.removeHandler(h);
            }
            root.addHandler(sharedFileHandler);
            root.setLevel(Level.ALL);

            initialized = true;
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Logger getLogger(Class<?> cls) {
        if (!initialized) init(); 
        return Logger.getLogger(cls.getName());
    }

    /** Custom formatter:
     * [DD:MM:YYYY-HH:mm:ss] {classname}:{methodname}:{LEVEL}:: {MESSAGE}
     */
    private static class CustomFormatter extends Formatter {
        private final DateTimeFormatter tsFormatter =
                DateTimeFormatter.ofPattern("dd:MM:yyyy-HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            String timestamp = LocalDateTime.now().format(tsFormatter);
            String className = safe(record.getSourceClassName());
            String methodName = safe(record.getSourceMethodName());
            String level = record.getLevel().getName();
            String message = formatMessage(record);
            String thrown = "";
            if (record.getThrown() != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n").append(record.getThrown().toString()).append("\n");
                for (StackTraceElement el : record.getThrown().getStackTrace()) {
                    sb.append("\t at ").append(el.toString()).append("\n");
                }
                thrown = sb.toString();
            }
            return String.format("[%s] %s:%s:%s:: %s%s%n", timestamp, className, methodName, level, message, thrown);
        }

        private String safe(String s) {
            return (s == null) ? "Unknown" : s;
        }
    }
}
