public class Config {
    
    public static final String RMI_REGISTRY_HOST = System.getProperty("registry.host", "localhost");
    public static final int RMI_REGISTRY_PORT = Integer.parseInt(System.getProperty("registry.port", "1099"));
    public static final int BASE_NODE_PORT = 2000;
    
    public static final int MIN_NODES = 2;
    public static final int MAX_NODES = 10;
    
    // Optimized timing parameters
    public static final int MIN_REQUEST_DELAY = 1000;    // ms - Reduced minimum delay for better responsiveness
    public static final int MAX_REQUEST_DELAY = 10000;    // ms - Reduced maximum delay for faster recovery
    public static final int MIN_CS_WORK_TIME = 500;      // ms - Reduced minimum work time for faster throughput
    public static final int MAX_CS_WORK_TIME = 3000;     // ms - Reduced maximum for more predictable performance
    public static final int POLLING_INTERVAL = 500;      // ms - More responsive to incoming messages
    
    // Timeout configuration
    public static final int NODE_RESPONSE_TIMEOUT_MS = 5000;  // 10 seconds timeout for node responses
    
    public static final boolean ENABLE_DEBUG_LOGS = false;
    public static final boolean ENABLE_TIMESTAMP_LOGS = true;
    
    public static boolean isValidNodeCount(int nodeCount) {
        return nodeCount >= MIN_NODES && nodeCount <= MAX_NODES;
    }
    
    public static int getRandomRequestDelay() {
        return MIN_REQUEST_DELAY + (int) (Math.random() * (MAX_REQUEST_DELAY - MIN_REQUEST_DELAY));
    }
    
    public static int getRandomWorkTime() {
        return MIN_CS_WORK_TIME + (int) (Math.random() * (MAX_CS_WORK_TIME - MIN_CS_WORK_TIME));
    }
}
