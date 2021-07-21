package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface used by the admin to call client methods
 * @param <T> Interface used for Messages
 * @param <V> Interface used for Responses
 */
public interface ClientToAdminInterface<T, V> extends ClientToPeerInterface<T, V>, Remote {

    /**
     * Gives the client its broker's RMI name as present in the registry
     * @param RMIName broker's RMI name
     * @return Response of the client
     * @throws RemoteException
     */
    V setBrokerId(T RMIName) throws RemoteException;
}
