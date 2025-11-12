package casey.lcbdev.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoggerFactory {
    private static final String LOG_DIR = "logs";

    public static Logger getLogger(Class<?> theClass) {
        Logger logger = Logger.getLogger(theClass.getName());
        logger.setUseParentHandlers(false);

        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        try {
            Path logDir = Path.of(LOG_DIR);
            if(!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            String logFile = LOG_DIR + "/netships_" + System.currentTimeMillis() + ".log";
            FileHandler fileHandler = new FileHandler(logFile, true);
            fileHandler.setFormatter(new CustomFormatter());
            fileHandler.setLevel(Level.ALL);

            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return logger;
    }

    private static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd:MM:yyyy-HH:mm:ss"));
            String className = record.getSourceClassName();
            String methodName = record.getSourceMethodName();
            String level = record.getLevel().getName();
            String message = formatMessage(record);

            return String.format("[%s] %s:%s:%s:: %s%n", timestamp, className, methodName, level, message);
        }
    }
}
