import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    public enum Level {
        INFO, DEBUG, ERROR
    }
    
    public static void info(String message) {
        log(Level.INFO, message);
    }
    
    public static void debug(String message) {
        if (Config.ENABLE_DEBUG_LOGS) {
            log(Level.DEBUG, message);
        }
    }
    
    public static void error(String message) {
        log(Level.ERROR, message);
    }
    
    public static void log(Level level, String message) {
        String timestamp = Config.ENABLE_TIMESTAMP_LOGS ? 
            LocalDateTime.now().format(TIMESTAMP_FORMAT) + " " : "";
        
        System.out.println(timestamp + "[" + level + "] " + message);
    }
    
    public static void logNode(int nodeId, Level level, String message) {
        String timestamp = Config.ENABLE_TIMESTAMP_LOGS ? 
            LocalDateTime.now().format(TIMESTAMP_FORMAT) + " " : "";
        
        System.out.println(timestamp + "[Node" + nodeId + "][" + level + "] " + message);
    }
}
