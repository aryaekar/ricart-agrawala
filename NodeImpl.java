import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class NodeImpl implements Node {
    
    public enum State {
        REQUESTING,  // Requesting access to critical section
        HELD,        // Currently in critical section
        RELEASED     // Not using critical section
    }
    
    private final int nodeId;
    private final List<Node> otherNodes;
    private final AtomicLong logicalClock;
    private final ReentrantLock lock;
    
    private State state;
    private long requestTimestamp;
    private int repliesReceived;
    private final Map<Integer, Boolean> deferredReplies;
    private final int totalNodes;
    
    public NodeImpl(int nodeId, List<Node> otherNodes, int totalNodes) throws RemoteException {
        this.nodeId = nodeId;
        this.otherNodes = new ArrayList<>(otherNodes);
        this.totalNodes = totalNodes;
        this.logicalClock = new AtomicLong(0);
        this.lock = new ReentrantLock();
        this.state = State.RELEASED;
        this.requestTimestamp = 0;
        this.repliesReceived = 0;
        this.deferredReplies = new ConcurrentHashMap<>();
    }
    
    // Handle incoming request from another node
    @Override
    public boolean request(int requesterId, long timestamp) throws RemoteException {
        lock.lock();
        try {
            updateLogicalClock(timestamp);
            
            Logger.logNode(nodeId, Logger.Level.INFO, "Received request from Node " + requesterId + " [timestamp:" + logicalClock.get() + "]");
            
            boolean shouldGrant = shouldGrantPermission(requesterId, timestamp);
            
            if (shouldGrant) {
                Logger.logNode(nodeId, Logger.Level.INFO, "Grants permission to Node " + requesterId);
                sendReply(requesterId);
                return true;
            } else {
                deferredReplies.put(requesterId, true);
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void reply(int replierId, int requesterId) throws RemoteException {
        lock.lock();
        try {
            Logger.logNode(nodeId, Logger.Level.INFO, "Received reply from Node " + replierId + " [timestamp:" + logicalClock.get() + "]");
            repliesReceived++;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void release(int releaserId) throws RemoteException {
        lock.lock();
        try {
            Logger.logNode(nodeId, Logger.Level.INFO, "Received release from Node " + releaserId + " [timestamp:" + logicalClock.get() + "]");
            
            int deferredCount = 0;
            for (Map.Entry<Integer, Boolean> entry : deferredReplies.entrySet()) {
                if (entry.getValue()) {
                    int deferredNodeId = entry.getKey();
                    sendReply(deferredNodeId);
                    deferredCount++;
                }
            }
            deferredReplies.clear();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public int getNodeId() throws RemoteException {
        return nodeId;
    }
    
    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }
    
    // Grant permission if node is not requesting/held, or requester has higher priority
    private boolean shouldGrantPermission(int requesterId, long timestamp) {
        if (state != State.REQUESTING && state != State.HELD) {
            return true;
        }
        
        if (timestamp < requestTimestamp) {
            return true;
        }
        
        if (timestamp == requestTimestamp && requesterId < nodeId) {
            return true;
        }
        
        return false;
    }
    
    private void sendReply(int requesterId) {
        for (Node node : otherNodes) {
            try {
                if (node.getNodeId() == requesterId) {
                    node.reply(nodeId, requesterId);
                    break;
                }
            } catch (RemoteException e) {
                Logger.logNode(nodeId, Logger.Level.ERROR, "Failed to send reply to Node " + requesterId);
            }
        }
    }
    
    public void requestCriticalSection() {
        lock.lock();
        try {
            if (state == State.REQUESTING || state == State.HELD) {
                Logger.logNode(nodeId, Logger.Level.INFO, "Already requesting or in critical section");
                return;
            }
            
            state = State.REQUESTING;
            requestTimestamp = logicalClock.incrementAndGet();
            repliesReceived = 0;
            deferredReplies.clear();
            
            Logger.logNode(nodeId, Logger.Level.INFO, "Requesting critical section [timestamp:" + requestTimestamp + "]");
            
        } finally {
            lock.unlock();
        }
        
        for (Node node : otherNodes) {
            try {
                node.request(nodeId, requestTimestamp);
            } catch (RemoteException e) {
                Logger.logNode(nodeId, Logger.Level.ERROR, "Failed to send request to node");
            }
        }
        
        waitForReplies();
        
        if (repliesReceived < otherNodes.size()) {
            Logger.logNode(nodeId, Logger.Level.ERROR, "Timeout waiting for replies");
            state = State.RELEASED;
            return;
        }
        
        Logger.logNode(nodeId, Logger.Level.INFO, "Received all replies, entering critical section");
        
        enterCriticalSection();
    }
    
    private void waitForReplies() {
        try {
            long startTime = System.currentTimeMillis();
            
            while (repliesReceived < otherNodes.size()) {
                if (System.currentTimeMillis() - startTime > Config.NODE_RESPONSE_TIMEOUT_MS) {
                    break;
                }
                Thread.sleep(Config.POLLING_INTERVAL);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.logNode(nodeId, Logger.Level.ERROR, "Thread interrupted while waiting for replies");
        }
    }
    
    private void enterCriticalSection() {
        lock.lock();
        try {
            state = State.HELD;
        } finally {
            lock.unlock();
        }
        
        Logger.logNode(nodeId, Logger.Level.INFO, "*** ENTERED CRITICAL SECTION *** [timestamp:" + logicalClock.get() + "]");
        
        try {
            Thread.sleep(Config.getRandomWorkTime());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            exitCriticalSection();
        }
    }
    
    private void exitCriticalSection() {
        lock.lock();
        try {
            state = State.RELEASED;
            Logger.logNode(nodeId, Logger.Level.INFO, "*** EXITED CRITICAL SECTION *** [timestamp:" + logicalClock.get() + "]");
            
            for (Node node : otherNodes) {
                try {
                    node.release(nodeId);
                } catch (RemoteException e) {
                    Logger.logNode(nodeId, Logger.Level.ERROR, "Failed to send release to node");
                }
            }
            
        } finally {
            lock.unlock();
        }
    }
    
    // Update logical clock to maintain event ordering
    private void updateLogicalClock(long receivedTimestamp) {
        long currentClock = logicalClock.get();
        long newClock = Math.max(currentClock, receivedTimestamp) + 1;
        logicalClock.set(newClock);
    }
    
    public long getLogicalClock() {
        return logicalClock.get();
    }
    
    public State getState() {
        return state;
    }
    
    public void updateOtherNodes(List<Node> otherNodes) {
        this.otherNodes.clear();
        this.otherNodes.addAll(otherNodes);
    }
    
    private Thread simulationThread;
    
    public void startSimulation() {
        simulationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int delay = Config.getRandomRequestDelay();
                    Thread.sleep(delay);
                    
                    requestCriticalSection();
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        simulationThread.start();
    }
    
    public void stopSimulation() {
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
    }
}