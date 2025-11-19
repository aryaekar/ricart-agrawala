import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote {
    
    boolean request(int requesterId, long timestamp) throws RemoteException;
    
    void reply(int replierId, int requesterId) throws RemoteException;
    
    void release(int releaserId) throws RemoteException;
    
    int getNodeId() throws RemoteException;
    
    boolean isAlive() throws RemoteException;
}
