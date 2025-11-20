import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class RegistryServer {
    public static void main(String[] args) {
        try {
            // Get port from system property or default to 1099
            int port = Integer.getInteger("registry.port", 1099);
            
            // Get hostname for display purposes
            String hostname = System.getProperty("java.rmi.server.hostname", "localhost");
            
            LocateRegistry.createRegistry(port);
            
            System.out.println("=== RMI Registry Started ===");
            System.out.println("Host: " + hostname);
            System.out.println("Port: " + port);
            System.out.println("Ready for connections.");
            System.out.println("Press Enter to stop the registry...");
            
            new Scanner(System.in).nextLine();
            
            System.out.println("Stopping registry...");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Failed to start registry: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
