package logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class LogUtils {
    private static Logger logger = LogManager.getLogger();

    public static void info(String message) {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        Marker marker = MarkerManager.getMarker(className + "[" + lineNumber + "]");
        logger.info(marker, message);
    }

    public static void error(String message) {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        Marker marker = MarkerManager.getMarker(className + "[" + lineNumber + "]");
        logger.error(marker, message);
    }

    public static void error(StackTraceElement[] stackTraceElement) {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        Marker marker = MarkerManager.getMarker(className + "[" + lineNumber + "]");
        logger.error(marker, stackTraceElement);
    }


    public static void debug(String message) {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        Marker marker = MarkerManager.getMarker(className + "[" + lineNumber + "]");
        logger.debug(marker, message);
    }
}
