import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Custom registry interface that allows remote node registration
 * Solves the "Registry.rebind disallowed" error for multi-machine setups
 */
public interface NodeRegistry extends Remote {
    void registerNode(int nodeId, Node node) throws RemoteException;
    void unregisterNode(int nodeId) throws RemoteException;
    Node getNode(int nodeId) throws RemoteException;
    List<Integer> getRegisteredNodeIds() throws RemoteException;
    boolean isNodeRegistered(int nodeId) throws RemoteException;
}
