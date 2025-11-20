import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RicartAgrawalaApp {
    
    private List<NodeImpl> nodes;
    private Registry registry;
    
    public RicartAgrawalaApp() {
        this.nodes = new ArrayList<>();
    }
    
    // Initialize or connect to RMI registry
    private void initializeRegistry() throws Exception {
        try {
            registry = LocateRegistry.getRegistry(Config.RMI_REGISTRY_HOST, Config.RMI_REGISTRY_PORT);
            registry.list();
            Logger.info("Connected to existing RMI registry at " + Config.RMI_REGISTRY_HOST + ":" + Config.RMI_REGISTRY_PORT);
        } catch (Exception e) {
            Logger.info("Could not connect to existing RMI registry at " + Config.RMI_REGISTRY_HOST + ":" + Config.RMI_REGISTRY_PORT);
            Logger.info("Creating new RMI registry...");
            try {
                registry = LocateRegistry.createRegistry(Config.RMI_REGISTRY_PORT);
                Logger.info("RMI registry created on port " + Config.RMI_REGISTRY_PORT);
            } catch (java.rmi.server.ExportException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("Port already in use")) {
                    Logger.error("Port " + Config.RMI_REGISTRY_PORT + " is already in use. " +
                               "Please stop any existing rmiregistry process or use a different port.");
                    Logger.error("To stop existing registry: kill $(lsof -t -i:" + Config.RMI_REGISTRY_PORT + ")");
                    throw new Exception("Port " + Config.RMI_REGISTRY_PORT + " is already in use. " +
                                      "Another rmiregistry may be running. Please stop it first.", ex);
                }
                throw ex;
            }
        }
    }
    
    // Create and register all nodes, then connect them
    private void createNodes(int numNodes) throws Exception {
        Logger.info("Creating " + numNodes + " nodes...");
        
        // Create and register nodes
        for (int i = 0; i < numNodes; i++) {
            NodeImpl node = new NodeImpl(i, new ArrayList<>(), numNodes);
            nodes.add(node);
            
            int port = Config.BASE_NODE_PORT + i;
            try {
                UnicastRemoteObject.exportObject(node, port);
                
                String nodeName = "Node" + i;
                registry.rebind(nodeName, node);
                
                Logger.info("Created and registered " + nodeName + " on port " + port);
            } catch (Exception e) {
                Logger.error("Failed to export node " + i + ": " + e.getMessage());
                throw e;
            }
        }
        
        // Connect nodes to each other
        for (int i = 0; i < numNodes; i++) {
            NodeImpl currentNode = nodes.get(i);
            List<Node> otherNodes = new ArrayList<>();
            
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    String otherNodeName = "Node" + j;
                    Node otherNode = (Node) registry.lookup(otherNodeName);
                    otherNodes.add(otherNode);
                }
            }
            
            currentNode.updateOtherNodes(otherNodes);
        }
        
        Logger.info("All nodes created and connected successfully!");
    }
    
    // Create and register a single node for multi-machine mode
    private void createSingleNode(int nodeId) throws Exception {
        Logger.info("Creating single node " + nodeId + "...");
        
        // Create single node
        NodeImpl node = new NodeImpl(nodeId, new ArrayList<>(), 10); // Assume max 10 nodes
        nodes.add(node);
        
        int port = Config.BASE_NODE_PORT + nodeId;
        try {
            UnicastRemoteObject.exportObject(node, port);
            
            String nodeName = "Node" + nodeId;
            registry.rebind(nodeName, node);
            
            Logger.info("Created and registered " + nodeName + " on port " + port);
        } catch (Exception e) {
            Logger.error("Failed to export node " + nodeId + ": " + e.getMessage());
            throw e;
        }
        
        // Connect to other existing nodes
        connectToOtherNodes(node, nodeId);
        
        // Start connection refresh thread
        startConnectionRefreshThread(node, nodeId);
    }
    
    // Connect to other existing nodes with retry mechanism
    private void connectToOtherNodes(NodeImpl node, int nodeId) {
        List<Node> otherNodes = new ArrayList<>();
        int connectedCount = 0;
        
        for (int i = 0; i < 10; i++) {
            if (i != nodeId) {
                Node otherNode = connectToNodeWithRetry(i, 1); // Only try once to avoid delays
                if (otherNode != null) {
                    otherNodes.add(otherNode);
                    connectedCount++;
                }
            }
        }
        
        node.updateOtherNodes(otherNodes);
        
        // Only log when connection count changes
        if (connectedCount > 0) {
            Logger.debug("Node " + nodeId + " now has " + connectedCount + " connections");
        }
    }
    
    // Connect to a specific node with retry mechanism
    private Node connectToNodeWithRetry(int targetNodeId, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String nodeName = "Node" + targetNodeId;
                Node node = (Node) registry.lookup(nodeName);
                
                // Test the connection by calling a method
                node.getNodeId();
                
                return node;
            } catch (Exception e) {
                // Logger.debug("Attempt " + attempt + " to connect to Node " + targetNodeId + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000); // Wait 1 second before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return null;
    }
    
    // Start background thread to periodically refresh connections
    private void startConnectionRefreshThread(NodeImpl node, int nodeId) {
        Thread refreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(2000); // Refresh every 2 seconds
                    
                    // Always refresh connections to handle new nodes joining
                    connectToOtherNodes(node, nodeId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Logger.debug("Error refreshing connections: " + e.getMessage());
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
    
    private void startSimulation() {
        Logger.info("\nStarting simulation...");
        Logger.info("Nodes will periodically request access to critical section.");
        Logger.info("Press 'q' and Enter to quit the simulation.\n");
        
        for (NodeImpl node : nodes) {
            node.startSimulation();
        }
    }
    
    private void cleanup() {
        Logger.info("\nShutting down...");
        
        try {
            // Stop all simulation threads
            for (NodeImpl node : nodes) {
                node.stopSimulation();
            }
            
            // Wait for threads to stop
            Thread.sleep(1000);
            
            for (int i = 0; i < nodes.size(); i++) {
                try {
                    String nodeName = "Node" + i;
                    registry.unbind(nodeName);
                } catch (Exception e) {
                    Logger.debug("Failed to unbind " + "Node" + i + ": " + e.getMessage());
                }
            }
            
            for (NodeImpl node : nodes) {
                try {
                    UnicastRemoteObject.unexportObject(node, true);
                } catch (Exception e) {
                    Logger.debug("Failed to unexport node: " + e.getMessage());
                }
            }
            
            Logger.info("Cleanup completed.");
        } catch (Exception e) {
            Logger.error("Error during cleanup: " + e.getMessage());
        }
    }
    
    private static void configureRmiHostname() {
        String existingHost = System.getProperty("java.rmi.server.hostname");
        if (existingHost == null || existingHost.isBlank()) {
            System.setProperty("java.rmi.server.hostname", Config.LOCAL_HOST);
            Logger.info("java.rmi.server.hostname not provided. Using detected address: " + Config.LOCAL_HOST);
        } else {
            Logger.info("Using configured java.rmi.server.hostname: " + existingHost);
        }
    }
    
    public static void main(String[] args) {
        configureRmiHostname();
        RicartAgrawalaApp app = new RicartAgrawalaApp();
        Scanner scanner = new Scanner(System.in);
        
        // Add shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.cleanup();
        }));
        
        try {
            Logger.info("=== Ricart-Agrawala Distributed Mutual Exclusion Algorithm ===");
            
            // Check if running in multi-machine mode
            boolean multiMachineMode = args.length > 0 && "multi".equals(args[0]);
            
            if (multiMachineMode) {
                // Multi-machine mode: run single node
                Logger.info("Running in multi-machine mode (single node per process)");
                System.out.print("Enter node ID (0-9): ");
                int nodeId = scanner.nextInt();
                
                if (nodeId < 0 || nodeId > 9) {
                    Logger.error("Node ID must be between 0 and 9");
                    return;
                }
                
                app.initializeRegistry();
                app.createSingleNode(nodeId);
                
                // Wait a bit for other nodes to connect
                Logger.info("Waiting 3 seconds for other nodes to connect...");
                Thread.sleep(3000);
                
                app.startSimulation();
                
                System.out.println("Node " + nodeId + " is running... Press 'q' and Enter to quit:");
                while (true) {
                    try {
                        String input = scanner.nextLine().trim();
                        if ("q".equalsIgnoreCase(input)) {
                            break;
                        }
                    } catch (Exception e) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            } else {
                // Single-machine mode: run all nodes
                Logger.info("Running in single-machine mode (all nodes in one process)");
                System.out.print("Enter number of nodes (2-10): ");
                int numNodes = scanner.nextInt();
                
                if (!Config.isValidNodeCount(numNodes)) {
                    Logger.error("Number of nodes must be between " + Config.MIN_NODES + " and " + Config.MAX_NODES);
                    return;
                }
                
                app.initializeRegistry();
                app.createNodes(numNodes);
                app.startSimulation();
                
                System.out.println("Simulation is running... Press 'q' and Enter to quit:");
                while (true) {
                    try {
                        String input = scanner.nextLine().trim();
                        if ("q".equalsIgnoreCase(input)) {
                            break;
                        }
                    } catch (Exception e) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            Logger.error("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            app.cleanup();
            scanner.close();
        }
    }
}
